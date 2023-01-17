package at.techbee.benchmark

import androidx.benchmark.macro.*
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

const val BENCHMARK_TAG_LISTCARD = "benchmark:ListCard"
const val BENCHMARK_TAG_DETAILSUMMARY = "benchmark:DetailSummary"

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
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test fun startupCompilationNone() = startup(CompilationMode.None())
    @Test fun startupCompilationPartial() = startup(CompilationMode.Partial())

    @Test fun startupAndGoToDetailCompilationNone() = startupAndGoToDetail(CompilationMode.None())
    @Test fun startupAndGoToDetailCompilationPartial() = startupAndGoToDetail(CompilationMode.Partial())

    private fun startup(compilationMode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = "at.techbee.jtx",
        metrics = listOf(StartupTimingMetric()),
        iterations = 5,
        compilationMode = compilationMode,
        startupMode = StartupMode.COLD
    ) {
        pressHome()
        startActivityAndWait()

        device.wait(Until.hasObject(By.res("benchmark:ListCard")), 30_000)
    }

    private fun startupAndGoToDetail(compilationMode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = "at.techbee.jtx",
        metrics = listOf(StartupTimingMetric()),
        iterations = 5,
        compilationMode = compilationMode,
        startupMode = StartupMode.COLD,
    ) {
        pressHome()
        startActivityAndWait()
        device.wait(Until.hasObject(By.res(BENCHMARK_TAG_LISTCARD)), 30_000)
        device.findObject(By.res(BENCHMARK_TAG_LISTCARD)).click()
        device.wait(Until.hasObject(By.res(BENCHMARK_TAG_DETAILSUMMARY)), 30_000)
    }
}


// Add more benchmarks in future: https://www.youtube.com/watch?v=XHz_cFwdfoM
