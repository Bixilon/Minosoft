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

package de.bixilon.minosoft.gui.rendering.models.loader

import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJsonObject
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.legacy.CustomBlockModel
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.models.block.BlockModel
import de.bixilon.minosoft.gui.rendering.models.block.state.DirectBlockModel
import de.bixilon.minosoft.gui.rendering.models.loader.ModelLoader.Companion.model
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class BlockLoader(private val loader: ModelLoader) {
    val assets = loader.context.connection.assetsManager
    val version = loader.context.connection.version

    fun loadBlock(name: ResourceLocation): BlockModel? {
        val file = name.model("block/")
        val data = assets.getOrNull(file)?.readJsonObject() ?: return null

        val parent = data["parent"]?.toString()?.let { loadBlock(it.toResourceLocation()) }


        return BlockModel.deserialize(parent, data)
    }

    fun loadState(block: Block): DirectBlockModel? {
        val file = (if (block is CustomBlockModel) block.getModelName(version) else block.identifier)?.blockState() ?: return null
        val data = assets.getOrNull(file)?.readJsonObject() ?: return null

        return DirectBlockModel.deserialize(this, data)
    }

    fun load(latch: AbstractLatch?) {
        for (block in loader.context.connection.registries.block) {
            val model = loadState(block) ?: continue

            for (state in block.states) {
                val apply = model.choose(state) ?: continue
                state.model = apply.bake(loader.context.textures)
            }
        }
    }


    companion object {

        fun ResourceLocation.blockState(): ResourceLocation {
            return ResourceLocation(this.namespace, "blockstates/" + this.path + ".json")
        }
    }
}
