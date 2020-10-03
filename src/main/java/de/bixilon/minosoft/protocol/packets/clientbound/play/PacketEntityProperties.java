/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.entities.EntityProperty;
import de.bixilon.minosoft.game.datatypes.entities.EntityPropertyKeys;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

import java.util.HashMap;
import java.util.UUID;

public class PacketEntityProperties implements ClientboundPacket {
    final HashMap<EntityPropertyKeys, EntityProperty> properties = new HashMap<>();
    int entityId;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.entityId = buffer.readEntityId();
        if (buffer.getProtocolId() < 7) {
            int count = buffer.readInt();
            for (int i = 0; i < count; i++) {
                EntityPropertyKeys key = EntityPropertyKeys.byName(buffer.readString(), buffer.getProtocolId());
                double value = buffer.readDouble();
                short listLength = buffer.readShort();
                for (int ii = 0; ii < listLength; ii++) {
                    UUID uuid = buffer.readUUID();
                    double amount = buffer.readDouble();
                    ModifierActions operation = ModifierActions.byId(buffer.readByte());
                    // ToDo: modifiers
                }
                properties.put(key, new EntityProperty(value));
            }
            return true;
        }
        int count = buffer.readInt();
        for (int i = 0; i < count; i++) {
            EntityPropertyKeys key = EntityPropertyKeys.byName(buffer.readString(), buffer.getProtocolId());
            double value = buffer.readDouble();
            int listLength = buffer.readVarInt();
            for (int ii = 0; ii < listLength; ii++) {
                UUID uuid = buffer.readUUID();
                double amount = buffer.readDouble();
                ModifierActions operation = ModifierActions.byId(buffer.readByte());
                // ToDo: modifiers
            }
            properties.put(key, new EntityProperty(value));
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received entity properties (entityId=%d)", entityId));
    }

    public int getEntityId() {
        return entityId;
    }

    public enum ModifierActions {
        ADD,
        ADD_PERCENT,
        MULTIPLY;

        public static ModifierActions byId(int id) {
            return values()[id];
        }

        public int getId() {
            return ordinal();
        }
    }
}
