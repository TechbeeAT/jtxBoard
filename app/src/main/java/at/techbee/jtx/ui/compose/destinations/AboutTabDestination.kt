package at.techbee.jtx.ui.compose.destinations

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import at.techbee.jtx.R

sealed class AboutTabDestination (
    val tabIndex: Int,
    val titleResource: Int,
    val icon: ImageVector,
    //val badgeCount: Int?
) {
    object Jtx: AboutTabDestination(
        tabIndex = 0,
        titleResource = R.string.about_tabitem_jtx,
        icon = Icons.Outlined.Info,
    )
    object Releasenotes: AboutTabDestination(
        tabIndex = 1,
        titleResource = R.string.about_tabitem_releasenotes,
        icon = Icons.Outlined.NewReleases,
    )
    object Libraries: AboutTabDestination(
        tabIndex = 2,
        titleResource = R.string.about_tabitem_libraries,
        icon = Icons.Outlined.DataObject,
    )
    object Translations: AboutTabDestination(
        tabIndex = 3,
        titleResource = R.string.about_tabitem_translations,
        icon = Icons.Outlined.Translate,
    )
    object Thanks: AboutTabDestination(
        tabIndex = 4,
        titleResource = R.string.about_tabitem_thanks,
        icon = Icons.Outlined.Handshake,
    )

}
