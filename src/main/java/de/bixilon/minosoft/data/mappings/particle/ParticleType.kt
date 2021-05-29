/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.mappings.particle

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.particle.data.ParticleData
import de.bixilon.minosoft.data.mappings.registry.RegistryItem
import de.bixilon.minosoft.data.mappings.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.mappings.versions.Registries
import de.bixilon.minosoft.gui.rendering.particle.DefaultParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.types.Particle
import de.bixilon.minosoft.gui.rendering.textures.Texture

data class ParticleType(
    override val resourceLocation: ResourceLocation,
    val textures: List<ResourceLocation>,
    val overrideLimiter: Boolean = false,
    val factory: ParticleFactory<out Particle>? = null,
) : RegistryItem {

    override fun toString(): String {
        return resourceLocation.full
    }

    fun default(): ParticleData {
        return ParticleData(this)
    }

    companion object : ResourceLocationDeserializer<ParticleType> {
        override fun deserialize(mappings: Registries?, resourceLocation: ResourceLocation, data: JsonObject): ParticleType {
            val textures: MutableList<ResourceLocation> = mutableListOf()
            data["render"]?.asJsonObject?.get("textures")?.asJsonArray?.let {
                for (texture in it) {
                    val textureResourceLocation = ResourceLocation(texture.asString)
                    textures += Texture.getResourceTextureIdentifier(textureResourceLocation.namespace, textureName = "particle/${textureResourceLocation.path}")
                }
            }
            val factory = DefaultParticleFactory[resourceLocation]
            return ParticleType(
                resourceLocation = resourceLocation,
                textures = textures.toList(),
                overrideLimiter = data["override_limiter"]?.asBoolean ?: false,
                factory = factory,
            )
        }
    }
}
