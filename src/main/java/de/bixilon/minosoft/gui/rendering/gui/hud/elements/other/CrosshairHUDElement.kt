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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.other

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.camera.target.targets.EntityTarget
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.BlockWithEntity
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasElement
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.CustomHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.other.debug.DebugHUDElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.collections.floats.BufferedArrayFloatList

class CrosshairHUDElement(guiRenderer: GUIRenderer) : CustomHUDElement(guiRenderer) {
    private val profile = guiRenderer.connection.profiles.gui
    private val crosshairProfile = profile.hud.crosshair
    private var crosshairAtlasElement: AtlasElement? = null
    private var mesh: GUIMesh? = null
    private var previousDebugEnabled: Boolean? = true
    private var reapply = true
    private var previousNeedsDraw = needsDraw

    override fun init() {
        crosshairAtlasElement = guiRenderer.atlasManager[ATLAS_NAME]
        crosshairProfile::color.observe(this) { reapply = true }
    }

    override fun draw() {
        val debugHUDElement: LayoutedGUIElement<DebugHUDElement>? = guiRenderer.hud[DebugHUDElement]

        val needsDraw = needsDraw
        if (this.needsDraw != needsDraw || debugHUDElement?.enabled != previousDebugEnabled || reapply) {
            apply()
            previousDebugEnabled = debugHUDElement?.enabled
            this.previousNeedsDraw = needsDraw
        }

        val mesh = mesh ?: return

        if (crosshairProfile.complementaryColor) {
            context.renderSystem.reset(blending = true, sourceRGB = BlendingFunctions.ONE_MINUS_DESTINATION_COLOR, destinationRGB = BlendingFunctions.ONE_MINUS_SOURCE_COLOR)
        } else {
            context.renderSystem.reset()
        }

        guiRenderer.shader.use()
        mesh.draw()
    }

    private val needsDraw: Boolean
        get() {
            // Custom draw to make the crosshair inverted
            if (context.connection.player.gamemode == Gamemodes.SPECTATOR) {
                val target = context.connection.camera.target.target
                if (target !is EntityTarget && (target !is BlockTarget || target.state.block !is BlockWithEntity<*>)) {
                    return false
                }
            }

            val debugHUDElement: LayoutedGUIElement<DebugHUDElement>? = guiRenderer.hud[DebugHUDElement]

            if (debugHUDElement?.enabled == true) {
                // ToDo: Debug crosshair
                // return
            }
            return true
        }

    override fun apply() {
        val crosshairAtlasElement = crosshairAtlasElement ?: return

        mesh?.unload()
        this.mesh = null

        val mesh = GUIMesh(context, guiRenderer.halfSize, BufferedArrayFloatList(42))
        val start = (guiRenderer.scaledSize - CROSSHAIR_SIZE) / 2
        mesh.addQuad(start, start + CROSSHAIR_SIZE, crosshairAtlasElement, crosshairProfile.color, null)


        // ToDo: Attack indicator

        mesh.load()
        this.mesh = mesh
        this.reapply = false
    }


    companion object : HUDBuilder<CrosshairHUDElement> {
        const val CROSSHAIR_SIZE = 16
        override val identifier: ResourceLocation = "minosoft:crosshair".toResourceLocation()

        private val ATLAS_NAME = "minecraft:crosshair".toResourceLocation()

        override fun build(guiRenderer: GUIRenderer): CrosshairHUDElement {
            return CrosshairHUDElement(guiRenderer)
        }
    }
}
