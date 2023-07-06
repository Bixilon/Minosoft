/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

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
        assertEquals(button.size, Vec2(11.5, 19))
    }

    fun `fixed size verification`() {
        var invoked = 0
        val button = ButtonElement(GuiRenderTestUtil.create(), "bc") { invoked++ }
        button.preferredSize = Vec2(123, 123)
        button.tryUpdate()
        assertEquals(button.size, Vec2(123, 123))
    }

    fun `basic rendering`() {
        var invoked = 0
        val button = ButtonElement(GuiRenderTestUtil.create(), "bc") { invoked++ }
        button.textProperties = button.textProperties.copy(shadow = false)
        button.tryUpdate()
        val consumer = GuiTestConsumer()

        button.forceRender(Vec2(3, 3), consumer, null)

        consumer.assert(
            GuiTestConsumer.RendererdQuad(Vec2(3, 3), Vec2(13.5, 22)),
        )
        consumer.assert(
            GuiTestConsumer.RendererdCodePoint(Vec2(7, 8)),
            GuiTestConsumer.RendererdCodePoint(Vec2(8.5, 8)),
        )
    }
}
