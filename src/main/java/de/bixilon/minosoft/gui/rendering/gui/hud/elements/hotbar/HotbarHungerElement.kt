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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.registries.effects.other.OtherEffect
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.Atlas
import de.bixilon.minosoft.gui.rendering.gui.atlas.Atlas.Companion.get
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasElement
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import java.util.concurrent.ThreadLocalRandom

class HotbarHungerElement(guiRenderer: GUIRenderer) : Element(guiRenderer), Pollable {
    private val random = ThreadLocalRandom.current()
    private val profile = guiRenderer.connection.profiles.gui
    private val hungerProfile = profile.hud.hotbar.hunger
    private var ticks = 0

    private val atlas = HungerAtlas(guiRenderer.atlas[ATLAS])

    private var hungerEffect = false
    private var animate = true
        set(value) {
            super.cacheEnabled = !value
            field = value
        }

    private var hunger = 0
    private var saturation = 0.0f


    init {
        _size = Vec2(HUNGER_CONTAINERS, 1) * HUNGER_SIZE + Vec2(1, 0) // 1 pixel is overlapping per hunger, so one more
        hungerProfile::saturationBar.observe(this) { cacheUpToDate = false }
    }

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        var hungerLeft = hunger
        var saturationLeft = saturation.toInt()

        for (i in HUNGER_CONTAINERS - 1 downTo 0) {
            var animateOffset = 0

            if (animate) {
                animateOffset = random.nextInt(3) - 1
            }

            val hungerOffset = offset + Vec2i(i * HUNGER_SIZE.x, animateOffset)

            if (hungerProfile.saturationBar) {
                val container = when {
                    hungerEffect -> atlas.hungerContainer
                    saturationLeft == 1 -> {
                        saturationLeft -= 1
                        atlas.getSaturationContainer(true)
                    }

                    saturationLeft > 1 -> {
                        saturationLeft -= 2
                        atlas.getSaturationContainer(false)
                    }

                    else -> atlas.container
                }
                AtlasImageElement(guiRenderer, container).render(hungerOffset, consumer, options)
            }

            if (hungerLeft <= 0) {
                continue
            }

            val hungerElement: AtlasElement?

            if (hungerLeft == 1) {
                hungerLeft--
                hungerElement = atlas.getHunger(true, hungerEffect)
            } else {
                hungerLeft -= 2
                hungerElement = atlas.getHunger(false, hungerEffect)
            }

            AtlasImageElement(guiRenderer, hungerElement).render(hungerOffset, consumer, options)
        }
    }

    override fun forceSilentApply() {
        cacheUpToDate = false
    }

    override fun tick() {
        val healthCondition = guiRenderer.context.connection.player.healthCondition

        animate = healthCondition.saturation <= 0.0f && ticks++ % (healthCondition.hunger * 3 + 1) == 0

        apply()
    }

    override fun poll(): Boolean {
        val healthCondition = guiRenderer.context.connection.player.healthCondition

        val hunger = healthCondition.hunger
        val saturation = healthCondition.saturation

        val hungerEffect = guiRenderer.context.connection.player.effects[OtherEffect.Hunger] != null

        if (this.hunger == hunger && this.saturation == saturation && this.hungerEffect == hungerEffect) {
            return false
        }

        this.hunger = hunger
        this.saturation = saturation
        this.hungerEffect = hungerEffect

        return true
    }

    private class HungerAtlas(val atlas: Atlas?) {
        val container = atlas["container"]
        val hungerContainer = atlas["container_hunger"]

        fun getHunger(half: Boolean, hunger: Boolean): AtlasElement = TODO()
        fun getSaturationContainer(half: Boolean): AtlasElement = TODO()
    }

    companion object {
        private val ATLAS = minosoft("hud/hotbar/hunger")
        const val MAX_HUNGER = 20
        private const val HUNGER_CONTAINERS = MAX_HUNGER / 2
        private val HUNGER_SIZE = Vec2i(8, 9)
    }
}
