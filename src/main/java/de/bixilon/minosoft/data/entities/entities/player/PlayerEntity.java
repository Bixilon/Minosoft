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

package de.bixilon.minosoft.data.entities.entities.player;

import de.bixilon.minosoft.data.PlayerPropertyData;
import de.bixilon.minosoft.data.entities.EntityMetaDataFields;
import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction;
import de.bixilon.minosoft.data.entities.entities.LivingEntity;
import de.bixilon.minosoft.data.mappings.Item;
import de.bixilon.minosoft.data.player.Hands;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerEntity extends LivingEntity {
    private final String name;
    private final PlayerPropertyData[] properties;
    private Item currentItem;

    public PlayerEntity(Connection connection, int entityId, UUID uuid, Location location, EntityRotation rotation) {
        super(connection, entityId, uuid, location, rotation);
        this.name = "Ghost Player";
        this.properties = new PlayerPropertyData[0];
    }

    public PlayerEntity(Connection connection, int entityId, UUID uuid, Location location, EntityRotation rotation, String name, PlayerPropertyData[] properties, Item currentItem) {
        super(connection, entityId, uuid, location, rotation);
        this.name = name;
        this.properties = properties;
        this.currentItem = currentItem;
    }

    @EntityMetaDataFunction(identifier = "absorptionHearts")
    public float getPlayerAbsorptionHearts() {
        return metaData.getSets().getFloat(EntityMetaDataFields.PLAYER_ABSORPTION_HEARTS);
    }

    @EntityMetaDataFunction(identifier = "score")
    public int getScore() {
        return metaData.getSets().getInt(EntityMetaDataFields.PLAYER_SCORE);
    }

    private boolean getSkinPartsFlag(int bitMask) {
        return metaData.getSets().getBitMask(EntityMetaDataFields.PLAYER_SKIN_PARTS_FLAGS, bitMask);
    }

    @EntityMetaDataFunction(identifier = "mainHand")
    public Hands getMainHand() {
        return metaData.getSets().getByte(EntityMetaDataFields.PLAYER_SKIN_MAIN_HAND) == 0x01 ? Hands.OFF_HAND : Hands.MAIN_HAND;
    }

    @EntityMetaDataFunction(identifier = "leftShoulderEntityData")
    @Nullable
    public CompoundTag getLeftShoulderData() {
        return metaData.getSets().getNBT(EntityMetaDataFields.PLAYER_LEFT_SHOULDER_DATA);
    }

    @EntityMetaDataFunction(identifier = "rightShoulderEntityData")
    @Nullable
    public CompoundTag getRightShoulderData() {
        return metaData.getSets().getNBT(EntityMetaDataFields.PLAYER_RIGHT_SHOULDER_DATA);
    }

    @EntityMetaDataFunction(identifier = "name")
    public String getName() {
        return name;
    }

    @EntityMetaDataFunction(identifier = "properties")
    public PlayerPropertyData[] getProperties() {
        return properties;
    }

    @Deprecated
    public Item getCurrentItem() {
        return currentItem;
    }
}

