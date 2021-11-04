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

package de.bixilon.minosoft.gui.rendering.models.unbaked.block

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.gui.rendering.models.unbaked.GenericUnbakedModel
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedModel
import de.bixilon.minosoft.util.KUtil.unsafeCast

interface RootModel {

    fun getModelForState(blockState: BlockState): UnbakedModel

    companion object {
        operator fun invoke(models: Map<ResourceLocation, GenericUnbakedModel>, data: Map<String, Any>): RootModel? {
            val variants = data["variants"]
            val multipart = data["multipart"]
            return when {
                // ToDo: Single?
                variants != null -> SimpleRootModel(models, variants.unsafeCast())
                // ToDo: multipart != null -> MultipartUnbakedBlockStateModel(models, multipart.unsafeCast())
                multipart != null -> null
                else -> TODO("Don't know what type of block state model to choose: $data")
            }
        }
    }
}
