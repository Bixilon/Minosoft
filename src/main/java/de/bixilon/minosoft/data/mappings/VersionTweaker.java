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

package de.bixilon.minosoft.data.mappings;

import de.bixilon.minosoft.data.entities.EntityMetaData;
import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.entities.entities.animal.horse.*;
import de.bixilon.minosoft.data.entities.entities.monster.*;
import de.bixilon.minosoft.data.entities.entities.vehicle.*;

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
}
