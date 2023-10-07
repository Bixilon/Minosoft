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
package de.bixilon.minosoft.data.registries.effects

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.effects.properties.categories.CategorizedEffect
import de.bixilon.minosoft.data.registries.effects.properties.categories.StatusEffectCategories
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.codec.ResourceLocationCodec
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.Colored
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asRGBColor

class PixlyzerStatusEffectType(
    override val identifier: ResourceLocation,
    override val color: RGBColor,
    override val category: StatusEffectCategories,
) : StatusEffectType(), Colored, CategorizedEffect {

    override fun toString(): String {
        return identifier.toString()
    }

    companion object : ResourceLocationCodec<StatusEffectType> {

        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): PixlyzerStatusEffectType {

            return PixlyzerStatusEffectType(
                identifier = resourceLocation,
                category = data["category"]?.unsafeCast<String>()?.let { return@let StatusEffectCategories[it] } ?: StatusEffectCategories.NEUTRAL,
                color = data["color"]?.toInt()?.asRGBColor() ?: ChatColors.WHITE,
            )
        }
    }
}
