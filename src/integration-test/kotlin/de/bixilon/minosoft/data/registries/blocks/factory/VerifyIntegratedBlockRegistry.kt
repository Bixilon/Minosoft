/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
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

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.enums.inline.IntInlineSet
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.reflection.ReflectionUtil.field
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.registries.blocks.registry.codec.FlattenedBlockStateCodec
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags.toFlagSet
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.building.FenceBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.nether.SoulSand
import de.bixilon.minosoft.data.registries.blocks.types.building.plants.FernBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.plants.FlowerBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.snow.SnowLayerBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.Bedrock
import de.bixilon.minosoft.data.registries.blocks.types.climbing.ScaffoldingBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.storage.ShulkerBoxBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.storage.WoodenChestBlock
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.fluid.water.BubbleColumnBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.PixLyzerBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.SlimeBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.snow.PowderSnowBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.hardness.UnbreakableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.offset.OffsetBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.tool.properties.requirement.HandBreakable
import de.bixilon.minosoft.data.registries.item.items.tool.properties.requirement.ToolRequirement
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_19_4
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.test.ITUtil.allocate
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.RegistriesUtil.setParent
import de.bixilon.minosoft.util.logging.Log

object VerifyIntegratedBlockRegistry {
    private val SHAPES = Registries::shape.field

    private fun StringBuilder.compare(comparison: () -> Unit) {
        try {
            comparison.invoke()
        } catch (error: IntegratedBlockError) {
            this.append(error.message).appendLine()
        } catch (error: IntegratedBlockStateError) {
            this.append(error.message).appendLine()
        }
    }

    private fun compareCollisionShape(pixlyzer: BlockState, integrated: BlockState) {
        if (integrated.block is ScaffoldingBlock || integrated.block is ShulkerBoxBlock) return
        val expected = if (pixlyzer.block is CollidableBlock) pixlyzer.block.unsafeCast<CollidableBlock>().getCollisionShape(pixlyzer) else null
        val actual = if (integrated.block is CollidableBlock) integrated.block.unsafeCast<CollidableBlock>().getCollisionShape(integrated) else null

        if (expected == actual) return

        throw IntegratedBlockStateError(pixlyzer, "collision", expected, actual)
    }

    private fun compareOutlineShape(session: PlaySession, pixlyzer: BlockState, integrated: BlockState) {
        if (integrated.block is ScaffoldingBlock) return
        if (integrated.block is OffsetBlock) return // Don't compare, pixlyzer is probably wrong

        val expected = if (pixlyzer.block is OutlinedBlock) pixlyzer.block.unsafeCast<OutlinedBlock>().getOutlineShape(session, BlockPosition.EMPTY, pixlyzer) else null
        val actual = if (integrated.block is OutlinedBlock) integrated.block.unsafeCast<OutlinedBlock>().getOutlineShape(session, BlockPosition.EMPTY, pixlyzer) else null


        if (expected == actual) return

        throw IntegratedBlockStateError(pixlyzer, "outline", expected, actual)
    }

    private fun IntInlineSet.fixed(block: Block): IntInlineSet {
        var flags = this
        flags -= BlockStateFlags.TINTED
        flags -= BlockStateFlags.ENTITY
        flags -= BlockStateFlags.CUSTOM_CULLING
        flags -= BlockStateFlags.CAVE_SURFACE
        flags -= BlockStateFlags.MINOR_VISUAL_IMPACT
        flags -= BlockStateFlags.RANDOM_TICKS

        if (block is ShulkerBoxBlock) {
            flags -= BlockStateFlags.FULL_COLLISION
            flags -= BlockStateFlags.FULL_OUTLINE
        }
        if (block is FernBlock) {
            flags -= BlockStateFlags.OFFSET
        }
        if (block is SnowLayerBlock) {
            flags -= BlockStateFlags.COLLISIONS
        }
        if (block is PowderSnowBlock) {
            flags -= BlockStateFlags.COLLISIONS
        }
        if (block is SoulSand) {
            flags -= BlockStateFlags.VELOCITY // TODO: Only in <1.14.4
        }
        if (block is FlowerBlock) {
            flags -= BlockStateFlags.OFFSET
        }
        if (block is FenceBlock) {
            flags -= BlockStateFlags.FULL_COLLISION
            flags -= BlockStateFlags.FULL_OUTLINE
        }

        return flags
    }

    private fun compareFlags(pixlyzer: BlockState, integrated: BlockState) {
        val fixedIntegrated = integrated.flags.fixed(integrated.block)
        val pixlyzerFixed = pixlyzer.flags.fixed(integrated.block)
        if (fixedIntegrated == pixlyzerFixed) return

        throw IntegratedBlockStateError(pixlyzer, "flags", pixlyzerFixed.toFlagSet(), fixedIntegrated.toFlagSet())
    }

