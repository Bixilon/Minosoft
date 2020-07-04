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
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketEntityAnimation implements ClientboundPacket {
    int entityId;
    EntityAnimations animation;

    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
            case VERSION_1_8:
            case VERSION_1_9_4:
                entityId = buffer.readVarInt();
                animation = EntityAnimations.byId(buffer.readByte(), buffer.getVersion());
                return true;
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Play entity animation (entityId=%d, animation=%s)", entityId, animation.name()));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public enum EntityAnimations {
        SWING_RIGHT_ARM(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 0)}),
        TAKE_DAMAGE(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 1)}),
        LEAVE_BED(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 2)}),
        EAT_FOOD(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 3), new MapSet<>(ProtocolVersion.VERSION_1_9_4, -1)}),
        SWING_LEFT_ARM(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_9_4, 3)}),
        CRITICAL_EFFECT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 4)}),
        MAGIC_CRITICAL_EFFECT(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 5)}),
        UNKNOWN_1(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 102), new MapSet<>(ProtocolVersion.VERSION_1_8, -1)}), // name currently unknown //ToDo
        SNEAK(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 104), new MapSet<>(ProtocolVersion.VERSION_1_8, -1)}),
        UN_SNEAK(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 105), new MapSet<>(ProtocolVersion.VERSION_1_8, -1)});

        final VersionValueMap<Integer> valueMap;

        EntityAnimations(MapSet<ProtocolVersion, Integer>[] values) {
            valueMap = new VersionValueMap<>(values, true);
        }

        public static EntityAnimations byId(int id, ProtocolVersion version) {
            for (EntityAnimations animation : values()) {
                if (animation.getId(version) == id) {
                    return animation;
                }
            }
            return null;
        }

        public int getId(ProtocolVersion version) {
            Integer ret = valueMap.get(version);
            if (ret == null) {
                return -2;
            }
            return ret;
        }
    }
}
