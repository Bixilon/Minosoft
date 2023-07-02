package de.bixilon.minosoft.gui.rendering.gui.elements.input.button

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.gui.rendering.gui.test.GuiRenderTestUtil
import de.bixilon.minosoft.gui.rendering.gui.test.GuiTestConsumer
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["gui"])
class ButtonElementTest {

    fun `basic verification`() {
        var invoked = 0
        val button = ButtonElement(GuiRenderTestUtil.create(), "bc") { invoked++ }
        assertEquals(button.size, Vec2(10.5, 19))
    }

    fun `fixed size verification`() {
        var invoked = 0
        val button = ButtonElement(GuiRenderTestUtil.create(), "bc") { invoked++ }
        button.size = Vec2(123, 123)
        assertEquals(button.size, Vec2(123, 123))
    }

    fun `basic rendering`() {
        var invoked = 0
        val button = ButtonElement(GuiRenderTestUtil.create(), "bc") { invoked++ }
        val consumer = GuiTestConsumer()

        button.forceRender(Vec2(3, 3), consumer, null)

        consumer.assert(
            GuiTestConsumer.RendererdQuad(Vec2(3, 3), Vec2(13.5, 22.5)),
        )
        consumer.assert(
            GuiTestConsumer.RendererdCodePoint(Vec2(7, 7)),
            GuiTestConsumer.RendererdCodePoint(Vec2(8.5, 7)),
        )
    }
}
