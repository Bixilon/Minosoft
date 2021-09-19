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
import de.bixilon.minosoft.data.registries.effects.attributes.DefaultStatusEffectAttributeNames
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.atlas.HUDAtlasElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.util.KUtil.decide
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.MMath.ceil
import glm_.vec2.Vec2i

class HotbarHealthElement(hudRenderer: HUDRenderer) : Element(hudRenderer) {
    private val witherStatusEffect = hudRenderer.connection.registries.statusEffectRegistry[DefaultStatusEffects.WITHER]
    private val poisonStatusEffect = hudRenderer.connection.registries.statusEffectRegistry[DefaultStatusEffects.POISON]
    private val atlasManager = hudRenderer.atlasManager

    /**
     *  [normal|hardcore][normal|poison|wither][normal|damage][full|half]
     */
    private val hearts = arrayOf(
        arrayOf(
            arrayOf(
                arrayOf(
                    atlasManager["minecraft:normal_heart"],
                    atlasManager["minecraft:half_normal_heart"],
                ),
                arrayOf(
                    atlasManager["minecraft:normal_damage_heart"],
                    atlasManager["minecraft:half_normal_damage_heart"],
                ),
            ),
            arrayOf(
                arrayOf(
                    atlasManager["minecraft:poison_heart"],
                    atlasManager["minecraft:half_poison_heart"],
                ),
                arrayOf(
                    atlasManager["minecraft:poison_damage_heart"],
                    atlasManager["minecraft:half_poison_damage_heart"],
                ),
            ),
            arrayOf(
                arrayOf(
                    atlasManager["minecraft:wither_heart"],
                    atlasManager["minecraft:half_wither_heart"],
                ),
                arrayOf(
                    atlasManager["minecraft:wither_damage_heart"],
                    atlasManager["minecraft:half_wither_damage_heart"],
                ),
            ),
        ),
        arrayOf(
            arrayOf(
                arrayOf(
                    atlasManager["minecraft:hardcore_normal_heart"],
                    atlasManager["minecraft:hardcore_half_normal_heart"],
                ),
                arrayOf(
                    atlasManager["minecraft:hardcore_normal_damage_heart"],
                    atlasManager["minecraft:hardcore_half_normal_damage_heart"],
                ),
            ),
            arrayOf(
                arrayOf(
                    atlasManager["minecraft:hardcore_poison_heart"],
                    atlasManager["minecraft:hardcore_half_poison_heart"],
                ),
                arrayOf(
                    atlasManager["minecraft:hardcore_poison_damage_heart"],
                    atlasManager["minecraft:hardcore_half_poison_damage_heart"],
                ),
            ),
            arrayOf(
                arrayOf(
                    atlasManager["minecraft:hardcore_wither_heart"],
                    atlasManager["minecraft:hardcore_half_wither_heart"],
                ),
                arrayOf(
                    atlasManager["minecraft:hardcore_wither_damage_heart"],
                    atlasManager["minecraft:hardcore_half_wither_damage_heart"],
                ),
            ),
        ),
    )

    /**
     * [normal|hardcore][full|half]
     */
    private val absorptionHearts = arrayOf(
        arrayOf(
            atlasManager["minecraft:absorption_heart"],
            atlasManager["minecraft:half_absorption_heart"],
        ),
        arrayOf(
            atlasManager["minecraft:hardcore_absorption_heart"],
            atlasManager["minecraft:hardcore_half_absorption_heart"],
        ),
    )

    /**
     * [normal|hardcore][full|half]
     */
    private val frozenHearts = arrayOf(
        arrayOf(
            atlasManager["minecraft:frozen_heart"],
            atlasManager["minecraft:half_frozen_heart"],
        ),
        arrayOf(
            atlasManager["minecraft:hardcore_frozen_heart"],
            atlasManager["minecraft:hardcore_half_frozen_heart"],
        ),
    )
    private val whiteHeartContainer = atlasManager["minecraft:white_heart_container"]
    private val blackHeartContainer = atlasManager["minecraft:black_heart_container"]

    private var hardcode = false
    private var poison = false
    private var wither = false
    private var frozen = false

    private var health = 0.0f
    private var absorptionsAmount = 0.0f
    private var totalHealth = 0.0f

    private var maxHealth = 0.0f
    private var totalMaxHealth = 0.0f

    private var totalMaxHearts = 0
    private var rows = 0

    override fun render(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        blackHeartContainer ?: return 0
        whiteHeartContainer ?: return 0

        // ToDo: Damage animation, regeneration, caching
        for (heart in 0 until totalMaxHearts) {
            val row = heart / HEARTS_PER_ROW
            val column = heart % HEARTS_PER_ROW

            val image = ImageElement(hudRenderer, blackHeartContainer)

            image.render(offset + Vec2i(column, (rows - 1) - row) * HEART_SIZE, z, consumer)
        }

        val hardcoreIndex = hardcode.decide(1, 0)

        var healthLeft = totalHealth
        var heart = 0

        while (healthLeft > 0.5f) {
            val row = heart / HEARTS_PER_ROW
            val column = heart % HEARTS_PER_ROW


            var selectArray: Array<*>? = null

            var normalHeart = false

            if (healthLeft <= absorptionsAmount) {
                selectArray = absorptionHearts[hardcoreIndex]
            } else {
                selectArray = hearts[hardcoreIndex]

                // heart type
                selectArray = selectArray[when {
                    poison -> 1
                    wither -> 2
                    else -> {
                        normalHeart = true
                        0
                    }
                }]

                // ToDo: damage heart
                selectArray = selectArray[0]
            }

            if (frozen && normalHeart) {
                selectArray = frozenHearts[hardcoreIndex]
            }


            val halfHeart = healthLeft < 1.5f
            val image = when {
                halfHeart -> selectArray.unsafeCast<Array<HUDAtlasElement?>>()[1]?.let { ImageElement(hudRenderer, it) }
                else -> selectArray.unsafeCast<Array<HUDAtlasElement?>>()[0]?.let { ImageElement(hudRenderer, it) }
            }

            image?.render(offset + Vec2i(column, (rows - 1) - row) * HEART_SIZE, z + 1, consumer)

            heart++
            healthLeft -= halfHeart.decide(1.0f, 2.0f)
        }

        return 2
    }

    override fun silentApply() {
        blackHeartContainer ?: return
        whiteHeartContainer ?: return

        val player = hudRenderer.connection.player

        hardcode = hudRenderer.connection.world.hardcore
        poison = poisonStatusEffect?.let { player.activeStatusEffects[it] != null } ?: false
        wither = witherStatusEffect?.let { player.activeStatusEffects[it] != null } ?: false
        frozen = player.ticksFrozen > 0

        health = player.healthCondition.hp
        absorptionsAmount = player.playerAbsorptionHearts // ToDo: This is (probably) calculated as effect instance
        totalHealth = health + absorptionsAmount

        maxHealth = player.getAttributeValue(DefaultStatusEffectAttributeNames.GENERIC_MAX_HEALTH).toFloat()
        totalMaxHealth = maxHealth + absorptionsAmount

        totalMaxHearts = (totalMaxHealth / 2).ceil

        rows = totalMaxHearts / HEARTS_PER_ROW
        if (totalMaxHearts % HP_PER_ROW != 0) {
            rows++
        }

        size = Vec2i(HEARTS_PER_ROW, rows) * HEART_SIZE
    }


    companion object {
        private const val HP_PER_ROW = 20
        private const val HEARTS_PER_ROW = HP_PER_ROW / 2
        private val HEART_SIZE = Vec2i(8, 9)
    }
}
