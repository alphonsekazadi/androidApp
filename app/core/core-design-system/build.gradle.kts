plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.material3)

  implementation(libs.accompanist.systemUiController)
  implementation(libs.androidx.compose.material)
  implementation(libs.kotlinx.datetime)
  implementation(libs.slimber)
  implementation(projects.coreIcons)
  implementation(projects.coreResources)
}

android {
  namespace = "com.hedvig.android.core.designsystem"

  defaultConfig {
    vectorDrawables.useSupportLibrary = true
  }
}
