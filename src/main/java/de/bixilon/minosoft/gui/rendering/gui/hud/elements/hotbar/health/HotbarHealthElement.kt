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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar.health

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.math.simple.FloatMath.rounded10
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.data.registries.effects.attributes.MinecraftAttributes
import de.bixilon.minosoft.data.registries.effects.damage.DamageEffect
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asColor
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar.AbstractHotbarHealthElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import java.lang.Float.max
import java.lang.Float.min

class HotbarHealthElement(guiRenderer: GUIRenderer) : AbstractHotbarHealthElement(guiRenderer), Pollable {

    private val atlas = HeartAtlas(guiRenderer.atlas[HeartAtlas.ATLAS])

    private var hardcode = false
    private var poison = false
    private var wither = false
    private var frozen = false

    private var health = 0.0f
    private var absorptionsAmount = 0.0f
    override var totalHealth = 0.0f

    private var maxHealth = 0.0f
    override var totalMaxHealth = 0.0f

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        if (text) {
            return super.forceRender(offset, consumer, options)
        }
        // ToDo: Damage animation, regeneration, caching, stacking
        drawCanisters(offset, consumer, options, atlas.container)


        var healthLeft = totalHealth
        var heart = 0

        while (healthLeft >= 0.5f) {
            val row = heart / HEARTS_PER_ROW
            val column = heart % HEARTS_PER_ROW


            val heartType = when {
                healthLeft <= absorptionsAmount -> atlas.absorption
                poison -> atlas.poisoned
                wither -> atlas.withered
                frozen -> atlas.frozen
                else -> atlas.normal
            }


            val halfHeart = healthLeft < 1.5f
            val image = heartType.get(!halfHeart, hardcode, false)

            AtlasImageElement(guiRenderer, image).render(offset + Vec2i(column, (rows - 1) - row) * HEART_SIZE, consumer, options)

            heart++
            healthLeft -= halfHeart.decide(1.0f, 2.0f)
        }
    }

    override fun forceSilentApply() {
        totalHealth = health + absorptionsAmount

        totalMaxHealth = maxHealth + absorptionsAmount
        super.forceSilentApply()
    }

    override fun poll(): Boolean {
        val player = guiRenderer.context.connection.player
        val hardcode = guiRenderer.context.connection.world.hardcore
        val poison = player.effects[DamageEffect.Poison] != null
        val wither = player.effects[DamageEffect.Wither] != null
        val frozen = player.ticksFrozen > 0

        val absorptionsAmount = max(0.0f, player.playerAbsorptionHearts)

        val maxHealth = max(0.0f, player.attributes[MinecraftAttributes.MAX_HEALTH].toFloat())

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

        text += TextComponent(totalHealth.rounded10).apply {
            color = when {
                poison -> POISON_TEXT_COLOR
                wither -> WITHER_TEXT_COLOR
                frozen -> FROZEN_TEXT_COLOR
                else -> NORMAL_TEXT_COLOR
            }
        }
        text += TextComponent("/")
        text += TextComponent(totalMaxHealth.rounded10).apply {
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
