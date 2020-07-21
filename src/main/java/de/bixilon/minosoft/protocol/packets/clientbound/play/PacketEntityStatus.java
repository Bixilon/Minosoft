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
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketEntityStatus implements ClientboundPacket {
    int entityId;
    Status status;

    @Override
    public boolean read(InByteBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                entityId = buffer.readInt();
                status = Status.byId(buffer.readByte());
                return true;
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Entity status: (entityId=%d, animation=%s)", entityId, status));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public enum Status {
        SPAWN_TIPPED_ARROW(0),
        RESET_MOB_SPAWNER_MINECART_TIMER(1),
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
        PLAY_GUARDIAN_SOUND(21),
        ENABLE_REDUCED_DEBUG_SCREEN(22),
        DISABLE_REDUCED_DEBUG_SCREEN(23),
        OP_PERMISSION_LEVEL_0(24),
        OP_PERMISSION_LEVEL_1(25),
        OP_PERMISSION_LEVEL_2(26),
        OP_PERMISSION_LEVEL_3(27),
        OP_PERMISSION_LEVEL_4(28),
        SHIELD_BLOCK_SOUND(29),
        SHIELD_BREAK_SOUND(30),
        FISHING_ROD_BOBBER(31),
        ARMOR_STAND_HIT(32),
        ENTITY_HURT_THORNS(33),
        PUT_AWAY_GOLEM_POPPY(34),
        TOTEM_OF_UNDYING_ANIMATION(35),
        ENTITY_HURT_DROWN(36),
        ENTITY_HURT_BURN(37),
        SPAWN_CLOUD_PARTICLE(43),
        ENTITY_HURT_BERRY_BUSH(44),
        PORTAL_PARTICLE_CHORUS(46);
        // ToDo: 1.11+ (for each entity)


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
