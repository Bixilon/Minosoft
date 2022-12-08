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

package de.bixilon.minosoft.gui.rendering.models.unbaked.block

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.gui.rendering.models.ModelLoader
import de.bixilon.minosoft.gui.rendering.models.unbaked.UnbakedModel

interface RootModel {

    fun getModelForState(blockState: BlockState): UnbakedModel

    companion object {
        operator fun invoke(modeLoader: ModelLoader, data: Map<String, Any>): RootModel {
            data["variants"]?.let { return SimpleRootModel(modeLoader, it.unsafeCast()) }
            data["multipart"]?.let { return MultipartRootModel(modeLoader, it.unsafeCast()) }
            TODO("Don't know what type of block state model to choose: $data")
        }
    }
}
