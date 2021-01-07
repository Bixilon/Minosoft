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

package de.bixilon.minosoft.data.mappings.tweaker;

import de.bixilon.minosoft.data.entities.EntityMetaData;
import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.entities.entities.animal.horse.*;
import de.bixilon.minosoft.data.entities.entities.monster.*;
import de.bixilon.minosoft.data.entities.entities.vehicle.*;
import de.bixilon.minosoft.data.mappings.blocks.Block;
import de.bixilon.minosoft.data.world.Chunk;
import de.bixilon.minosoft.data.world.ChunkSection;
import de.bixilon.minosoft.data.world.InChunkLocation;
import de.bixilon.minosoft.data.world.InChunkSectionLocation;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;

import java.util.Map;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_8_9;

public class VersionTweaker {
    // some data was packed in mata data in early versions (1.8). This function converts it to the real identifier
    public static Class<? extends Entity> getRealEntityClass(Class<? extends Entity> fakeClass, EntityMetaData metaData, int versionId) {
        if (fakeClass == ZombiePigman.class) {
            return ZombifiedPiglin.class;
        } else if (fakeClass == Zombie.class) {
            if (versionId > V_1_8_9) { // ToDo: No clue here
                return fakeClass;
            }
            if (metaData.getSets().getInt(EntityMetaDataFields.ZOMBIE_SPECIAL_TYPE) == 1) {
                return ZombieVillager.class;
            }
        } else if (fakeClass == Skeleton.class) {
            if (versionId > V_1_8_9) { // ToDo: No clue here
                return fakeClass;
            }
            if (metaData.getSets().getInt(EntityMetaDataFields.LEGACY_SKELETON_TYPE) == 1) {
                return WitherSkeleton.class;
            }
        } else if (fakeClass == Guardian.class) {
            if (versionId > V_1_8_9) { // ToDo: No clue here
                return fakeClass;
            }
            if (metaData.getSets().getBitMask(EntityMetaDataFields.LEGACY_GUARDIAN_FLAGS, 0x02)) {
                return ElderGuardian.class;
            }
        } else if (fakeClass == Horse.class) {
            if (versionId > V_1_8_9) { // ToDo: No clue here
                return fakeClass;
            }
            return switch (metaData.getSets().getByte(EntityMetaDataFields.LEGACY_HORSE_SPECIAL_TYPE)) {
                default -> fakeClass;
                case 1 -> Donkey.class;
                case 2 -> Mule.class;
                case 3 -> ZombieHorse.class;
                case 4 -> SkeletonHorse.class;
            };

        }
        return fakeClass;
    }

    public static Class<? extends Entity> getRealEntityObjectClass(Class<? extends Entity> fakeClass, int data, int versionId) {
        if (fakeClass == Minecart.class) {
            if (versionId > V_1_8_9) { // ToDo: No clue here
                return fakeClass;
            }
            return switch (data) {
                default -> fakeClass;
                case 1 -> MinecartChest.class;
                case 2 -> MinecartFurnace.class;
                case 3 -> MinecartTNT.class;
                case 4 -> MinecartSpawner.class;
                case 5 -> MinecartHopper.class;
                case 6 -> MinecartCommandBlock.class;
            };
        }
        return fakeClass;
    }

    public static Chunk transformChunk(Chunk chunk, int versionId) {
        // some blocks need to be tweaked. eg. Grass with a snow block on top becomes snowy grass block
        if (versionId >= ProtocolDefinition.FLATTING_VERSION_ID) {
            return chunk;
        }
        for (Map.Entry<Byte, ChunkSection> sectionEntry : chunk.getSections().entrySet()) {
            for (Map.Entry<InChunkSectionLocation, Block> blockEntry : sectionEntry.getValue().getBlocks().entrySet()) {
                Block newBlock = transformBlock(blockEntry.getValue(), chunk, blockEntry.getKey(), sectionEntry.getKey());
                if (newBlock == blockEntry.getValue()) {
                    continue;
                }
                sectionEntry.getValue().setBlock(blockEntry.getKey(), newBlock);
            }
        }
        return chunk;
    }

    public static Block transformBlock(Block originalBlock, Chunk chunk, InChunkLocation location) {
        return transformBlock(originalBlock, chunk, location.getInChunkSectionLocation(), (byte) (location.getY() / ProtocolDefinition.SECTION_HEIGHT_Y));
    }

    public static Block transformBlock(Block originalBlock, Chunk chunk, InChunkSectionLocation location, byte sectionHeight) {
        if (originalBlock == null) {
            return null;
        }
        switch (originalBlock.getFullIdentifier()) {
            case "minecraft:grass" -> {
                Block above = getBlockAbove(chunk, location, sectionHeight);
                if (above == null) {
                    break;
                }
                if (above.equals(TweakBlocks.SNOW) || above.equals(TweakBlocks.SNOW_LAYER)) {
                    return TweakBlocks.GRASS_BLOCK_SNOWY_YES;
                } else {
                    return TweakBlocks.GRASS_BLOCK_SNOWY_NO;
                }
            }
            // ToDo: all blocks. e.g. doors, etc
        }
        return originalBlock;
    }

    private static Block getBlockAbove(Chunk chunk, InChunkSectionLocation location, byte sectionHeight) {
        return chunk.getBlock(location.getInChunkLocation(sectionHeight));
    }
}
