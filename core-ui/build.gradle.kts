plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
}

dependencies {
  implementation(projects.coreResources)
  implementation(projects.coreDesignSystem)

  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.material)
  debugApi(libs.androidx.compose.uiTooling)
  api(libs.androidx.compose.uiToolingPreview)
  api(libs.accompanist.insets)
  api(libs.accompanist.insetsUi)
  implementation(libs.androidx.compose.mdcAdapter)
}