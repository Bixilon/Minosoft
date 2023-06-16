package de.bixilon.minosoft.gui.rendering.font.types.bitmap

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.font.types.empty.EmptyCodeRenderer
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.system.dummy.texture.DummyTexture
import org.testng.Assert.*
import org.testng.annotations.Test
import java.nio.ByteBuffer
import java.util.stream.IntStream
import kotlin.reflect.full.companionObject


@Test(groups = ["font"])
class BitmapFontTypeTest {
    private val LOAD = BitmapFontType::class.companionObject!!.java.getDeclaredMethod("load", Texture::class.java, Int::class.java, Int::class.java, Array<IntStream>::class.java).apply { isAccessible = true }

    private fun createTexture(start: IntArray, end: IntArray, width: Int, height: Int): Texture {
        check(start.size == end.size)
        val rows = (start.size / 16) + 1
        val size = Vec2i(width * 16, rows * height)

        val buffer = ByteBuffer.allocate(size.x * size.y * 4)

        for (row in 0 until rows) {
            for (height in 0 until height) {
                for (char in 0 until 16) {
                    val start = start.getOrNull((row * 16) + char) ?: 0
                    val end = end.getOrNull((row * 16) + char) ?: width

                    for (pixel in 0 until width) {
                        buffer.put(0xFF.toByte())
                        buffer.put(0xFF.toByte())
                        buffer.put(0xFF.toByte())

                        if (pixel in start..end) {
                            buffer.put(0xFF.toByte())
                        } else {
                            buffer.put(0x00.toByte())
                        }
                    }
                }
            }
        }
        assertEquals(buffer.position(), buffer.limit())

        val texture = DummyTexture()
        texture.size = size
        texture.data = buffer

        return texture
    }

    private fun load(start: IntArray, end: IntArray, width: Int = 8, height: Int = 8, ascent: Int = 8, chars: Array<IntArray>): BitmapFontType {
        val texture = createTexture(start, end, width, height)

        val fontType = LOAD(BitmapFontType, texture, height, ascent, chars.map { IntStream.of(*it) }.toTypedArray()) as BitmapFontType


        return fontType
    }

    fun `space size`() {
        val font = load(intArrayOf(-1), intArrayOf(-1), chars = arrayOf(intArrayOf('a'.code)))

        val a = font['a'.code]!!
        assertTrue(a is EmptyCodeRenderer)
        assertEquals((a as EmptyCodeRenderer).width, 4)
    }

    fun `load basic with default options`() {
        val font = load(intArrayOf(1, 2, 3), intArrayOf(7, 4, 6), chars = arrayOf(intArrayOf('a'.code, 'b'.code, 'c'.code)))

        val a = font['a'.code]!! as BitmapCodeRenderer
        assertEquals(a.width, 7)
        assertEquals(a.uvStart, Vec2(0.0078125, 0))
        assertEquals(a.uvEnd, Vec2(0.0625, 0))

        // TODO
    }
}
