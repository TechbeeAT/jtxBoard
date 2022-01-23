/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.util

import android.accounts.Account
import android.content.ContentResolver
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import at.techbee.jtx.AUTHORITY_FILEPROVIDER
import at.techbee.jtx.SyncContentProviderTest
import at.techbee.jtx.database.ICalDatabase
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream

class Ical4androidUtilTest {

    private var mContentResolver: ContentResolver? = null

    private var defaultTestAccount = Account("testAccount", "testAccount")
    private var defaultCollectionUri: Uri? = null
    private var defaultCollectionId: Long? = null
    private var defaultICalObjectUri: Uri? = null
    private var defaultICalObjectId: Long? = null

    private val fileContentProviderBaseUri = "content://$AUTHORITY_FILEPROVIDER"


    @Before
    fun setUp() {
        //val context: Context = ApplicationProvider.getApplicationContext()
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        ICalDatabase.switchToInMemory(context)
        mContentResolver = context.contentResolver

        //prepare
        defaultCollectionUri = SyncContentProviderTest.insertCollection(defaultTestAccount, null, null, mContentResolver)
        defaultCollectionId = defaultCollectionUri?.lastPathSegment?.toLongOrNull()

        defaultICalObjectUri = SyncContentProviderTest.insertIcalObject(defaultTestAccount, "summary", defaultCollectionId!!, mContentResolver)
        defaultICalObjectId = defaultICalObjectUri?.lastPathSegment?.toLongOrNull()
    }

    @After
    fun tearDown() {

        //cleanup
        mContentResolver?.delete(defaultCollectionUri!!, null, null)
        defaultCollectionUri = null
        defaultCollectionId = null
        defaultICalObjectId = null
        defaultICalObjectUri = null
    }


    @Test
    fun getICSFormatFromProvider() {
        val ics = Ical4androidUtil.getICSFormatFromProvider(defaultTestAccount, InstrumentationRegistry.getInstrumentation().targetContext, defaultCollectionId!!, defaultICalObjectId!!)
        assertTrue(ics?.contains("BEGIN:VCALENDAR") == true)
        assertTrue(ics?.contains("BEGIN:VJOURNAL") == true)
        assertTrue(ics?.contains("SUMMARY:summary") == true)
        assertTrue(ics?.contains("END:VJOURNAL") == true)
        assertTrue(ics?.contains("END:VCALENDAR") == true)
    }

    @Test
    fun writeICSFormatFromProviderToOS() {
        val os = ByteArrayOutputStream()
        Ical4androidUtil.writeICSFormatFromProviderToOS(defaultTestAccount, InstrumentationRegistry.getInstrumentation().targetContext, defaultCollectionId!!, defaultICalObjectId!!, os)

        val ics = os.toString()
        assertTrue(ics.contains("BEGIN:VCALENDAR"))
        assertTrue(ics.contains("BEGIN:VJOURNAL"))
        assertTrue(ics.contains("SUMMARY:summary"))
        assertTrue(ics.contains("END:VJOURNAL"))
        assertTrue(ics.contains("END:VCALENDAR"))
    }
}