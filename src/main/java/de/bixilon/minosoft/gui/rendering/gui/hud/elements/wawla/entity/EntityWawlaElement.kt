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

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.math.simple.DoubleMath.rounded10
import de.bixilon.minosoft.camera.target.targets.EntityTarget
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.wawla.EntityWawlaProvider
import de.bixilon.minosoft.data.registries.effects.attributes.MinecraftAttributes
import de.bixilon.minosoft.data.registries.identified.Namespaces
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponentUtil.removeTrailingNewlines
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.wawla.WawlaElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.wawla.WawlaHUDElement

class EntityWawlaElement(wawla: WawlaHUDElement, private val target: EntityTarget) : WawlaElement(wawla) {
    override val elements: List<Element?> = listOf(
        createName(),
        createBaseInformation(),
        createAdditionalInformation(),
        createIdentifierElement(target.entity.type),
        createMod(),
    )

    init {
        forceSilentApply()
    }


    private fun createName(): TextElement {
        if (target.entity is PlayerEntity) {
            val name = target.entity.additional.tabDisplayName
            if (name.length > 0) {
                name.setFallbackColor(ChatColors.WHITE)
                return TextElement(guiRenderer, name, background = null, properties = TextRenderProperties(scale = 1.2f))
            }
        }
        return createNameElement(target.entity.type.translationKey)
    }

    private fun createMod(): TextElement? {
        val namespace = target.entity.type.identifier.namespace
        if (namespace == Namespaces.DEFAULT) {
            return null
        }
        return TextElement(guiRenderer, TextComponent(namespace).color(ChatColors.BLUE), background = null)
    }

    private fun createBaseInformation(): TextElement? {
        val entity = target.entity
        val component = BaseComponent()
        if (entity is LivingEntity && wawla.profile.entity.health) {
            component += TextComponent("Health: ${entity.health.rounded10}/${java.lang.Float.max(0.0f, entity.attributes[MinecraftAttributes.MAX_HEALTH].toFloat() + entity.absorptionHearts)}")
            component += "\n"
        }
        val hand = entity.nullCast<LivingEntity>()?.equipment?.get(EquipmentSlots.MAIN_HAND)
        if (wawla.profile.entity.hand && hand != null) {
            component += TextComponent("Hand: ${hand.item.count}x ${hand.item.item}")
            component += "\n"
        }

        component.removeTrailingNewlines()
        if (component.length == 0) return null

        component.setFallbackColor(ChatColors.GRAY)

        return TextElement(guiRenderer, component, background = null)
    }

    private fun createAdditionalInformation(): TextElement? {
        if (target.entity !is EntityWawlaProvider) return null

        val text = target.entity.getWawlaInformation(context.connection, target)

        if (text is BaseComponent) text.removeTrailingNewlines()
        if (text.length == 0) return null // isEmpty

        text.setFallbackColor(ChatColors.GRAY)

        return TextElement(guiRenderer, text, background = null)
    }
}
