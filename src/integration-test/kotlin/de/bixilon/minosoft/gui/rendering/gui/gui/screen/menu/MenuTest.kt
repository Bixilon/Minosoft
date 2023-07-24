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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.test.GuiRenderTestUtil
import de.bixilon.minosoft.gui.rendering.gui.test.GuiTestConsumer
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["gui"])
class MenuTest {

    fun initialize() {
        menu(GuiRenderTestUtil.create())
    }

    fun `assert correct screen size`() {
        val menu = menu(GuiRenderTestUtil.create())
        assertEquals(menu.size, Vec2(1920, 1080))
    }

    fun `basic rendering`() {
        val menu = menu(GuiRenderTestUtil.create())

        val consumer = GuiTestConsumer()
        menu.render(Vec2(0, 0), consumer, null)

        consumer.assert(GuiTestConsumer.RendererdQuad(Vec2(0, 0), Vec2(1920, 1080)))
        // TODO
    }

    // TODO: test correct layout, mouse, keyboard action, tabbing, element width (e.g. buttons), changing of elements,


    fun menu(guiRenderer: GUIRenderer) = object : Menu(guiRenderer) {

        init {
            this += TextElement(guiRenderer, "bcd", background = null, properties = TextRenderProperties(shadow = false))
        }
    }
}
