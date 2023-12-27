package at.techbee.jtx.ui.reusable.destinations

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
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
    }

    data object Detail: DetailDestination(
        route = "details/{$argICalObjectId}/{$argICalObjectIdList}?$argIsEditMode={$argIsEditMode}&$argReturnToLauncher={$argReturnToLauncher}",
        args = listOf(
            navArgument(argICalObjectId) { type = NavType.LongType },
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
}
