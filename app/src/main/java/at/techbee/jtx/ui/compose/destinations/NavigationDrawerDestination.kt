package at.techbee.jtx.ui.compose.destinations

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import at.techbee.jtx.MainActivity
import at.techbee.jtx.R

enum class NavigationDrawerDestination (
    val titleResource: Int,
    val icon: ImageVector? = null,
    val iconResource: Int? = null,
    val groupResource: Int? = null,
    val navigationAction: (navController: NavController, context: Context) -> Unit
    //val badgeCount: Int?
) {
    BOARD(
        titleResource = R.string.navigation_drawer_board,
        iconResource = R.drawable.ic_jtx,
        navigationAction = { navHost, _ -> navHost.navigate(BOARD.name)}
    ),
    COLLECTIONS(
        titleResource = R.string.navigation_drawer_collections,
        icon = Icons.Outlined.Folder,
        navigationAction = { navHost, _ -> navHost.navigate(COLLECTIONS.name)}
    ),
    SYNC(
        titleResource = R.string.navigation_drawer_sync,
        iconResource = R.drawable.davx5,
        navigationAction = { navHost, _ -> navHost.navigate(SYNC.name)}
    ),
    ABOUT(
        titleResource = R.string.navigation_drawer_about,
        icon = Icons.Outlined.Copyright,
        navigationAction = { navHost, _ -> navHost.navigate(ABOUT.name)}
    ),
    BUYPRO(
        titleResource = R.string.navigation_drawer_buypro,
        icon = Icons.Outlined.Folder,
        navigationAction = { navHost, _ -> navHost.navigate(BUYPRO.name)}
    ),
    ADINFO(
        titleResource = R.string.navigation_drawer_adinfo,
        icon = Icons.Outlined.AdsClick,
        navigationAction = { navHost, _ -> navHost.navigate(ADINFO.name)}
    ),
    DONATE(
        titleResource = R.string.navigation_drawer_donate,
        icon = Icons.Outlined.CardGiftcard,
        navigationAction = { navHost, _ -> navHost.navigate(DONATE.name)}
    ),
    SETTINGS(
        titleResource = R.string.navigation_drawer_settings,
        icon = Icons.Outlined.Settings,
        navigationAction = { navHost, _ -> navHost.navigate(SETTINGS.name)}
    ),
    TWITTER(
        titleResource = R.string.twitter_account_name,
        iconResource = R.drawable.twitter,
        groupResource = R.string.navigation_drawer_news_updates,
        navigationAction = { _, context -> context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(context.getString(R.string.link_jtx_twitter))
            )
        ) }
    ),
    WEBSITE(
        titleResource = R.string.navigation_drawer_website,
        icon = Icons.Outlined.Home,
        groupResource = R.string.navigation_drawer_external_links,
        navigationAction = { _, context -> context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(context.getString(R.string.link_jtx))
            )
        ) }
    ),
    NEWS(
        titleResource = R.string.navigation_drawer_website_news,
        icon = Icons.Outlined.Newspaper,
        groupResource = R.string.navigation_drawer_external_links,
        navigationAction = { _, context -> context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(context.getString(R.string.link_jtx_news))
            )
        ) }
    ),
    SUPPORT(
        titleResource = R.string.navigation_drawer_support,
        icon = Icons.Outlined.Support,
        groupResource = R.string.navigation_drawer_external_links,
        navigationAction = { _, context -> context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(context.getString(R.string.link_jtx_support))
            )
        ) }
    ),
    PRIVACY(
        titleResource = R.string.navigation_drawer_privacy_policy,
        icon = Icons.Outlined.PrivacyTip,
        groupResource = R.string.navigation_drawer_external_links,
        navigationAction = { _, context -> context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(context.getString(R.string.link_jtx_privacy_policy))
            )
        ) }
    );

    companion object {

        fun valuesFor(flavor: String): List<NavigationDrawerDestination> {
            return when(flavor) {
                MainActivity.BUILD_FLAVOR_GOOGLEPLAY -> listOf(BOARD, COLLECTIONS, SYNC, ABOUT, BUYPRO, SETTINGS, TWITTER, WEBSITE, NEWS, SUPPORT, PRIVACY)
                MainActivity.BUILD_FLAVOR_HUAWEI -> listOf(BOARD, COLLECTIONS, SYNC, ABOUT, ADINFO, SETTINGS, TWITTER, WEBSITE, NEWS, SUPPORT, PRIVACY)
                MainActivity.BUILD_FLAVOR_OSE -> listOf(BOARD, COLLECTIONS, SYNC, ABOUT, DONATE, SETTINGS, TWITTER, WEBSITE, NEWS, SUPPORT, PRIVACY)
                else -> listOf(BOARD, COLLECTIONS, SYNC, ABOUT, BUYPRO, SETTINGS, TWITTER, WEBSITE, NEWS, SUPPORT, PRIVACY)
            }
        }

    }
}
