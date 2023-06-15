package de.bixilon.minosoft.gui.rendering.font.types.unicode.legacy

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.system.dummy.texture.DummyTexture
import org.testng.Assert
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.reflect.full.companionObject

@Test(groups = ["font"])
class LegacyUnicodeFontTypeTest {
    private val LOAD_PAGE = LegacyUnicodeFontType::class.companionObject!!.java.getDeclaredMethod("loadPage", Int::class.java, AbstractTexture::class.java, Array<LegacyUnicodeCodeRenderer>::class.java, InputStream::class.java)

    init {
        LOAD_PAGE.isAccessible = true
    }

    private fun createTexture(): DummyTexture {
        return DummyTexture(minosoft("test"))
    }

    private fun load(sizes: ByteArray): Array<LegacyUnicodeCodeRenderer?> {
        val array = ByteArray(256)
        System.arraycopy(sizes, 0, array, 0, sizes.size)

        val sizes = ByteArrayInputStream(array)
        val texture = createTexture()
        val chars: Array<LegacyUnicodeCodeRenderer?> = arrayOfNulls(256)


        LOAD_PAGE.invoke(LegacyUnicodeFontType, 0, texture, chars, sizes)

        return chars
    }

    fun basicLoading() {
        val chars = load(byteArrayOf())
        assertEquals(chars.size, 256)

        for (entry in chars) {
            Assert.assertNotNull(entry)
        }
    }

    fun firstChar() {
        val sizes = byteArrayOf(0x09, 0x12, 0x45)
        val chars = load(sizes)

        val char = chars[0]!!
        assertEquals(char.uvStart, Vec2(0, 0))
        assertEquals(char.uvEnd, Vec2(0.0390625f, 0.0625f))
        assertEquals(char.width, 5.0f)
    }

    fun emptyChar() {
        val sizes = byteArrayOf(0x00)
        val chars = load(sizes)

        val char = chars[0]!!
        assertEquals(char.uvStart, Vec2(0, 0))
        assertEquals(char.uvEnd, Vec2(0.00390625, 0.0625))
        assertEquals(char.width, 0.5f)
    }

    fun secondChar() {
        val sizes = byteArrayOf(0x00, 0x18)
        val chars = load(sizes)

        val char = chars[1]!!
        assertEquals(char.uvStart, Vec2(0.06640625, 0))
        assertEquals(char.uvEnd, Vec2(0.09765625, 0.0625f))
        assertEquals(char.width, 4.0f)
    }

    fun `19th char`() {
        val sizes = ByteArray(19)
        sizes[18] = 0x8E.toByte()
        val chars = load(sizes)

        val char = chars[18]!!
        assertEquals(char.uvStart, Vec2(0.15625, 0.0625))
        assertEquals(char.uvEnd, Vec2(0.18359375, 0.125))
        assertEquals(char.width, 3.5f)
    }
}
