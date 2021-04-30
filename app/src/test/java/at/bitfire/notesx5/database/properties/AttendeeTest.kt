/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.notesx5.database.properties

import at.bitfire.notesx5.R
import org.junit.Test

import org.junit.Assert.*

class AttendeeTest {


    @Test
    fun getDrawableResourceByParam() {
        assertEquals(R.string.attendee_role_chair, Role.CHAIR.stringResource)
    }

    @Test
    fun checkCutypeparam() {
        assertEquals(Cutype.GROUP.name, "GROUP")
        assertEquals(Cutype.RESOURCE.name, "RESOURCE")
    }

}

