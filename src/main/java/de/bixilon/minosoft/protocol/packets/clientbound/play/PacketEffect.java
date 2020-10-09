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

import de.bixilon.minosoft.game.datatypes.MapSet;
import de.bixilon.minosoft.game.datatypes.VersionValueMap;
import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketEffect implements ClientboundPacket {
    EffectEffects effect;
    BlockPosition position;
    int data;
    boolean disableRelativeVolume;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getProtocolId() < 6) {
            this.effect = EffectEffects.byId(buffer.readInt(), buffer.getProtocolId());
            position = buffer.readBlockPosition();
            data = buffer.readInt();
            disableRelativeVolume = buffer.readBoolean();
            return true;
        }
        this.effect = EffectEffects.byId(buffer.readInt(), buffer.getProtocolId());
        position = buffer.readPosition();
        data = buffer.readInt();
        disableRelativeVolume = buffer.readBoolean();
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received effect packet at %s (effect=%s, data=%d, disableRelativeVolume=%s)", position, effect, data, disableRelativeVolume));
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

    public SmokeDirections getSmokeDirection() {
        if (effect == EffectEffects.PARTICLE_10_SMOKE) {
            return SmokeDirections.byId(data);
        }
        return null;
    }
    // ToDo all other dataTypes

    public boolean isDisableRelativeVolume() {
        return disableRelativeVolume;
    }

    public enum EffectEffects {

        //ToDo: find out correct versions
        RANDOM_CLICK(new MapSet[]{new MapSet<>(0, 1000), new MapSet<>(110, -1)}),
        DISPENSER_DISPENSES(new MapSet[]{new MapSet<>(110, 1000)}),
        RANDOM_CLICK1(new MapSet[]{new MapSet<>(0, 1001), new MapSet<>(110, -1)}),
        DISPENSER_FAILS(new MapSet[]{new MapSet<>(110, 1001)}),
        RANDOM_BOW(new MapSet[]{new MapSet<>(0, 1002), new MapSet<>(110, -1)}),
        DISPENSER_SHOOTS(new MapSet[]{new MapSet<>(110, 1002)}),
        RANDOM_DOOR_OPEN_CLOSE(new MapSet[]{new MapSet<>(0, 1003), new MapSet<>(110, -1)}),
        ENDER_EYE_LAUNCHED(new MapSet[]{new MapSet<>(110, 1003)}),
        RANDOM_FIZZ(new MapSet[]{new MapSet<>(0, 1004), new MapSet<>(110, -1)}),
        FIREWORK_SHOT(new MapSet[]{new MapSet<>(110, 1004)}),
        MUSIC_DISK(new MapSet[]{new MapSet<>(0, 1005), new MapSet<>(110, 1010)}), // data: recordId
        IRON_DOOR_OPENED(new MapSet[]{new MapSet<>(110, 1005)}),
        WOODEN_DOOR_OPENED(new MapSet[]{new MapSet<>(110, 1006)}),
        MOB_GHAST_CHARGE(new MapSet[]{new MapSet<>(0, 1007), new MapSet<>(110, -1)}),
        WOODEN_TRAP_DOOR_OPENED(new MapSet[]{new MapSet<>(110, 1007)}),
        MOB_GHAST_FIREBALL(new MapSet[]{new MapSet<>(0, 1008), new MapSet<>(110, -1)}),
        FENCE_GATE_OPENED(new MapSet[]{new MapSet<>(110, 1008)}),
        MOB_GHAST_FIREBALL_LOW(new MapSet[]{new MapSet<>(0, 1009), new MapSet<>(110, -1)}),
        FIRE_EXTINGUISHED(new MapSet[]{new MapSet<>(110, 1009)}),
        MOB_ZOMBIE_ATTACKS_WOOD_DOOR(new MapSet[]{new MapSet<>(0, 1010), new MapSet<>(110, 1019)}),
        MOB_ZOMBIE_ATTACKS_METAL_DOOR(new MapSet[]{new MapSet<>(0, 1011), new MapSet<>(110, 1020)}),
        IRON_DOOR_CLOSED(new MapSet[]{new MapSet<>(110, 1011)}),
        MOB_ZOMBIE_WOODEN_DOOR_BREAK(new MapSet[]{new MapSet<>(0, 1012), new MapSet<>(110, 1021)}),
        WOODEN_DOOR_CLOSED(new MapSet[]{new MapSet<>(110, 1012)}),
        MOB_WITHER_SPAWN(new MapSet[]{new MapSet<>(0, 1013), new MapSet<>(110, 1023)}),
        WOODEN_TRAP_DOOR_CLOSED(new MapSet[]{new MapSet<>(110, 1013)}),
        MOB_WITHER_SHOOT(new MapSet[]{new MapSet<>(0, 1014), new MapSet<>(110, 1024)}),
        FENCE_GATE_CLOSED(new MapSet[]{new MapSet<>(110, 1014)}),
        MOB_BAT_TAKEOFF(new MapSet[]{new MapSet<>(0, 1015), new MapSet<>(110, 1025)}),
        GHAST_WARNS(new MapSet[]{new MapSet<>(110, 1015)}),
        MOB_ZOMBIE_INFECT(new MapSet[]{new MapSet<>(0, 1016), new MapSet<>(110, 1026)}),
        GHAST_SHOOTS(new MapSet[]{new MapSet<>(110, 1016)}),
        MOB_ZOMBIE_UNFECT(new MapSet[]{new MapSet<>(0, 1017), new MapSet<>(110, -1)}),
        ENDER_DRAGON_SHOOTS(new MapSet[]{new MapSet<>(110, 1017)}),
        MOB_ENDERDRAGON_DEATH(new MapSet[]{new MapSet<>(0, 1018), new MapSet<>(110, 1028)}),
        BLAZE_SHOOTS(new MapSet[]{new MapSet<>(110, 1018)}),
        ANVIL_BREAK(new MapSet[]{new MapSet<>(0, 1020), new MapSet<>(110, 1029)}),
        ANVIL_USE(new MapSet[]{new MapSet<>(0, 1021), new MapSet<>(110, 1030)}),
        ANVIL_LAND(new MapSet[]{new MapSet<>(0, 1022), new MapSet<>(110, 1031)}),
        MOB_WITHER_BREAKS_BLOCKS(new MapSet[]{new MapSet<>(110, 1022)}),
        MOB_ZOMBIE_CONVERTED(new MapSet[]{new MapSet<>(110, 1027)}),
        PORTAL_TRAVEL(new MapSet[]{new MapSet<>(110, 1032)}),
        CHORUS_FLOWER_GROWN(new MapSet[]{new MapSet<>(110, 1033)}),
        CHORUS_FLOWER_DIED(new MapSet[]{new MapSet<>(110, 1034)}),
        BREWING_STAND_BREWED(new MapSet[]{new MapSet<>(110, 1035)}),
        IRON_TRAP_DOOR_OPENED(new MapSet[]{new MapSet<>(110, 1036)}),
        IRON_TRAP_DOOR_CLOSED(new MapSet[]{new MapSet<>(110, 1037)}),
        END_PORTAL_CREATED(new MapSet[]{new MapSet<>(578, 1038)}),
        PHANTOM_BITES(new MapSet[]{new MapSet<>(578, 1039)}),
        ZOMBIE_CONVERTS_TO_DROWNED(new MapSet[]{new MapSet<>(578, 1040)}),
        HUSK_CONVERT_TO_ZOMBIE_DROWNING(new MapSet[]{new MapSet<>(578, 1041)}),
        GRINDSTONE_USED(new MapSet[]{new MapSet<>(578, 1042)}),
        BOOK_PAGE_TURNED(new MapSet[]{new MapSet<>(578, 1043)}),

        PARTICLE_10_SMOKE(new MapSet[]{new MapSet<>(0, 2000)}), // data: smoke direction
        BLOCK_BREAK(new MapSet[]{new MapSet<>(0, 2001)}), // data: blockId
        SPLASH_POTION(new MapSet[]{new MapSet<>(0, 2002)}), //data: portionId
        EYE_OF_ENDER_BREAK_ANIMATION(new MapSet[]{new MapSet<>(0, 2003)}),
        MOB_SPAWN_SMOKE_FLAMES(new MapSet[]{new MapSet<>(0, 2004)}),
        SPAWN_HAPPY_VILLAGER(new MapSet[]{new MapSet<>(0, 2005), new MapSet<>(110, -1)}),
        BONE_MEAL_PARTICLES(new MapSet[]{new MapSet<>(110, 2005)}),
        SPAWN_FALL_PARTICLES(new MapSet[]{new MapSet<>(0, 2006), new MapSet<>(110, -1)}), // data: fall damage (particle speed)
        DRAGON_BREATH(new MapSet[]{new MapSet<>(110, 2006)}),
        INSTANT_SPLASH_POTION(new MapSet[]{new MapSet<>(340, 2007)}),
        ENDER_DRAGON_BLOCK_DESTROY(new MapSet[]{new MapSet<>(340, 2008)}),
        WET_SPONGE_VAPORIZES_NETHER(new MapSet[]{new MapSet<>(340, 2009)}),

        END_GATEWAY_SPAWN(new MapSet[]{new MapSet<>(110, 3000)}),
        MOB_ENDER_DRAGON_GROWL(new MapSet[]{new MapSet<>(110, 3001)});

        final VersionValueMap<Integer> valueMap;

        EffectEffects(MapSet<Integer, Integer>[] values) {
            valueMap = new VersionValueMap<>(values, true);
        }

        public static EffectEffects byId(int id, int protocolId) {
            for (EffectEffects effect : values()) {
                if (effect.getId(protocolId) == id) {
                    return effect;
                }
            }
            return null;
        }

        public int getId(Integer protocolId) {
            Integer ret = valueMap.get(protocolId);
            if (ret == null) {
                return -2;
            }
            return ret;
        }
    }

    public enum SmokeDirections {
        SOUTH_EAST,
        SOUTH,
        SOUTH_WEST,
        EAST,
        UP,
        WEST,
        NORTH_EAST,
        NORTH,
        NORTH_WEST;

        public static SmokeDirections byId(int id) {
            return values()[id];
        }
    }
}
