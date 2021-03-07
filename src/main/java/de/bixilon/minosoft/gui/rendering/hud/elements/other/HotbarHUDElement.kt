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
import de.bixilon.minosoft.data.Gamemodes
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.hud.HUDElementProperties
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.atlas.HUDAtlasElement
import de.bixilon.minosoft.gui.rendering.hud.atlas.ProgressBarAtlasElement
import de.bixilon.minosoft.gui.rendering.hud.elements.HUDElement
import de.bixilon.minosoft.gui.rendering.hud.elements.primitive.ElementListElement
import de.bixilon.minosoft.gui.rendering.hud.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.hud.elements.primitive.ProgressBar
import glm_.vec2.Vec2

class HotbarHUDElement(
    hudRender: HUDRenderer,
) : HUDElement(hudRender) {
    override val elementProperties = HUDElementProperties(
        position = Vec2(0f, -1.0f),
        xBinding = HUDElementProperties.PositionBindings.CENTER,
        yBinding = HUDElementProperties.PositionBindings.FURTHEST_POINT_AWAY,
        scale = 1.0f,
        enabled = true,
    )


    private lateinit var hotbarBaseAtlasElement: HUDAtlasElement
    private lateinit var hotbarSelectedSlotFrameAtlasElement: HUDAtlasElement

    private lateinit var experienceBarAtlasElement: ProgressBarAtlasElement


    override fun init() {
        hotbarBaseAtlasElement = hudRenderer.hudAtlasElements[ResourceLocation("minecraft:hotbar_base")]!!
        hotbarSelectedSlotFrameAtlasElement = hudRenderer.hudAtlasElements[ResourceLocation("minecraft:hotbar_selected_slot_frame")]!!

        experienceBarAtlasElement = ProgressBarAtlasElement(
            hudRenderer.hudAtlasElements[ResourceLocation("minecraft:experience_bar_empty")]!!,
            hudRenderer.hudAtlasElements[ResourceLocation("minecraft:experience_bar_full")]!!,
        )

        for ((slotIndex, resourceLocation) in KeyBindingsNames.SELECT_HOTBAR_SLOTS.withIndex()) {
            hudRenderer.renderWindow.registerKeyCallback(resourceLocation) { _, _ ->
                hudRenderer.connection.sender.selectSlot(slotIndex)
            }
        }
    }


    override fun prepare(elementList: ElementListElement) {
        if (hudRenderer.connection.player.gamemode == Gamemodes.SPECTATOR) {
            return
        }


        elementList.forceX = hotbarBaseAtlasElement.binding.size.x.toInt()


        if (hudRenderer.connection.player.gamemode != Gamemodes.CREATIVE) {
            // experience bar
            val experienceBarEnd = Vec2(hotbarBaseAtlasElement.binding.size.x, experienceBarAtlasElement.emptyAtlasElement.binding.size.y)

            val experienceBar = ProgressBar(Vec2(), experienceBarEnd, experienceBarAtlasElement, hudRenderer.connection.player.experienceBarProgress, 1)
            elementList.addChild(experienceBar)
        }


        val hotbarStart = Vec2(0, elementList.size.y + ELEMENT_PADDING)

        elementList.addChild(ImageElement(hotbarStart, hotbarBaseAtlasElement.binding.size + Vec2(0, hotbarStart.y), hotbarBaseAtlasElement))

        // selectedFrame
        val selectedSlotBinding = hotbarBaseAtlasElement.slots[hudRenderer.connection.player.selectedSlot] ?: return
        val selectedSlotFrameBinding = hotbarSelectedSlotFrameAtlasElement.slots[0] ?: return

        val selectedFrameOffset = Vec2((hotbarSelectedSlotFrameAtlasElement.binding.size.y - hotbarBaseAtlasElement.binding.size.y) * 2)


        elementList.addChild(ImageElement(hotbarStart + selectedSlotBinding.start - selectedFrameOffset, hotbarStart + selectedSlotBinding.end + selectedFrameOffset + Vec2(1, 0), hotbarSelectedSlotFrameAtlasElement, 2))

    }

    companion object {
        private const val ELEMENT_PADDING = 2
    }
}
