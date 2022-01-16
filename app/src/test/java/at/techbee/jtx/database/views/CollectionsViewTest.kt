/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.database.views

import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import org.junit.Assert.*

import org.junit.Test

class CollectionsViewTest {

    @Test
    fun toICalCollection() {
        val view = CollectionsView().apply {
            this.collectionId = 222L
            this.accountName = "test"
            this.accountType = LOCAL_ACCOUNT_TYPE
            this.color = 65280
            this.description = "testDesc"
            this.displayName = "test Display name"
            this.numJournals = 5
            this.numNotes = 8
            this.numTodos = 9
            this.owner = "owner"
            this.readonly = false
            this.supportsVEVENT = true
            this.supportsVJOURNAL = true
            this.supportsVTODO = true
            this.syncversion = "1"
            this.url = "https://localhost"
        }

        val collection = view.toICalCollection()
        assertEquals(view.collectionId, collection.collectionId)
        assertEquals(view.accountName, collection.accountName)
        assertEquals(view.accountType, collection.accountType)
        assertEquals(view.color, collection.color)
        assertEquals(view.description, collection.description)
        assertEquals(view.displayName, collection.displayName)
        assertEquals(view.owner, collection.owner)
        assertEquals(view.readonly, collection.readonly)
        assertEquals(view.supportsVEVENT, collection.supportsVEVENT)
        assertEquals(view.supportsVJOURNAL, collection.supportsVJOURNAL)
        assertEquals(view.supportsVTODO, collection.supportsVTODO)
        assertEquals(view.syncversion, collection.syncversion)
        assertEquals(view.url, collection.url)
    }
}