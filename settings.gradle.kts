enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  includeBuild("build-logic")
  includeBuild("lokalise-gradle-plugin")
  repositories {
    google()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  @Suppress("UnstableApiUsage")
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  @Suppress("UnstableApiUsage")
  repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.github.com/HedvigInsurance/odyssey") {
      name = "odyssey"
      credentials(PasswordCredentials::class)
    }
    maven("https://maven.pkg.github.com/HedvigInsurance/authlib") {
      name = "authlib"
      credentials(PasswordCredentials::class)
    }
    maven("https://jitpack.io")
  }
}

rootProject.name = "hedvigandroid"

include(":app:apollo")
include(":app:apollo:core")
include(":app:apollo:di")
include(":app:apollo:giraffe")
include(":app:apollo:octopus")
include(":app:app")
include(":app:audio-player")
include(":app:auth:auth-android")
include(":app:auth:auth-core")
include(":app:auth:auth-event-core")
include(":app:auth:auth-event-test")
include(":app:auth:auth-test")
include(":app:core:core-common")
include(":app:core:core-common-android")
include(":app:core:core-common-android-test")
include(":app:core:core-common-test")
include(":app:core:core-datastore")
include(":app:core:core-datastore-test")
include(":app:core:core-design-system")
include(":app:core:core-resources")
include(":app:core:core-ui")
include(":app:datadog")
include(":app:feature:feature-businessmodel")
include(":app:feature:feature-changeaddress")
include(":app:feature:feature-odyssey")
include(":app:feature:feature-terminate-insurance")
include(":app:hanalytics:hanalytics-android")
include(":app:hanalytics:hanalytics-core")
include(":app:hanalytics:hanalytics-feature-flags")
include(":app:hanalytics:hanalytics-feature-flags-test")
include(":app:hanalytics:hanalytics-test")
include(":app:hedvig-language")
include(":app:hedvig-market")
include(":app:navigation:navigation-activity")
include(":app:navigation:navigation-compose-typed")
include(":app:notification-badge-data")
include(":app:notification:firebase")
include(":app:notification:notification-core")
include(":app:testdata")
include(":micro-apps:design-showcase")
