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
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.RGBColor.Companion.asColor
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.atlas.HUDAtlasElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.util.KUtil.decide
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.MMath.round10
import glm_.vec2.Vec2i
import java.lang.Float.max
import java.lang.Float.min

class HotbarHealthElement(hudRenderer: HUDRenderer) : AbstractHotbarHealthElement(hudRenderer), Pollable {
    private val witherStatusEffect = hudRenderer.connection.registries.statusEffectRegistry[DefaultStatusEffects.WITHER]
    private val poisonStatusEffect = hudRenderer.connection.registries.statusEffectRegistry[DefaultStatusEffects.POISON]
    private val atlasManager = hudRenderer.atlasManager

    /**
     *  [normal|hardcore] [normal|poison|wither] [normal|damage] [full|half]
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
    private val blackHeartContainer = atlasManager["minecraft:black_heart_container"]!!

    private var hardcode = false
    private var poison = false
    private var wither = false
    private var frozen = false

    private var health = 0.0f
    private var absorptionsAmount = 0.0f
    override var totalHealth = 0.0f

    private var maxHealth = 0.0f
    override var totalMaxHealth = 0.0f

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        if (text) {
            return super.forceRender(offset, z, consumer, options)
        }
        // ToDo: Damage animation, regeneration, caching, stacking
        drawCanisters(offset, z, consumer, options, blackHeartContainer)

        val hardcoreIndex = hardcode.decide(1, 0)

        var healthLeft = totalHealth
        var heart = 0

        while (healthLeft >= 0.5f) {
            val row = heart / HEARTS_PER_ROW
            val column = heart % HEARTS_PER_ROW


            var selectArray: Array<*>?

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
            val image = selectArray.unsafeCast<Array<HUDAtlasElement?>>()[when {
                halfHeart -> 1
                else -> 0
            }]?.let { ImageElement(hudRenderer, it) }

            image?.render(offset + Vec2i(column, (rows - 1) - row) * HEART_SIZE, z + 1, consumer, options)

            heart++
            healthLeft -= halfHeart.decide(1.0f, 2.0f)
        }

        return LAYERS
    }

    override fun forceSilentApply() {
        totalHealth = health + absorptionsAmount

        totalMaxHealth = maxHealth + absorptionsAmount
        super.forceSilentApply()
    }

    override fun poll(): Boolean {
        val player = hudRenderer.connection.player
        val hardcode = hudRenderer.connection.world.hardcore
        val poison = poisonStatusEffect?.let { player.activeStatusEffects[it] != null } ?: false
        val wither = witherStatusEffect?.let { player.activeStatusEffects[it] != null } ?: false
        val frozen = player.ticksFrozen > 0

        val absorptionsAmount = max(0.0f, player.playerAbsorptionHearts)

        val maxHealth = max(0.0f, player.getAttributeValue(DefaultStatusEffectAttributeNames.GENERIC_MAX_HEALTH).toFloat())

        var health = player.healthCondition.hp
        if (health > 0.0f && health < 0.5f) {
            health = 0.5f
        }
        health = min(health, maxHealth)

        if (this.hardcode == hardcode && this.poison == poison && this.wither == wither && this.frozen == frozen && this.health == health && this.absorptionsAmount == absorptionsAmount && this.maxHealth == maxHealth) {
            return false
        }

        this.hardcode = hardcode
        this.poison = poison
        this.wither = wither
        this.frozen = frozen
        this.health = health
        this.absorptionsAmount = absorptionsAmount
        this.maxHealth = maxHealth

        return true
    }

    override fun createText(): ChatComponent {
        val text = BaseComponent()

        text += TextComponent(totalHealth.round10).apply {
            color = when {
                poison -> POISON_TEXT_COLOR
                wither -> WITHER_TEXT_COLOR
                frozen -> FROZEN_TEXT_COLOR
                else -> NORMAL_TEXT_COLOR
            }
        }
        text += TextComponent("/")
        text += TextComponent(totalMaxHealth.round10).apply {
            color = when {
                absorptionsAmount > 0.0f -> ABSORPTION_TEXT_COLOR
                else -> NORMAL_TEXT_COLOR
            }
        }

        return text
    }

    override fun tick() {
        apply()
    }

    companion object {
        private val POISON_TEXT_COLOR = "#602020".asColor()
        private val WITHER_TEXT_COLOR = "#2b2b2b".asColor()
        private val FROZEN_TEXT_COLOR = "#a8f7ff".asColor()
        private val ABSORPTION_TEXT_COLOR = "#d4af37".asColor()
    }
}
