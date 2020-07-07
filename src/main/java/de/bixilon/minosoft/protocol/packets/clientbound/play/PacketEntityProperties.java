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

import de.bixilon.minosoft.game.datatypes.Identifier;
import de.bixilon.minosoft.game.datatypes.entities.EntityProperty;
import de.bixilon.minosoft.game.datatypes.entities.EntityPropertyKey;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

import java.util.HashMap;
import java.util.UUID;

public class PacketEntityProperties implements ClientboundPacket {
    int entityId;
    final HashMap<EntityPropertyKey, EntityProperty> properties = new HashMap<>();


    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10: {
                entityId = buffer.readInt();
                int count = buffer.readInt();
                for (int i = 0; i < count; i++) {
                    EntityPropertyKey key = EntityPropertyKey.byIdentifier(new Identifier(buffer.readString()));
                    double value = buffer.readDouble();
                    short listLength = buffer.readShort();
                    for (int ii = 0; ii < listLength; ii++) {
                        UUID uuid = buffer.readUUID();
                        double amount = buffer.readDouble();
                        ModifierAction operation = ModifierAction.byId(buffer.readByte());
                        //ToDo: modifiers
                    }
                    properties.put(key, new EntityProperty(value));
                }
                return true;
            }
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2: {
                entityId = buffer.readVarInt();
                int count = buffer.readInt();
                for (int i = 0; i < count; i++) {
                    EntityPropertyKey key = EntityPropertyKey.byIdentifier(new Identifier(buffer.readString()));
                    double value = buffer.readDouble();
                    int listLength = buffer.readVarInt();
                    for (int ii = 0; ii < listLength; ii++) {
                        UUID uuid = buffer.readUUID();
                        double amount = buffer.readDouble();
                        ModifierAction operation = ModifierAction.byId(buffer.readByte());
                        //ToDo: modifiers
                    }
                    properties.put(key, new EntityProperty(value));
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received entity properties (entityId=%d)", entityId));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public int getEntityId() {
        return entityId;
    }

    public enum ModifierAction {
        ADD(0),
        ADD_PERCENT(1),
        MULTIPLY(2);

        final int id;

        ModifierAction(int id) {
            this.id = id;
        }

        public static ModifierAction byId(int id) {
            for (ModifierAction a : values()) {
                if (a.getId() == id) {
                    return a;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
