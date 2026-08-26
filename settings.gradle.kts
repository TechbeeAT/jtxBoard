pluginManagement {
    repositories {
        google()
        mavenCentral()

        // AboutLibraries
        maven("https://plugins.gradle.org/m2/")

        // Huawei - only for use in Huawei branch
        // maven("https://developer.huawei.com/repo/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // AppIntro, dav4jvm
        maven("https://jitpack.io")

        // Huawei - only for use in Huawei branch
        // maven("https://developer.huawei.com/repo/")
    }
}

include(":app")
include(":baselineprofile")
