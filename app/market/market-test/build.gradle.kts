plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(projects.marketCore)
}

android {
  namespace = "com.hedvig.android.market.test"
}
