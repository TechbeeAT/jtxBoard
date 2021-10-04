package at.techbee.jtx.util

import at.techbee.jtx.*
import org.junit.Assert.*
import org.junit.Test

class UtilKtTest {

    val sampleDateTime = 1632395090107L   // = Thu Sep 23 2021 11:04:50 (UTC)

    // TODO: Those tests might fail in the future as the methods return locales, check for a better solution
    //@Test fun convertLongToDateString() = assertEquals("", convertLongToDateString(sampleDateTime))
    //@Test fun convertLongToTimeString()  = assertEquals("11:04", convertLongToTimeString(sampleDateTime))
    //@Test fun convertLongToMonthString() = assertEquals("September", convertLongToMonthString(sampleDateTime))

    @Test fun convertLongToDayString_test() = assertEquals("23", convertLongToDayString(sampleDateTime))
    @Test fun convertLongToYearString_test() = assertEquals("2021", convertLongToYearString(sampleDateTime))

    @Test fun isValidEmail_testTrue() = assertTrue(isValidEmail("valid@email.com"))
    @Test fun isValidEmail_testFalse1() = assertFalse(isValidEmail("invalid.com"))
    @Test fun isValidEmail_testFalse2() = assertFalse(isValidEmail("invalid@com"))

    @Test fun isValidURL_testTrue1() = assertTrue(isValidURL("example.com"))
    @Test fun isValidURL_testTrue2() = assertTrue(isValidURL("www.example.com"))
    @Test fun isValidURL_testTrue3() = assertTrue(isValidURL("http://example.com"))
    @Test fun isValidURL_testTrue4() = assertTrue(isValidURL("https://www.example.com/asdf"))
    @Test fun isValidURL_testFalse1() = assertFalse(isValidURL("AABB"))
    @Test fun isValidURL_testFalse2() = assertFalse(isValidURL("asdf://AABB.com"))

    @Test fun getAttachmentSizeString_bytes() = assertEquals("100 Bytes", getAttachmentSizeString(100))
    @Test fun getAttachmentSizeString_kilobytes() = assertEquals("1 KB", getAttachmentSizeString(1024))
    @Test fun getAttachmentSizeString_megabytes() = assertEquals("1 MB", getAttachmentSizeString(1048576))

    @Test fun addLongToCSVString() = assertEquals(("1622800800000,1622887200000,1624010400000"), addLongToCSVString("1622800800000,1622887200000", 1624010400000L))
    @Test fun addLongToCSVString_noDuplicate() = assertEquals(("1622800800000,1622887200000,1624010400000"), addLongToCSVString("1622800800000,1622887200000,1624010400000", 1624010400000L))
    @Test fun addLongToCSVString_fromEmpty() = assertEquals(("1624010400000"), addLongToCSVString(null, 1624010400000L))
    @Test fun addLongToCSVString_fromEmpty2() = assertEquals(("1624010400000"), addLongToCSVString("", 1624010400000L))
    @Test fun addLongToCSVString_fromEmpty3() = assertEquals(("1624010400000"), addLongToCSVString("   ", 1624010400000L))

    @Test fun getLongListfromCSVString() {
        val list = getLongListfromCSVString("1622800800000,1622887200000,1624010400000")
        assertEquals(1622800800000L, list[0])
        assertEquals(1622887200000L, list[1])
        assertEquals(1624010400000L, list[2])
    }

    @Test fun getLongListfromCSVString_oneException() {
        val list = getLongListfromCSVString("asdf,1622887200000,1624010400000")
        assertEquals(1622887200000L, list[0])
        assertEquals(1624010400000L, list[1])
    }

    @Test fun getLongListfromCSVString_empty() = assertEquals(emptyList<Long>(), getLongListfromCSVString(null))
    @Test fun getLongListfromCSVString_empty2() = assertEquals(emptyList<Long>(), getLongListfromCSVString("asdf"))


}