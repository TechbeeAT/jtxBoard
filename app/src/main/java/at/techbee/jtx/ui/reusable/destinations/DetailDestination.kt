package at.techbee.jtx.ui.reusable.destinations

import androidx.navigation.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


sealed class DetailDestination (
    val route: String,
    val args: List<NamedNavArgument>
    ) {

    companion object {
        const val argICalObjectId = "icalObjectId"
        const val argIsEditMode = "isEditMode"
        const val argICalObjectIdList = "icalObjectIdList"
    }

    object Detail: DetailDestination(
        route = "details/{$argICalObjectId}/{$argICalObjectIdList}/{$argIsEditMode}",
        args = listOf(
            navArgument(argICalObjectId) { type = NavType.LongType },
            navArgument(argICalObjectIdList) { type = NavType.StringType },
            navArgument(argIsEditMode) {
                type = NavType.BoolType
                defaultValue = false
            },
        )
    ) {
        fun getRoute(iCalObjectId: Long, icalObjectIdList: List<Long>, isEditMode: Boolean = false) = "details/$iCalObjectId/${Json.encodeToString(icalObjectIdList)}/$isEditMode"
    }
}
