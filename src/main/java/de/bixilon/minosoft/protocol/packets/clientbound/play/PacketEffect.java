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

import de.bixilon.minosoft.data.MapSet;
import de.bixilon.minosoft.data.VersionValueMap;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.modding.event.events.EffectEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

import static de.bixilon.minosoft.protocol.protocol.Versions.*;

public class PacketEffect extends ClientboundPacket {
    EffectEffects effect;
    BlockPosition position;
    int data;
    boolean disableRelativeVolume;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.effect = EffectEffects.byId(buffer.readInt(), buffer.getVersionId());
        if (buffer.getVersionId() < V_14W03B) {
            this.position = buffer.readBlockPositionByte();
        } else {
            this.position = buffer.readPosition();
        }
        this.data = buffer.readInt();
        this.disableRelativeVolume = buffer.readBoolean();
        return true;
    }

    @Override
    public void handle(Connection connection) {
        if (connection.fireEvent(new EffectEvent(connection, this))) {
            return;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received effect packet at %s (effect=%s, data=%d, disableRelativeVolume=%s)", this.position, this.effect, this.data, this.disableRelativeVolume));
    }

    public BlockPosition getPosition() {
        return this.position;
    }

    public EffectEffects getEffect() {
        return this.effect;
    }

    public int getData() {
        return this.data;
    }

    public SmokeDirections getSmokeDirection() {
        if (this.effect == EffectEffects.PARTICLE_10_SMOKE) {
            return SmokeDirections.byId(this.data);
        }
        return null;
    }
    // ToDo all other dataTypes

    public boolean isDisableRelativeVolume() {
        return this.disableRelativeVolume;
    }

    public enum EffectEffects {

        // ToDo: find out correct versions
        RANDOM_CLICK(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1000), new MapSet<>(V_1_9_4, -1)}),
        DISPENSER_DISPENSES(new MapSet[]{new MapSet<>(V_1_9_4, 1000)}),
        RANDOM_CLICK1(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1001), new MapSet<>(V_1_9_4, -1)}),
        DISPENSER_FAILS(new MapSet[]{new MapSet<>(V_1_9_4, 1001)}),
        RANDOM_BOW(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1002), new MapSet<>(V_1_9_4, -1)}),
        DISPENSER_SHOOTS(new MapSet[]{new MapSet<>(V_1_9_4, 1002)}),
        RANDOM_DOOR_OPEN_CLOSE(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1003), new MapSet<>(V_1_9_4, -1)}),
        ENDER_EYE_LAUNCHED(new MapSet[]{new MapSet<>(V_1_9_4, 1003)}),
        RANDOM_FIZZ(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1004), new MapSet<>(V_1_9_4, -1)}),
        FIREWORK_SHOT(new MapSet[]{new MapSet<>(V_1_9_4, 1004)}),
        MUSIC_DISK(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1005), new MapSet<>(V_1_9_4, 1010)}), // data: recordId
        IRON_DOOR_OPENED(new MapSet[]{new MapSet<>(V_1_9_4, 1005)}),
        WOODEN_DOOR_OPENED(new MapSet[]{new MapSet<>(V_1_9_4, 1006)}),
        MOB_GHAST_CHARGE(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1007), new MapSet<>(V_1_9_4, -1)}),
        WOODEN_TRAP_DOOR_OPENED(new MapSet[]{new MapSet<>(V_1_9_4, 1007)}),
        MOB_GHAST_FIREBALL(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1008), new MapSet<>(V_1_9_4, -1)}),
        FENCE_GATE_OPENED(new MapSet[]{new MapSet<>(V_1_9_4, 1008)}),
        MOB_GHAST_FIREBALL_LOW(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1009), new MapSet<>(V_1_9_4, -1)}),
        FIRE_EXTINGUISHED(new MapSet[]{new MapSet<>(V_1_9_4, 1009)}),
        MOB_ZOMBIE_ATTACKS_WOOD_DOOR(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1010), new MapSet<>(V_1_9_4, 1019)}),
        MOB_ZOMBIE_ATTACKS_METAL_DOOR(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1011), new MapSet<>(V_1_9_4, 1020)}),
        IRON_DOOR_CLOSED(new MapSet[]{new MapSet<>(V_1_9_4, 1011)}),
        MOB_ZOMBIE_WOODEN_DOOR_BREAK(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1012), new MapSet<>(V_1_9_4, 1021)}),
        WOODEN_DOOR_CLOSED(new MapSet[]{new MapSet<>(V_1_9_4, 1012)}),
        MOB_WITHER_SPAWN(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1013), new MapSet<>(V_1_9_4, 1023)}),
        WOODEN_TRAP_DOOR_CLOSED(new MapSet[]{new MapSet<>(V_1_9_4, 1013)}),
        MOB_WITHER_SHOOT(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1014), new MapSet<>(V_1_9_4, 1024)}),
        FENCE_GATE_CLOSED(new MapSet[]{new MapSet<>(V_1_9_4, 1014)}),
        MOB_BAT_TAKEOFF(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1015), new MapSet<>(V_1_9_4, 1025)}),
        GHAST_WARNS(new MapSet[]{new MapSet<>(V_1_9_4, 1015)}),
        MOB_ZOMBIE_INFECT(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1016), new MapSet<>(V_1_9_4, 1026)}),
        GHAST_SHOOTS(new MapSet[]{new MapSet<>(V_1_9_4, 1016)}),
        MOB_ZOMBIE_UNFECT(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1017), new MapSet<>(V_1_9_4, -1)}),
        ENDER_DRAGON_SHOOTS(new MapSet[]{new MapSet<>(V_1_9_4, 1017)}),
        MOB_ENDERDRAGON_DEATH(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1018), new MapSet<>(V_1_9_4, 1028)}),
        BLAZE_SHOOTS(new MapSet[]{new MapSet<>(V_1_9_4, 1018)}),
        ANVIL_BREAK(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1020), new MapSet<>(V_1_9_4, 1029)}),
        ANVIL_USE(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1021), new MapSet<>(V_1_9_4, 1030)}),
        ANVIL_LAND(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1022), new MapSet<>(V_1_9_4, 1031)}),
        MOB_WITHER_BREAKS_BLOCKS(new MapSet[]{new MapSet<>(V_1_9_4, 1022)}),
        MOB_ZOMBIE_CONVERTED(new MapSet[]{new MapSet<>(V_1_9_4, 1027)}),
        PORTAL_TRAVEL(new MapSet[]{new MapSet<>(V_1_9_4, 1032)}),
        CHORUS_FLOWER_GROWN(new MapSet[]{new MapSet<>(V_1_9_4, 1033)}),
        CHORUS_FLOWER_DIED(new MapSet[]{new MapSet<>(V_1_9_4, 1034)}),
        BREWING_STAND_BREWED(new MapSet[]{new MapSet<>(V_1_9_4, 1035)}),
        IRON_TRAP_DOOR_OPENED(new MapSet[]{new MapSet<>(V_1_9_4, 1036)}),
        IRON_TRAP_DOOR_CLOSED(new MapSet[]{new MapSet<>(V_1_9_4, 1037)}),
        END_PORTAL_CREATED(new MapSet[]{new MapSet<>(V_1_15_2, 1038)}),
        PHANTOM_BITES(new MapSet[]{new MapSet<>(V_1_15_2, 1039)}),
        ZOMBIE_CONVERTS_TO_DROWNED(new MapSet[]{new MapSet<>(V_1_15_2, 1040)}),
        HUSK_CONVERT_TO_ZOMBIE_DROWNING(new MapSet[]{new MapSet<>(V_1_15_2, 1041)}),
        GRINDSTONE_USED(new MapSet[]{new MapSet<>(V_1_15_2, 1042)}),
        BOOK_PAGE_TURNED(new MapSet[]{new MapSet<>(V_1_15_2, 1043)}),

        PARTICLE_10_SMOKE(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 2000)}), // data: smoke direction
        BLOCK_BREAK(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 2001)}), // data: blockId
        SPLASH_POTION(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 2002)}), // data: portionId
        EYE_OF_ENDER_BREAK_ANIMATION(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 2003)}),
        MOB_SPAWN_SMOKE_FLAMES(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 2004)}),
        SPAWN_HAPPY_VILLAGER(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 2005), new MapSet<>(V_1_9_4, -1)}),
        BONE_MEAL_PARTICLES(new MapSet[]{new MapSet<>(V_1_9_4, 2005)}),
        SPAWN_FALL_PARTICLES(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 2006), new MapSet<>(V_1_9_4, -1)}), // data: fall damage (particle speed)
        DRAGON_BREATH(new MapSet[]{new MapSet<>(V_1_9_4, 2006)}),
        INSTANT_SPLASH_POTION(new MapSet[]{new MapSet<>(V_1_12_2, 2007)}),
        ENDER_DRAGON_BLOCK_DESTROY(new MapSet[]{new MapSet<>(V_1_12_2, 2008)}),
        WET_SPONGE_VAPORIZES_NETHER(new MapSet[]{new MapSet<>(V_1_12_2, 2009)}),

        END_GATEWAY_SPAWN(new MapSet[]{new MapSet<>(V_1_9_4, 3000)}),
        MOB_ENDER_DRAGON_GROWL(new MapSet[]{new MapSet<>(V_1_9_4, 3001)});

        final VersionValueMap<Integer> valueMap;

        EffectEffects(MapSet<Integer, Integer>[] values) {
            this.valueMap = new VersionValueMap<>(values);
        }

        public static EffectEffects byId(int id, int versionId) {
            for (EffectEffects effect : values()) {
                if (effect.getId(versionId) == id) {
                    return effect;
                }
            }
            return null;
        }

        public int getId(Integer versionId) {
            Integer ret = this.valueMap.get(versionId);
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

        private static final SmokeDirections[] SMOKE_DIRECTIONS = values();

        public static SmokeDirections byId(int id) {
            return SMOKE_DIRECTIONS[id];
        }
    }
}
