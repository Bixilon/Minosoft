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
package de.bixilon.minosoft.data.mappings.fluid

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.items.Item
import de.bixilon.minosoft.data.mappings.particle.Particle
import de.bixilon.minosoft.data.mappings.registry.RegistryItem
import de.bixilon.minosoft.data.mappings.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.mappings.versions.VersionMapping

data class Fluid(
    override val resourceLocation: ResourceLocation,
    private val bucketItemId: Int?,
    val dripParticle: Particle?,
    val renderTexture: ResourceLocation?,
) : RegistryItem {
    var bucketItem: Item? = null
        private set

    override fun toString(): String {
        return resourceLocation.full
    }

    override fun postInit(versionMapping: VersionMapping) {
        bucketItem = bucketItemId?.let { versionMapping.itemRegistry.get(it) }
    }

    companion object : ResourceLocationDeserializer<Fluid> {
        override fun deserialize(mappings: VersionMapping?, resourceLocation: ResourceLocation, data: JsonObject): Fluid {
            check(mappings != null) { "VersionMapping is null!" }
            return Fluid(
                resourceLocation = resourceLocation,
                bucketItemId = data["bucket"]?.asInt,
                dripParticle = data["drip_particle_type"]?.asInt?.let { mappings.particleRegistry.get(it) },
                renderTexture = data["render"]?.asJsonObject?.get("texture")?.asString?.let { ResourceLocation(it) },
            )
        }
    }
}
