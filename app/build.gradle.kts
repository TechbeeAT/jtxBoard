/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */


plugins {

}

/*
val localPropertiesFile = rootProject.file("local.properties")
val localProperties = Properties()
if(localPropertiesFile.exists()) {
    localProperties.load(new FileInputStream(localPropertiesFile))
}

 */

android {
    namespace = "at.techbee.jtx"
    compileSdk = 34
    defaultConfig {
        applicationId = "at.techbee.jtx"
        //buildConfigField = "long", "buildTime", System.currentTimeMillis() + "L"
        minSdkVersion = 21
        targetSdkVersion = 34
        versionCode = 205010011
        versionName = "2.05.01-rc9"      // keep -release as a suffix also for release, build flavor adds the suffix e.g. .gplay (e.g. 1.00.00-rc0.gplay)
        //buildConfigField "String", "versionCodename", "\"Love is love ❤️🌈\""
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testOptions {
            unitTests.includeAndroidResources = true
            unitTests.returnDefaultValues = true
        }
        // defining the room.schemaLocation explicitly allows AutoMigrations
        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }

        //def locales = getLocales()
        //buildConfigField "String[]", "TRANSLATION_ARRAY", "new String[]{\""+locales.join("\",\"")+"\"}"
        //resConfigs locales

        buildConfigField("String", "CROWDIN_API_KEY", "\"" + (System.getenv("CROWDIN_API_KEY") ?: localProperties["crowdin.apikey"] ) + "\"")
        resValue("string", "google_geo_api_key", System.getenv("GOOGLE_GEO_API_KEY") ?: localProperties["google.geo.apikey"] ?: "")
    }

    signingConfigs {
        jtx {
            storeFile = file(System.getenv("ANDROID_KEYSTORE") ?: localProperties["keystore.file"] ?: file("/dev/null"))
            storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD") ?: localProperties["keystore.password"]
            keyAlias = System.getenv("ANDROID_KEY_ALIAS") ?: localProperties["keystore.key.alias"]
            keyPassword = System.getenv("ANDROID_KEY_PASSWORD") ?: localProperties["keystore.key.password"]
        }
    }

    buildTypes {
        release {
            minifyEnabled = true
            shrinkResources = false
            proguardFiles(getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro')

            signingConfig = signingConfigs.jtx
        }

        benchmark {
            signingConfig = signingConfigs.debug
            matchingFallbacks = ['release']
            debuggable = false
        }

        // uncomment to test minifyEnabled without signing the package
        debug {
            //minifyEnabled = true
            //shrinkResources = true
            //proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }

    // Enables data binding.
    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.8"
    }

    packagingOptions {
        resources {
            excludes += ['META-INF/DEPENDENCIES', 'META-INF/LICENSE', 'META-INF/*.md']
            pickFirsts += ['META-INF/AL2.0', 'META-INF/LGPL2.1']
        }
    }

    flavorDimensions = "version"
    productFlavors {
        gplay {
            dimension = "version"
            versionNameSuffix = ".gplay"
        }
        amazon {
            dimension = "version"
            versionNameSuffix = ".amazon"
        }
        ose {
            dimension = "version"
            versionNameSuffix = ".ose"
        }
        generic {
            dimension = "version"
            versionNameSuffix = ".generic"
        }
        huawei {
            dimension = "version"
            versionNameSuffix = ".huawei"
        }
    }

    compileOptions {
        // Flag to enable support for the new language APIs, this is especially necessary to use java.time for versions < O
        // https://developer.android.com/studio/write/java8-support#library-desugaring
        coreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint {
        disable('MissingTranslation', 'ExtraTranslation')
    }
    sourceSets {
        main {
            res.srcDirs += ['build/generated/res/locale']
        }
    }
}


configurations {
    configureEach {
        // exclude modules which are in conflict with system libraries
        exclude("commons-logging")
        exclude("org.json","json")

        // Groovy requires SDK 26+, and it's not required, so exclude it
        exclude("org.codehaus.groovy")
    }
}


dependencies {
    val version_core = "1.12.0-beta01"
    val version_coroutine = "1.7.3"
    val version_appcompat = "1.6.1"
    val version_gradle = '8.1.0'
    val version_kotlin = "1.8.22"
    val version_room = "2.5.2"
    val version_work = "2.8.1"
    val version_billing = "6.0.1"
    val version_review = "2.0.1"
    val version_compose = "1.4.3"
    val version_about_libraries = "10.8.3"
    val version_huawei = "1.8.1.300"
    val version_huawei_iap = "6.10.0.300"
    val version_accompanist = "0.31.5-beta"
    val version_osmdroid = "6.1.16"

    implementation("com.github.bitfireAT:ical4android:a78e72f580")

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Android KTX
    implementation("androidx.core:core-ktx:$version_core")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    // Android Annotations
    implementation("androidx.annotation:annotation:1.6.0")

    // Room dependencies
    implementation("androidx.room:room-runtime:$version_room")
    implementation("androidx.room:room-ktx:$version_room")   // Kotlin Extensions and Coroutines support for Room
    kapt("androidx.room:room-compiler:$version_room")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$version_coroutine")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$version_coroutine")

    implementation("androidx.preference:preference-ktx:1.2.0")

    // Volley for simple network requests
    implementation("com.android.volley:volley:1.2.1")

    // Google billing
    gplayImplementation("com.android.billingclient:billing-ktx:$version_billing")
    // Google request for review
    gplayImplementation("com.google.android.play:review:$version_review")
    gplayImplementation("com.google.android.play:review-ktx:$version_review")

    // Amazon billing & maps support
    amazonImplementation("com.amazon.device:amazon-appstore-sdk:3.0.4")
    amazonImplementation("com.google.maps.android:maps-compose:2.12.0")
    amazonImplementation("com.google.android.gms:play-services-maps:18.1.0")
    amazonImplementation("com.google.android.gms:play-services-location:21.0.1")

    // Huawei billing
    //huaweiImplementation("com.huawei.hms:iap:$version_huawei_iap"

    // Google Maps
    gplayImplementation("com.google.maps.android:maps-compose:2.12.0")
    gplayImplementation("com.google.android.gms:play-services-maps:18.1.0")
    gplayImplementation("com.google.android.gms:play-services-location:21.0.1")

    //OSM
    oseImplementation("org.osmdroid:osmdroid-android:$version_osmdroid")
    genericImplementation("org.osmdroid:osmdroid-android:$version_osmdroid")
    huaweiImplementation("org.osmdroid:osmdroid-android:$version_osmdroid")

    // Workmanager support
    implementation("androidx.work:work-runtime-ktx:$version_work")
    androidTestImplementation("androidx.work:work-testing:$version_work")

    //support for java.time for older Android versions (<O)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
    //androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0-alpha07")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$version_coroutine")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")

    // Testing-only dependencies
    androidTestImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version_kotlin")
    androidTestImplementation("androidx.test:core-ktx:1.5.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.room:room-testing:$version_room")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("androidx.test:core-ktx:1.5.0")
    // Required -- JUnit 4 framework
    testImplementation("androidx.test.ext:junit-ktx:1.1.5")
    //testImplementation("org.mockito:mockito-core:5.3.1")

    //About Libraries
    implementation("com.mikepenz:aboutlibraries-core:${version_about_libraries}")
    implementation("com.mikepenz:aboutlibraries-compose:${version_about_libraries}")

    // Compose Color Picker
    implementation("com.godaddy.android.colorpicker:compose-color-picker-android:0.7.0")

    // appcompat for language selection
    implementation("androidx.appcompat:appcompat:$version_appcompat")

    // jetpack compose
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.compose.ui:ui:$version_compose")
    implementation("androidx.compose.material3:material3:1.2.0-alpha04")
    implementation("androidx.compose.material:material-icons-extended:1.4.3")
    implementation("androidx.compose.material:material:1.4.3")
    implementation("androidx.compose.ui:ui-tooling-preview:$version_compose")
    //implementation("androidx.activity:activity-compose:1.6.0")
    implementation("androidx.compose.runtime:runtime-livedata:$version_compose")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.navigation:navigation-compose:2.7.0-rc01")
    implementation("com.arnyminerz.markdowntext:markdowntext:1.3.2")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$version_compose")
    debugImplementation("androidx.compose.ui:ui-tooling:$version_compose")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$version_compose")

    // permissions from accompanist
    implementation("com.google.accompanist:accompanist-permissions:$version_accompanist")

    implementation("androidx.glance:glance-appwidget:1.0.0-alpha05")

    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha05")
    
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")
}
