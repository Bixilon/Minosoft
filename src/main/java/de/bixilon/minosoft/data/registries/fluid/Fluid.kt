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
package de.bixilon.minosoft.data.registries.fluid

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.fluid.lava.FlowingLavaFluid
import de.bixilon.minosoft.data.registries.fluid.lava.StillLavaFluid
import de.bixilon.minosoft.data.registries.fluid.water.FlowingWaterFluid
import de.bixilon.minosoft.data.registries.fluid.water.StillWaterFluid
import de.bixilon.minosoft.data.registries.items.Item
import de.bixilon.minosoft.data.registries.particle.ParticleType
import de.bixilon.minosoft.data.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.registries.versions.Registries

open class Fluid(
    override val resourceLocation: ResourceLocation,
    registries: Registries,
    data: JsonObject,
) : RegistryItem {
    private val bucketItemId = data["bucket"]?.asInt
    val dripParticle: ParticleType? = data["drip_particle_type"]?.asInt?.let { registries.particleTypeRegistry[it] }
    val renderTexture: ResourceLocation? = data["render"]?.asJsonObject?.get("texture")?.asString?.let { ResourceLocation(it) }
    var bucketItem: Item? = null
        private set

    override fun toString(): String {
        return resourceLocation.full
    }

    override fun postInit(registries: Registries) {
        bucketItem = bucketItemId?.let { registries.itemRegistry[it] }
    }

    companion object : ResourceLocationDeserializer<Fluid> {
        private val CONSTRUCTORS: Map<String, (resourceLocation: ResourceLocation, registries: Registries, data: JsonObject) -> Fluid> = mapOf(
            "EmptyFluid" to { resourceLocation, registries, data -> EmptyFluid(resourceLocation, registries, data) },
            "WaterFluid\$Flowing" to { resourceLocation, registries, data -> FlowingWaterFluid(resourceLocation, registries, data) },
            "WaterFluid\$Still" to { resourceLocation, registries, data -> StillWaterFluid(resourceLocation, registries, data) },
            "LavaFluid\$Flowing" to { resourceLocation, registries, data -> FlowingLavaFluid(resourceLocation, registries, data) },
            "LavaFluid\$Still" to { resourceLocation, registries, data -> StillLavaFluid(resourceLocation, registries, data) },
        )

        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: JsonObject): Fluid {
            check(registries != null) { "Registries is null!" }
            CONSTRUCTORS[data["class"]?.asString]?.let {
                return it.invoke(resourceLocation, registries, data)
            }

            return Fluid(resourceLocation, registries, data)
        }
    }
}
