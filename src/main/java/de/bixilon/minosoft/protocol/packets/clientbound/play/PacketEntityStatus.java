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

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketEntityStatus implements ClientboundPacket {
    int entityId;
    Status status;

    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
            case VERSION_1_8:
                entityId = buffer.readInteger();
                status = Status.byId(buffer.readByte());
                break;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Entity status: (entityId=%d, animation=%s)", entityId, status.name()));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public enum Status {
        TO_DO_1(0),
        TO_DO_2(1),
        LIVING_ENTITY_HURT(2),
        LIVING_ENTITY_DEATH(3),
        IRON_GOLEM_SWING_ARMS(4),
        TAMING_HARTS(6),
        TAMING_SMOKE(7),
        WOLF_SHAKE_WATER(8),
        EATING_ACCEPTED(9),
        SHEEP_EATING_GRASS(10),
        IRON_GOLEM_HAND_OVER_ROSE(11),
        VILLAGER_MATING_HEARTS(12),
        VILLAGER_ANGRY(13),
        VILLAGER_HAPPY(14),
        WITCH_MAGIC(15),
        ZOMBIE_CONVERTING(16),
        FIREWORK_EXPLODING(17),
        ANIMAL_IN_LOVE(18),
        RESET_SQUID_ROTATION(19),
        SPAWN_EXPLOSION_PARTICLE(20),
        ENABLE_REDUCED_DEBUG_SCREEN(21),
        DISABLE_REDUCED_DEBUG_SCREEN(22);


        final int id;

        Status(int id) {
            this.id = id;
        }

        public static Status byId(int id) {
            for (Status s : values()) {
                if (s.getId() == id) {
                    return s;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
