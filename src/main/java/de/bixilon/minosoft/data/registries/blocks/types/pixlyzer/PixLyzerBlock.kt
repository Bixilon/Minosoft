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
package de.bixilon.minosoft.data.registries.blocks.types.pixlyzer

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.primitive.FloatUtil.toFloat
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.blocks.factory.PixLyzerBlockFactories
import de.bixilon.minosoft.data.registries.blocks.factory.PixLyzerBlockFactory
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.AdvancedBlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateBuilder
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.properties.FrictionBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.JumpBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.VelocityBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.offset.RandomOffsetBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.offset.RandomOffsetTypes
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.ShapedBlock
import de.bixilon.minosoft.data.registries.factory.clazz.MultiClassFactory
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.codec.ResourceLocationCodec
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

open class PixLyzerBlock(
    identifier: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : Block(identifier, BlockSettings(soundGroup = data["sound_group"]?.toInt()?.let { registries.soundGroup[it] }, item = data["item"]?.toInt())), FrictionBlock, JumpBlock, VelocityBlock, RandomOffsetBlock, ShapedBlock, BlockStateBuilder {

    override val randomOffset: RandomOffsetTypes? = data["offset_type"].nullCast<String>()?.let { RandomOffsetTypes[it] }

    override val friction = data["friction"]?.toFloat() ?: 0.6f

    override val velocity = data["velocity_multiplier"]?.toFloat() ?: 1.0f // ToDo: They exist since ~1.15

    override val jumpBoost = data["jump_velocity_multiplier"]?.toFloat() ?: 1.0f

    override fun buildState(settings: BlockStateSettings): BlockState {
        return AdvancedBlockState(this, settings.properties ?: emptyMap(), settings.luminance, settings.collisionShape, settings.outlineShape, settings.lightProperties)
    }

    override fun toString(): String {
        return identifier.toString()
    }

    companion object : ResourceLocationCodec<Block>, PixLyzerBlockFactory<Block>, MultiClassFactory<Block> {
        override val ALIASES: Set<String> = setOf("Block")

        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): Block {
            check(registries != null) { "Registries is null!" }

            val className = data["class"].toString()
            var factory = PixLyzerBlockFactories[className]
            if (factory == null) {
                Log.log(LogMessageType.VERSION_LOADING, LogLevels.VERBOSE) { "Block for class $className not found, defaulting..." }
                factory = PixLyzerBlock
            }

            return factory.build(resourceLocation, registries, data)
        }

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): PixLyzerBlock {
            return PixLyzerBlock(resourceLocation, registries, data)
        }
    }
}
