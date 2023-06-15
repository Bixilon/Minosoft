package de.bixilon.minosoft.gui.rendering.gui.elements.text

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec4.Vec4
import de.bixilon.minosoft.gui.rendering.font.renderer.component.DummyComponentConsumer
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.elements.text.background.TextBackground
import de.bixilon.minosoft.gui.rendering.gui.test.GuiRenderTestUtil
import de.bixilon.minosoft.gui.rendering.gui.test.GuiRenderTestUtil.assetPrefSize
import de.bixilon.minosoft.gui.rendering.gui.test.GuiRenderTestUtil.assetSize
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4Util.marginOf
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["font", "gui"])
class TextElementTest {

    fun `size empty`() {
        val element = TextElement(GuiRenderTestUtil.create(), "")
        element.assetSize(Vec2(0, 0))
    }

    fun `size of single char`() {
        val element = TextElement(GuiRenderTestUtil.create(), "b", background = null, properties = TextRenderProperties(shadow = false))
        element.assetSize(Vec2(0.5f, 11.0f))
    }

    fun `size of multiple chars`() {
        val element = TextElement(GuiRenderTestUtil.create(), "bc", background = null, properties = TextRenderProperties(shadow = false))
        element.assetSize(Vec2(2.5f, 11.0f))
    }

    fun `size with new line`() {
        val element = TextElement(GuiRenderTestUtil.create(), "bc\nbc", background = null, properties = TextRenderProperties(shadow = false))
        element.assetSize(Vec2(2.5f, 22.0f))
    }

    fun `size with background`() {
        val element = TextElement(GuiRenderTestUtil.create(), "bc", background = TextBackground(size = Vec4(1)), properties = TextRenderProperties(shadow = false))
        element.assetSize(Vec2(4.5f, 13.0f))
    }

    fun `size with background and newline`() {
        val element = TextElement(GuiRenderTestUtil.create(), "bc\nbc", background = TextBackground(size = Vec4(1)), properties = TextRenderProperties(shadow = false))
        element.assetSize(Vec2(4.5f, 24.0f))
    }

    fun `size with background and newlines`() {
        val element = TextElement(GuiRenderTestUtil.create(), "bcd\nbcd\nbcd", background = TextBackground(size = Vec4(1)), properties = TextRenderProperties(shadow = false))
        element.assetSize(Vec2(7.0f, 35.0f))
    }

    fun `size if text changed`() {
        val element = TextElement(GuiRenderTestUtil.create(), "bc\nbc", background = TextBackground(size = Vec4(1)), properties = TextRenderProperties(shadow = false))
        element.text = "bcd\nbcd\nbcd"
        element.assetSize(Vec2(7.0f, 35.0f))
    }

    fun `size if background cleared`() {
        val element = TextElement(GuiRenderTestUtil.create(), "bcd\nbcd\nbcd", background = TextBackground(size = Vec4(1)), properties = TextRenderProperties(shadow = false))
        element.background = null
        element.assetSize(Vec2(5.0f, 33.0f))
    }

    fun `size if background set`() {
        val element = TextElement(GuiRenderTestUtil.create(), "bcd\nbcd\nbcd", properties = TextRenderProperties(shadow = false))
        element.background = TextBackground(size = Vec4(2.0f))
        element.assetSize(Vec2(9.0f, 37.0f))
    }

    fun `limited size but not actually limited`() {
        val element = TextElement(GuiRenderTestUtil.create(), "bcd\nbcd\nbcd", background = null, properties = TextRenderProperties(shadow = false))
        element.prefMaxSize = Vec2(10.0f, 11.0f)
        element.assetSize(Vec2(5.0f, 11.0f))
    }

    fun `limited size`() {
        val element = TextElement(GuiRenderTestUtil.create(), "bcd\nbcd\nbcd", background = null, properties = TextRenderProperties(shadow = false))
        element.prefMaxSize = Vec2(3.0f, 11.0f)
        element.assetSize(Vec2(2.5f, 11.0f))
        assertEquals(element.info.size, Vec2(2.5f, 11.0f))
    }

    fun `limited size with background`() {
        val element = TextElement(GuiRenderTestUtil.create(), "bcd\nbcd\nbcd", background = TextBackground(size = Vec4(1.0f)), properties = TextRenderProperties(shadow = false))
        element.prefMaxSize = Vec2(5.0f, 11.0f)
        element.assetSize(Vec2(0.0f, 0.0f))
        assertEquals(element.info.size, Vec2(0.0f, 0.0f))
    }

    fun `limited size with background 2`() {
        val element = TextElement(GuiRenderTestUtil.create(), "bcd\nbcd\nbcd", background = TextBackground(size = Vec4(1.0f)), properties = TextRenderProperties(shadow = false))
        element.prefMaxSize = Vec2(5.0f, 13.0f)
        element.assetSize(Vec2(4.5f, 13.0f))
        assertEquals(element.info.size, Vec2(2.5f, 11.0f))
    }

    fun `single line background and text`() {
        val consumer = DummyComponentConsumer()
        val element = TextElement(GuiRenderTestUtil.create(), "bcd", background = TextBackground(size = Vec4(1.0f)), properties = TextRenderProperties(font = consumer.Font(), shadow = false))

        element.forceRender(Vec2(), consumer, null)


        consumer.assert(
            DummyComponentConsumer.RendererdCodePoint(Vec2(1, 1)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(2.5, 1)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(4.5, 1)),
        )

        consumer.assert(
            DummyComponentConsumer.RendererdQuad(Vec2(0, 0), Vec2(7, 13)),
        )
    }

    fun `single line with different background sizes and text`() {
        val consumer = DummyComponentConsumer()
        val element = TextElement(GuiRenderTestUtil.create(), "bcd", background = TextBackground(size = marginOf(1.0f, 2.0f, 3.0f, 4.0f)), properties = TextRenderProperties(font = consumer.Font(), shadow = false))

        element.forceRender(Vec2(), consumer, null)
        element.assetSize(Vec2(11, 15))
        element.assetPrefSize(Vec2(11, 15))


        consumer.assert(
            DummyComponentConsumer.RendererdCodePoint(Vec2(4.0, 1)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(5.5, 1)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(7.5, 1)),
        )

        consumer.assert(
            DummyComponentConsumer.RendererdQuad(Vec2(0, 0), Vec2(11, 15)),
        )
    }

    fun `multiple lines background and text`() {
        val consumer = DummyComponentConsumer()
        val element = TextElement(GuiRenderTestUtil.create(), "bcd\nbcd\nbcd", background = TextBackground(size = Vec4(1.0f)), properties = TextRenderProperties(font = consumer.Font(), shadow = false))

        element.forceRender(Vec2(), consumer, null)


        consumer.assert(
            DummyComponentConsumer.RendererdCodePoint(Vec2(1, 1)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(2.5, 1)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(4.5, 1)),

            DummyComponentConsumer.RendererdCodePoint(Vec2(1, 12)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(2.5, 12)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(4.5, 12)),

            DummyComponentConsumer.RendererdCodePoint(Vec2(1, 23)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(2.5, 23)),
            DummyComponentConsumer.RendererdCodePoint(Vec2(4.5, 23)),
        )

        consumer.assert(
            DummyComponentConsumer.RendererdQuad(Vec2(0, 0), Vec2(7, 13)),
            DummyComponentConsumer.RendererdQuad(Vec2(0, 11), Vec2(7, 24)),
            DummyComponentConsumer.RendererdQuad(Vec2(0, 22), Vec2(7, 35)),
        )
    }


    // TODO: test on mouse (click/hover events)
}
