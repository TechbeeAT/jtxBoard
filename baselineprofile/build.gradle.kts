import com.android.build.api.dsl.ManagedVirtualDevice

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "at.techbee.jtx.baselineprofile"
    compileSdk = 35

    kotlin {
        jvmToolchain(21) // Or your desired consistent JVM version
    }

    defaultConfig {
        minSdk = 28
        targetSdk = 35

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"

    flavorDimensions += listOf("version")
    productFlavors {
        create("gplay") { dimension = "version" }
        create("amazon") { dimension = "version" }
        create("ose") { dimension = "version" }
    }

    // This code creates the gradle managed device used to generate baseline profiles.
    // To use GMD please invoke generation through the command line:
    // ./gradlew :app:generateBaselineProfile
    testOptions.managedDevices.devices {
        create<ManagedVirtualDevice>("pixel6Api34") {
            device = "Pixel 6"
            apiLevel = 34
            systemImageSource = "google"
        }
    }
}

// This is the configuration block for the Baseline Profile plugin.
// You can specify to run the generators on a managed devices or connected devices.
baselineProfile {
    managedDevices += "pixel6Api34"
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.test.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}

androidComponents {
    onVariants { v ->
        val artifactsLoader = v.artifacts.getBuiltArtifactsLoader()
        v.instrumentationRunnerArguments.put(
            "targetAppId",
            v.testedApks.map { artifactsLoader.load(it)?.applicationId }
        )
    }
}