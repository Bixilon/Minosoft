/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
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

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.atlas.HUDAtlasElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.CustomHUDElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMesh
import de.bixilon.minosoft.gui.rendering.input.camera.hit.BlockRaycastHit
import de.bixilon.minosoft.gui.rendering.input.camera.hit.EntityRaycastHit
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class CrosshairHUDElement(hudRenderer: HUDRenderer) : CustomHUDElement(hudRenderer) {
    private lateinit var crosshairAtlasElement: HUDAtlasElement
    private var mesh: GUIMesh? = null
    private var previousDebugEnabled: Boolean? = true

    override fun init() {
        crosshairAtlasElement = hudRenderer.atlasManager[ATLAS_NAME]!!
    }

    override fun draw() {
        val debugHUDElement: DebugHUDElement? = hudRenderer[DebugHUDElement]

        if (debugHUDElement?.enabled != previousDebugEnabled) {
            apply()
            previousDebugEnabled = debugHUDElement?.enabled
        }

        val mesh = mesh ?: return

        if (Minosoft.config.config.game.hud.crosshair.complementaryColor) {
            renderWindow.renderSystem.reset(blending = true, sourceAlpha = BlendingFunctions.ONE_MINUS_DESTINATION_COLOR, destinationAlpha = BlendingFunctions.ONE_MINUS_SOURCE_COLOR)
        } else {
            renderWindow.renderSystem.reset()
        }

        hudRenderer.shader.use()
        mesh.draw()
    }

    override fun apply() {
        mesh?.unload()
        this.mesh = null

        val config = Minosoft.config.config.game.hud.crosshair


        val mesh = GUIMesh(renderWindow, hudRenderer.matrix)

        // Custom draw to make the crosshair inverted
        if (renderWindow.connection.player.gamemode == Gamemodes.SPECTATOR) {
            val hitResult = renderWindow.inputHandler.camera.target ?: return
            if (hitResult is EntityRaycastHit && (hitResult !is BlockRaycastHit || renderWindow.connection.world.getBlockEntity(hitResult.blockPosition) == null)) {
                return
            }
        }

        val debugHUDElement: DebugHUDElement? = hudRenderer[DebugHUDElement]

        if (debugHUDElement?.enabled == true) {
            // ToDo: Debug crosshair
            return
        }

        val start = (hudRenderer.scaledSize - CROSSHAIR_SIZE) / 2

        mesh.addQuad(start, start + CROSSHAIR_SIZE, 0, crosshairAtlasElement, config.color, null)


        // ToDo: Attack indicator

        mesh.load()
        this.mesh = mesh
    }


    companion object : HUDBuilder<CrosshairHUDElement> {
        const val CROSSHAIR_SIZE = 16
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:crosshair".toResourceLocation()

        private val ATLAS_NAME = "minecraft:crosshair".toResourceLocation()

        override fun build(hudRenderer: HUDRenderer): CrosshairHUDElement {
            return CrosshairHUDElement(hudRenderer)
        }
    }
}
