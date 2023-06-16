package de.bixilon.minosoft.gui.rendering.font.types.bitmap

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.font.types.empty.EmptyCodeRenderer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.TextureData
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
        val rows = (start.size / 16) + if (start.size % 16 == 0) 0 else 1
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
        texture.data = TextureData(size, buffer)

        return texture
    }

    private fun load(start: IntArray, end: IntArray, width: Int = 8, height: Int = 8, ascent: Int = 8, chars: Array<IntArray>): BitmapFontType {
        val texture = createTexture(start, end, width, height)

        val fontType = LOAD(BitmapFontType, texture, height, ascent, chars.map { IntStream.of(*it) }.toTypedArray()) as BitmapFontType


        return fontType
    }

    private fun BitmapFontType.assert(char: Char, width: Float, uvStart: Vec2, uvEnd: Vec2) {
        val char = this[char.code]!! as BitmapCodeRenderer
        assertEquals(char.width, width, "width mismatch")
        assertEquals(char.uvStart, uvStart, "uv start mismatch")
        assertEquals(char.uvEnd, uvEnd, "uv end mismatch")
    }

    fun `space size`() {
        val font = load(intArrayOf(-1), intArrayOf(-1), chars = arrayOf(intArrayOf('a'.code)))

        val a = font['a'.code]!!
        assertTrue(a is EmptyCodeRenderer)
        assertEquals((a as EmptyCodeRenderer).width, 4)
    }

    fun `load basic with default options`() {
        val font = load(intArrayOf(1, 2, 3), intArrayOf(7, 4, 6), chars = arrayOf(intArrayOf('a'.code, 'b'.code, 'c'.code)))

        font.assert('a', 7.0f, Vec2(0.0068125f, 0), Vec2(0.0546875f, 1.0f))
        font.assert('b', 3.0f, Vec2(0.077125f, 0), Vec2(0.0859375f, 1.0f))
        font.assert('c', 4.0f, Vec2(0.1474375f, 0.0), Vec2(0.15625f, 1.0f))
    }

    fun `multiple rows`() {
        val font = load(IntArray(64) { it % 3 }, IntArray(64) { (it + 2) % 4 }, chars = arrayOf(IntArray(16) { 'A'.code + it }, IntArray(16) { 'A'.code + 16 + it }, IntArray(16) { 'A'.code + 32 + it }, IntArray(16) { 'A'.code + 48 + it }))

        font.assert('A', 3.0f, Vec2(0.0f, 0), Vec2(0.0234375f, 0.25f))
        font.assert('P', 2.0f, Vec2(0.9365f, 0.0), Vec2(0.953125f, 0.25f))

        font.assert('Q', 2.0f, Vec2(0.0068125f, 0.25f), Vec2(0.015625f, 0.5f))
        font.assert('a', 1.0f, Vec2(0.014625f, 0.5f), Vec2(0.0078125f, 0.75f))
        font.assert('q', 3.0f, Vec2(0.0f, 0.75f), Vec2(0.0234375f, 1.0f))
    }
}
