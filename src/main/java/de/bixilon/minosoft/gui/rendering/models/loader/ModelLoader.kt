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
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.entities.EntityModels
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ModelLoader(
    val context: RenderContext,
) {
    val fluids = FluidModelLoader(this)
    val entities = EntityModels(this)
    val block = BlockLoader(this)
    val item = ItemLoader(this)


    fun load(latch: AbstractLatch) {
        fluids.load(latch)
        entities.load(latch)
        block.load(latch)
        item.load(latch)

        Log.log(LogMessageType.LOADING, LogLevels.VERBOSE) { "Loading all models!" }
    }

    fun bake(latch: AbstractLatch) {
        block.bake(latch)
        item.bake(latch)
        entities.bake()

        Log.log(LogMessageType.LOADING, LogLevels.VERBOSE) { "Baked models!" }
    }

    companion object {

        fun ResourceLocation.model(prefix: String? = null): ResourceLocation {
            var path = this.path
            if (prefix != null && !path.startsWith(prefix)) {
                path = prefix + path
            }
            return ResourceLocation(this.namespace, "models/$path.json")
        }

        fun ResourceLocation.bbModel(): ResourceLocation {
            return ResourceLocation(this.namespace, "models/" + this.path + ".bbmodel")
        }
    }
}
