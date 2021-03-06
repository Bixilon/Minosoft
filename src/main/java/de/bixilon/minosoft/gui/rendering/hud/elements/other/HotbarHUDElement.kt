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
import de.bixilon.minosoft.gui.rendering.hud.ElementMesh
import de.bixilon.minosoft.gui.rendering.hud.HUDElementProperties
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.atlas.HUDAtlasElement
import de.bixilon.minosoft.gui.rendering.hud.elements.HUDElement
import de.bixilon.minosoft.gui.rendering.hud.elements.ProgressBar
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

    //   private var lastYChange = 0L
    //   private var lastXChange = 0L

    private lateinit var hotbarBaseAtlasElement: HUDAtlasElement
    private lateinit var hotbarSelectedSlotFrameAtlasElement: HUDAtlasElement

    private lateinit var experienceBar: ProgressBar


    override fun init() {
        hotbarBaseAtlasElement = hudRenderer.hudAtlasElements[ResourceLocation("minecraft:hotbar_base")]!!
        hotbarSelectedSlotFrameAtlasElement = hudRenderer.hudAtlasElements[ResourceLocation("minecraft:hotbar_selected_slot_frame")]!!

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


    override fun prepare(elementMesh: ElementMesh) {
        // test code
        //    val time = System.currentTimeMillis()
        //    if (time - lastYChange > 600) {
        //        if (elementProperties.position.y >= 1.0f) {
        //            elementProperties.position.y = -1.0f
        //        } else {
        //            elementProperties.position.y += 0.1f
        //        }
        //        lastYChange = time
        //    }

        //    if (time - lastXChange > 1500) {
        //        if (elementProperties.position.x >= 1.0f) {
        //            elementProperties.position.x = -1.0f
        //        } else {
        //            elementProperties.position.x += 0.1f
        //        }
        //        lastXChange = time
        //    }


        if (hudRenderer.connection.player.gameMode == GameModes.SPECTATOR) {
            return
        }

        elementMesh.forceX = hotbarBaseAtlasElement.binding.size.x.toInt()

        val hotbarStart = Vec2(0, elementMesh.size.y)

        elementMesh.addElement(hotbarStart, hotbarBaseAtlasElement.binding.size, hotbarBaseAtlasElement)

        // selectedFrame
        val selectedSlotBinding = hotbarBaseAtlasElement.slots[hudRenderer.connection.player.selectedSlot] ?: return
        val selectedSlotFrameBinding = hotbarSelectedSlotFrameAtlasElement.slots[0] ?: return

        val selectedFrameOffset = Vec2((hotbarSelectedSlotFrameAtlasElement.binding.size.y - hotbarBaseAtlasElement.binding.size.y) * 2)


        elementMesh.addElement(selectedSlotBinding.start - selectedFrameOffset, selectedSlotBinding.end + selectedFrameOffset + Vec2(1, 0), hotbarSelectedSlotFrameAtlasElement, 2)


        if (hudRenderer.connection.player.gameMode == GameModes.CREATIVE) {
            return
        }
//
        //     // experience bar
        //     val experienceBarStart = Vec2(hotbarStart.x, (hotbarEnd.y + (experienceBar.size.y * realScale) + elementPadding))
        //     val experienceBarEnd = Vec2(hotbarEnd.x, (hotbarEnd.y + elementPadding))
//
        //     experienceBar.draw(hudMesh, experienceBarStart, experienceBarEnd, hudRenderer.connection.player.experienceBarProgress, 3)
    }
}
