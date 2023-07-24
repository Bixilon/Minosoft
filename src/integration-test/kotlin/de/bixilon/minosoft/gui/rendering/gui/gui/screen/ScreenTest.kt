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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.properties.GUIScreen
import de.bixilon.minosoft.gui.rendering.gui.test.GuiRenderTestUtil
import de.bixilon.minosoft.gui.rendering.gui.test.GuiTestConsumer
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["gui"])
class ScreenTest {

    fun initialize() {
        screen(GuiRenderTestUtil.create())
    }

    fun `verify clean`() {
        val screen = screen(GuiRenderTestUtil.create())
        assertFalse(screen.update)
    }

    fun `verify initial size`() {
        val screen = screen(GuiRenderTestUtil.create())
        assertEquals(screen.size, Vec2(1920, 1080))
    }

    fun `modify size`() {
        val renderer = GuiRenderTestUtil.create()
        val screen = screen(renderer)
        renderer.screen = GUIScreen(Vec2i(123, 345), Vec2(123, 345))
        screen.invalidate()
        assertTrue(screen.update)
        screen.tryUpdate()
        assertFalse(screen.update)
        assertEquals(screen.size, Vec2(123, 345))
    }

    fun `rendering initial size`() {
        val renderer = GuiRenderTestUtil.create()
        val screen = screen(renderer)
        val consumer = GuiTestConsumer()
        screen.render(Vec2(), consumer, null)

        consumer.assert(GuiTestConsumer.RendererdQuad(Vec2(0, 0), Vec2(1920, 1080)))
    }

    fun `rendering modified size`() {
        val renderer = GuiRenderTestUtil.create()
        val screen = screen(renderer)

        renderer.screen = GUIScreen(Vec2i(123, 345), Vec2(123, 345))
        screen.invalidate()
        screen.tryUpdate()

        val consumer = GuiTestConsumer()
        screen.render(Vec2(), consumer, null)

        consumer.assert(GuiTestConsumer.RendererdQuad(Vec2(0, 0), Vec2(123, 345)))
    }


    private fun screen(guiRenderer: GUIRenderer) = object : Screen(guiRenderer) {
    }
}
