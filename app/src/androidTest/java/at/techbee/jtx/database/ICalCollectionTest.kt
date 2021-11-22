/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database

import android.content.ContentValues
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.properties.*
import org.junit.Test

import org.junit.Assert.*
import java.lang.IllegalArgumentException

class ICalCollectionTest {
// Android Test as Content Values need Android libraries to run


    @Test
    fun createFromContentValues() {

        val sampleCollection = ICalCollection(
            collectionId = 0L,
            url = "https://10.0.0.138",
            displayName = "TEST",
            description = "Test-Description",
            owner = "it's me, who else",
            color = 0,
            supportsVEVENT = true,
            supportsVTODO = true,
            supportsVJOURNAL = true,
            accountName = "Test-Account name",
            accountType = "TestType",
            syncversion = "V1",
            readonly = false
        )

        val cv = ContentValues(13).apply {
            put(COLUMN_COLLECTION_ID, sampleCollection.collectionId)
            put(COLUMN_COLLECTION_URL, sampleCollection.url)
            put(COLUMN_COLLECTION_DISPLAYNAME, sampleCollection.displayName)
            put(COLUMN_COLLECTION_DESCRIPTION, sampleCollection.description)
            put(COLUMN_COLLECTION_OWNER, sampleCollection.owner)
            put(COLUMN_COLLECTION_COLOR, sampleCollection.color)
            put(COLUMN_COLLECTION_SUPPORTSVEVENT, sampleCollection.supportsVEVENT)
            put(COLUMN_COLLECTION_SUPPORTSVTODO, sampleCollection.supportsVTODO)
            put(COLUMN_COLLECTION_SUPPORTSVJOURNAL, sampleCollection.supportsVJOURNAL)
            put(COLUMN_COLLECTION_ACCOUNT_NAME, sampleCollection.accountName)
            put(COLUMN_COLLECTION_ACCOUNT_TYPE, sampleCollection.accountType)
            put(COLUMN_COLLECTION_SYNC_VERSION, sampleCollection.syncversion)
            put(COLUMN_COLLECTION_READONLY, sampleCollection.readonly)
        }

        val cvResource = ICalCollection.fromContentValues(cv)
        assertEquals(sampleCollection, cvResource)
    }

    @Test
    fun createFromContentValuesWithoutValues() {

        val cvICalCollection = ICalCollection.fromContentValues(null)
        assertNull(cvICalCollection)
    }


    @Test(expected = IllegalArgumentException::class)
    fun createFromContentValuesWithForbiddenType() {


        val sampleCollection = ICalCollection(
            accountType = LOCAL_ACCOUNT_TYPE
        )

        val cv = ContentValues(13).apply {
            put(COLUMN_COLLECTION_ACCOUNT_TYPE, sampleCollection.accountType)
        }
        ICalCollection.fromContentValues(cv)
    }
}