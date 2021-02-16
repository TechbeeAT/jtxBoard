package at.bitfire.notesx5.database.properties

import android.content.ContentValues
import org.junit.Test

import org.junit.Assert.*

class AttendeeAndroidTest {
// Android Test as Content Values need Android libraries to run

    @Test
    fun fromContentValues_correct_Content_Values() {
        val conval = ContentValues()
        conval.put(COLUMN_ATTENDEE_ICALOBJECT_ID, 1L)
        conval.put(COLUMN_ATTENDEE_CALADDRESS, "mailto:test@test.com")

        val attendee = Attendee.fromContentValues(conval)

        assertEquals(attendee!!.icalObjectId, conval.get(COLUMN_ATTENDEE_ICALOBJECT_ID))
        assertEquals(attendee.caladdress, conval.get(COLUMN_ATTENDEE_CALADDRESS))
    }

    @Test
    fun fromContentValues_missing_caladdress() {
        val conval = ContentValues()
        conval.put(COLUMN_ATTENDEE_ICALOBJECT_ID, 1L)
        val attendee = Attendee.fromContentValues(conval)

        assertNull(attendee)
    }

    @Test
    fun fromContentValues_missing_icalobjectid() {
        val conval = ContentValues()
        conval.put(COLUMN_ATTENDEE_CALADDRESS, "mailto:test@test.com")
        val attendee = Attendee.fromContentValues(conval)

        assertNull(attendee)
    }

    @Test
    fun applyContentValues_icalObjectId() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = 1L

        conval.put(COLUMN_ATTENDEE_ICALOBJECT_ID, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.icalObjectId = newVal

        assertEquals(attendee, attendee2)
    }

    @Test
    fun applyContentValues_caladdress() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "https://mycaladdress.com"

        conval.put(COLUMN_ATTENDEE_CALADDRESS, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.caladdress = newVal

        assertEquals(attendee, attendee2)
    }

    @Test
    fun applyContentValues_cutype() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = Cutypeparam.INDIVIDUAL.param

        conval.put(COLUMN_ATTENDEE_CUTYPEPARAM, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.cutypeparam = newVal

        assertEquals(attendee, attendee2)
    }


    @Test
    fun applyContentValues_memberparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "https://mycalmembership.com"

        conval.put(COLUMN_ATTENDEE_MEMBERPARAM, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.memberparam = newVal

        assertEquals(attendee, attendee2)
    }

    @Test
    fun applyContentValues_roleparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = Roleparam.NON_PARTICIPANT.param

        conval.put(COLUMN_ATTENDEE_ROLEPARAM, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.roleparam = newVal

        assertEquals(attendee, attendee2)
    }


    @Test
    fun applyContentValues_partstatparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "ACCEPTED"

        conval.put(COLUMN_ATTENDEE_PARTSTATPARAM, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.partstatparam = newVal

        assertEquals(attendee, attendee2)
    }


    @Test
    fun applyContentValues_rvspparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "TRUE"

        conval.put(COLUMN_ATTENDEE_RSVPPARAM, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.rsvpparam = newVal

        assertEquals(attendee, attendee2)
    }

    @Test
    fun applyContentValues_deltoparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "mailto:me@home.com"

        conval.put(COLUMN_ATTENDEE_DELTOPARAM, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.deltoparam = newVal

        assertEquals(attendee, attendee2)
    }

    @Test
    fun applyContentValues_delfromparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "mailto:me@home.com"

        conval.put(COLUMN_ATTENDEE_DELFROMPARAM, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.delfromparam = newVal

        assertEquals(attendee, attendee2)
    }

    @Test
    fun applyContentValues_sentbyparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "mailto:me@home.com"

        conval.put(COLUMN_ATTENDEE_SENTBYPARAM, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.sentbyparam = newVal

        assertEquals(attendee, attendee2)
    }


    @Test
    fun applyContentValues_cnparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "Patrick"

        conval.put(COLUMN_ATTENDEE_CNPARAM, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.cnparam = newVal

        assertEquals(attendee, attendee2)
    }

    @Test
    fun applyContentValues_dirparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "/a/b/c"

        conval.put(COLUMN_ATTENDEE_DIRPARAM, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.dirparam = newVal

        assertEquals(attendee, attendee2)
    }

    @Test
    fun applyContentValues_languageparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "en"

        conval.put(COLUMN_ATTENDEE_LANGUAGEPARAM, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.languageparam = newVal

        assertEquals(attendee, attendee2)
    }

    @Test
    fun applyContentValues_otherparam() {

        val attendee = Attendee()
        val conval = ContentValues()
        val newVal = "whatever"

        conval.put(COLUMN_ATTENDEE_OTHERPARAM, newVal)
        val attendee2 = attendee.applyContentValues(conval)
        attendee.otherparam = newVal

        assertEquals(attendee, attendee2)
    }

}