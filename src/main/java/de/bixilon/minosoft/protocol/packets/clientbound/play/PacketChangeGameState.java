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
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketChangeGameState implements ClientboundPacket {
    Reason reason;
    float value;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.reason = Reason.byId(buffer.readByte(), buffer.getVersionId());
        this.value = buffer.readFloat();
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received game status change (%s)", getReason()));
    }

    public Reason getReason() {
        return this.reason;
    }

    public float getFloatValue() {
        return this.value;
    }

    public int getIntValue() {
        return (int) this.value;
    }

    public enum Reason {
        NO_RESPAWN_BLOCK_AVAILABLE(new MapSet[]{new MapSet<>(0, 0)}),
        START_RAINING(new MapSet[]{new MapSet<>(0, 1), new MapSet<>(498, 2), new MapSet<>(578, 1)}), // ToDo: when exactly did these 2 switch?
        STOP_RAINING(new MapSet[]{new MapSet<>(0, 2), new MapSet<>(498, 1), new MapSet<>(578, 2)}),
        CHANGE_GAMEMODE(new MapSet[]{new MapSet<>(0, 3)}),
        ENTER_CREDITS(new MapSet[]{new MapSet<>(0, 4)}),
        DEMO_MESSAGES(new MapSet[]{new MapSet<>(0, 5)}),
        ARROW_HITTING_PLAYER(new MapSet[]{new MapSet<>(0, 6)}),
        RAIN_LEVEL_CHANGE(new MapSet[]{new MapSet<>(0, 7)}),
        THUNDER_LEVEL_CHANGE(new MapSet[]{new MapSet<>(0, 8)}),
        PUFFERFISH_STING(new MapSet[]{new MapSet<>(0, 9)}),
        GUARDIAN_ELDER_EFFECT(new MapSet[]{new MapSet<>(0, 10)}),
        IMMEDIATE_RESPAWN(new MapSet[]{new MapSet<>(552, 11)});

        final VersionValueMap<Integer> valueMap;

        Reason(MapSet<Integer, Integer>[] values) {
            this.valueMap = new VersionValueMap<>(values, true);
        }

        public static Reason byId(int id, int versionId) {
            for (Reason reason : values()) {
                if (reason.getId(versionId) == id) {
                    return reason;
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
