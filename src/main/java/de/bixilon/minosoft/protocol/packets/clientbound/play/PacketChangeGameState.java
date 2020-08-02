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

public class PacketChangeGameState implements ClientboundPacket {
    Reason reason;
    float value;

    @Override
    public boolean read(InByteBuffer buffer) {
        reason = Reason.byId(buffer.readByte(), buffer.getProtocolId());
        value = buffer.readFloat();
        return true;
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
        INVALID_BED(new MapSet[]{new MapSet<>(0, 0)}),
        END_RAIN(new MapSet[]{new MapSet<>(0, 1), new MapSet<>(498, 2), new MapSet<>(578, 1)}), // ToDo: when excactly did these 2 switch?
        START_RAIN(new MapSet[]{new MapSet<>(0, 2), new MapSet<>(498, 1), new MapSet<>(578, 2)}),
        CHANGE_GAMEMODE(new MapSet[]{new MapSet<>(0, 3)}),
        ENTER_CREDITS(new MapSet[]{new MapSet<>(0, 4)}),
        DEMO_MESSAGES(new MapSet[]{new MapSet<>(0, 5)}),
        ARROW_HITTING_PLAYER(new MapSet[]{new MapSet<>(0, 6)}),
        FADE_VALUE(new MapSet[]{new MapSet<>(0, 7)}),
        FADE_TIME(new MapSet[]{new MapSet<>(0, 8)}),
        PLAY_PUFFERFISH_STING_SOUND(new MapSet[]{new MapSet<>(0, 9)}),
        PLAY_ELDER_GUARDIAN_MOB_APPEARANCE(new MapSet[]{new MapSet<>(0, 10)}),
        ENABLE_RESPAWN_SCREEN(new MapSet[]{new MapSet<>(552, 11)});

        final VersionValueMap<Integer> valueMap;

        Reason(MapSet<Integer, Integer>[] values) {
            valueMap = new VersionValueMap<>(values, true);
        }

        public static Reason byId(int id, int protocolId) {
            for (Reason reason : values()) {
                if (reason.getId(protocolId) == id) {
                    return reason;
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
