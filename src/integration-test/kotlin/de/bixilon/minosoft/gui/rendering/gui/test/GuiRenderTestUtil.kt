/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.test

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.font.manager.FontManager
import de.bixilon.minosoft.gui.rendering.font.types.dummy.DummyFontType
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.textures.CodeTexturePart
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.system.dummy.DummyRenderSystem
import de.bixilon.minosoft.gui.rendering.system.dummy.texture.DummyTexture
import de.bixilon.minosoft.gui.rendering.system.dummy.texture.DummyTextureManager
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.assertEquals

object GuiRenderTestUtil {

    private fun createContext(): RenderContext {
        val context = RenderContext::class.java.allocate()
        context.font = FontManager(DummyFontType)
        context::system.forceSet(DummyRenderSystem(context))
        context::textures.forceSet(DummyTextureManager(context))

        context.textures::whiteTexture.forceSet(CodeTexturePart(DummyTexture(), size = Vec2i(16, 16)))

        return context
    }

    fun create(size: Vec2f = Vec2f(1920.0f, 1080.0f)): GUIRenderer {
        val renderer = GUIRenderer::class.java.allocate()
        renderer::scaledSize.forceSet(DataObserver(size))
        renderer::halfSize.forceSet((size / 2.0f)._0)

        renderer::context.forceSet(createContext())

        return renderer
    }


    fun Element.assetSize(size: Vec2f) {
        assertEquals(this.size, size)
    }

    fun Element.assetPrefSize(size: Vec2f) {
        assertEquals(this.prefSize, size)
    }
}
