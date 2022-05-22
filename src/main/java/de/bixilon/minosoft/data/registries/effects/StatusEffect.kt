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
package de.bixilon.minosoft.data.registries.effects

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.minosoft.data.language.Translatable
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.effects.attributes.EntityAttributeModifier
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registries.registry.codec.ResourceLocationCodec
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.text.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.datafixer.EntityAttributeFixer.fix
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.util.*

data class StatusEffect(
    override val resourceLocation: ResourceLocation,
    val category: StatusEffectCategories?,
    override val translationKey: ResourceLocation?,
    val color: RGBColor,
    val attributes: Map<ResourceLocation, EntityAttributeModifier>,
    val uuidAttributes: Map<UUID, EntityAttributeModifier>,
) : RegistryItem(), Translatable {

    override fun toString(): String {
        return resourceLocation.full
    }

    companion object : ResourceLocationCodec<StatusEffect> {
        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): StatusEffect {
            val attributes: MutableMap<ResourceLocation, EntityAttributeModifier> = mutableMapOf()
            val uuidAttributes: MutableMap<UUID, EntityAttributeModifier> = mutableMapOf()

            data["attributes"]?.toJsonObject()?.let {
                for ((key, value) in it) {
                    val attribute = EntityAttributeModifier.deserialize(value.asJsonObject())
                    attributes[ResourceLocation.getResourceLocation(key).fix()] = attribute
                    uuidAttributes[attribute.uuid] = attribute
                }
            }

            return StatusEffect(
                resourceLocation = resourceLocation,
                category = data["category"]?.unsafeCast<String>()?.let { return@let StatusEffectCategories[it] },
                translationKey = data["translation_key"]?.toResourceLocation(),
                color = data["color"].unsafeCast<Int>().asRGBColor(),
                attributes = attributes.toMap(),
                uuidAttributes = uuidAttributes.toMap(),
            )
        }
    }
}
