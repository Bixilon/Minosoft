package de.bixilon.minosoft.gui.rendering.font.types.unicode.unihex

import de.bixilon.minosoft.gui.rendering.font.types.unicode.unihex.UnihexFontType.Companion.fromHex
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.testng.Assert.assertEquals
import org.testng.Assert.assertThrows
import org.testng.annotations.Test
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.reflect.full.companionObject

@Test(groups = ["font"])
class UnihexFontTypeTest {
    val readUnihex = UnihexFontType::class.companionObject!!.java.getDeclaredMethod("readUnihex", InputStream::class.java, Int2ObjectOpenHashMap::class.java).apply { isAccessible = true }

    fun `from hex`() {
        assertEquals('0'.code.fromHex(), 0x00)
        assertEquals('9'.code.fromHex(), 0x09)

        assertEquals('a'.code.fromHex(), 0x0A)
        assertEquals('A'.code.fromHex(), 0x0A)

        assertEquals('f'.code.fromHex(), 0x0F)
        assertEquals('F'.code.fromHex(), 0x0F)

        assertThrows { 'z'.code.fromHex() }
    }

    fun `read unihex`() {
        val data = "0E01DB:555580000001A2382241A238140588380001B31808A1913020A9BB100001AAAA\n298B:0000000E0808080808080808080E000E"

        val chars = Int2ObjectOpenHashMap<ByteArray>()
        readUnihex(UnihexFontType, ByteArrayInputStream(data.toByteArray()), chars)

        assertEquals(chars.size, 2)
        val char = chars['\u298B'.code]
        assertEquals(char.size, 16)
        assertEquals(char, byteArrayOf(0x00, 0x00, 0x00, 0x0E, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x0E, 0x00, 0x0E))
    }

    fun `empty read unihex`() {
        val data = ""

        val chars = Int2ObjectOpenHashMap<ByteArray>()
        readUnihex(UnihexFontType, ByteArrayInputStream(data.toByteArray()), chars)

        assertEquals(chars.size, 0)
    }

    fun `trailing new lines read unihex`() {
        val data = "\n0E01DB:555580000001A2382241A238140588380001B31808A1913020A9BB100001AAAA\n298B:0000000E0808080808080808080E000E\n\n"

        val chars = Int2ObjectOpenHashMap<ByteArray>()
        readUnihex(UnihexFontType, ByteArrayInputStream(data.toByteArray()), chars)

        assertEquals(chars.size, 2)
        val char = chars['\u298B'.code]
        assertEquals(char.size, 16)
        assertEquals(char, byteArrayOf(0x00, 0x00, 0x00, 0x0E, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x0E, 0x00, 0x0E))
    }
}
