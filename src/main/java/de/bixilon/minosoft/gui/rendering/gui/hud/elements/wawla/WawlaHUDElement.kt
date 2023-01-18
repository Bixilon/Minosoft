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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.wawla

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asRGBAColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.gui.rendering.camera.target.targets.BlockTarget
import de.bixilon.minosoft.gui.rendering.camera.target.targets.EntityTarget
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.wawla.block.BlockWawlaElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.wawla.entity.EntityWawlaElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.renderer.drawable.AsyncDrawable
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class WawlaHUDElement(guiRenderer: GUIRenderer) : Element(guiRenderer), LayoutedElement, AsyncDrawable {
    private var element: WawlaElement? = null

    val profile = guiRenderer.connection.profiles.gui.hud.wawla

    override val layoutOffset: Vec2i
        get() = Vec2i((guiRenderer.scaledSize.x - ((element?.size?.x ?: 0) + BACKGROUND_SIZE)) / 2, BACKGROUND_SIZE)
    override val skipDraw: Boolean
        get() = !profile.enabled


    override fun drawAsync() {
        val target = context.camera.targetHandler.target

        if (target == null) {
            this.element = null
            forceSilentApply()
            return
        }
        val distance = target.distance


        val element: WawlaElement? = when {
            target is BlockTarget && profile.block.enabled && (!profile.limitReach || distance <= context.connection.player.reachDistance) -> BlockWawlaElement(this, target)
            target is EntityTarget && profile.entity.enabled && (!profile.limitReach || distance <= 3.0) -> EntityWawlaElement(this, target) // TODO: use constant for distance
            else -> null
        }
        this.element = element
        forceSilentApply()
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val element = this.element ?: return
        val size = element.size
        ColorElement(guiRenderer, size + BACKGROUND_SIZE * 2, 0x3c05aa.asRGBColor()).render(offset, consumer, options)
        ColorElement(guiRenderer, size + (BACKGROUND_SIZE - 2) * 2, 0x160611A0.asRGBAColor()).render(offset + (BACKGROUND_SIZE - (BACKGROUND_SIZE - 2)), consumer, options)
        element.forceRender(offset + BACKGROUND_SIZE, consumer, options)
    }

    override fun onChildChange(child: Element) {
        forceSilentApply()
        super.onChildChange(this)
    }

    override fun forceSilentApply() {
        cacheUpToDate = false
    }

    companion object : HUDBuilder<LayoutedGUIElement<WawlaHUDElement>> {
        private const val BACKGROUND_SIZE = 5
        override val identifier: ResourceLocation = "minosoft:wawla".toResourceLocation()

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<WawlaHUDElement> {
            return LayoutedGUIElement(WawlaHUDElement(guiRenderer))
        }
    }
}
