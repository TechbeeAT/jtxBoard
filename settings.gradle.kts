pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()

        // ical4android
        maven { setUrl("https://jitpack.io") }
        // Huawei - only for use in Huawei branch
        maven { setUrl("https://developer.huawei.com/repo/") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()


        // AboutLibraries
        maven { setUrl("https://plugins.gradle.org/m2/") }

        // Huawei - only for use in Huawei branch
        //maven { url 'https://developer.huawei.com/repo/' }
    }

    /*
    dependencies {
        classpath("com.android.tools.build:gradle:$version_gradle")
        classpath("com.mikepenz.aboutlibraries.plugin:aboutlibraries-plugin:$version_about_libraries")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$version_kotlin")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$version_kotlin")
        // Huawei - only for use in Huawei branch
        //classpath "com.huawei.agconnect:agcp:$version_huawei"
    }

     */
}

plugins {
    id("com.mikepenz.aboutlibraries.plugin") version "10.8.3"
}


rootProject.name="jtx Board"
include(":app")
include(":benchmark")
