import java.io.FileInputStream
import java.io.IOException
import java.util.Properties

/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */



plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.mikepenz.aboutLibraries)
    //alias(libs.plugins.huawei.agconnect)
}

// Creates a variable called keystorePropertiesFile, and initializes it to the keystore.properties file.
// see https://developer.android.com/build/gradle-tips#remove-private-signing-information-from-your-project
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties() // Initializes a new Properties() object called keystoreProperties.
try {
    FileInputStream(keystorePropertiesFile).use { keystoreProperties.load(it) }
} catch (e: IOException) {
    logger.log(LogLevel.WARN, "Could not load keystore properties from local file: ${e.message}")
}

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "at.techbee.jtx"
    compileSdk = 37
    defaultConfig {
        applicationId = "at.techbee.jtx"
        minSdk = 24
        targetSdk = 37
        versionCode = 217020007
        versionName = "2.17.00-alpha07"      // keep -release as a suffix also for release, build flavor adds the suffix e.g. .gplay (e.g. 1.00.00-rc0.gplay)
        buildConfigField("String", "versionCodename", "\"Be kind. \uD83C\uDF08\"")
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        testOptions {
            unitTests.isIncludeAndroidResources = true
            unitTests.isReturnDefaultValues = true
        }


        //don't forget to update locales_config.xml when changing the languages!
        //def locales = ["en", "de", "cs", "el", "es", "fr", "it", "nl", "ru", "zh", "ca", "ja", "zh-rTW", "hu", "vi", "sv"]
        //buildConfigField "String[]", "TRANSLATION_ARRAY", "new String[]{\""+locales.join("\",\"")+"\"}"
        //resourceConfigurations += locales

        buildConfigField("String", "CROWDIN_API_KEY", "\"" + (System.getenv("CROWDIN_API_KEY") ?: keystoreProperties["crowdin.apikey"]?.toString()) + "\"")
        //buildConfigField("String", "GITHUB_CONTRIBUTORS_API_KEY", "\"" + (System.getenv("GH_CONTRIBUTORS_API_KEY") ?: providers.gradleProperty("githubcontributors.apikey") ) + "\"")
        resValue("string", "google_geo_api_key", System.getenv("GOOGLE_GEO_API_KEY") ?: keystoreProperties["google.geo.apikey"]?.toString() ?: "")
    }

    compileOptions {
        // enable because ical4android requires desugaring
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        buildConfig = true
        compose = true
        resValues = true
    }


    flavorDimensions += "version"
    productFlavors {
        create("gplay") {
            versionNameSuffix = ".gplay"
        }
        create("amazon") {
            versionNameSuffix = ".amazon"
        }
        create("ose") {
            versionNameSuffix = ".ose"
            isDefault = true
        }
        /*
        create("generic") {
            versionNameSuffix = ".generic"
        }
         */
        /*
        create("huawei") {
            versionNameSuffix = ".huawei"
        }
         */
    }

    signingConfigs {
        create("jtx") {
            storeFile = file(System.getenv("ANDROID_KEYSTORE") ?: keystoreProperties["keystore.file"]?.toString() ?: "/dev/null") // )
            storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD") ?: keystoreProperties["keystore.password"]?.toString()
            keyAlias = System.getenv("ANDROID_KEY_ALIAS") ?: keystoreProperties["keystore.key.alias"]?.toString()
            keyPassword = System.getenv("ANDROID_KEY_PASSWORD") ?: keystoreProperties["keystore.key.password"]?.toString()
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isShrinkResources = true
            signingConfig = signingConfigs.findByName("jtx")
        }

        // benchmark not migrated
    }


    packaging {
        resources {
            excludes += arrayOf("META-INF/*.md")
        }
    }

    androidResources {
        @Suppress("UnstableApiUsage")
        generateLocaleConfig = true
    }

    @Suppress("UnstableApiUsage")
    testOptions {
        managedDevices {
            localDevices {
                create("virtual") {
                    device = "Pixel 3"
                    apiLevel = 34
                    systemImageSource = "aosp-atd"
                }
            }
        }
    }

    lint {
        disable += arrayOf(
            "MissingTranslation",
            "ExtraTranslation"
        )
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

configurations {
    configureEach {
        // exclude modules which are in conflict with system libraries
        exclude(module = "commons-logging")
        exclude(group = "org.json", module = "json")
    }
}


dependencies {
    // core
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.androidx.ui.text.google.fonts)
    "baselineProfile"(project(":baselineprofile"))
    coreLibraryDesugaring(libs.android.desugaring)

    // support libs
    implementation(libs.androidx.activityCompose)
    implementation(libs.androidx.appcompat)
    implementation(libs.compose.navigation)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.base)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.work.base)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.annotation)
    implementation(libs.volley) // Volley for simple network requests
    implementation(libs.coil3.compose)   // image loading
    implementation(libs.coil3.network.okhttp)   // image loading from web
    implementation (libs.androidx.biometric.ktx)
    implementation (libs.androidx.profileinstaller)
    implementation (libs.libphonenumber)

    // Jetpack Compose
    implementation(libs.compose.accompanist.permissions)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.materialIconsExtended)
    implementation(libs.compose.runtime.livedata)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.toolingPreview)

    // Glance Widgets
    implementation(libs.glance.base)
    implementation(libs.glance.material)

    // Jetpack Room
    implementation(libs.room.runtime)
    implementation(libs.room.base)
    ksp(libs.room.compiler)

    // bitfire libraries
    implementation(libs.synctools)

    // third-party libs
    implementation(libs.mikepenz.aboutLibraries)
    implementation(libs.mikepenz.multiplatform.markdown.renderer.m3)
    implementation(libs.mikepenz.multiplatform.markdown.renderer.coil3)
    implementation(libs.mikepenz.multiplatform.markdown.renderer.code)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.colorpicker.compose)  // Compose Color Picker
    implementation(libs.osmdroid.android) //Open Street Maps
    implementation (libs.calendar.compose)
    implementation (libs.reorderable)


    // for tests
    androidTestImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.room.testing)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.androidx.test.core)
    // Required -- JUnit 4 framework
    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.mockito.core)
}

// build variants (flavors)
val gplayImplementation by configurations {
    dependencies {
        implementation(libs.android.billing)
        implementation(libs.android.review)

        // Google Maps
        implementation(libs.maps.compose)
        implementation(libs.play.services.maps)
        implementation(libs.play.services.location)
    }
}

val amazonImplementation by configurations {
    dependencies {
        // Amazon billing & maps support
        implementation(libs.amazon.appstore.sdk)
        implementation(libs.maps.compose)
        implementation(libs.play.services.maps)
        implementation(libs.play.services.location)
    }
}

val oseImplementation by configurations {
    dependencies {
    }
}

/*
val huaweiImplementation by configurations {
    dependencies {
        implementation(libs.huawei.iap)
        implementation(libs.huawei.agcp)
    }
}

 */


