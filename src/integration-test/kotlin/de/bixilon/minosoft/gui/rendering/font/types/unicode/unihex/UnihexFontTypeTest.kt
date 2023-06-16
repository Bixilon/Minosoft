package de.bixilon.minosoft.gui.rendering.font.types.unicode.unihex

import de.bixilon.minosoft.gui.rendering.font.types.unicode.unihex.UnihexFontType.Companion.fromHex
import org.testng.Assert.assertEquals
import org.testng.Assert.assertThrows
import org.testng.annotations.Test

@Test(groups = ["font"])
class UnihexFontTypeTest {

    fun `from hex`() {
        assertEquals('0'.code.fromHex(), 0x00)
        assertEquals('9'.code.fromHex(), 0x09)

        assertEquals('a'.code.fromHex(), 0x0A)
        assertEquals('A'.code.fromHex(), 0x0A)

        assertEquals('f'.code.fromHex(), 0x0F)
        assertEquals('F'.code.fromHex(), 0x0F)

        assertThrows { 'z'.code.fromHex() }
    }
}
