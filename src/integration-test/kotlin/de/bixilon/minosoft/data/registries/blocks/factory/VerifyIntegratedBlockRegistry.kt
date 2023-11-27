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

package de.bixilon.minosoft.data.registries.blocks.factory

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EmptyCollisionContext
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.PropertyBlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.climbing.ScaffoldingBlock
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.fluid.water.BubbleColumnBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.PixLyzerBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.snow.PowderSnowBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.offset.OffsetBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.fixed.FixedCollidable
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.registries.PixLyzerUtil
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.test.IT.NULL_CONNECTION
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log

object VerifyIntegratedBlockRegistry {
    private val connection = createConnection()


    private fun StringBuilder.appendBlock(block: Block) {
        append("\n")
        append(block.identifier)
        append(": ")
    }

    private fun StringBuilder.appendState(pixlyzer: BlockState, integrated: BlockState) {
        appendBlock(pixlyzer.block)
        if (pixlyzer is PropertyBlockState && pixlyzer.properties.isNotEmpty()) {
            append("p=")
            append(pixlyzer.properties)
            append(": ")
        }
        if (integrated is PropertyBlockState && integrated.properties.isNotEmpty() && (pixlyzer !is PropertyBlockState || pixlyzer.properties == integrated.properties)) {
            append("i=")
            append(integrated.properties)
            append(": ")
        }
    }

    private fun compareCollisionShape(pixlyzer: BlockState, integrated: BlockState, errors: StringBuilder) {
        if (integrated.block is OutlinedBlock && integrated.block !is FixedCollidable) return // not checkable without context

        val expected = if (pixlyzer.block is CollidableBlock) pixlyzer.block.unsafeCast<CollidableBlock>().getCollisionShape(NULL_CONNECTION, EmptyCollisionContext, Vec3i.EMPTY, pixlyzer, null) else null
        val actual = if (integrated.block is CollidableBlock) integrated.block.unsafeCast<CollidableBlock>().getCollisionShape(NULL_CONNECTION, EmptyCollisionContext, Vec3i.EMPTY, pixlyzer, null) else null

        if (expected == actual) {
            return
        }
        errors.appendState(pixlyzer, integrated)
        errors.append("collision: e=")
        errors.append(expected)
        errors.append(", a=")
        errors.append(actual)
    }

    private fun compareOutlineShape(pixlyzer: BlockState, integrated: BlockState, errors: StringBuilder) {
        if (integrated.block is ScaffoldingBlock) return
        if (integrated.block is OffsetBlock) return // Don't compare, pixlyzer is probably wrong

        val expected = if (pixlyzer.block is OutlinedBlock) pixlyzer.block.unsafeCast<OutlinedBlock>().getOutlineShape(connection, Vec3i.EMPTY, pixlyzer) else null
        val actual = if (integrated.block is OutlinedBlock) integrated.block.unsafeCast<OutlinedBlock>().getOutlineShape(connection, Vec3i.EMPTY, pixlyzer) else null

        if (expected == actual) {
            return
        }
        errors.appendState(pixlyzer, integrated)
        errors.append("outline: e=")
        errors.append(expected)
        errors.append(", a=")
        errors.append(actual)
    }

    private fun compareHardness(pixlyzer: Block, integrated: Block, errors: StringBuilder) {
        if (pixlyzer.hardness == integrated.hardness) {
            return
        }

        errors.appendBlock(pixlyzer)
        errors.append("hardness: e=")
        errors.append(pixlyzer.hardness)
        errors.append(", a=")
        errors.append(integrated.hardness)
    }

    private fun compareItem(pixlyzer: Block, integrated: Block, errors: StringBuilder) {
        if (integrated is PowderSnowBlock) return

        val item = pixlyzer.nullCast<BlockWithItem<*>>()?.item
        val integratedItem = integrated.nullCast<BlockWithItem<*>>()?.item
        if (item == integratedItem) {
            return
        }

        errors.appendBlock(pixlyzer)
        errors.append("item: e=")
        errors.append(item)
        errors.append(", a=")
        errors.append(integratedItem)
    }

    private fun compare(pixlyzer: PixLyzerBlock, integrated: Block, errors: StringBuilder) {
        compareHardness(pixlyzer, integrated, errors)
        compareItem(pixlyzer, integrated, errors)
        for (state in pixlyzer.states) {
            val integratedState = if (state is PropertyBlockState) integrated.states.withProperties(state.properties) else integrated.states.default

            compareCollisionShape(state, integratedState, errors)
            compareOutlineShape(state, integratedState, errors)
        }
    }

    fun verify(registries: Registries, version: Version) {
        val error = StringBuilder()
        val data = PixLyzerUtil.loadPixlyzerData(ResourcesProfile(), version)["blocks"]!!.unsafeCast<Map<String, JsonObject>>()

        for ((id, value) in data) {
            if (value["class"] == "AirBlock") {
                continue
            }
            val identifier = id.toResourceLocation()
            val integrated = registries.block[identifier] ?: Broken("Block $id does not exist in the registry?")
            if (integrated is PixLyzerBlock) {
                // useless to compare
                continue
            }
            if (integrated is FluidBlock || integrated is BubbleColumnBlock) {
                // they work different in minosoft
                continue
            }
            val parsed = PixLyzerBlock.deserialize(registries, identifier, value).unsafeCast<PixLyzerBlock>()
            registries.block.flattened(parsed, integrated.properties, value, registries, version, false)
            parsed.postInit(registries)
            parsed.inject(registries)


            compare(parsed, integrated, error)
        }


        if (error.isEmpty()) {
            return
        }
        error.removePrefix("\n")
        Log.ERROR_PRINT_STREAM.println(error)
        throw AssertionError("Does not match, see above!")
    }
}
