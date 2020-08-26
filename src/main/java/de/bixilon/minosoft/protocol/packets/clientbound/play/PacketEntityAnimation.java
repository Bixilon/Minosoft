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
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketEntityAnimation implements ClientboundPacket {
    int entityId;
    EntityAnimations animation;

    @Override
    public boolean read(InByteBuffer buffer) {
        entityId = buffer.readVarInt();
        animation = EntityAnimations.byId(buffer.readByte(), buffer.getProtocolId());
        return true;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Play entity animation (entityId=%d, animation=%s)", entityId, animation));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public enum EntityAnimations {
        //ToDo
        SWING_RIGHT_ARM(new MapSet[]{new MapSet<>(0, 0)}),
        TAKE_DAMAGE(new MapSet[]{new MapSet<>(0, 1)}),
        LEAVE_BED(new MapSet[]{new MapSet<>(0, 2)}),
        EAT_FOOD(new MapSet[]{new MapSet<>(0, 3), new MapSet<>(110, -1)}),
        SWING_LEFT_ARM(new MapSet[]{new MapSet<>(110, 3)}),
        CRITICAL_EFFECT(new MapSet[]{new MapSet<>(0, 4)}),
        MAGIC_CRITICAL_EFFECT(new MapSet[]{new MapSet<>(0, 5)}),
        UNKNOWN_1(new MapSet[]{new MapSet<>(0, 102), new MapSet<>(47, -1)}), // name currently unknown // ToDo
        SNEAK(new MapSet[]{new MapSet<>(0, 104), new MapSet<>(47, -1)}),
        UN_SNEAK(new MapSet[]{new MapSet<>(0, 105), new MapSet<>(47, -1)});

        final VersionValueMap<Integer> valueMap;

        EntityAnimations(MapSet<Integer, Integer>[] values) {
            valueMap = new VersionValueMap<>(values, true);
        }

        public static EntityAnimations byId(int id, int protocolId) {
            for (EntityAnimations animation : values()) {
                if (animation.getId(protocolId) == id) {
                    return animation;
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
}
