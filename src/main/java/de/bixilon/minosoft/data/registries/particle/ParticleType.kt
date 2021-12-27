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
package de.bixilon.minosoft.data.registries.particle

import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registries.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.gui.rendering.particle.DefaultParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.ParticleFactory
import de.bixilon.minosoft.gui.rendering.particle.types.Particle
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.compoundCast
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast

data class ParticleType(
    override val resourceLocation: ResourceLocation,
    val textures: List<ResourceLocation>,
    val overrideLimiter: Boolean = false,
    val factory: ParticleFactory<out Particle>? = null,
) : RegistryItem() {

    override fun toString(): String {
        return resourceLocation.full
    }

    fun default(): ParticleData {
        return ParticleData(this)
    }

    companion object : ResourceLocationDeserializer<ParticleType> {
        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): ParticleType {
            val textures: MutableList<ResourceLocation> = mutableListOf()
            data["render"]?.compoundCast()?.get("textures")?.listCast<String>()?.let {
                for (texture in it) {
                    val textureResourceLocation = ResourceLocation(texture)
                    textures += Texture.getResourceTextureIdentifier(textureResourceLocation.namespace, textureName = "particle/${textureResourceLocation.path}")
                }
            }
            val factory = DefaultParticleFactory[resourceLocation]
            return ParticleType(
                resourceLocation = resourceLocation,
                textures = textures.toList(),
                overrideLimiter = data["override_limiter"]?.toBoolean() ?: false,
                factory = factory,
            )
        }
    }
}
