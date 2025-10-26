/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.models.loader

import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.map.SynchronizedMap
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJson
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.identified.ResourceLocationUtil.extend
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.mesh.SkeletalMesh
import de.bixilon.minosoft.gui.rendering.skeletal.mesh.SkeletalMeshBuilder
import de.bixilon.minosoft.gui.rendering.skeletal.model.SkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.model.textures.SkeletalTextureMap
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class SkeletalLoader(private val loader: ModelLoader) {
    private val registered: SynchronizedMap<ResourceLocation, RegisteredModel> = synchronizedMapOf()
    private val baked: MutableMap<ResourceLocation, BakedSkeletalModel> = HashMap()
    private var loaded = false

    fun load(latch: AbstractLatch?) {
        if (loaded) throw IllegalStateException("Already loaded!")
        val templates: MutableMap<ResourceLocation, SkeletalModel> = HashMap(this.registered.size, 0.1f)

        for ((_, registered) in this.registered) {
            var template = templates[registered.template]
            if (template == null) {
                template = loader.context.session.assetsManager.getOrNull(registered.template)?.readJson()
                if (template == null) {
                    Log.log(LogMessageType.LOADING, LogLevels.WARN) { "Can not find skeletal model ${registered.template}" }
                    continue
                }
                templates[registered.template] = template
            }

            template.load(loader.context, registered.override.keys)
            registered.model = template
        }
        loaded = true
    }

    fun bake(latch: AbstractLatch?) {
        for ((name, registered) in this.registered) {
            val baked = registered.model?.bake(registered.override, registered.mesh.buildMesh(loader.context)) ?: continue
            this.baked[name] = baked
        }
    }

    fun upload() {
        for ((_, baked) in this.baked) {
            baked.load()
        }
    }

    fun cleanup() {
        this::registered.forceSet(null)
    }

    operator fun get(name: ResourceLocation): BakedSkeletalModel? {
        return this.baked[name]
    }

    fun register(name: ResourceLocation, template: ResourceLocation = name, override: SkeletalTextureMap = emptyMap(), mesh: SkeletalMeshBuilder = SkeletalMesh) {
        if (loaded) throw IllegalStateException("Already loaded!")
        val previous = this.registered.put(name, RegisteredModel(template, override, mesh))
        if (previous != null) throw IllegalArgumentException("A model with the name $name was already registered!")
    }

    fun unload() {
        for ((_, baked) in this.baked) {
            baked.unload()
        }
    }

    private data class RegisteredModel(
        val template: ResourceLocation,
        val override: SkeletalTextureMap,
        var mesh: SkeletalMeshBuilder,
        var model: SkeletalModel? = null,
    )

    companion object {

        fun ResourceLocation.sModel(): ResourceLocation {
            return this.extend(prefix = "models/", suffix = ".smodel")
        }
    }
}
