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

package de.bixilon.minosoft.gui.rendering.models.raw.block.state

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.minosoft.gui.rendering.models.loader.BlockLoader
import de.bixilon.minosoft.gui.rendering.models.raw.block.state.condition.ConditionBlockModel
import de.bixilon.minosoft.gui.rendering.models.raw.block.state.variant.VariantBlockModel

interface DirectBlockModel {

    companion object {

        fun deserialize(loader: BlockLoader, data: JsonObject): DirectBlockModel? {
            data["variants"]?.toJsonObject()?.let { return VariantBlockModel.deserialize(loader, it) }
            data["multipart"]?.toJsonObject()?.let { return ConditionBlockModel.deserialize(loader, it) }

            return null
        }
    }
}
