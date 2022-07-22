package com.hedvig.android.lokalise.task

import com.hedvig.android.lokalise.config.DownloadConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.buffer
import okio.sink
import okio.source
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File
import java.net.URL

abstract class DownloadStringsTask : DefaultTask() {
  init {
    description = "Tasks which downloads the lokalise strings into strings.xml"
    group = "lokalise"
  }

  @get:Input
  @get:Option(option = "lokaliseProjectId", description = "The ID of the lokalise project")
  abstract val lokaliseProjectId: Property<String>

  @get:Input
  @get:Option(option = "lokaliseToken", description = "Token to authenticate with Lokalise")
  abstract val lokaliseToken: Property<String>

  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val outputDirectory: ConfigurableFileCollection

  @get:Input
  @get:Option(option = "downloadConfig", description = "Configuration of how to download the strings")
  abstract val downloadConfig: Property<DownloadConfig>

  private val tag = "[Hedvig Lokalise Plugin]"

  @TaskAction
  fun handle() {
    val dirRes = outputDirectory
    logger.debug("$tag strings will be put at path:${outputDirectory.asPath}")

    val nameTempLokaliseDir = "lokalise"
    val localBuildDir = project.file("${project.buildDir.path}${File.separator}$nameTempLokaliseDir")
    val zipPath = "$localBuildDir${File.separator}lang-file.zip"

    val bucketUrl = fetchBucketUrl()
    localBuildDir.mkdirs()
    saveUrlContentToFile(zipPath, bucketUrl)
    logger.debug("$tag zip file:${File(zipPath).readLines()}")
    unzipReceivedZipFile(zipPath, dirRes)
    logger.debug("$tag dirRes:${dirRes.asFileTree.map { it.absolutePath }}")
    localBuildDir.delete()
  }

  private fun fetchBucketUrl(): String {
    val postBodyJson = buildJsonObject {
      put("format", "xml")
      put("export_sort", downloadConfig.get().stringsOrder.value)
      put("export_empty_as", downloadConfig.get().emptyTranslationStrategy.value)
      put("replace_breaks", true)
    }
    val requestBody = postBodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
    val postRequest = Request.Builder()
      .url("https://api.lokalise.com/api2/projects/${lokaliseProjectId.get()}/files/download")
      .header("x-api-token", lokaliseToken.get())
      .header("content-type", "application/json")
      .post(requestBody)
      .build()
    logger.debug("$tag postRequest:$postRequest")
    val response = OkHttpClient().newCall(postRequest).execute().body?.string()
      ?: error("Lokalise responded with a null body")
    logger.debug("$tag post response:$response")
    val amazonBucketUrl = Json.parseToJsonElement(response).jsonObject["bundle_url"]
      ?: error("Lokalise response contained no bucket with the strings. Response was instead: $response")
    logger.debug("$tag amazonBucketUrl:$amazonBucketUrl")
    return amazonBucketUrl.jsonPrimitive.content
  }

  private fun saveUrlContentToFile(zipPath: String, bucketUrl: String) {
    URL(bucketUrl).openStream().source().buffer().use { zipSource ->
      project.file(zipPath).sink().buffer().use { localFileSink ->
        localFileSink.writeAll(zipSource)
      }
    }
  }

  private fun unzipReceivedZipFile(fullZipFilePath: String, dirForUnzipped: ConfigurableFileCollection) {
    project.copy {
      it.from(project.zipTree(File(fullZipFilePath)))
      it.into(dirForUnzipped.asPath)
    }
  }
}
