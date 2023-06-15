package de.bixilon.minosoft.gui.rendering.gui.elements.text

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec4.Vec4
import de.bixilon.minosoft.gui.rendering.gui.GuiRenderTestUtil
import de.bixilon.minosoft.gui.rendering.gui.GuiRenderTestUtil.assetSize
import de.bixilon.minosoft.gui.rendering.gui.elements.text.background.TextBackground
import org.testng.annotations.Test

@Test(groups = ["font", "gui"])
class TextElementTest {

    fun `size empty`() {
        val element = TextElement(GuiRenderTestUtil.create(), "")
        element.assetSize(Vec2(0, 0))
    }

    fun `size of single char`() {
        val element = TextElement(GuiRenderTestUtil.create(), "b", background = null)
        element.assetSize(Vec2(0.5f, 11.0f))
    }

    fun `size of multiple chars`() {
        val element = TextElement(GuiRenderTestUtil.create(), "bc", background = null)
        element.assetSize(Vec2(2.5f, 11.0f))
    }

    fun `size with new line`() {
        val element = TextElement(GuiRenderTestUtil.create(), "bc\nbc", background = null)
        element.assetSize(Vec2(2.5f, 22.0f))
    }

    fun `size with background`() {
        val element = TextElement(GuiRenderTestUtil.create(), "bc")
        element.assetSize(Vec2(4.5f, 13.0f))
    }

    fun `size with background and newlines`() {
        val element = TextElement(GuiRenderTestUtil.create(), "bc\nbc")
        element.assetSize(Vec2(4.5f, 24.0f))
    }

    fun `size if text changed`() {
        val element = TextElement(GuiRenderTestUtil.create(), "bc\nbc")
        element.text = "bcd\nbcd\nbcd"
        element.assetSize(Vec2(6.0f, 35.0f))
    }

    fun `size if background cleared`() {
        val element = TextElement(GuiRenderTestUtil.create(), "bcd\nbcd\nbcd")
        element.background = null
        element.assetSize(Vec2(4.0f, 33.0f))
    }

    fun `size if background set`() {
        val element = TextElement(GuiRenderTestUtil.create(), "bcd\nbcd\nbcd")
        element.background = TextBackground(size = Vec4(2.0f))
        element.assetSize(Vec2(8.0f, 37.0f))
    }


    // TODO: test on mouse (click/hover events), rendering, size limiting
}
