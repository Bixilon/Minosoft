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
import de.bixilon.kutil.time.TimeUtil.nanos
import de.bixilon.kutil.unit.UnitFormatter.formatNanos
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.identified.ResourceLocationUtil.extend
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.entities.DefaultEntityModels
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ModelLoader(
    val context: RenderContext,
) {
    val packFormat = context.connection.assetsManager.properties.pack.format
    val fluids = FluidModelLoader(this)
    val block = BlockLoader(this)
    val item = ItemLoader(this)
    val skeletal = SkeletalLoader(this)


    fun load(latch: AbstractLatch) {
        val start = nanos()

        DefaultEntityModels.load(this, latch)
        fluids.load(latch)
        block.load(latch)
        item.load(latch)
        skeletal.load(latch)

        val time = nanos() - start

        Log.log(LogMessageType.LOADING, LogLevels.VERBOSE) { "Loaded all models in ${time.formatNanos()}!" }
    }

    fun bake(latch: AbstractLatch) {
        val start = nanos()

        block.bake(latch)
        item.bake(latch)
        skeletal.bake(latch)

        val time = nanos() - start

        Log.log(LogMessageType.LOADING, LogLevels.VERBOSE) { "Baked models in ${time.formatNanos()}!" }
    }

    fun upload() {
        skeletal.upload()
    }

    fun cleanup() {
        block.cleanup()
        item.cleanup()
        skeletal.cleanup()
    }

    companion object {

        fun ResourceLocation.model(): ResourceLocation {
            return this.extend(prefix = "models/", suffix = ".json")
        }
    }
}
