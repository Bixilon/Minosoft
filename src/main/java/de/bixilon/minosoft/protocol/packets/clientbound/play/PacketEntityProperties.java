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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.data.entities.EntityProperty;
import de.bixilon.minosoft.data.entities.EntityPropertyKeys;
import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import java.util.HashMap;
import java.util.UUID;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W04A;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_21W08A;

public class PacketEntityProperties extends PlayClientboundPacket {
    private final HashMap<EntityPropertyKeys, EntityProperty> properties = new HashMap<>();
    private final int entityId;

    public PacketEntityProperties(PlayInByteBuffer buffer) {
        this.entityId = buffer.readEntityId();
        if (buffer.getVersionId() < V_14W04A) {
            int count = buffer.readInt();
            for (int i = 0; i < count; i++) {
                EntityPropertyKeys key = EntityPropertyKeys.byName(buffer.readString());
                double value = buffer.readDouble();
                int listLength = buffer.readUnsignedShort();
                for (int ii = 0; ii < listLength; ii++) {
                    UUID uuid = buffer.readUUID();
                    double amount = buffer.readDouble();
                    ModifierActions operation = ModifierActions.byId(buffer.readUnsignedByte());
                    // ToDo: modifiers
                }
                this.properties.put(key, new EntityProperty(value));
            }
            return;
        }
        int count;
        if (buffer.getVersionId() < V_21W08A) {
            count = buffer.readInt();
        } else {
            count = buffer.readVarInt();
        }
        for (int i = 0; i < count; i++) {
            EntityPropertyKeys key = EntityPropertyKeys.byName(buffer.readString());
            double value = buffer.readDouble();
            int listLength = buffer.readVarInt();
            for (int ii = 0; ii < listLength; ii++) {
                UUID uuid = buffer.readUUID();
                double amount = buffer.readDouble();
                ModifierActions operation = ModifierActions.byId(buffer.readUnsignedByte());
                // ToDo: modifiers
            }
            this.properties.put(key, new EntityProperty(value));
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received entity properties (entityId=%d)", this.entityId));
    }

    public int getEntityId() {
        return this.entityId;
    }

    public enum ModifierActions {
        ADD,
        ADD_PERCENT,
        MULTIPLY;

        private static final ModifierActions[] MODIFIER_ACTIONS = values();

        public static ModifierActions byId(int id) {
            return MODIFIER_ACTIONS[id];
        }
    }
}
