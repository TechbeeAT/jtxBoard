package at.techbee.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This is an example startup benchmark.
 *
 * It navigates to the device's home screen, and launches the default activity.
 *
 * Before running this benchmark:
 * 1) switch your app's active build variant in the Studio (affects Studio runs only)
 * 2) add `<profileable android:shell="true" />` to your app's manifest, within the `<application>` tag
 *
 * Run this benchmark from Studio to see startup measurements, and captured system traces
 * for investigating your app's performance.
 *
 * Execute the following command to generate the baseline profile:
 * ./gradlew :benchmark:pixel2Api31BenchmarkAndroidTest -P android.testInstrumentationRunnerArguments.androidx.benchmark.enabledRules=BaselineProfile
 * Follow the instructions in the video to copy the generated file to the main folder:
 * https://www.youtube.com/watch?v=hqYnZ5qCw8Y
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collectBaselineProfile("at.techbee.jtx") {
        pressHome()
        startActivityAndWait()
        device.wait(Until.hasObject(By.res(BENCHMARK_TAG_LISTCARD)), 30_000)
        device.findObject(By.res(BENCHMARK_TAG_LISTCARD)).click()
        device.wait(Until.hasObject(By.res(BENCHMARK_TAG_DETAILSUMMARY)), 30_000)
    }
}