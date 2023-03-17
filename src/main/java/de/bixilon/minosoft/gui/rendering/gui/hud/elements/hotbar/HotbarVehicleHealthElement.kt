/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.math.simple.FloatMath.rounded10
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.registries.effects.attributes.MinecraftAttributes
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asColor
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import java.lang.Float.max

class HotbarVehicleHealthElement(guiRenderer: GUIRenderer) : AbstractHotbarHealthElement(guiRenderer), Pollable {
    private val atlasManager = guiRenderer.atlasManager

    /**
     *  [full|half]
     */
    private val hearts = arrayOf(
        atlasManager["minecraft:vehicle_heart"],
        atlasManager["minecraft:vehicle_half_heart"],
    )
    private val vehicleHeartContainer = atlasManager["minecraft:vehicle_heart_container"]!!

    private var shown = false
    override var totalHealth = 0.0f
    override var totalMaxHealth = 0.0f

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        if (!shown) {
            return
        }
        if (text) {
            return super.forceRender(offset, consumer, options)
        }
        drawCanisters(offset, consumer, options, vehicleHeartContainer)

        var healthLeft = totalHealth
        var heart = 0

        while (healthLeft >= 0.5f) {
            val row = heart / HEARTS_PER_ROW
            val column = heart % HEARTS_PER_ROW


            val halfHeart = healthLeft < 1.5f
            val image = hearts[when {
                halfHeart -> 1
                else -> 0
            }]?.let { AtlasImageElement(guiRenderer, it) }

            image?.render(offset + Vec2i(column, (rows - 1) - row) * HEART_SIZE, consumer, options)

            heart++
            healthLeft -= halfHeart.decide(1.0f, 2.0f)
        }
    }

    override fun poll(): Boolean {
        val riddenEntity = guiRenderer.context.connection.player.attachment.vehicle
        if (riddenEntity == null || riddenEntity !is LivingEntity) {
            if (this.shown) {
                totalHealth = 0.0f
                totalMaxHealth = 0.0f
                this.shown = false
                return true
            }
            return false
        }

        val health = riddenEntity.health.toFloat()
        val maxHealth = max(0.0f, riddenEntity.attributes[MinecraftAttributes.MAX_HEALTH].toFloat())

        if (health == this.totalHealth && this.totalMaxHealth == maxHealth) {
            return false
        }
        this.totalHealth = health
        this.totalMaxHealth = maxHealth

        return true
    }

    override fun createText(): ChatComponent {
        return TextComponent("${totalHealth.rounded10} / ${totalMaxHealth.rounded10}").color(NORMAL_TEXT_COLOR)
    }

    override fun tick() {
        apply()
    }

    companion object {
        private val NORMAL_TEXT_COLOR = "#da662c".asColor()
    }
}
