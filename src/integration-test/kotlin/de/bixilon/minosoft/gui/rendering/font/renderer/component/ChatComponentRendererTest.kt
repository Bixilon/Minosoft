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
import org.testng.annotations.Test

@Test(groups = ["font"])
class ChatComponentRendererTest {
    private val fontManager = FontManager(DummyFontType)

    private fun render(text: ChatComponent, fontManager: FontManager = this.fontManager, properties: TextRenderProperties = TextRenderProperties(shadow = false), maxSize: Vec2 = Vec2.MAX, consumer: GUIVertexConsumer? = null): TextRenderInfo {
        val info = TextRenderInfo(maxSize)
        ChatComponentRenderer.render(TextOffset(Vec2(10, 10)), fontManager, properties, info, consumer, null, text)

        return info
    }

    private fun TextRenderInfo.assert(
        lineIndex: Int? = null,
        lines: List<TextLineInfo>? = null,
        size: Vec2? = null,
        cutOff: Boolean = false,
    ) {
        if (lineIndex != null) assertEquals(this.lineIndex, lineIndex, "Line index mismatch")
        if (lines != null) assertEquals(this.lines, lines, "Lines mismatch")
        if (size != null) assertEquals(this.size, size, "Size mismatch")
        assertEquals(this.cutOff, cutOff, "Cutoff mismatch!")
    }

    fun noText() {
        val info = render(ChatComponent.EMPTY)
        info.assert(lineIndex = 0, lines = emptyList(), size = Vec2())
    }

    fun emptyChar() {
        val info = render(TextComponent("a")) // a has a length of 0px
        info.assert(lineIndex = 0, lines = emptyList(), size = Vec2())
    }

    fun singleChar() {
        val info = render(TextComponent("b"))
        info.assert(
            lineIndex = 0,
            lines = listOf(TextLineInfo(BaseComponent(TextComponent("b")), 0.5f)),
            size = Vec2(0.5f, 11.0f),
        )
    }

    fun `2 chars`() {
        val info = render(TextComponent("bc"))
        info.assert(
            lineIndex = 0,
            lines = listOf(TextLineInfo(BaseComponent(TextComponent("bc")), 2.5f)),
            size = Vec2(2.5f, 11.0f), // b + spacing + c
        )
    }

    fun `3 chars`() {
        val info = render(TextComponent("bcd"))
        info.assert(
            lineIndex = 0,
            lines = listOf(TextLineInfo(BaseComponent(TextComponent("bcd")), 5.0f)),
            size = Vec2(5.0f, 11.0f),
        )
    }

    fun `max line size 1`() {
        val info = render(TextComponent("bcdef"), maxSize = Vec2(5.5f, Float.MAX_VALUE))
        info.assert(
            lineIndex = 1,
            lines = listOf(
                TextLineInfo(BaseComponent(TextComponent("bcd")), 5.0f),
                TextLineInfo(BaseComponent(TextComponent("ef")), 5.5f),
            ),
            size = Vec2(5.5f, 22.0f),
        )
    }

    fun `max line size 2`() {
        val info = render(TextComponent("bcdefg"), maxSize = Vec2(5.5f, Float.MAX_VALUE))
        info.assert(
            lineIndex = 2,
            lines = listOf(
                TextLineInfo(BaseComponent(TextComponent("bcd")), 5.0f),
                TextLineInfo(BaseComponent(TextComponent("ef")), 5.5f),
                TextLineInfo(BaseComponent(TextComponent("g")), 3.0f),
            ),
            size = Vec2(5.5f, 33.0f),
        )
    }

    fun `single line with spacing`() {
        val info = render(TextComponent("bcd"), maxSize = Vec2(5.5f, Float.MAX_VALUE), properties = TextRenderProperties(lineSpacing = 100.0f, shadow = false))
        info.assert(
            lineIndex = 0,
            lines = listOf(
                TextLineInfo(BaseComponent(TextComponent("bcd")), 5.0f),
            ),
            size = Vec2(5.0f, 11.0f),
        )
    }

    fun `line spacing 1`() {
        val info = render(TextComponent("bcdef"), maxSize = Vec2(5.5f, Float.MAX_VALUE), properties = TextRenderProperties(lineSpacing = 1.0f, shadow = false))
        info.assert(
            lineIndex = 1,
            lines = listOf(
                TextLineInfo(BaseComponent(TextComponent("bcd")), 5.0f),
                TextLineInfo(BaseComponent(TextComponent("ef")), 5.5f),
            ),
            size = Vec2(5.5f, 23.0f),
        )
    }

    fun `line spacing 2`() {
        val info = render(TextComponent("bcdefg"), maxSize = Vec2(5.5f, Float.MAX_VALUE), properties = TextRenderProperties(lineSpacing = 1.0f, shadow = false))
        info.assert(
            lineIndex = 2,
            lines = listOf(
                TextLineInfo(BaseComponent(TextComponent("bcd")), 5.0f),
                TextLineInfo(BaseComponent(TextComponent("ef")), 5.5f),
                TextLineInfo(BaseComponent(TextComponent("g")), 3.0f),
            ),
            size = Vec2(5.5f, 35.0f),
        )
    }

    fun `line spacing 3`() {
        val info = render(TextComponent("bcdefg"), maxSize = Vec2(5.5f, Float.MAX_VALUE), properties = TextRenderProperties(lineSpacing = 20.0f, shadow = false))
        info.assert(
            lineIndex = 2,
            lines = listOf(
                TextLineInfo(BaseComponent(TextComponent("bcd")), 5.0f),
                TextLineInfo(BaseComponent(TextComponent("ef")), 5.5f),
                TextLineInfo(BaseComponent(TextComponent("g")), 3.0f),
            ),
            size = Vec2(5.5f, 73.0f),
        )
    }

    fun `empty new line`() {
        val info = render(TextComponent("\n"))
        info.assert(
            lineIndex = 1,
            lines = listOf(
                TextLineInfo(BaseComponent(), 0.0f),
            ),
            size = Vec2(0.0f, 11.0f),
        )
    }

    fun `basic new line 1`() {
        val info = render(TextComponent("b\nb"))
        info.assert(
            lineIndex = 1,
            lines = listOf(
                TextLineInfo(BaseComponent(TextComponent("b")), 0.5f),
                TextLineInfo(BaseComponent(TextComponent("b")), 0.5f),
            ),
            size = Vec2(0.5f, 22.0f),
        )
    }

    fun `basic new line 2`() {
        val info = render(TextComponent("bcd\n\nefgh"))
        info.assert(
            lineIndex = 2,
            lines = listOf(
                TextLineInfo(BaseComponent(TextComponent("bcd")), 5.0f),
                TextLineInfo(BaseComponent(), 0.0f),
                TextLineInfo(BaseComponent(TextComponent("efgh")), 14.0f),
            ),
            size = Vec2(14f, 33.0f),
        )
    }

    // TODO: shadow, cutoff, underline, strikethrough, using with consumer, formatting (just basic, that is code point renderer's job)
}
