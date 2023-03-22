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

package de.bixilon.minosoft.gui.rendering.models.raw.block.state.apply

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.gui.rendering.models.raw.block.BlockModel

class BlockStateModel(
    val model: BlockModel,
    val uvLock: Boolean,
    val weight: Int,
    val x: Int,
    val y: Int,
) : BlockStateApply {

    fun deserialize(model: BlockModel, data: JsonObject): BlockStateModel {
        val uvLock = data["uvlock"]?.toBoolean() ?: false
        val weight = data["weight"]?.toInt() ?: 1
        val x = data["x"]?.toInt() ?: 0
        val y = data["y"]?.toInt() ?: 0

        return BlockStateModel(model, uvLock, weight, x, y)
    }
}
