/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import at.techbee.jtx.R


/*
val montserratAlternates = FontFamily(
    Font(R.font.montserrat_alternates)
)
 */

val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val robotoFont = FontFamily(Font(googleFont = GoogleFont("Roboto"), fontProvider = fontProvider))

val notoFont = FontFamily(Font(googleFont = GoogleFont("Noto Sans"), fontProvider = fontProvider))

val montserratAlternatesFont = FontFamily(Font(googleFont = GoogleFont("Montserrat Alternates"), fontProvider = fontProvider))