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
import de.bixilon.minosoft.data.GameModes
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.hud.HUDElementProperties
import de.bixilon.minosoft.gui.rendering.hud.HUDMesh
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.atlas.HUDAtlasElement
import de.bixilon.minosoft.gui.rendering.hud.elements.HUDElement
import de.bixilon.minosoft.gui.rendering.hud.elements.ProgressBar
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

    private val realScale = elementProperties.scale * hudRenderer.hudScale.scale
    private val elementPadding = 2 * realScale

    private lateinit var hotbarBaseAtlasElement: HUDAtlasElement
    private lateinit var hotbarBaseRealSize: Vec2
    private lateinit var hotbarSelectedSlotFrameAtlasElement: HUDAtlasElement
    private lateinit var hotbarSelectedSlotFrameRealSize: Vec2


    private lateinit var experienceBar: ProgressBar


    override fun init() {
        hotbarBaseAtlasElement = hudRenderer.hudAtlasElements[ResourceLocation("minecraft:hotbar_base")]!!
        hotbarBaseRealSize = hotbarBaseAtlasElement.binding.size * hudRenderer.hudScale.scale * elementProperties.scale
        hotbarSelectedSlotFrameAtlasElement = hudRenderer.hudAtlasElements[ResourceLocation("minecraft:hotbar_selected_slot_frame")]!!
        hotbarSelectedSlotFrameRealSize = hotbarSelectedSlotFrameAtlasElement.binding.size * hudRenderer.hudScale.scale * elementProperties.scale

        experienceBar = ProgressBar(
            hudRenderer.hudAtlasElements[ResourceLocation("minecraft:experience_bar_empty")]!!,
            hudRenderer.hudAtlasElements[ResourceLocation("minecraft:experience_bar_full")]!!,
            this
        )

        for ((slotIndex, resourceLocation) in KeyBindingsNames.SELECT_HOTBAR_SLOTS.withIndex()) {
            hudRenderer.renderWindow.registerKeyCallback(resourceLocation) { _, _ ->
                hudRenderer.connection.sender.selectSlot(slotIndex)
            }
        }
    }


    override fun prepare(hudMesh: HUDMesh) {
        if (hudRenderer.connection.player.gameMode == GameModes.SPECTATOR) {
            return
        }
        // hotbar
        val hotbarStart = getRealPosition(hotbarBaseRealSize, elementProperties, RealTypes.START)
        val hotbarEnd = getRealPosition(hotbarBaseRealSize, elementProperties, RealTypes.END)
        drawImage(hotbarStart, hotbarEnd, hudMesh, hotbarBaseAtlasElement, 1)

        // selectedFrame
        val selectedSlotBinding = hotbarBaseAtlasElement.slots[hudRenderer.connection.player.selectedSlot] ?: return
        val selectedSlotFrameBinding = hotbarSelectedSlotFrameAtlasElement.slots[0] ?: return

        val slotSizeFactorDelta = glm.abs(Vec2(hotbarSelectedSlotFrameAtlasElement.binding.size - selectedSlotFrameBinding.size))

        val selectedSlotStart = hotbarStart + selectedSlotBinding.start * realScale - slotSizeFactorDelta + Vec2(0, realScale)
        val selectedSlotEnd = hotbarStart + selectedSlotBinding.end * realScale + slotSizeFactorDelta + Vec2(realScale, realScale)

        drawImage(selectedSlotStart, selectedSlotEnd, hudMesh, hotbarSelectedSlotFrameAtlasElement, 2)


        if (hudRenderer.connection.player.gameMode == GameModes.CREATIVE) {
            return
        }

        // experience bar
        val experienceBarStart = Vec2(hotbarStart.x, (hotbarEnd.y + (experienceBar.size.y * realScale) + elementPadding))
        val experienceBarEnd = Vec2(hotbarEnd.x, (hotbarEnd.y + elementPadding))

        experienceBar.draw(hudMesh, experienceBarStart, experienceBarEnd, hudRenderer.connection.player.experienceBarProgress, 3)
    }
}
