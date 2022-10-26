/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.util.TypedValue
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.ContentScale
import androidx.glance.unit.ColorProvider
import kotlin.math.roundToInt

/**
 * Provides an image component whose contents can be tinted.
 * @author Arnau Mora
 * @since 20220924
 * @param resource The image drawable to display.
 * @param tintColor A color provider to use as tint.
 * @param contentDescription The description of the image for accessibility.
 * @param imageHeight If not null, will override the drawable's height. Otherwise keeps intrinsic.
 * @param imageWidth If not null, will override the drawable's height. If null, keeps the intrinsic
 * width. Defaults to height for square images.
 * @param modifier The modifier to use for the image.
 * @param contentScale The content scale to apply for the image.
 */
@Composable
fun TintImage(
    @DrawableRes resource: Int,
    tintColor: ColorProvider,
    contentDescription: String,
    @Px imageHeight: Int? = null,
    @Px imageWidth: Int? = imageHeight,
    modifier: GlanceModifier = GlanceModifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val context = LocalContext.current
    Image(
        provider = ImageProvider(
            ContextCompat.getDrawable(context, resource)
                ?.apply { setTint(tintColor.getColor(context).toArgb()) }
                ?.let { drawable ->
                    drawable.toBitmapOrNull(
                        width = imageWidth ?: drawable.intrinsicWidth,
                        height = imageHeight ?: drawable.intrinsicHeight,
                    )
                }!!
        ),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
    )
}

/**
 * Converts the dps to px. <b>Attention! Use only with Glance.</b>
 * @author Arnau Mora
 * @since 20221001
 * @see <a href="https://stackoverflow.com/a/65921800/5717211">StackOverflow</a>
 */
val Dp.px: Int
    @Px
    @Composable
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.value,
        LocalContext.current.resources.displayMetrics,
    ).roundToInt()
