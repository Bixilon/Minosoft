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

package de.bixilon.minosoft.gui.rendering.gui.elements

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.gui.rendering.gui.GuiDelegate
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.ChildedElement
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.manager.collection.SetChildrenManager
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.test.GuiRenderTestUtil
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["gui"])
class ElementUpdateTest {


    fun initial() {
        val element = child()
        assertTrue(element.update)
        assertEquals(element.updateCalls, 0)
    }

    fun update() {
        val element = child()
        element.update()
        assertFalse(element.update)
        assertEquals(element.updateCalls, 1)
    }

    fun `set property to same value`() {
        val element = child()
        element.update()
        element.property = "abc"
        assertFalse(element.update)
    }

    fun `set property to different value`() {
        val element = child()
        element.update()
        element.property = "bcd"
        assertTrue(element.update)
    }

    fun `modify child and check parent`() {
        val child = child()
        val parent = parent()
        child.parent = parent

        assertTrue(child.update)
        parent.update()
        assertFalse(child.update)

        child.property = "bcd"
        assertTrue(child.update)
        assertTrue(parent.update)
    }

    private fun child() = object : Element(GuiRenderTestUtil.create()) {
        var property by GuiDelegate("abc")
        var updateCalls = 0
        var renderCalls = 0

        override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
            renderCalls++
        }

        override fun update() {
            super.update()
            updateCalls++
        }
    }

    private fun parent() = object : Element(GuiRenderTestUtil.create()), ChildedElement {
        override val children = SetChildrenManager(this)

        var property by GuiDelegate("abc")
        var updateCalls = 0
        var renderCalls = 0

        override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
            renderCalls++
            for (child in children) {
                child.render(offset, consumer, options)
            }
        }

        override fun update() {
            super<Element>.update()
            updateCalls++
        }
    }
}
