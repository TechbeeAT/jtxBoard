package at.techbee.jtx.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This test class generates a basic startup baseline profile for the target package.
 *
 * We recommend you start with this but add important user flows to the profile to improve their performance.
 * Refer to the [baseline profile documentation](https://d.android.com/topic/performance/baselineprofiles)
 * for more information.
 *
 * You can run the generator with the "Generate Baseline Profile" run configuration in Android Studio or
 * the equivalent `generateBaselineProfile` gradle task:
 * ```
 * ./gradlew :app:generateReleaseBaselineProfile
 * ```
 * The run configuration runs the Gradle task and applies filtering to run only the generators.
 *
 * Check [documentation](https://d.android.com/topic/performance/benchmarking/macrobenchmark-instrumentation-args)
 * for more information about available instrumentation arguments.
 *
 * After you run the generator, you can verify the improvements running the [StartupBenchmarks] benchmark.
 *
 * When using this class to generate a baseline profile, only API 33+ or rooted API 28+ are supported.
 *
 * The minimum required version of androidx.benchmark to generate a baseline profile is 1.2.0.
 **/
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        // The application id for the running build variant is read from the instrumentation arguments.
        val targetAppId = InstrumentationRegistry.getArguments().getString("targetAppId")
            ?: throw Exception("targetAppId not passed as instrumentation runner arg")

        rule.collect(
            packageName = targetAppId,
            // See: https://d.android.com/topic/performance/baselineprofiles/dex-layout-optimizations
            includeInStartupProfile = true
        ) {
            // This block defines the app's critical user journey. Here we are interested in
            // optimizing for app startup. But you can also navigate and scroll through your most important UI.

            // Start default activity for your app
            pressHome()
            startActivityAndWait()
            
            // Dismiss potential dialogs
            repeat(3) {
                device.wait(Until.hasObject(By.res(BENCHMARK_TAG_DIALOG_OK)), 2_000)
                device.findObject(By.res(BENCHMARK_TAG_DIALOG_OK))?.click()
            }
            
            if (device.wait(Until.hasObject(By.res(BENCHMARK_TAG_LISTCARD)), 10_000)) {
                device.findObject(By.res(BENCHMARK_TAG_LISTCARD))?.click()
                
                if (device.wait(Until.hasObject(By.res(BENCHMARK_TAG_DETAILSUMMARY)), 10_000)) {
                    device.findObject(By.res(BENCHMARK_TAG_DETAILSUMMARY))?.click()
                    
                    if (device.wait(Until.hasObject(By.res(BENCHMARK_TAG_DETAILSUMMARY_EDITTEXT)), 10_000)) {
                        device.findObject(By.res(BENCHMARK_TAG_DETAILSUMMARY_EDITTEXT))?.click()
                    }
                }
            }

            // Ensure the app has some time to settle and write profiles
            device.waitForIdle()
            Thread.sleep(5_000)
        }
    }
}
