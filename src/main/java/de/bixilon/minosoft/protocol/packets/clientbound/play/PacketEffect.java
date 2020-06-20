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

import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketEffect implements ClientboundPacket {
    // is this class used??? What about PacketParticle or PacketSoundEffect?
    EffectEffects effect;
    BlockPosition position;
    int data;
    boolean disableRelativeVolume; // normally only at MOB_ENDERDRAGON_END and MOB_WITHER_SPAWN, but we allow this everywhere

    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                this.effect = EffectEffects.byId(buffer.readInteger());
                position = buffer.readBlockPosition();
                data = buffer.readInteger();
                disableRelativeVolume = buffer.readBoolean();
                break;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received effect packet at %s (effect=%s, data=%d, disableRelativeVolume=%s)", position.toString(), effect.name(), data, disableRelativeVolume));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public BlockPosition getPosition() {
        return position;
    }

    public EffectEffects getEffect() {
        return effect;
    }

    public int getData() {
        return data;
    }

    public SmokeDirection getSmokeDirection() {
        if (effect == EffectEffects.PARTICLE_10_SMOKE) {
            return SmokeDirection.byId(data);
        }
        return null;
    }
    //ToDo all other dataTypes


    public boolean isDisableRelativeVolume() {
        return disableRelativeVolume;
    }

    public enum EffectEffects {
        RANDOM_CLICK(1000),
        RANDOM_CLICK1(1001),
        RANDOM_BOW(1002),
        RANDOM_DOOR_OPEN_CLOSE(1003),
        RANDOM_FIZZ(1004),
        MUSIC_DISK(1005), // data: recordId
        MOB_GHAST_CHARGE(1007),
        MOB_GHAST_FIREBALL(1008),
        MOB_GHAST_FIREBALL_LOW(1009),
        MOB_ZOMBIE_WOOD(1010),
        MOB_ZOMBIE_METAL(1011),
        MOB_ZOMBIE_WOODBREAK(1012),
        MOB_WITHER_SPAWN(1013),
        MOB_WITHER_SHOOT(1014),
        MOB_BAT_TAKEOFF(1015),
        MOB_ZOMBIE_INFECT(1016),
        MOB_ZOMBIE_UNFECT(1017),
        MOB_ENDERDRAGON_END(1018),
        RANDOM_ANVIL_BREAK(1019),
        RANDOM_ANVIL_USE(1020),
        RANDOM_ANVIL_LAND(1021),

        PARTICLE_10_SMOKE(2000), // data: smoke direction
        PARTICLE_BLOCK_BREAK(2001), // data: blockId
        PARTICLE_SPLASH_POTION(2002), //data: portionId
        EYE_OF_ENDER_BREAK_ANIMATION(2003),
        MOB_SPAWN_SMOKE(2004),
        SPAWN_HAPPY_VILLAGER(2005),
        SPAWN_FALL_PARTICLES(2006); // data: fall damage (particle speed)


        final int id;

        EffectEffects(int id) {
            this.id = id;
        }

        public static EffectEffects byId(int id) {
            for (EffectEffects r : values()) {
                if (r.getId() == id) {
                    return r;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }

    public enum SmokeDirection {
        SOUTH_EAST(0),
        SOUTH(1),
        SOUTH_WEST(2),
        EAST(3),
        UP(4),
        WEST(5),
        NORTH_EAST(6),
        NORTH(7),
        NORTH_WEST(8);


        final int id;

        SmokeDirection(int id) {
            this.id = id;
        }

        public static SmokeDirection byId(int id) {
            for (SmokeDirection s : values()) {
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
