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

package de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity

import de.bixilon.minosoft.data.entities.block.BedBlockEntity
import de.bixilon.minosoft.data.registries.blocks.factory.PixLyzerBlockFactory
import de.bixilon.minosoft.data.registries.blocks.handler.entity.landing.BouncingHandler
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries

open class BedBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : BlockWithEntity<BedBlockEntity>(resourceLocation, registries, data), BouncingHandler {
    override val bounceStrength: Double get() = 0.66f.toDouble()

    companion object : PixLyzerBlockFactory<BedBlock> {

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): BedBlock {
            return BedBlock(resourceLocation, registries, data)
        }
    }
}

