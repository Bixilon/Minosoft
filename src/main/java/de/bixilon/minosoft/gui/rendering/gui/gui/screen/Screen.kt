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
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.ChildedElement
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.manager.ChildrenManager
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.manager.collection.SetChildrenManager
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.ScreenPositionedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY

abstract class Screen(
    guiRenderer: GUIRenderer,
) : Element(guiRenderer), ScreenPositionedElement, ChildedElement {
    override val children: ChildrenManager = SetChildrenManager(this)
    protected val background = AtlasImageElement(guiRenderer, context.textures.whiteTexture, size = guiRenderer.screen.scaled, tint = RGBColor(0.0f, 0.0f, 0.0f, 0.8f))
    override val screenOffset: Vec2 = Vec2(0, 0)


    init {
        construct()
    }

    override fun construct() {
        background.parent = this
        super.construct()
    }

    override fun update() {
        background.preferredSize = guiRenderer.screen.scaled
        size = guiRenderer.screen.scaled
        super.update()
    }

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        background.render(Vec2.EMPTY, consumer, options)
    }
}
