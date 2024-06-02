package at.techbee.jtx.ui.reusable.destinations

import android.net.Uri
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.locals.StoredListSettingData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


sealed class FilteredListDestination (
    val route: String,
    val args: List<NamedNavArgument>
    ) {

    companion object {
        const val ARG_MODULE = "module"
        const val ARG_STORED_LIST_SETTING_DATA = "storedListSettingData"
    }

    data object FilteredList: FilteredListDestination(
        route = "filteredList/{$ARG_MODULE}?$ARG_STORED_LIST_SETTING_DATA={$ARG_STORED_LIST_SETTING_DATA}",
        args = listOf(
            navArgument(ARG_MODULE) { type = NavType.StringType },
            navArgument(ARG_STORED_LIST_SETTING_DATA) { type = NavType.StringType }
        )
    ) {
        fun getRoute(
            module: Module,
            storedListSettingData: StoredListSettingData?
        ): String {
            return Uri.parse("filteredList/${module.name}")
                .buildUpon()
                .appendQueryParameter(ARG_STORED_LIST_SETTING_DATA, storedListSettingData?.let { Json.encodeToString(it)})
                .build()
                .toString()
        }
    }

    data object FilteredListFromWidget: FilteredListDestination(
        route = "filteredListFromWidget",
        args = emptyList()
    )
}
