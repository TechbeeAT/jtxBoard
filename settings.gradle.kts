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

// Consume the synctools library from the davx5-ose submodule as a Gradle composite build
// instead of a (currently unavailable) jitpack artifact. The explicit dependency substitution
// keeps only the :synctools project (and davx5-ose's build-logic) configured, and maps the
// coordinate declared in the version catalog (libs.synctools) to the local project.
includeBuild("davx5-ose") {
    dependencySubstitution {
        substitute(module("com.github.bitfireat.davx5-ose:synctools"))
            .using(project(":synctools"))
    }
}

include(":app")
include(":baselineprofile")
