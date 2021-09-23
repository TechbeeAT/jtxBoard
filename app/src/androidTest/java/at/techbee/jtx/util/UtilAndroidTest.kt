package at.techbee.jtx.util

import androidx.test.filters.SmallTest
import at.techbee.jtx.getLocalizedOrdinal
import at.techbee.jtx.getLocalizedWeekdays
import at.techbee.jtx.isLocalizedWeekstartMonday
import org.junit.Assert.*

import org.junit.Test
import java.util.*



@SmallTest
class UtilAndroidTest {

    @Test
    fun getLocalizedOrdinal_GERMAN() {

        Locale.setDefault(Locale.GERMAN)
        val ordinal = getLocalizedOrdinal(1, 4)

        assertEquals("1.", ordinal[0])
        assertEquals("2.", ordinal[1])
        assertEquals("3.", ordinal[2])
        assertEquals("4.", ordinal[3])
    }

    @Test
    fun getLocalizedOrdinal_ENGLISH() {

        Locale.setDefault(Locale.ENGLISH)
        val ordinal = getLocalizedOrdinal(1, 5)

        assertEquals("1st", ordinal[0])
        assertEquals("2nd", ordinal[1])
        assertEquals("3rd", ordinal[2])
        assertEquals("4th", ordinal[3])
        assertEquals("5th", ordinal[4])
    }

    @Test
    fun getLocalizedOrdinal_FRENCH() {

        Locale.setDefault(Locale.FRENCH)
        val ordinal = getLocalizedOrdinal(1, 3)

        assertEquals("1er", ordinal[0])
        assertEquals("2e", ordinal[1])
        assertEquals("3e", ordinal[2])
    }


    @Test
    fun getLocalizedWeekdays_GERMAN() {

        Locale.setDefault(Locale.GERMAN)

        val weekdays = getLocalizedWeekdays()

        assertEquals("Mo", weekdays[0])
        assertEquals("Di", weekdays[1])
        assertEquals("Mi", weekdays[2])
        assertEquals("Do", weekdays[3])
        assertEquals("Fr", weekdays[4])
        assertEquals("Sa", weekdays[5])
        assertEquals("So", weekdays[6])
    }

    @Test
    fun getLocalizedWeekdays_ENGLISH() {

        Locale.setDefault(Locale.ENGLISH)

        val weekdays = getLocalizedWeekdays()

        assertEquals("Sun", weekdays[0])
        assertEquals("Mon", weekdays[1])
        assertEquals("Tue", weekdays[2])
        assertEquals("Wed", weekdays[3])
        assertEquals("Thu", weekdays[4])
        assertEquals("Fri", weekdays[5])
        assertEquals("Sat", weekdays[6])
    }

    @Test
    fun getLocalizedWeekdays_US() {

        Locale.setDefault(Locale.US)

        val weekdays = getLocalizedWeekdays()

        assertEquals("Sun", weekdays[0])
        assertEquals("Mon", weekdays[1])
        assertEquals("Tue", weekdays[2])
        assertEquals("Wed", weekdays[3])
        assertEquals("Thu", weekdays[4])
        assertEquals("Fri", weekdays[5])
        assertEquals("Sat", weekdays[6])
    }


    @Test
    fun getLocalizedWeekdays_FRENCH() {

        Locale.setDefault(Locale.FRENCH)

        val weekdays = getLocalizedWeekdays()

        assertEquals("lun.", weekdays[0])
        assertEquals("mar.", weekdays[1])
        assertEquals("mer.", weekdays[2])
        assertEquals("jeu.", weekdays[3])
        assertEquals("ven.", weekdays[4])
        assertEquals("sam.", weekdays[5])
        assertEquals("dim.", weekdays[6])
    }

    @Test
    fun isLocalizedWeekstartMonday_GERMAN() {
        Locale.setDefault(Locale.GERMAN)
        assertEquals(true, isLocalizedWeekstartMonday())
    }

    @Test
    fun isLocalizedWeekstartMonday_US() {
        Locale.setDefault(Locale.US)
        assertEquals(false, isLocalizedWeekstartMonday())
    }
}