    private fun compareLightProperties(pixlyzer: BlockState, integrated: BlockState) {
        if (integrated.block is ShulkerBoxBlock || integrated.block is WoodenChestBlock<*> || integrated.block is SlimeBlock || BlockStateFlags.WATERLOGGED in integrated.flags || integrated.block is FenceBlock) return

        val lightPixlyzer = pixlyzer.block.getLightProperties(pixlyzer)
        val lightIntegrated = integrated.block.getLightProperties(integrated)

        if (lightPixlyzer == lightIntegrated) return

        throw IntegratedBlockStateError(pixlyzer, "flags", lightPixlyzer, lightIntegrated)
    }

    private fun compareHardness(pixlyzer: Block, integrated: Block) {
        if (pixlyzer.hardness == integrated.hardness || (pixlyzer.hardness < 0.0f && integrated is UnbreakableBlock)) {
            return
        }
        throw IntegratedBlockError(pixlyzer.identifier, "hardness", pixlyzer.hardness, integrated.hardness)
    }

    private fun compareItem(pixlyzer: Block, integrated: Block) {
        if (integrated is PowderSnowBlock) return

        val item = pixlyzer.nullCast<BlockWithItem<*>>()?.item
        val integratedItem = integrated.nullCast<BlockWithItem<*>>()?.item
        if (item == integratedItem) {
            return
        }

        throw IntegratedBlockError(pixlyzer.identifier, "item", item, integratedItem)
    }


    private fun compareToolRequirement(pixlyzer: Block, integrated: Block) {
        if (integrated is Bedrock) return
        val requiresTool = (integrated is ToolRequirement && integrated !is HandBreakable)
        if (pixlyzer !is PixLyzerBlock) return

        if (pixlyzer.requiresTool == requiresTool) return

        throw IntegratedBlockError(pixlyzer.identifier, "requires_tool", pixlyzer.requiresTool, requiresTool)
    }

    private fun compare(session: PlaySession, pixlyzer: PixLyzerBlock, integrated: Block, errors: StringBuilder) {
        errors.compare { compareHardness(pixlyzer, integrated) }
        errors.compare { compareItem(pixlyzer, integrated) }
        if (session.version.versionId != V_1_19_4) {
            errors.compare { compareToolRequirement(pixlyzer, integrated) }
        }
        for (state in pixlyzer.states) {
            val integratedState = try {
                integrated.states.withProperties(state.properties)
            } catch (error: IllegalArgumentException) {
                continue
            }

            errors.compare { compareCollisionShape(state, integratedState) }
            errors.compare { compareOutlineShape(session, state, integratedState) }
            errors.compare { compareFlags(state, integratedState) }
            errors.compare { compareLightProperties(state, integratedState) }
        }
    }

    private fun compareImplemented(identifier: ResourceLocation, errors: StringBuilder) {
        if (identifier.path.endsWith("_fence")) errors.append("Fence not implemented: $identifier\n")
        if (identifier.path.endsWith("_stairs")) errors.append("Stairs not implemented: $identifier\n")
        if (identifier.path.endsWith("_slab")) errors.append("Slab not implemented: $identifier\n")
        if (identifier.path.endsWith("_leaves")) errors.append("Leaves not implemented: $identifier\n")
    }

    fun verify(registries: Registries, version: Version, data: Map<String, JsonObject>) {
        val error = StringBuilder()

        val session = PlaySession::class.java.allocate()
        session::version.forceSet(version)

        for ((id, value) in data) {
            if (value["class"] == "AirBlock") {
                continue
            }
            val identifier = id.toResourceLocation()
            val integrated = registries.block[identifier] ?: Broken("Block $id does not exist in the registry?")
            if (integrated is PixLyzerBlock) {
                compareImplemented(identifier, error)
                // useless to compare
                continue
            }
            if (integrated is FluidBlock || integrated is BubbleColumnBlock) {
                // they work different in minosoft
                continue
            }
            val parsed = PixLyzerBlock.deserialize(registries, identifier, value).unsafeCast<PixLyzerBlock>()
            val fake = Registries(version = version)
            fake.setParent(registries)
            SHAPES[fake] = registries.shape

            val states = FlattenedBlockStateCodec.deserialize(parsed, BlockStateFlags.of(parsed), value, version, fake)

            Block.STATES[parsed] = states

            parsed.postInit(registries)
            parsed.inject(registries)


            compare(session, parsed, integrated, error)
        }


        if (error.isEmpty()) {
            return
        }
        error.removePrefix("\n")
        Log.ERROR_PRINT_STREAM.println(error)
        throw AssertionError("Does not match, see above!")
    }
}
