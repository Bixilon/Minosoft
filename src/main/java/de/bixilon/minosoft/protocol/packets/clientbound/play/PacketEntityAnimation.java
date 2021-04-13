/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.data.VersionValueMap;
import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import java.util.Map;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketEntityAnimation extends PlayClientboundPacket {
    private final int entityId;
    private final EntityAnimations animation;

    public PacketEntityAnimation(PlayInByteBuffer buffer) {
        this.entityId = buffer.readVarInt();
        this.animation = EntityAnimations.byId(buffer.readUnsignedByte(), buffer.getVersionId());
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Play entity animation (entityId=%d, animation=%s)", this.entityId, this.animation));
    }

    public enum EntityAnimations {
        // ToDo
        SWING_RIGHT_ARM(Map.of(LOWEST_VERSION_SUPPORTED, 0)),
        TAKE_DAMAGE(Map.of(LOWEST_VERSION_SUPPORTED, 1)),
        LEAVE_BED(Map.of(LOWEST_VERSION_SUPPORTED, 2)),
        EAT_FOOD(Map.of(LOWEST_VERSION_SUPPORTED, 3, V_1_9_4, -1)),
        SWING_LEFT_ARM(Map.of(V_1_9_4, 3)),
        CRITICAL_EFFECT(Map.of(LOWEST_VERSION_SUPPORTED, 4)),
        MAGIC_CRITICAL_EFFECT(Map.of(LOWEST_VERSION_SUPPORTED, 5)),
        UNKNOWN_1(Map.of(LOWEST_VERSION_SUPPORTED, 102, V_1_8_9, -1)), // name currently unknown // ToDo
        SNEAK(Map.of(LOWEST_VERSION_SUPPORTED, 104, V_1_8_9, -1)),
        UN_SNEAK(Map.of(LOWEST_VERSION_SUPPORTED, 105, V_1_8_9, -1));

   private final VersionValueMap<Integer> valueMap;

        EntityAnimations(Map<Integer, Integer> values) {
            this.valueMap = new VersionValueMap<>(values);
        }

        public static EntityAnimations byId(int id, int versionId) {
            for (EntityAnimations animation : values()) {
                if (animation.getId(versionId) == id) {
                    return animation;
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
}
