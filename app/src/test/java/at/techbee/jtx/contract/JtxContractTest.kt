package at.techbee.jtx.contract

import net.fortuna.ical4j.model.Parameter
import net.fortuna.ical4j.model.ParameterList
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.PropertyList
import net.fortuna.ical4j.model.parameter.XParameter
import net.fortuna.ical4j.model.property.XProperty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// Can be removed once Robolectric supports SDK 37
@Config(sdk = [36])
class JtxContractTest {

    // region getXPropertyListFromJson() tests

    @Test
    fun `getXPropertyListFromJson with single property`() {
        val input = """{"X-PROPERTY":"value"}"""

        val result = JtxContract.getXPropertyListFromJson(input)

        assertEquals(propertyListOf(XProperty("X-PROPERTY", "value")), result)
    }

    @Test
    fun `getXPropertyListFromJson with multiple property`() {
        val input = """{"X-PROPERTY-1":"value1","X-PROPERTY-2":"value2","X-PROPERTY-3":"value3"}"""

        val result = JtxContract.getXPropertyListFromJson(input)

        assertEquals(
            propertyListOf(
                XProperty("X-PROPERTY-1", "value1"),
                XProperty("X-PROPERTY-2", "value2"),
                XProperty("X-PROPERTY-3", "value3"),
            ),
            result
        )
    }

    @Test
    fun `getXPropertyListFromJson with blank property name`() {
        val input = """{" ":"value"}"""

        val result = JtxContract.getXPropertyListFromJson(input)

        assertEquals(PropertyList(), result)
    }

    @Test
    fun `getXPropertyListFromJson with blank property value`() {
        val input = """{"X-PROPERTY":" "}"""

        val result = JtxContract.getXPropertyListFromJson(input)

        assertEquals(PropertyList(), result)
    }

    @Test
    fun `getXPropertyListFromJson without property`() {
        val input = "{}"

        val result = JtxContract.getXPropertyListFromJson(input)

        assertEquals(PropertyList(), result)
    }

    @Test
    fun `getXPropertyListFromJson with empty string`() {
        val input = ""

        val result = JtxContract.getXPropertyListFromJson(input)

        assertEquals(PropertyList(), result)
    }

    @Test
    fun `getXPropertyListFromJson with blank string`() {
        val input = " "

        val result = JtxContract.getXPropertyListFromJson(input)

        assertEquals(PropertyList(), result)
    }

    // endregion

    // region getJsonStringFromXProperties() tests

    @Test
    fun `getJsonStringFromXProperties with single property`() {
        val input = propertyListOf(XProperty("X-PROPERTY", "value"))

        val result = JtxContract.getJsonStringFromXProperties(input)

        assertEquals("""{"X-PROPERTY":"value"}""", result)
    }

    @Test
    fun `getJsonStringFromXProperties with multiple properties`() {
        val input = propertyListOf(
            XProperty("X-PROPERTY-1", "value1"),
            XProperty("X-PROPERTY-2", "value2"),
            XProperty("X-PROPERTY-3", "value3"),
        )

        val result = JtxContract.getJsonStringFromXProperties(input)

        assertEquals(
            """{"X-PROPERTY-1":"value1","X-PROPERTY-2":"value2","X-PROPERTY-3":"value3"}""",
            result
        )
    }

    @Test
    fun `getJsonStringFromXProperties with empty PropertyList`() {
        val input = PropertyList()

        val result = JtxContract.getJsonStringFromXProperties(input)

        assertNull(result)
    }

    @Test
    fun `getJsonStringFromXProperties with null argument`() {
        val input = null

        val result = JtxContract.getJsonStringFromXProperties(input)

        assertNull(result)
    }

    // endregion

    // region getXParametersFromJson() tests

    @Test
    fun `getXParametersFromJson with single parameter`() {
        val input = """{"X-PARAMETER":"value"}"""

        val result = JtxContract.getXParametersFromJson(input)

        assertEquals(listOf(XParameter("X-PARAMETER", "value")), result)
    }

    @Test
    fun `getXParametersFromJson with multiple parameters`() {
        val input = """
            {
                "X-PARAMETER-1": "value1",
                "X-PARAMETER-2": "value2",
                "X-PARAMETER-3": "value3"
            }
            """.trimIndent()

        val result = JtxContract.getXParametersFromJson(input)

        assertEquals(
            listOf(
                XParameter("X-PARAMETER-1", "value1"),
                XParameter("X-PARAMETER-2", "value2"),
                XParameter("X-PARAMETER-3", "value3"),
            ),
            result
        )
    }

    @Test
    fun `getXParametersFromJson with blank property name`() {
        val input = """{" ":"value"}"""

        val result = JtxContract.getXParametersFromJson(input)

        assertEquals(emptyList<XParameter>(), result)
    }

    @Test
    fun `getXParametersFromJson with blank property value`() {
        val input = """{"X-PARAMETER":" "}"""

        val result = JtxContract.getXParametersFromJson(input)

        assertEquals(emptyList<XParameter>(), result)
    }

    @Test
    fun `getXParametersFromJson without property`() {
        val input = "{}"

        val result = JtxContract.getXParametersFromJson(input)

        assertEquals(emptyList<XParameter>(), result)
    }

    // endregion

    // region getJsonStringFromXParameters() tests

    @Test
    fun `getJsonStringFromXParameters with single property`() {
        val input = parameterListOf(XParameter("X-PARAMETER", "value"))

        val result = JtxContract.getJsonStringFromXParameters(input)

        assertEquals("""{"X-PARAMETER":"value"}""", result)
    }

    @Test
    fun `getJsonStringFromXParameters with multiple properties`() {
        val input = parameterListOf(
            XParameter("X-PARAMETER-1", "value1"),
            XParameter("X-PARAMETER-2", "value2"),
            XParameter("X-PARAMETER-3", "value3"),
        )

        val result = JtxContract.getJsonStringFromXParameters(input)

        assertEquals(
            """{"X-PARAMETER-1":"value1","X-PARAMETER-2":"value2","X-PARAMETER-3":"value3"}""",
            result
        )
    }

    @Test
    fun `getJsonStringFromXParameters with empty ParameterList`() {
        val input = ParameterList()

        val result = JtxContract.getJsonStringFromXParameters(input)

        assertNull(result)
    }

    @Test
    fun `getJsonStringFromXParameters with null argument`() {
        val input = null

        val result = JtxContract.getJsonStringFromXParameters(input)

        assertNull(result)
    }

    // endregion
}

private fun propertyListOf(vararg properties: Property) = PropertyList(properties.toList())

private fun parameterListOf(vararg parameters: Parameter) = ParameterList(parameters.toList())
