package at.techbee.jtx.ui.reusable.destinations

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.locals.StoredListSettingData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder


sealed class FilteredListDestination (
    val route: String,
    val args: List<NamedNavArgument>
    ) {

    companion object {
        const val argModule = "module"
        const val argStoredListSettingData = "storedListSettingData"
    }

    object FilteredList: FilteredListDestination(
        route = "filteredList/{$argModule}?$argStoredListSettingData={$argStoredListSettingData}",
        args = listOf(
            navArgument(argModule) { type = NavType.StringType },
            navArgument(argStoredListSettingData) { type = NavType.StringType }
        )
    ) {
        fun getRoute(
            module: Module,
            storedListSettingData: StoredListSettingData?
        ): String {
            return "filteredList/${module.name}?" + storedListSettingData?.let { "$argStoredListSettingData=${URLEncoder.encode(Json.encodeToString(it), "utf-8")}" }
        }
    }
}
