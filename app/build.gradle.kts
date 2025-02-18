/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */



plugins {
    alias(libs.plugins.mikepenz.aboutLibraries)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.baselineprofile)
    //alias(libs.plugins.huawei.agconnect)
}


android {
    namespace = "at.techbee.jtx"
    compileSdk = 35
    defaultConfig {
        applicationId = "at.techbee.jtx"
        buildConfigField("long", "buildTime", "${System.currentTimeMillis()}L")
        minSdk = 23
        targetSdk = 35
        versionCode = 210010011
        versionName = "2.10.01"      // keep -release as a suffix also for release, build flavor adds the suffix e.g. .gplay (e.g. 1.00.00-rc0.gplay)
        buildConfigField("String", "versionCodename", "\"Pride is a protest \uD83C\uDF08\"")
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

        buildConfigField("String", "CROWDIN_API_KEY", "\"" + (System.getenv("CROWDIN_API_KEY") ?: providers.gradleProperty("crowdin.apikey") ) + "\"")
        //buildConfigField("String", "GITHUB_CONTRIBUTORS_API_KEY", "\"" + (System.getenv("GH_CONTRIBUTORS_API_KEY") ?: providers.gradleProperty("githubcontributors.apikey") ) + "\"")
        resValue("string", "google_geo_api_key", System.getenv("GOOGLE_GEO_API_KEY") ?: "")
    }

    compileOptions {
        // enable because ical4android requires desugaring
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
        compose = true
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
            storeFile = file(System.getenv("ANDROID_KEYSTORE") ?: "/dev/null") //?: providers.gradleProperty("keystore.file") )
            storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD") //?: providers.gradleProperty("keystore.password")
            keyAlias = System.getenv("ANDROID_KEY_ALIAS") //?: providers.gradleProperty("keystore.key.alias")
            keyPassword = System.getenv("ANDROID_KEY_PASSWORD") //?: providers.gradleProperty("keystore.key.password")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
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

        // Groovy requires SDK 26+, and it's not required, so exclude it
        exclude(group = "org.codehaus.groovy")
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
    implementation (libs.coil.compose)  // image loading from web
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
    implementation(libs.bitfire.ical4android)

    // third-party libs
    implementation(libs.mikepenz.aboutLibraries)
    implementation(libs.colorpicker.compose)  // Compose Color Picker
    implementation(libs.markdowntext) // Markdown support
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


