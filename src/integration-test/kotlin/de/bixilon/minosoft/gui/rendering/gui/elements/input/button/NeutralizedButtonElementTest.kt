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
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.test.GuiRenderTestUtil
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["gui"])
class NeutralizedButtonElementTest {

    fun `single invocation`() {
        var invoked = 0
        val button = NeutralizedButtonElement(GuiRenderTestUtil.create(), "bc") { invoked++ }
        button.onMouseAction(Vec2(0, 0), MouseButtons.LEFT, MouseActions.PRESS, 1)
        assertEquals(invoked, 0)
    }

    fun `two times single invocation`() {
        var invoked = 0
        val button = NeutralizedButtonElement(GuiRenderTestUtil.create(), "bc") { invoked++ }
        button.onMouseAction(Vec2(0, 0), MouseButtons.LEFT, MouseActions.PRESS, 1)
        button.onMouseAction(Vec2(0, 0), MouseButtons.LEFT, MouseActions.PRESS, 1)
        assertEquals(invoked, 1)
    }

    fun `triple invocation`() {
        var invoked = 0
        val button = NeutralizedButtonElement(GuiRenderTestUtil.create(), "bc") { invoked++ }
        button.onMouseAction(Vec2(0, 0), MouseButtons.LEFT, MouseActions.PRESS, 1)
        button.onMouseAction(Vec2(0, 0), MouseButtons.LEFT, MouseActions.PRESS, 1)
        button.onMouseAction(Vec2(0, 0), MouseButtons.LEFT, MouseActions.PRESS, 1)
        assertEquals(invoked, 1)
    }

    fun `double invocation`() {
        var invoked = 0
        val button = NeutralizedButtonElement(GuiRenderTestUtil.create(), "bc") { invoked++ }
        button.onMouseAction(Vec2(0, 0), MouseButtons.LEFT, MouseActions.PRESS, 2)
        assertEquals(invoked, 1)
    }

    fun `not neutralized again`() {
        var invoked = 0
        val button = NeutralizedButtonElement(GuiRenderTestUtil.create(), "bc", ticks = 3) { invoked++ }
        button.onMouseAction(Vec2(0, 0), MouseButtons.LEFT, MouseActions.PRESS, 1)
        button.tick(3)
        button.onMouseAction(Vec2(0, 0), MouseButtons.LEFT, MouseActions.PRESS, 1)
        assertEquals(invoked, 1)
    }

    fun `neutralized again`() {
        var invoked = 0
        val button = NeutralizedButtonElement(GuiRenderTestUtil.create(), "bc", ticks = 3) { invoked++ }
        button.onMouseAction(Vec2(0, 0), MouseButtons.LEFT, MouseActions.PRESS, 1)
        button.tick(4)
        button.onMouseAction(Vec2(0, 0), MouseButtons.LEFT, MouseActions.PRESS, 1)
        assertEquals(invoked, 0)
    }
}
