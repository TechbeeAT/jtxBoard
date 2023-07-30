/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.about

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.ui.graphics.vector.ImageVector
import at.techbee.jtx.R

sealed class AboutTabDestination (
    val titleResource: Int,
    val icon: ImageVector,
    //val badgeCount: Int?
) {
    object Jtx: AboutTabDestination(
        titleResource = R.string.about_tabitem_jtx,
        icon = Icons.Outlined.Info,
    )
    object JtxBoardPro: AboutTabDestination(
        titleResource = R.string.buypro_initial_dialog_title,
        icon = Icons.Outlined.CardGiftcard,
    )
    object Releasenotes: AboutTabDestination(
        titleResource = R.string.about_tabitem_releasenotes,
        icon = Icons.Outlined.NewReleases,
    )
    object Libraries: AboutTabDestination(
        titleResource = R.string.about_tabitem_libraries,
        icon = Icons.Outlined.DataObject,
    )
    object Translations: AboutTabDestination(
        titleResource = R.string.about_tabitem_translations,
        icon = Icons.Outlined.Translate,
    )
    object Contributors: AboutTabDestination(
        titleResource = R.string.about_tabitem_contributors,
        icon = Icons.Outlined.Handshake,
    )
}
