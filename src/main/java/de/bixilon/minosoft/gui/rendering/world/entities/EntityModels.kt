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

package de.bixilon.minosoft.gui.rendering.world.entities

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJson
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.loader.ModelLoader
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.model.SkeletalModel
import de.bixilon.minosoft.gui.rendering.system.base.texture.ShaderTexture
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class EntityModels(private val loader: ModelLoader) {
    val context: RenderContext = loader.context
    private val unbakedModels: MutableMap<ResourceLocation, SkeletalModel> = mutableMapOf()
    val skeletal: MutableMap<ResourceLocation, BakedSkeletalModel> = mutableMapOf()

    @Synchronized
    fun loadUnbakedModel(path: ResourceLocation): SkeletalModel {
        return unbakedModels.getOrPut(path) { context.connection.assetsManager[path].readJson() }
    }

    @Synchronized
    fun loadModel(name: ResourceLocation, path: ResourceLocation, textureOverride: MutableMap<Int, ShaderTexture> = mutableMapOf()): BakedSkeletalModel {
        return skeletal.getOrPut(name) { loadUnbakedModel(path).bake(context, textureOverride) }
    }

    fun cleanup() {
        unbakedModels.clear()
    }

    fun loadSkeletal() {
        val latch = CountUpAndDownLatch(1)
        for (model in skeletal.values) {
            latch.inc()
            DefaultThreadPool += { model.preload(context); latch.dec() }
        }
        latch.dec()
        latch.await()

        for (model in skeletal.values) {
            model.load()
        }
    }


    fun load(latch: CountUpAndDownLatch) {
        Log.log(LogMessageType.VERSION_LOADING, LogLevels.VERBOSE) { "Loading entity models..." }
        val innerLatch = CountUpAndDownLatch(DefaultEntityModels.MODELS.size, latch)

        for (register in DefaultEntityModels.MODELS) {
            DefaultThreadPool += { register.register(context, loader); innerLatch.dec() }
        }
        innerLatch.await()
    }
}
