package at.bitfire.notesx5.database.properties

import at.bitfire.notesx5.R
import org.junit.Test

import org.junit.Assert.*

class AttendeeTest {

    @Test
    fun getRoleparamById() {

        assertEquals(Role.getRoleparamById(0), "CHAIR")
        assertEquals(Role.getRoleparamById(3), Role.NON_PARTICIPANT.param)
        assertEquals(Role.getRoleparamById(Role.REQ_PARTICIPANT.id), Role.REQ_PARTICIPANT.param)
    }

    @Test
    fun getDrawableResourceByParam() {
        assertEquals(R.string.attendee_role_chair, Role.CHAIR.stringResource)
    }

    @Test
    fun checkCutypeparam() {
        assertEquals(Cutype.INDIVIDUAL.id, 0)
        assertEquals(Cutype.GROUP.name, "GROUP")
        assertEquals(Cutype.RESOURCE.name, "RESOURCE")
    }

}

