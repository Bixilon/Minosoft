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
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.atlas.HUDAtlasElement
import de.bixilon.minosoft.gui.rendering.hud.elements.HUDElement
import de.bixilon.minosoft.gui.rendering.hud.elements.primitive.*
import de.bixilon.minosoft.modding.event.EventInvokerCallback
import de.bixilon.minosoft.modding.event.events.ChangeGameStateEvent
import de.bixilon.minosoft.modding.event.events.ExperienceChangeEvent
import de.bixilon.minosoft.modding.event.events.HeldItemChangeEvent
import de.bixilon.minosoft.modding.event.events.UpdateHealthEvent
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketChangeGameState
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

class HotbarHUDElement(
    hudRender: HUDRenderer,
) : HUDElement(hudRender) {
    private lateinit var hotbarBase: HotbarBaseElement

    private lateinit var experienceBar: ProgressBar
    private lateinit var levelText: TextElement

    private lateinit var healthBar: HealthBar


    override fun init() {
        hotbarBase = HotbarBaseElement(
            baseHUDAtlasElement = hudRenderer.hudAtlasElements[ResourceLocation("minecraft:hotbar_base")]!!,
            frameHUDAtlasElement = hudRenderer.hudAtlasElements[ResourceLocation("minecraft:hotbar_selected_slot_frame")]!!
        )

        experienceBar = ProgressBar(
            emptyAtlasElement = hudRenderer.hudAtlasElements[ResourceLocation("minecraft:experience_bar_empty")]!!,
            fullAtlasElement = hudRenderer.hudAtlasElements[ResourceLocation("minecraft:experience_bar_full")]!!,
            z = 1,
        )

        levelText = TextElement(
            font = hudRenderer.renderWindow.font,
            background = false,
            z = 2,
        )

        healthBar = HealthBar(
            blackHeartContainerAtlasElement = hudRenderer.hudAtlasElements[ResourceLocation("minecraft:black_heart_container")]!!,
            whiteHeartContainerAtlasElement = hudRenderer.hudAtlasElements[ResourceLocation("minecraft:white_heart_container")]!!,
            halfHartAtlasElement = hudRenderer.hudAtlasElements[ResourceLocation("minecraft:half_red_heart")]!!,
            hartAtlasElement = hudRenderer.hudAtlasElements[ResourceLocation("minecraft:full_red_heart")]!!,
            maxValue = 20.0f,
            textReplaceValue = 40.0f,
            textColor = RenderConstants.HP_TEXT_COLOR,
            font = hudRenderer.renderWindow.font,
            z = 5,
        )

        registerEvents()

        prepare()
    }

    private fun registerEvents() {
        for ((slotIndex, resourceLocation) in KeyBindingsNames.SELECT_HOTBAR_SLOTS.withIndex()) {
            hudRenderer.renderWindow.registerKeyCallback(resourceLocation) { _, _ ->
                hudRenderer.connection.sender.selectSlot(slotIndex)
                prepare()
            }
        }

        hudRenderer.connection.registerEvent(EventInvokerCallback<ExperienceChangeEvent> {
            experienceBar.progress = it.bar
            levelText.text = TextComponent(it.level.toString()).color(RenderConstants.EXPERIENCE_BAR_LEVEL_COLOR)
            experienceBar.prepare()
            prepare()
        })

        hudRenderer.connection.registerEvent(EventInvokerCallback<HeldItemChangeEvent> {
            hotbarBase.selectedSlot = it.slot
            hotbarBase.prepare()
            prepare()
        })
        hudRenderer.connection.registerEvent(EventInvokerCallback<ChangeGameStateEvent> {
            if (it.reason != PacketChangeGameState.Reason.CHANGE_GAMEMODE) {
                return@EventInvokerCallback
            }
            prepare()
        })
        hudRenderer.connection.registerEvent(EventInvokerCallback<UpdateHealthEvent> {
            healthBar.value = it.health
            healthBar.prepare()
            prepare()
        })
    }

    private fun prepare() {
        layout.clear()
        if (hudRenderer.connection.player.entity.gamemode == Gamemodes.SPECTATOR) {
            // ToDo: Spectator hotbar
            return
        }


        if (hudRenderer.connection.player.entity.gamemode != Gamemodes.CREATIVE) {
            // add health bar, hunger, armor, experience and more
            layout.addChild(healthBar)


            // if (hudRenderer.connection.player.level != 0) {
            // experience
            levelText.start = Vec2i((hotbarBase.size.x - levelText.size.x) / 2, layout.size.y - (levelText.size.y - ELEMENT_PADDING) + ELEMENT_PADDING)
            layout.addChild(levelText)
            // }

            // experience bar
            experienceBar.start = Vec2i(0, levelText.start.y + levelText.size.y - ELEMENT_PADDING)
            layout.addChild(experienceBar)
        }


        hotbarBase.start = Vec2i(0, layout.size.y + ELEMENT_PADDING)

        layout.addChild(hotbarBase)
    }

    companion object {
        private const val ELEMENT_PADDING = 2
    }

    class HotbarBaseElement(
        start: Vec2i = Vec2i(0, 0),
        val baseHUDAtlasElement: HUDAtlasElement,
        val frameHUDAtlasElement: HUDAtlasElement,
        z: Int = 1,
    ) : Layout(start, z) {
        private var _selectedSlot = 0

        var selectedSlot: Int
            get() = _selectedSlot
            set(value) {
                _selectedSlot = value
                prepare()
            }

        private val base = ImageElement(textureLike = baseHUDAtlasElement)
        private val frame = ImageElement(textureLike = frameHUDAtlasElement, z = 2)

        init {
            fakeX = base.size.x
            addChild(base)
            addChild(frame)
        }

        fun prepare() {
            // selectedFrame
            val slotBinding = baseHUDAtlasElement.slots[selectedSlot] ?: return
            val frameSlotBinding = frameHUDAtlasElement.slots[0] ?: return

            val frameSizeFactor = slotBinding.size / frameSlotBinding.size

            val selectedFrameOffset = Vec2(((frameHUDAtlasElement.binding.size.y * frameSizeFactor.y) - baseHUDAtlasElement.binding.size.y) * 2)

            frame.start = slotBinding.start - selectedFrameOffset
            frame.end = slotBinding.end + selectedFrameOffset

            cache.clear()
        }
    }
}

