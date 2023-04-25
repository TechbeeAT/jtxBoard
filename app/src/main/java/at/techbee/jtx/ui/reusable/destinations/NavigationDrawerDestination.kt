package at.techbee.jtx.ui.reusable.destinations

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import at.techbee.jtx.MainActivity2
import at.techbee.jtx.R

enum class NavigationDrawerDestination (
    val titleResource: Int,
    @DrawableRes val iconRes: Int,
    @StringRes val groupRes: Int? = null,
    val navigationAction: (navController: NavController, context: Context) -> Unit
    //val badgeCount: Int?
) {
    BOARD(
        titleResource = R.string.navigation_drawer_board,
        iconRes = R.drawable.ic_widget_jtx,
        navigationAction = { navHost, _ -> navHost.popBackStack(BOARD.name, false)}
    ),
    PRESETS(
        titleResource = R.string.navigation_drawer_presets,
        iconRes = R.drawable.ic_presets,
        navigationAction = { navHost, _ -> navHost.navigate(PRESETS.name)}
    ),
    COLLECTIONS(
        titleResource = R.string.navigation_drawer_collections,
        iconRes = R.drawable.ic_collection,
        navigationAction = { navHost, _ -> navHost.navigate(COLLECTIONS.name)}
    ),
    SYNC(
        titleResource = R.string.navigation_drawer_synchronization,
        iconRes = R.drawable.ic_sync,
        navigationAction = { navHost, _ -> navHost.navigate(SYNC.name)}
    ),
    ABOUT(
        titleResource = R.string.navigation_drawer_about,
        iconRes = R.drawable.ic_copyright,
        navigationAction = { navHost, _ -> navHost.navigate(ABOUT.name)}
    ),
    BUYPRO(
        titleResource = R.string.navigation_drawer_buypro,
        iconRes = R.drawable.ic_buypro_donate,
        navigationAction = { navHost, _ -> navHost.navigate(BUYPRO.name)}
    ),
    DONATE(
        titleResource = R.string.navigation_drawer_donate,
        iconRes = R.drawable.ic_buypro_donate,
        navigationAction = { navHost, _ -> navHost.navigate(DONATE.name)}
    ),
    SETTINGS(
        titleResource = R.string.navigation_drawer_settings,
        iconRes = R.drawable.ic_settings,
        navigationAction = { navHost, _ -> navHost.navigate(SETTINGS.name)}
    ),
    TWITTER(
        titleResource = R.string.twitter_account_name,
        iconRes = R.drawable.twitter,
        groupRes = R.string.navigation_drawer_news_updates,
        navigationAction = { _, context -> context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(context.getString(R.string.link_jtx_twitter))
            )
        ) }
    ),
    MASTODON(
        titleResource = R.string.mastodon_account_name,
        iconRes = R.drawable.logo_mastodon,
        groupRes = R.string.navigation_drawer_news_updates,
        navigationAction = { _, context -> context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(context.getString(R.string.link_jtx_mastodon))
            )
        ) }
    ),
    WEBSITE(
        titleResource = R.string.navigation_drawer_website,
        iconRes = R.drawable.ic_website,
        groupRes = R.string.navigation_drawer_external_links,
        navigationAction = { _, context -> context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(context.getString(R.string.link_jtx))
            )
        ) }
    ),
    SUPPORT(
        titleResource = R.string.navigation_drawer_support,
        iconRes = R.drawable.ic_support,
        groupRes = R.string.navigation_drawer_external_links,
        navigationAction = { _, context -> context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(context.getString(R.string.link_jtx_support))
            )
        ) }
    ),
    PRIVACY(
        titleResource = R.string.navigation_drawer_privacy_policy,
        iconRes = R.drawable.ic_privacy,
        groupRes = R.string.navigation_drawer_external_links,
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
                MainActivity2.BUILD_FLAVOR_GOOGLEPLAY -> listOf(BOARD, PRESETS, COLLECTIONS, SYNC, ABOUT, BUYPRO, SETTINGS, TWITTER, WEBSITE, SUPPORT, PRIVACY)
                MainActivity2.BUILD_FLAVOR_AMAZON -> listOf(BOARD, PRESETS, COLLECTIONS, SYNC, ABOUT, BUYPRO, SETTINGS, TWITTER, WEBSITE, SUPPORT, PRIVACY)
                MainActivity2.BUILD_FLAVOR_OSE -> listOf(BOARD, PRESETS, COLLECTIONS, SYNC, ABOUT, DONATE, SETTINGS, MASTODON, WEBSITE, SUPPORT, PRIVACY)
                MainActivity2.BUILD_FLAVOR_HUAWEI -> listOf(BOARD, PRESETS, COLLECTIONS, SYNC, ABOUT, BUYPRO, SETTINGS, TWITTER, WEBSITE, SUPPORT, PRIVACY)
                MainActivity2.BUILD_FLAVOR_GENERIC -> listOf(BOARD, PRESETS, COLLECTIONS, SYNC, ABOUT, SETTINGS, TWITTER, WEBSITE, SUPPORT, PRIVACY)
                else -> listOf(BOARD, PRESETS, COLLECTIONS, SYNC, ABOUT, BUYPRO, SETTINGS, TWITTER, WEBSITE, SUPPORT, PRIVACY)
            }
        }
    }

    fun getIconComposable(modifier: Modifier, tint: Color): @Composable () -> Unit {
        return {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = modifier,
                tint = tint
            )
        }
    }
}
