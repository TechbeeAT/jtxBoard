package at.techbee.jtx.ui.reusable.destinations

import androidx.navigation.*
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.properties.Category
import at.techbee.jtx.database.properties.Resource
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


sealed class DetailDestination (
    val route: String,
    val args: List<NamedNavArgument>
    ) {

    companion object {
        const val argICalObjectId = "icalObjectId"
        const val argIsEditMode = "isEditMode"
        const val argReturnToLauncher = "returnToLauncher"
        const val argICalObjectIdList = "icalObjectIdList"

        const val argCategory2Filter = "category2Filter"
        const val argResource2Filter = "resource2Filter"
        const val argModule2Open = "module2open"

    }

    object Detail: DetailDestination(
        route = "details/{$argICalObjectId}/{$argICalObjectIdList}?$argIsEditMode={$argIsEditMode}&$argReturnToLauncher={$argReturnToLauncher}",
        args = listOf(
            navArgument(argICalObjectId) { type = NavType.StringType },
            navArgument(argICalObjectIdList) { type = NavType.StringType },
            navArgument(argIsEditMode) {
                type = NavType.BoolType
                defaultValue = false
            },
            navArgument(argReturnToLauncher) {
                type = NavType.BoolType
                defaultValue = false
            }
        )
    ) {
        fun getRoute(
            iCalObjectId: Long,
            icalObjectIdList: List<Long>,
            isEditMode: Boolean = false,
            returnToLauncher: Boolean = false
        ): String {
            return "details/$iCalObjectId/${Json.encodeToString(icalObjectIdList)}?$argIsEditMode=$isEditMode&$argReturnToLauncher=$returnToLauncher"
        }
    }

    object PreFilteredBoard: DetailDestination(
        route = "preFilteredBoard/$argModule2Open?$argCategory2Filter={$argCategory2Filter}&$argResource2Filter={$argResource2Filter}",
        args = listOf(
            navArgument(argModule2Open) {
                type = NavType.StringType
            },
            navArgument(argCategory2Filter) {
                type = NavType.StringType
                nullable = true
            },
            navArgument(argResource2Filter) {
                type = NavType.StringType
                nullable = true
            },
        )
    ) {
        fun getRoute(
            module2open: Module,
            category2Filter: Category?,
            resource2Filter: Resource?
        ): String {
            return "preFilteredBoard/$module2open/?$argCategory2Filter=$category2Filter&$argResource2Filter=$resource2Filter"
        }
    }
}
