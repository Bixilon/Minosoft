package de.bixilon.minosoft.gui.rendering.font.renderer.component

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.font.manager.FontManager
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextLineInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextOffset
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.font.types.dummy.DummyFontType
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.MAX
import org.testng.Assert.assertEquals
import org.testng.Assert.assertFalse
import org.testng.annotations.Test

@Test(groups = ["font"])
class ChatComponentRendererTest {
    private val fontManager = FontManager(DummyFontType)

    private fun render(text: ChatComponent, fontManager: FontManager = this.fontManager, properties: TextRenderProperties = TextRenderProperties(shadow = false), maxSize: Vec2 = Vec2.MAX, consumer: GUIVertexConsumer? = null): TextRenderInfo {
        val info = TextRenderInfo(maxSize)
        ChatComponentRenderer.render(TextOffset(Vec2(10, 10)), fontManager, properties, info, consumer, null, text)

        return info
    }

    fun noText() {
        val info = render(ChatComponent.EMPTY)
        assertEquals(info.lineIndex, 0)
        assertEquals(info.lines.size, 0)
        assertEquals(info.size, Vec2())
    }

    fun emptyChar() {
        val info = render(TextComponent("a")) // a has a length of 0px
        assertEquals(info.lineIndex, 0)
        assertEquals(info.lines.size, 0)
        assertEquals(info.size, Vec2())
    }

    fun singleChar() {
        val info = render(TextComponent("b"))
        assertEquals(info.lineIndex, 0)
        assertEquals(info.lines, listOf(TextLineInfo(BaseComponent(TextComponent("b")), 0.5f)))
        assertEquals(info.size, Vec2(0.5f, 11.0f))
        assertFalse(info.cutOff)
    }

    fun `2 chars`() {
        val info = render(TextComponent("bc"))
        assertEquals(info.lineIndex, 0)
        assertEquals(info.lines, listOf(TextLineInfo(BaseComponent(TextComponent("bc")), 2.5f)))
        assertEquals(info.size, Vec2(2.5f, 11.0f)) // b + spacing + c
        assertFalse(info.cutOff)
    }

    fun `3 chars`() {
        val info = render(TextComponent("bcd"))
        assertEquals(info.lineIndex, 0)
        assertEquals(info.lines, listOf(TextLineInfo(BaseComponent(TextComponent("bcd")), 4.5f)))
        assertEquals(info.size, Vec2(4.5f, 11.0f)) // b + spacing + c + spacing + d
        assertFalse(info.cutOff)
    }

    fun `max line size`() {
        val info = render(TextComponent("bcdef"), maxSize = Vec2(5.0f, Float.MAX_VALUE))
        assertEquals(info.lineIndex, 1)
        assertEquals(info.lines, listOf(TextLineInfo(BaseComponent(TextComponent("bcd")), 4.5f), TextLineInfo(BaseComponent(TextComponent("ef")), 5.0f)))
        assertEquals(info.size, Vec2(5.0f, 22.0f)) // b + spacing + c + spacing + d \n e + spacing + f
        assertFalse(info.cutOff)
    }
}
