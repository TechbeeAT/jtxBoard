package at.bitfire.notesx5.database.properties

import at.bitfire.notesx5.R
import org.junit.Test

import org.junit.Assert.*

class AttendeeTest {

    @Test
    fun getRoleparamById() {

        assertEquals(Roleparam.getRoleparamById(0), "CHAIR")
        assertEquals(Roleparam.getRoleparamById(3), Roleparam.NON_PARTICIPANT.param)
        assertEquals(Roleparam.getRoleparamById(Roleparam.REQ_PARTICIPANT.id), Roleparam.REQ_PARTICIPANT.param)
    }

    @Test
    fun getDrawableResourceByParam() {
        assertEquals(R.string.attendee_role_chair, Roleparam.CHAIR.stringResource)
    }

    @Test
    fun checkCutypeparam() {
        assertEquals(Cutypeparam.INDIVIDUAL.id, 0)
        assertEquals(Cutypeparam.GROUP.name, "GROUP")
        assertEquals(Cutypeparam.RESOURCE.name, "RESOURCE")
    }

}

