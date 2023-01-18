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

package de.bixilon.minosoft.data.registries.blocks

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactories
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.PixLyzerBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.MetaTypes
import de.bixilon.minosoft.data.registries.registries.registry.Registry

class BlockRegistry(
    parent: Registry<Block>? = null,
    flattened: Boolean = false,
) : Registry<Block>(parent = parent, codec = PixLyzerBlock, flattened = flattened, metaType = MetaTypes.BLOCK) {


    override fun deserialize(resourceLocation: ResourceLocation, data: JsonObject, registries: Registries?): Block? {
        if (registries != null) {
            BlockFactories.build(resourceLocation, registries, BlockSettings.of(data))?.let { return it }
        }
        return super.deserialize(resourceLocation, data, registries)
    }

    operator fun <T : Block> get(factory: BlockFactory<T>): T? {
        val item = this[factory.identifier] ?: return null
        return item.unsafeCast()
    }
}
