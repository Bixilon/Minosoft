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

package de.bixilon.minosoft.data.registries.blocks.types.air

import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries

@Deprecated("air == null")
@Suppress("DEPRECATION")
abstract class AirBlock(identifier: ResourceLocation, settings: BlockSettings) : Block(identifier, settings) {
    override val hardness: Float get() = Broken("Its air!")


    @Deprecated("air == null")
    open class Air(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AirBlock(identifier, settings) {

        companion object : BlockFactory<Air> {
            override val identifier = minecraft("air")

            override fun build(registries: Registries, settings: BlockSettings) = Air(settings = settings)
        }
    }

    @Deprecated("air == null")
    open class VoidAir(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AirBlock(identifier, settings) {

        companion object : BlockFactory<VoidAir> {
            override val identifier = minecraft("void_air")

            override fun build(registries: Registries, settings: BlockSettings) = VoidAir(settings = settings)
        }
    }

    @Deprecated("air == null")
    open class CaveAir(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : AirBlock(identifier, settings) {

        companion object : BlockFactory<CaveAir> {
            override val identifier = minecraft("cave_air")

            override fun build(registries: Registries, settings: BlockSettings) = CaveAir(settings = settings)
        }
    }
}
