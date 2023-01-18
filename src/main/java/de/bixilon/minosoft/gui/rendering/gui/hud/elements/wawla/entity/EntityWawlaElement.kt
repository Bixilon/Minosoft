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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.wawla.entity

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.math.simple.DoubleMath.rounded10
import de.bixilon.minosoft.data.container.EquipmentSlots
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.entities.wawla.EntityWawlaProvider
import de.bixilon.minosoft.data.registries.effects.attributes.DefaultStatusEffectAttributeNames
import de.bixilon.minosoft.data.registries.identified.Namespaces
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.camera.target.targets.EntityTarget
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.wawla.WawlaElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.wawla.WawlaHUDElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions

class EntityWawlaElement(wawla: WawlaHUDElement, private val target: EntityTarget) : WawlaElement(wawla) {
    private val name = createName()
    private val base = createBaseInformation()
    private val additional = createAdditionalInformation()
    private val mod = createMod()

    init {
        forceSilentApply()
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        name.render(offset, consumer, options)
        offset.y += name.size.y

        additional?.let { it.render(offset, consumer, options); offset.y += it.size.y }

        mod?.let { it.render(offset, consumer, options); offset.y += it.size.y }
    }

    override fun forceSilentApply() {
        val size = Vec2i(
            x = maxOf(name.size.x, mod?.size?.x ?: 0, additional?.size?.x ?: 0),
            y = name.size.y + (mod?.size?.y ?: 0) + (additional?.size?.y ?: 0),
        )

        this.size = size
    }

    private fun createName(): TextElement {
        return createNameElement(target.entity.type.translationKey)
    }

    private fun createMod(): TextElement? {
        val namespace = target.entity.type.identifier.namespace
        if (namespace == Namespaces.DEFAULT) {
            return null
        }
        return TextElement(guiRenderer, TextComponent(namespace).color(ChatColors.BLUE), background = false)
    }

    private fun createBaseInformation(): TextElement? {
        val entity = target.entity
        val component = BaseComponent()
        if (entity is LivingEntity && wawla.profile.entity.health) {
            component += TextComponent("Health: ${entity.health.rounded10}/${java.lang.Float.max(0.0f, entity.getAttributeValue(DefaultStatusEffectAttributeNames.GENERIC_MAX_HEALTH).toFloat() + entity.absorptionHearts)}")
            component += "\n"
        }
        val hand = entity.equipment[EquipmentSlots.MAIN_HAND]
        if (wawla.profile.entity.hand && hand != null) {
            component += TextComponent("Hand: ${hand.item.count}x ${hand.item.item}")
            component += "\n"
        }

        if (component.length == 0) return null

        component.setFallbackColor(ChatColors.GRAY)

        return TextElement(guiRenderer, component, background = false)
    }

    private fun createAdditionalInformation(): TextElement? {
        if (target.entity !is EntityWawlaProvider) return null

        val text = target.entity.getWawlaInformation(context.connection, target)

        if (text.length == 0) return null // isEmpty

        text.setFallbackColor(ChatColors.GRAY)

        return TextElement(guiRenderer, text, background = false)
    }
}
