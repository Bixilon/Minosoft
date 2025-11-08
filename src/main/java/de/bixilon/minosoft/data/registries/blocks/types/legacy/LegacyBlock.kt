/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.registries.blocks.types.legacy

import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.special.FullBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.models.loader.legacy.CustomModel
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.gui.rendering.tint.TintedBlock
import de.bixilon.minosoft.util.KUtil.toResourceLocation

open class LegacyBlock(
    identifier: ResourceLocation,
    settings: BlockSettings,
    model: ResourceLocation?,
) : Block(identifier, settings), FullBlock, CustomModel, TintedBlock {
    override var hardness: Float = 0.0f
    override var tintProvider: TintProvider? = null

    override val modelName: ResourceLocation? = model ?: identifier


    override fun toString(): String {
        return identifier.toString()
    }


    companion object {

        fun deserialize(identifier: ResourceLocation, settings: BlockSettings, data: JsonObject): Block {
            val model = data["model"]?.toResourceLocation()

            return LegacyBlock(identifier, settings, model)
        }
    }
}
