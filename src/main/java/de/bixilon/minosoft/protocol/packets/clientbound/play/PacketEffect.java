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

import de.bixilon.minosoft.data.VersionValueMap;
import de.bixilon.minosoft.modding.event.events.EffectEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3i;

import java.util.Map;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketEffect extends ClientboundPacket {
    private final EffectEffects effect;
    private final Vec3i position;
    private final int data;
    private final boolean disableRelativeVolume;

    public PacketEffect(InByteBuffer buffer) {
        this.effect = EffectEffects.byId(buffer.readInt(), buffer.getVersionId());
        if (buffer.getVersionId() < V_14W03B) {
            this.position = buffer.readBlockPositionByte();
        } else {
            this.position = buffer.readBlockPosition();
        }
        this.data = buffer.readInt();
        this.disableRelativeVolume = buffer.readBoolean();
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

    public Vec3i getPosition() {
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
        RANDOM_CLICK(Map.of(LOWEST_VERSION_SUPPORTED, 1000, V_1_9_4, -1)),
        DISPENSER_DISPENSES(Map.of(V_1_9_4, 1000)),
        RANDOM_CLICK1(Map.of(LOWEST_VERSION_SUPPORTED, 1001, V_1_9_4, -1)),
        DISPENSER_FAILS(Map.of(V_1_9_4, 1001)),
        RANDOM_BOW(Map.of(LOWEST_VERSION_SUPPORTED, 1002, V_1_9_4, -1)),
        DISPENSER_SHOOTS(Map.of(V_1_9_4, 1002)),
        RANDOM_DOOR_OPEN_CLOSE(Map.of(LOWEST_VERSION_SUPPORTED, 1003, V_1_9_4, -1)),
        ENDER_EYE_LAUNCHED(Map.of(V_1_9_4, 1003)),
        RANDOM_FIZZ(Map.of(LOWEST_VERSION_SUPPORTED, 1004, V_1_9_4, -1)),
        FIREWORK_SHOT(Map.of(V_1_9_4, 1004)),
        MUSIC_DISK(Map.of(LOWEST_VERSION_SUPPORTED, 1005, V_1_9_4, 1010)), // data: recordId
        IRON_DOOR_OPENED(Map.of(V_1_9_4, 1005)),
        WOODEN_DOOR_OPENED(Map.of(V_1_9_4, 1006)),
        MOB_GHAST_CHARGE(Map.of(LOWEST_VERSION_SUPPORTED, 1007, V_1_9_4, -1)),
        WOODEN_TRAP_DOOR_OPENED(Map.of(V_1_9_4, 1007)),
        MOB_GHAST_FIREBALL(Map.of(LOWEST_VERSION_SUPPORTED, 1008, V_1_9_4, -1)),
        FENCE_GATE_OPENED(Map.of(V_1_9_4, 1008)),
        MOB_GHAST_FIREBALL_LOW(Map.of(LOWEST_VERSION_SUPPORTED, 1009, V_1_9_4, -1)),
        FIRE_EXTINGUISHED(Map.of(V_1_9_4, 1009)),
        MOB_ZOMBIE_ATTACKS_WOOD_DOOR(Map.of(LOWEST_VERSION_SUPPORTED, 1010, V_1_9_4, 1019)),
        MOB_ZOMBIE_ATTACKS_METAL_DOOR(Map.of(LOWEST_VERSION_SUPPORTED, 1011, V_1_9_4, 1020)),
        IRON_DOOR_CLOSED(Map.of(V_1_9_4, 1011)),
        MOB_ZOMBIE_WOODEN_DOOR_BREAK(Map.of(LOWEST_VERSION_SUPPORTED, 1012, V_1_9_4, 1021)),
        WOODEN_DOOR_CLOSED(Map.of(V_1_9_4, 1012)),
        MOB_WITHER_SPAWN(Map.of(LOWEST_VERSION_SUPPORTED, 1013, V_1_9_4, 1023)),
        WOODEN_TRAP_DOOR_CLOSED(Map.of(V_1_9_4, 1013)),
        MOB_WITHER_SHOOT(Map.of(LOWEST_VERSION_SUPPORTED, 1014, V_1_9_4, 1024)),
        FENCE_GATE_CLOSED(Map.of(V_1_9_4, 1014)),
        MOB_BAT_TAKEOFF(Map.of(LOWEST_VERSION_SUPPORTED, 1015, V_1_9_4, 1025)),
        GHAST_WARNS(Map.of(V_1_9_4, 1015)),
        MOB_ZOMBIE_INFECT(Map.of(LOWEST_VERSION_SUPPORTED, 1016, V_1_9_4, 1026)),
        GHAST_SHOOTS(Map.of(V_1_9_4, 1016)),
        MOB_ZOMBIE_UNFECT(Map.of(LOWEST_VERSION_SUPPORTED, 1017, V_1_9_4, -1)),
        ENDER_DRAGON_SHOOTS(Map.of(V_1_9_4, 1017)),
        MOB_ENDERDRAGON_DEATH(Map.of(LOWEST_VERSION_SUPPORTED, 1018, V_1_9_4, 1028)),
        BLAZE_SHOOTS(Map.of(V_1_9_4, 1018)),
        ANVIL_BREAK(Map.of(LOWEST_VERSION_SUPPORTED, 1020, V_1_9_4, 1029)),
        ANVIL_USE(Map.of(LOWEST_VERSION_SUPPORTED, 1021, V_1_9_4, 1030)),
        ANVIL_LAND(Map.of(LOWEST_VERSION_SUPPORTED, 1022, V_1_9_4, 1031)),
        MOB_WITHER_BREAKS_BLOCKS(Map.of(V_1_9_4, 1022)),
        MOB_ZOMBIE_CONVERTED(Map.of(V_1_9_4, 1027)),
        PORTAL_TRAVEL(Map.of(V_1_9_4, 1032)),
        CHORUS_FLOWER_GROWN(Map.of(V_1_9_4, 1033)),
        CHORUS_FLOWER_DIED(Map.of(V_1_9_4, 1034)),
        BREWING_STAND_BREWED(Map.of(V_1_9_4, 1035)),
        IRON_TRAP_DOOR_OPENED(Map.of(V_1_9_4, 1036)),
        IRON_TRAP_DOOR_CLOSED(Map.of(V_1_9_4, 1037)),
        END_PORTAL_CREATED(Map.of(V_1_15_2, 1038)),
        PHANTOM_BITES(Map.of(V_1_15_2, 1039)),
        ZOMBIE_CONVERTS_TO_DROWNED(Map.of(V_1_15_2, 1040)),
        HUSK_CONVERT_TO_ZOMBIE_DROWNING(Map.of(V_1_15_2, 1041)),
        GRINDSTONE_USED(Map.of(V_1_15_2, 1042)),
        BOOK_PAGE_TURNED(Map.of(V_1_15_2, 1043)),

        PARTICLE_10_SMOKE(Map.of(LOWEST_VERSION_SUPPORTED, 2000)), // data: smoke direction
        BLOCK_BREAK(Map.of(LOWEST_VERSION_SUPPORTED, 2001)), // data: blockId
        SPLASH_POTION(Map.of(LOWEST_VERSION_SUPPORTED, 2002)), // data: portionId
        EYE_OF_ENDER_BREAK_ANIMATION(Map.of(LOWEST_VERSION_SUPPORTED, 2003)),
        MOB_SPAWN_SMOKE_FLAMES(Map.of(LOWEST_VERSION_SUPPORTED, 2004)),
        SPAWN_HAPPY_VILLAGER(Map.of(LOWEST_VERSION_SUPPORTED, 2005, V_1_9_4, -1)),
        BONE_MEAL_PARTICLES(Map.of(V_1_9_4, 2005)),
        SPAWN_FALL_PARTICLES(Map.of(LOWEST_VERSION_SUPPORTED, 2006, V_1_9_4, -1)), // data: fall damage (particle speed)
        DRAGON_BREATH(Map.of(V_1_9_4, 2006)),
        INSTANT_SPLASH_POTION(Map.of(V_1_12_2, 2007)),
        ENDER_DRAGON_BLOCK_DESTROY(Map.of(V_1_12_2, 2008)),
        WET_SPONGE_VAPORIZES_NETHER(Map.of(V_1_12_2, 2009)),

        END_GATEWAY_SPAWN(Map.of(V_1_9_4, 3000)),
        MOB_ENDER_DRAGON_GROWL(Map.of(V_1_9_4, 3001));

        private final VersionValueMap<Integer> valueMap;

        EffectEffects(Map<Integer, Integer> values) {
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
