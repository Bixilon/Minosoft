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
    EntityStates status;

    @Override
    public boolean read(InByteBuffer buffer) {
        entityId = buffer.readInt();
        status = EntityStates.byId(buffer.readByte());
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("Entity status: (entityId=%d, animation=%s)", entityId, status));
    }

    public enum EntityStates {
        SPAWN_TIPPED_ARROW,
        RESET_MOB_SPAWNER_MINECART_TIMER,
        LIVING_ENTITY_HURT,
        LIVING_ENTITY_DEATH,
        IRON_GOLEM_SWING_ARMS,
        TAMING_HARTS,
        TAMING_SMOKE,
        WOLF_SHAKE_WATER,
        EATING_ACCEPTED,
        SHEEP_EATING_GRASS,
        IRON_GOLEM_HAND_OVER_ROSE,
        VILLAGER_MATING_HEARTS,
        VILLAGER_ANGRY,
        VILLAGER_HAPPY,
        WITCH_MAGIC,
        ZOMBIE_CONVERTING,
        FIREWORK_EXPLODING,
        ANIMAL_IN_LOVE,
        RESET_SQUID_ROTATION,
        SPAWN_EXPLOSION_PARTICLE,
        PLAY_GUARDIAN_SOUND,
        ENABLE_REDUCED_DEBUG_SCREEN,
        DISABLE_REDUCED_DEBUG_SCREEN,
        OP_PERMISSION_LEVEL_0,
        OP_PERMISSION_LEVEL_1,
        OP_PERMISSION_LEVEL_2,
        OP_PERMISSION_LEVEL_3,
        OP_PERMISSION_LEVEL_4,
        SHIELD_BLOCK_SOUND,
        SHIELD_BREAK_SOUND,
        FISHING_ROD_BOBBER,
        ARMOR_STAND_HIT,
        ENTITY_HURT_THORNS,
        PUT_AWAY_GOLEM_POPPY,
        TOTEM_OF_UNDYING_ANIMATION,
        ENTITY_HURT_DROWN,
        ENTITY_HURT_BURN,
        SPAWN_CLOUD_PARTICLE,
        ENTITY_HURT_BERRY_BUSH,
        PORTAL_PARTICLE_CHORUS;
        // ToDo: 1.11+ (for each entity)

        public static EntityStates byId(int id) {
            return values()[id];
        }

        public int getId() {
            return ordinal();
        }
    }
}
