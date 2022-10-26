package at.techbee.jtx.util

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.os.Parcelable
import kotlin.reflect.KClass

/**
 * Uses the correct method for the current sdk level to get a [Parcelable] extra.
 * @since 20220928
 * @param key The name of the desired item.
 * @param kClass The type of the object expected.
 * @return The value of an item previously added with [Intent.putExtra], or null if no [Parcelable]
 * value was found.
 */
@Suppress("DEPRECATION")
fun <C: Parcelable> Intent.getParcelableExtraCompat(key: String, kClass: KClass<C>): C? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        getParcelableExtra(key, kClass.java)
    else
        getParcelableExtra(key)

/**
 * Uses the correct method for the current SDK level to get the package info.
 * @since 20220929
 * @param packageName The full name (i.e. com.google.apps.contacts) of the desired package.
 * @param flags Additional option flags to modify the data returned.
 * @return A PackageInfo object containing information about the package. If flag
 * [MATCH_UNINSTALLED_PACKAGES] is set and if the package is not found in the list of installed
 * applications, the package information is retrieved from the list of uninstalled applications
 * (which includes installed applications as well as applications with data directory i.e.
 * applications which had been deleted with `DELETE_KEEP_DATA` flag set).
 * @throws NameNotFoundException If no such package is available to the caller.
 */
@Suppress("DEPRECATION")
@Throws(NameNotFoundException::class)
fun PackageManager.getPackageInfoCompat(
    packageName: String,
    flags: Long,
): PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
    getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags))
else
    getPackageInfo(packageName, flags.toInt())
