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

package de.bixilon.minosoft.gui.rendering.models

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedMap
import de.bixilon.kutil.collections.map.SynchronizedMap
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.assets.util.FileUtil.readJsonObject
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.fluid.fluids.Fluid
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.models.builtin.BuiltinModels
import de.bixilon.minosoft.gui.rendering.models.unbaked.GenericUnbakedModel
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedBlockModel
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedItemModel
import de.bixilon.minosoft.gui.rendering.models.unbaked.block.RootModel
import de.bixilon.minosoft.gui.rendering.world.entities.DefaultEntityModels
import de.bixilon.minosoft.gui.rendering.world.entities.EntityModels
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ModelLoader(
    val renderWindow: RenderWindow,
) {
    private val assetsManager = renderWindow.connection.assetsManager
    private val unbakedBlockModels: SynchronizedMap<ResourceLocation, GenericUnbakedModel> = BuiltinModels.BUILTIN_MODELS.toSynchronizedMap()
    val entities = EntityModels(renderWindow)

    private val registry: Registries = renderWindow.connection.registries


    private fun cleanup() {
        unbakedBlockModels.clear()
    }

    private fun loadBlockStates(block: Block) {
        val blockStateJson = assetsManager[block.resourceLocation.blockState()].readJsonObject()

        val model = RootModel(this, blockStateJson)


        for (state in block.states) {
            state.blockModel = model.getModelForState(state).bake(renderWindow).unsafeCast()
        }
    }

    fun loadBlockModel(name: ResourceLocation): GenericUnbakedModel {
        unbakedBlockModels[name]?.let { return it.unsafeCast() }
        val data = assetsManager[name.model()].readJsonObject()

        val parent = data["parent"]?.toResourceLocation()?.let { loadBlockModel(it) }

        val model = UnbakedBlockModel(parent, data)

        unbakedBlockModels[name] = model
        return model
    }

    private fun loadFluid(fluid: Fluid) {
        if (fluid.model != null) {
            return
        }
        val model = fluid.createModel() ?: return
        model.load(renderWindow)
        fluid.model = model
    }

    fun loadItem(item: Item) {
        val model = loadItemModel(item.resourceLocation.prefix("item/"))

        item.model = model.bake(renderWindow).unsafeCast()
    }

    fun loadItemModel(name: ResourceLocation): GenericUnbakedModel {
        unbakedBlockModels[name]?.let { return it.unsafeCast() }
        val data = assetsManager[name.model()].readJsonObject()

        val parent = data["parent"]?.toResourceLocation()?.let { loadItemModel(it) }

        val model = UnbakedItemModel(parent, data)

        unbakedBlockModels[name] = model
        return model
    }

    private fun loadBlockModels(latch: CountUpAndDownLatch) {
        val blockLatch = CountUpAndDownLatch(1, latch)
        // ToDo: Optimize performance
        Log.log(LogMessageType.VERSION_LOADING, LogLevels.VERBOSE) { "Loading block models..." }

        for (block in registry.blockRegistry) {
            blockLatch.inc()
            DefaultThreadPool += { loadBlockStates(block); blockLatch.dec() }
        }
        blockLatch.dec()
        blockLatch.await()
    }

    private fun loadFluidModels(latch: CountUpAndDownLatch) {
        val blockLatch = CountUpAndDownLatch(1, latch)
        // ToDo: Optimize performance
        Log.log(LogMessageType.VERSION_LOADING, LogLevels.VERBOSE) { "Loading fluid models..." }

        for (fluid in registry.fluidRegistry) {
            blockLatch.inc()
            DefaultThreadPool += { loadFluid(fluid); blockLatch.dec() }
        }
        blockLatch.dec()
        blockLatch.await()
    }

    private fun loadItemModels(latch: CountUpAndDownLatch) {
        Log.log(LogMessageType.VERSION_LOADING, LogLevels.VERBOSE) { "Loading item models..." }
        val itemLatch = CountUpAndDownLatch(1, latch)


        for (item in registry.itemRegistry) {
            itemLatch.inc()
            DefaultThreadPool += { loadItem(item); itemLatch.dec() }
        }
        itemLatch.dec()
        itemLatch.await()
    }

    private fun loadEntityModels(latch: CountUpAndDownLatch) {
        Log.log(LogMessageType.VERSION_LOADING, LogLevels.VERBOSE) { "Loading entity models..." }
        val innerLatch = CountUpAndDownLatch(DefaultEntityModels.MODELS.size, latch)

        for (register in DefaultEntityModels.MODELS) {
            DefaultThreadPool += { register.register(renderWindow, this); innerLatch.dec() }
        }
        innerLatch.await()
    }

    fun load(latch: CountUpAndDownLatch) {
        loadBlockModels(latch)
        loadFluidModels(latch)
        loadItemModels(latch)
        loadEntityModels(latch)

        Log.log(LogMessageType.VERSION_LOADING, LogLevels.VERBOSE) { "Done loading models!" }

        cleanup()
    }

    companion object {

        fun ResourceLocation.model(): ResourceLocation {
            return ResourceLocation(this.namespace, "models/" + this.path + ".json")
        }

        fun ResourceLocation.blockState(): ResourceLocation {
            return ResourceLocation(this.namespace, "blockstates/" + this.path + ".json")
        }

        fun ResourceLocation.bbModel(): ResourceLocation {
            return ResourceLocation(this.namespace, "models/" + this.path + ".bbmodel")
        }
    }
}
