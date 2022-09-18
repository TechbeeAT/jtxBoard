package at.techbee.jtx.ui.reusable.destinations

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class DetailDestination (
    val route: String,
    val args: List<NamedNavArgument>
    ) {

    companion object {
        const val argICalObjectId = "icalObjectId"
        const val argIsEditMode = "isEditMode"
    }

    object Detail: DetailDestination(
        route = "details/{$argICalObjectId}?$argIsEditMode={$argIsEditMode}",
        args = listOf(
            navArgument(argICalObjectId) { type = NavType.LongType },
            navArgument(argIsEditMode) {
                type = NavType.BoolType
                defaultValue = false
            }
        )
    ) {
        fun getRoute(iCalObjectId: Long, isEditMode: Boolean = false) = "details/$iCalObjectId?$argIsEditMode=$isEditMode"
    }

}
