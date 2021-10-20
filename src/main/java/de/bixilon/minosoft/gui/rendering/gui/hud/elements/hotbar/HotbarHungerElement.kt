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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar

import de.bixilon.minosoft.data.registries.effects.DefaultStatusEffects
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.atlas.HUDAtlasElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import glm_.vec2.Vec2i
import java.util.concurrent.ThreadLocalRandom

class HotbarHungerElement(hudRenderer: HUDRenderer) : Element(hudRenderer), Pollable {
    private val random = ThreadLocalRandom.current()
    private var ticks = 0
    private val hungerStatusEffect = hudRenderer.connection.registries.statusEffectRegistry[DefaultStatusEffects.HUNGER]
    private val atlasManager = hudRenderer.atlasManager


    private val normalHungerContainer = atlasManager["minecraft:normal_hunger_container"]!!
    private val hungerHungerContainer = atlasManager["minecraft:hunger_hunger_container"]!!

    /**
     * [full|half]
     */
    private val saturationHungerContainer = arrayOf(
        atlasManager["minosoft:saturation_hunger_container"]!!,
        atlasManager["minosoft:half_saturation_hunger_container"]!!,
    )

    /**
     * [normal|hunger][full|half]
     */
    private val hungerElements = arrayOf(
        arrayOf(
            atlasManager["minecraft:normal_hunger"]!!,
            atlasManager["minecraft:half_normal_hunger"]!!,
        ),
        arrayOf(
            atlasManager["minecraft:hunger_hunger"]!!,
            atlasManager["minecraft:half_hunger_hunger"]!!,
        )
    )

    override var cacheEnabled: Boolean
        get() = super.cacheEnabled && !animate
        set(value) {
            super.cacheEnabled = value
        }

    private var hungerEffect = false
    private var animate = true

    private var hunger = 0
    private var saturation = 0.0f


    init {
        size = Vec2i(HUNGER_CONTAINERS, 1) * HUNGER_SIZE
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        var hungerLeft = hunger
        var saturationLeft = saturation.toInt()

        for (i in HUNGER_CONTAINERS - 1 downTo 0) {
            var animateOffset = 0

            if (animate) {
                animateOffset = random.nextInt(3) - 1
            }

            val hungerOffset = offset + Vec2i(i * HUNGER_SIZE.x, animateOffset)

            val container = when {
                hungerEffect -> hungerHungerContainer
                saturationLeft == 1 -> {
                    saturationLeft -= 1
                    saturationHungerContainer[1]
                }
                saturationLeft > 1 -> {
                    saturationLeft -= 2
                    saturationHungerContainer[0]
                }
                else -> normalHungerContainer
            }
            ImageElement(hudRenderer, container).render(hungerOffset, z, consumer)


            val selectArray: Array<*> = if (hungerEffect) {
                hungerElements[1]
            } else {
                hungerElements[0]
            }

            if (hungerLeft <= 0) {
                continue
            }

            val hungerElement: HUDAtlasElement

            if (hungerLeft == 1) {
                hungerLeft--
                hungerElement = selectArray[1] as HUDAtlasElement
            } else {
                hungerLeft -= 2
                hungerElement = selectArray[0] as HUDAtlasElement
            }

            ImageElement(hudRenderer, hungerElement).render(hungerOffset, z + 1, consumer)
        }


        return 2
    }

    override fun forceSilentApply() {
        cacheUpToDate = false
    }

    override fun tick() {
        val healthCondition = hudRenderer.connection.player.healthCondition

        animate = healthCondition.saturation <= 0.0f && ticks++ % (healthCondition.hunger * 3 + 1) == 0

        apply()
    }

    override fun poll(): Boolean {
        val healthCondition = hudRenderer.connection.player.healthCondition

        val hunger = healthCondition.hunger
        val saturation = healthCondition.saturation

        val hungerEffect = hudRenderer.connection.player.activeStatusEffects.contains(hungerStatusEffect)

        if (this.hunger == hunger && this.saturation == saturation && this.hungerEffect == hungerEffect) {
            return false
        }

        this.hunger = hunger
        this.saturation = saturation
        this.hungerEffect = hungerEffect

        return true
    }

    companion object {
        private const val MAX_HUNGER = 20
        private const val HUNGER_CONTAINERS = MAX_HUNGER / 2
        private val HUNGER_SIZE = Vec2i(8, 9)
    }
}
