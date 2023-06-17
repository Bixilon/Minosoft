package de.bixilon.minosoft.gui.rendering.font.renderer.component

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.font.manager.FontManager
import de.bixilon.minosoft.gui.rendering.font.renderer.element.LineRenderInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextOffset
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.font.types.dummy.DummyFontType
import de.bixilon.minosoft.gui.rendering.font.types.font.EmptyFont
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.mesh.DummyGUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.MAX
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["font"])
class ChatComponentRendererTest {
    private val fontManager = FontManager(DummyFontType)

    private fun render(text: ChatComponent, fontManager: FontManager = this.fontManager, properties: TextRenderProperties = TextRenderProperties(shadow = false), maxSize: Vec2 = Vec2.MAX, consumer: GUIVertexConsumer? = null): TextRenderInfo {
        val info = TextRenderInfo(maxSize)
        ChatComponentRenderer.render(TextOffset(Vec2(10, 10)), fontManager, properties, info, null, null, text)
        if (consumer != null) {
            info.rewind()
            ChatComponentRenderer.render(TextOffset(Vec2(10, 10)), fontManager, properties, info, consumer, null, text)
        }

        return info
    }

    private fun TextRenderInfo.assert(
        lineIndex: Int? = null,
        lines: List<LineRenderInfo>? = null,
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
            lines = listOf(LineRenderInfo(BaseComponent(TextComponent("b")), 0.5f)),
            size = Vec2(0.5f, 11.0f),
        )
    }

    fun `2 chars`() {
        val info = render(TextComponent("bc"))
        info.assert(
            lineIndex = 0,
            lines = listOf(LineRenderInfo(BaseComponent(TextComponent("bc")), 2.5f)),
            size = Vec2(2.5f, 11.0f), // b + spacing + c
        )
    }

    fun `3 chars`() {
        val info = render(TextComponent("bcd"))
        info.assert(
            lineIndex = 0,
            lines = listOf(LineRenderInfo(BaseComponent(TextComponent("bcd")), 5.0f)),
            size = Vec2(5.0f, 11.0f),
        )
    }

    fun `max line size 1`() {
        val info = render(TextComponent("bcdef"), maxSize = Vec2(5.5f, Float.MAX_VALUE))
        info.assert(
            lineIndex = 1,
            lines = listOf(
                LineRenderInfo(BaseComponent(TextComponent("bcd")), 5.0f),
                LineRenderInfo(BaseComponent(TextComponent("ef")), 5.5f),
            ),
            size = Vec2(5.5f, 22.0f),
        )
    }

    fun `max line size 2`() {
        val info = render(TextComponent("bcdefg"), maxSize = Vec2(5.5f, Float.MAX_VALUE))
        info.assert(
            lineIndex = 2,
            lines = listOf(
                LineRenderInfo(BaseComponent(TextComponent("bcd")), 5.0f),
                LineRenderInfo(BaseComponent(TextComponent("ef")), 5.5f),
                LineRenderInfo(BaseComponent(TextComponent("g")), 3.0f),
            ),
            size = Vec2(5.5f, 33.0f),
        )
    }

    fun `single line with spacing`() {
        val info = render(TextComponent("bcd"), maxSize = Vec2(5.5f, Float.MAX_VALUE), properties = TextRenderProperties(lineSpacing = 100.0f, shadow = false))
        info.assert(
            lineIndex = 0,
            lines = listOf(
                LineRenderInfo(BaseComponent(TextComponent("bcd")), 5.0f),
            ),
            size = Vec2(5.0f, 11.0f),
        )
    }

    fun `line spacing 1`() {
        val info = render(TextComponent("bcdef"), maxSize = Vec2(5.5f, Float.MAX_VALUE), properties = TextRenderProperties(lineSpacing = 1.0f, shadow = false))
        info.assert(
            lineIndex = 1,
            lines = listOf(
                LineRenderInfo(BaseComponent(TextComponent("bcd")), 5.0f),
                LineRenderInfo(BaseComponent(TextComponent("ef")), 5.5f),
            ),
            size = Vec2(5.5f, 23.0f),
        )
    }

    fun `line spacing 2`() {
        val info = render(TextComponent("bcdefg"), maxSize = Vec2(5.5f, Float.MAX_VALUE), properties = TextRenderProperties(lineSpacing = 1.0f, shadow = false))
        info.assert(
            lineIndex = 2,
            lines = listOf(
                LineRenderInfo(BaseComponent(TextComponent("bcd")), 5.0f),
                LineRenderInfo(BaseComponent(TextComponent("ef")), 5.5f),
                LineRenderInfo(BaseComponent(TextComponent("g")), 3.0f),
            ),
            size = Vec2(5.5f, 35.0f),
        )
    }

    fun `line spacing 3`() {
        val info = render(TextComponent("bcdefg"), maxSize = Vec2(5.5f, Float.MAX_VALUE), properties = TextRenderProperties(lineSpacing = 20.0f, shadow = false))
        info.assert(
            lineIndex = 2,
            lines = listOf(
                LineRenderInfo(BaseComponent(TextComponent("bcd")), 5.0f),
                LineRenderInfo(BaseComponent(TextComponent("ef")), 5.5f),
                LineRenderInfo(BaseComponent(TextComponent("g")), 3.0f),
            ),
            size = Vec2(5.5f, 73.0f),
        )
    }

    fun `empty new line`() {
        val info = render(TextComponent("\n"))
        info.assert(
            lineIndex = 1,
            lines = listOf(
                LineRenderInfo(BaseComponent(), 0.0f),
            ),
            size = Vec2(0.0f, 11.0f),
        )
    }

    fun `basic new line 1`() {
        val info = render(TextComponent("b\nb"))
        info.assert(
            lineIndex = 1,
            lines = listOf(
                LineRenderInfo(BaseComponent(TextComponent("b")), 0.5f),
                LineRenderInfo(BaseComponent(TextComponent("b")), 0.5f),
            ),
            size = Vec2(0.5f, 22.0f),
        )
    }

    fun `basic new line 2`() {
        val info = render(TextComponent("bcd\n\nefgh"))
        info.assert(
            lineIndex = 2,
            lines = listOf(
                LineRenderInfo(BaseComponent(TextComponent("bcd")), 5.0f),
                LineRenderInfo(BaseComponent(), 0.0f),
                LineRenderInfo(BaseComponent(TextComponent("efgh")), 14.0f),
            ),
            size = Vec2(14f, 33.0f),
        )
    }

    fun `no space x`() {
        val info = render(TextComponent("bcd\n\nefgh"), maxSize = Vec2(0.0f, Float.MAX_VALUE))
        info.assert(
            lineIndex = 0,
            lines = listOf(),
            size = Vec2(0.0f, 0.0f),
            cutOff = true,
        )
    }

    fun `no space y`() {
        val info = render(TextComponent("bcd\n\nefgh"), maxSize = Vec2(Float.MAX_VALUE, 0.0f))
        info.assert(
            lineIndex = 0,
            lines = listOf(),
            size = Vec2(0.0f, 0.0f),
            cutOff = true,
        )
    }

    fun `no space y with consumer`() {
        val info = render(TextComponent("bcd\n\nefgh"), maxSize = Vec2(Float.MAX_VALUE, 10.0f), consumer = DummyGUIVertexConsumer())
        info.assert(
            lineIndex = 0,
            lines = listOf(),
            size = Vec2(0.0f, 0.0f),
            cutOff = true,
        )
    }

    fun `no space`() {
        val info = render(TextComponent("bcd\n\nefgh"), maxSize = Vec2(0.0f, 0.0f))
        info.assert(
            lineIndex = 0,
            lines = listOf(),
            size = Vec2(0.0f, 0.0f),
            cutOff = true,
        )
    }

    fun `size limit one line`() {
        val info = render(TextComponent("bcd\nefgh"), maxSize = Vec2(Float.MAX_VALUE, 11.0f))
        info.assert(
            lineIndex = 0,
            lines = listOf(
                LineRenderInfo(BaseComponent(TextComponent("bcd")), 5.0f),
            ),
            size = Vec2(5.0f, 11.0f),
            cutOff = true,
        )
    }

    fun `size limit one line with overflow`() {
        val info = render(TextComponent("bcd\nefgh"), maxSize = Vec2(5.0f, 11.0f))
        info.assert(
            lineIndex = 0,
            lines = listOf(
                LineRenderInfo(BaseComponent(TextComponent("bcd")), 5.0f),
            ),
            size = Vec2(5.0f, 11.0f),
            cutOff = true,
        )
    }

    fun `size limit two line`() {
        val info = render(TextComponent("bcd\nefgh\nabc"), maxSize = Vec2(Float.MAX_VALUE, 22.0f))
        info.assert(
            lineIndex = 1,
            lines = listOf(
                LineRenderInfo(BaseComponent(TextComponent("bcd")), 5.0f),
                LineRenderInfo(BaseComponent(TextComponent("efgh")), 14.0f),
            ),
            size = Vec2(14.0f, 22.0f),
            cutOff = true,
        )
    }

    fun `newline, no space`() {
        val info = render(TextComponent("\n"), maxSize = Vec2(0.0f, 0.0f))
        info.assert(
            lineIndex = 0,
            lines = listOf(),
            size = Vec2(0.0f, 0.0f),
            cutOff = true,
        )
    }

    fun `no font`() {
        val info = TextRenderInfo(Vec2(Float.MAX_VALUE))
        ChatComponentRenderer.render(TextOffset(Vec2(10, 10)), FontManager(EmptyFont), TextRenderProperties(), info, null, null, TextComponent("abc\ndef"))

        info.assert(
            lineIndex = 1,
            lines = listOf(LineRenderInfo()),
            size = Vec2(0.0f, 11.0f),
        )
    }

    fun `no font, no size`() {
        val info = TextRenderInfo(Vec2(0.0f, 0.0f))
        ChatComponentRenderer.render(TextOffset(Vec2(10, 10)), FontManager(EmptyFont), TextRenderProperties(), info, null, null, TextComponent("abc\ndef"))

        info.assert(
            lineIndex = 0,
            lines = listOf(),
            size = Vec2(0.0f, 0.0f),
            cutOff = true,
        )
    }

    fun `single char rendering`() {
        val consumer = DummyComponentConsumer()
        render(TextComponent("b"), fontManager = FontManager(consumer.Font()), consumer = consumer)

        consumer.assert(
            DummyComponentConsumer.RendererdCodePoint(Vec2(10, 10)),
        )
    }

    fun `multiple char rendering`() {
        val consumer = DummyComponentConsumer()
        render(TextComponent("bc"), fontManager = FontManager(consumer.Font()), consumer = consumer)

        consumer.assert(
            DummyComponentConsumer.RendererdCodePoint(Vec2(10, 10)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(11.5, 10)),
        )
    }

    fun `newline rendering`() {
        val consumer = DummyComponentConsumer()
        render(TextComponent("bc\nde"), fontManager = FontManager(consumer.Font()), consumer = consumer)

        consumer.assert(
            DummyComponentConsumer.RendererdCodePoint(Vec2(10, 10)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(11.5, 10)),

            DummyComponentConsumer.RendererdCodePoint(Vec2(10.0, 21)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(12.5, 21)),
        )
    }

    fun `left alignment`() { // default
        val consumer = DummyComponentConsumer()
        render(TextComponent("bc\nde\nbc"), fontManager = FontManager(consumer.Font()), consumer = consumer, properties = TextRenderProperties(alignment = HorizontalAlignments.LEFT, shadow = false))

        consumer.assert(
            DummyComponentConsumer.RendererdCodePoint(Vec2(10, 10)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(11.5, 10)),

            DummyComponentConsumer.RendererdCodePoint(Vec2(10.0, 21)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(12.5, 21)),

            DummyComponentConsumer.RendererdCodePoint(Vec2(10, 32)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(11.5, 32)),
        )
    }

    fun `center alignment`() {
        val consumer = DummyComponentConsumer()
        render(TextComponent("bc\nde\nbc"), fontManager = FontManager(consumer.Font()), consumer = consumer, properties = TextRenderProperties(alignment = HorizontalAlignments.CENTER, shadow = false))

        consumer.assert(
            DummyComponentConsumer.RendererdCodePoint(Vec2(11, 10)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(12.5, 10)),

            DummyComponentConsumer.RendererdCodePoint(Vec2(10.0, 21)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(12.5, 21)),

            DummyComponentConsumer.RendererdCodePoint(Vec2(11, 32)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(12.5, 32)),
        )
    }

    fun `right alignment`() {
        val consumer = DummyComponentConsumer()
        render(TextComponent("bc\nde\nbc"), fontManager = FontManager(consumer.Font()), consumer = consumer, properties = TextRenderProperties(alignment = HorizontalAlignments.RIGHT, shadow = false))

        consumer.assert(
            DummyComponentConsumer.RendererdCodePoint(Vec2(12, 10)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(13.5, 10)),

            DummyComponentConsumer.RendererdCodePoint(Vec2(10.0, 21)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(12.5, 21)),

            DummyComponentConsumer.RendererdCodePoint(Vec2(12, 32)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(13.5, 32)),
        )
    }


    fun `single strikethrough rendering`() {
        val consumer = DummyComponentConsumer()
        render(TextComponent("bcd").strikethrough(), fontManager = FontManager(consumer.Font()), consumer = consumer)

        consumer.assert(
            DummyComponentConsumer.RendererdQuad(Vec2(10.0f, 14.0f), Vec2(15.0f, 15.0f)),
        )
    }

    fun `multiline strikethrough rendering`() {
        val consumer = DummyComponentConsumer()
        render(TextComponent("bcd\ncde").strikethrough(), fontManager = FontManager(consumer.Font()), consumer = consumer)

        consumer.assert(
            DummyComponentConsumer.RendererdQuad(Vec2(10.0f, 14.0f), Vec2(15.0f, 15.0f)),
            DummyComponentConsumer.RendererdQuad(Vec2(10.0f, 25.0f), Vec2(16.5f, 26.0f)),
        )
    }

    fun `single underline rendering`() {
        val consumer = DummyComponentConsumer()
        render(TextComponent("bcd").underline(), fontManager = FontManager(consumer.Font()), consumer = consumer)

        consumer.assert(
            DummyComponentConsumer.RendererdQuad(Vec2(10.0f, 19.0f), Vec2(15.0f, 20.0f)),
        )
    }

    fun `multiline underline rendering`() {
        val consumer = DummyComponentConsumer()
        render(TextComponent("bcd\ncde").underline(), fontManager = FontManager(consumer.Font()), consumer = consumer)

        consumer.assert(
            DummyComponentConsumer.RendererdQuad(Vec2(10.0f, 19.0f), Vec2(15.0f, 20.0f)),
            DummyComponentConsumer.RendererdQuad(Vec2(10.0f, 30.0f), Vec2(16.5f, 31.0f)),
        )
    }


    fun `mixed text strikethrough rendering`() {
        val consumer = DummyComponentConsumer()
        render(BaseComponent(TextComponent("bcd").strikethrough(), TextComponent("bcd")), fontManager = FontManager(consumer.Font()), consumer = consumer)

        consumer.assert(
            DummyComponentConsumer.RendererdQuad(Vec2(10.0f, 14.0f), Vec2(15.0f, 15.0f)),
        )
    }

    // TODO: shadow, formatting (just basic, that is code point renderer's job)
}
