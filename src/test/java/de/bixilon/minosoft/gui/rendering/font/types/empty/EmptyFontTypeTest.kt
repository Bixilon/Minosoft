package de.bixilon.minosoft.gui.rendering.font.types.empty

import de.bixilon.kutil.json.JsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EmptyFontTypeTest {

    @Test
    fun empty1() {
        val json: JsonObject = mapOf()
        assertNull(EmptyFontType.load(json))
    }

    @Test
    fun empty2() {
        val json: JsonObject = mapOf("advances" to mapOf<String, Any>())
        assertNull(EmptyFontType.load(json))
    }

    @Test
    fun singleChar() {
        val json: JsonObject = mapOf("advances" to mapOf(" " to 10))
        assertEquals(EmptyFontType.load(json), mapOf(' '.code to 10))
    }

    @Test
    fun multipleChars() {
        val json: JsonObject = mapOf("advances" to mapOf(" " to 10, "a" to 0))
        assertEquals(EmptyFontType.load(json), mapOf(' '.code to 10, 'a'.code to 0))
    }

    @Test
    fun invalidWith() {
        val json: JsonObject = mapOf("advances" to mapOf("r" to 1037))
        assertThrows<IllegalArgumentException> { EmptyFontType.load(json) }
    }
}
