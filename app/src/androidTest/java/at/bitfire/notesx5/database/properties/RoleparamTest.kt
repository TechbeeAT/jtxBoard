package at.bitfire.notesx5.database.properties

import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RoleparamTest {

    @Test
    fun getRoleparamById() {

        assertEquals(Roleparam.getRoleparamById(0), "CHAIR")

    }
}