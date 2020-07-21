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

import de.bixilon.minosoft.game.datatypes.GameMode;
import de.bixilon.minosoft.game.datatypes.MapSet;
import de.bixilon.minosoft.game.datatypes.VersionValueMap;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketChangeGameState implements ClientboundPacket {
    Reason reason;
    float value;

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
            case VERSION_1_14_4:
                reason = Reason.byId(buffer.readByte(), buffer.getVersion());
                value = buffer.readFloat();
                return true;
        }

        return false;
    }

    @Override
    public void log() {
        switch (getReason()) {
            case START_RAIN:
                Log.game("Received weather packet: Starting rain...");
                break;
            case END_RAIN:
                Log.game("Received weather packet: Stopping rain...");
                break;
            case CHANGE_GAMEMODE:
                Log.game(String.format("Received game mode change: Now in %s", GameMode.byId(getValue().intValue())));
                break;
            default:
                Log.protocol(String.format("Received game status change (%s)", getReason()));
                break;
        }
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public Reason getReason() {
        return reason;
    }

    public Float getValue() {
        return value;
    }

    public enum Reason {
        INVALID_BED(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 0)}),
        END_RAIN(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 1), new MapSet<>(ProtocolVersion.VERSION_1_7_10, 2)}),
        START_RAIN(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 2), new MapSet<>(ProtocolVersion.VERSION_1_7_10, 1)}),
        CHANGE_GAMEMODE(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 3)}),
        ENTER_CREDITS(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 4)}),
        DEMO_MESSAGES(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 5)}),
        ARROW_HITTING_PLAYER(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 6)}),
        FADE_VALUE(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 7)}),
        FADE_TIME(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 8)}),
        PLAY_PUFFERFISH_STING_SOUND(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 9)}),
        PLAY_ELDER_GUARDIAN_MOB_APPEARANCE(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 10)});


        final VersionValueMap<Integer> valueMap;

        Reason(MapSet<ProtocolVersion, Integer>[] values) {
            valueMap = new VersionValueMap<>(values, true);
        }

        public static Reason byId(int id, ProtocolVersion version) {
            for (Reason reason : values()) {
                if (reason.getId(version) == id) {
                    return reason;
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
