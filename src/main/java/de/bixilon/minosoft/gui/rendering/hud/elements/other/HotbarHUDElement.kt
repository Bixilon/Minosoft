/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.hud.elements.other

import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.hud.HUDElementProperties
import de.bixilon.minosoft.gui.rendering.hud.HUDMesh
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.atlas.HUDAtlasElement
import de.bixilon.minosoft.gui.rendering.hud.elements.HUDElement
import glm_.glm
import glm_.vec2.Vec2

class HotbarHUDElement(
    hudRender: HUDRenderer,
) : HUDElement(hudRender) {
    override val elementProperties = HUDElementProperties(
        position = Vec2(0f, -0.5f),
        xBinding = HUDElementProperties.PositionBindings.CENTER,
        yBinding = HUDElementProperties.PositionBindings.FAHRTEST_POINT_AWAY,
        scale = 1f,
        enabled = true,
    )

    lateinit var hotbarBaseAtlas: HUDAtlasElement
    lateinit var hotbarSelectedSlotFrame: HUDAtlasElement

    private lateinit var hotbarBaseRealSize: Vec2
    private lateinit var hotbarSelectedSlotFrameRealSize: Vec2


    override fun init() {
        hotbarBaseAtlas = hudRenderer.hudImages[ResourceLocation("minecraft:hotbar_base")]!!
        hotbarBaseRealSize = hotbarBaseAtlas.binding.size * hudRenderer.hudScale.scale * elementProperties.scale
        hotbarSelectedSlotFrame = hudRenderer.hudImages[ResourceLocation("minecraft:hotbar_selected_slot_frame")]!!
        hotbarSelectedSlotFrameRealSize = hotbarSelectedSlotFrame.binding.size * hudRenderer.hudScale.scale * elementProperties.scale

        for ((slotIndex, resourceLocation) in KeyBindingsNames.SELECT_HOTBAR_SLOTS.withIndex()) {
            hudRenderer.renderWindow.registerKeyCallback(resourceLocation) { _, _ ->
                hudRenderer.connection.sender.selectSlot(slotIndex)
            }
        }
    }


    enum class RealTypes {
        START,
        END
    }

    private fun drawBaseBar(hudMesh: HUDMesh) {
        val hotbarStart = getRealPosition(hotbarBaseRealSize, elementProperties, RealTypes.START)
        drawImage(hotbarStart, getRealPosition(hotbarBaseRealSize, elementProperties, RealTypes.END), hudMesh, hotbarBaseAtlas, 1)


        val selectedSlotBinding = hotbarBaseAtlas.slots[hudRenderer.connection.player.selectedSlot] ?: return
        val selectedSlotFrameBinding = hotbarSelectedSlotFrame.slots[0] ?: return

        val slotSizeFactorDelta = glm.abs(Vec2(hotbarSelectedSlotFrame.binding.size - selectedSlotFrameBinding.size))

        val selectedSlotStart = hotbarStart + selectedSlotBinding.start * hudRenderer.hudScale.scale * elementProperties.scale - slotSizeFactorDelta
        val selectedSlotEnd = hotbarStart + selectedSlotBinding.end * hudRenderer.hudScale.scale * elementProperties.scale + slotSizeFactorDelta

        drawImage(selectedSlotStart, selectedSlotEnd, hudMesh, hotbarSelectedSlotFrame, 2)
    }

    override fun prepare(hudMesh: HUDMesh) {
        drawBaseBar(hudMesh)
    }
}
