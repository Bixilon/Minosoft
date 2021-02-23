/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.mappings.tweaker

import de.bixilon.minosoft.data.entities.EntityMetaData
import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.animal.horse.*
import de.bixilon.minosoft.data.entities.entities.monster.*
import de.bixilon.minosoft.data.entities.entities.vehicle.*
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.world.*
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions

object VersionTweaker {
    // some data was packed in mata data in early versions (1.8). This function converts it to the real resource location
    @JvmStatic
    fun getRealEntityClass(fakeClass: Class<out Entity>, metaData: EntityMetaData, versionId: Int): Class<out Entity> {
        if (versionId > ProtocolVersions.V_1_8_9) { // ToDo: No clue here
            return fakeClass
        }
        when (fakeClass) {
            ZombiePigman::class.java -> {
                return ZombifiedPiglin::class.java
            }
            Zombie::class.java -> {
                if (metaData.sets.getInt(EntityMetaDataFields.ZOMBIE_SPECIAL_TYPE) == 1) {
                    return ZombieVillager::class.java
                }
            }
            Skeleton::class.java -> {
                if (metaData.sets.getInt(EntityMetaDataFields.LEGACY_SKELETON_TYPE) == 1) {
                    return WitherSkeleton::class.java
                }
            }
            Guardian::class.java -> {
                if (metaData.sets.getBitMask(EntityMetaDataFields.LEGACY_GUARDIAN_FLAGS, 0x02)) {
                    return ElderGuardian::class.java
                }
            }
            Horse::class.java -> {
                return when (metaData.sets.getByte(EntityMetaDataFields.LEGACY_HORSE_SPECIAL_TYPE).toInt()) {
                    1 -> Donkey::class.java
                    2 -> Mule::class.java
                    3 -> ZombieHorse::class.java
                    4 -> SkeletonHorse::class.java
                    else -> fakeClass
                }
            }
        }
        return fakeClass
    }

    @JvmStatic
    fun getRealEntityObjectClass(fakeClass: Class<out Entity>, data: Int, versionId: Int): Class<out Entity> {
        if (versionId > ProtocolVersions.V_1_8_9) { // ToDo: No clue here
            return fakeClass
        }
        when (fakeClass) {
            Minecart::class.java -> {
                return when (data) {
                    1 -> MinecartChest::class.java
                    2 -> MinecartFurnace::class.java
                    3 -> MinecartTNT::class.java
                    4 -> MinecartSpawner::class.java
                    5 -> MinecartHopper::class.java
                    6 -> MinecartCommandBlock::class.java
                    else -> fakeClass
                }
            }
        }
        return fakeClass
    }

    @JvmStatic
    fun transformChunk(chunk: Chunk, versionId: Int): Chunk {
        // some blocks need to be tweaked. eg. Grass with a snow block on top becomes snowy grass block
        if (versionId >= ProtocolDefinition.FLATTING_VERSION_ID) {
            return chunk
        }
        for ((sectionHeight, section) in chunk.sections) {
            for ((location, blockInfo) in section.blocks) {
                val newBlock = transformBlock(blockInfo.block, chunk, location, sectionHeight)
                if (newBlock === blockInfo.block) {
                    continue
                }
                if (newBlock == null) {
                    section.setBlockInfo(location, null)
                    continue
                }
                section.setBlockInfo(location, BlockInfo(newBlock, blockInfo.metaData, section.blocksFloatingInfo[location] ?: BlockFloatingInfo()))
            }
        }
        return chunk
    }


    // ToDo: Broken
    @JvmStatic
    fun transformBlock(originalBlock: BlockState, chunk: Chunk, location: InChunkSectionLocation, sectionHeight: Int): BlockState? {
        when (originalBlock.owner.resourceLocation) {
            ResourceLocation("minecraft:grass") -> {
                getBlockAbove(chunk, location, sectionHeight)?.let {
                    if (it.owner.resourceLocation == TweakBlocks.SNOW_RESOURCE_LOCATION || it.owner.resourceLocation == TweakBlocks.SNOW_LAYER_RESOURCE_LOCAION) {
                        return TweakBlocks.GRASS_BLOCK_SNOWY_YES
                    }
                }
                return TweakBlocks.GRASS_BLOCK_SNOWY_NO
            }
        }
        return originalBlock
    }

    private fun getBlockAbove(chunk: Chunk, location: InChunkSectionLocation, sectionHeight: Int): BlockState? {
        val above = location.getInChunkLocation(sectionHeight)
        return chunk.getBlockInfo(InChunkLocation(above.x, above.y + 1, above.z))?.block
    }
}
