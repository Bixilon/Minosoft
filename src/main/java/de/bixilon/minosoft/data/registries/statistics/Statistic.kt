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
package de.bixilon.minosoft.data.registries.statistics

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.language.translate.Translatable
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registries.registry.codec.IdentifierCodec
import de.bixilon.minosoft.util.KUtil.toResourceLocation

open class Statistic(
    override val identifier: ResourceLocation,
    override val translationKey: ResourceLocation?,
    val unit: StatisticUnits,
) : RegistryItem(), Translatable {

    override fun toString(): String {
        return identifier.toString()
    }

    companion object : IdentifierCodec<Statistic> {
        override fun deserialize(registries: Registries?, identifier: ResourceLocation, data: Map<String, Any>): Statistic {
            val translationKey = data["translation_id"]?.toResourceLocation()
            val unit = StatisticUnits[data["unit"]!!]!!

            data["sub_statistics"]?.unsafeCast<Set<*>>()?.let {
                val custom: MutableSet<ResourceLocation> = mutableSetOf()
                for (value in it) {
                    custom += value.toResourceLocation()
                }
                return OtherStatistic(
                    resourceLocation = identifier,
                    translationKey = translationKey,
                    unit = unit,
                    custom = custom,
                )
            }

            return Statistic(
                identifier = identifier,
                translationKey = translationKey,
                unit = unit,
            )
        }
    }
}
