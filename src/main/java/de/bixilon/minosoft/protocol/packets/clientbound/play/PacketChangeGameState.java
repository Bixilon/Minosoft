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

import de.bixilon.minosoft.data.Gamemodes;
import de.bixilon.minosoft.data.VersionValueMap;
import de.bixilon.minosoft.modding.event.events.ChangeGameStateEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import java.util.Map;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketChangeGameState extends ClientboundPacket {
    private final Reason reason;
    private final float value;

    public PacketChangeGameState(InByteBuffer buffer) {
        this.reason = Reason.byId(buffer.readByte(), buffer.getVersionId());
        this.value = buffer.readFloat();
    }

    @Override
    public void handle(Connection connection) {
        ChangeGameStateEvent event = new ChangeGameStateEvent(connection, this);
        if (connection.fireEvent(event)) {
            return;
        }

        Log.game(switch (getReason()) {
            case START_RAINING -> "Received weather packet: Starting rain...";
            case STOP_RAINING -> "Received weather packet: Stopping rain...";
            case CHANGE_GAMEMODE -> String.format("Received game mode change: Now in %s", Gamemodes.byId(getIntValue()));
            default -> "";
        });

        switch (getReason()) {
            case STOP_RAINING -> connection.getWorld().setRaining(false);
            case START_RAINING -> connection.getWorld().setRaining(true);
            case CHANGE_GAMEMODE -> connection.getPlayer().getEntity().setGamemode(Gamemodes.byId(getIntValue()));
        }
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
        NO_RESPAWN_BLOCK_AVAILABLE(Map.of(LOWEST_VERSION_SUPPORTED, 0)),
        START_RAINING(Map.of(LOWEST_VERSION_SUPPORTED, 1, V_1_14_4, 2, V_1_15_2, 1)), // ToDo: when exactly did these 2 switch?
        STOP_RAINING(Map.of(LOWEST_VERSION_SUPPORTED, 2, V_1_14_4, 1, V_1_15_2, 2)),
        CHANGE_GAMEMODE(Map.of(LOWEST_VERSION_SUPPORTED, 3)),
        ENTER_CREDITS(Map.of(LOWEST_VERSION_SUPPORTED, 4)),
        DEMO_MESSAGES(Map.of(LOWEST_VERSION_SUPPORTED, 5)),
        ARROW_HITTING_PLAYER(Map.of(LOWEST_VERSION_SUPPORTED, 6)),
        RAIN_LEVEL_CHANGE(Map.of(LOWEST_VERSION_SUPPORTED, 7)),
        THUNDER_LEVEL_CHANGE(Map.of(LOWEST_VERSION_SUPPORTED, 8)),
        PUFFERFISH_STING(Map.of(LOWEST_VERSION_SUPPORTED, 9)),
        GUARDIAN_ELDER_EFFECT(Map.of(LOWEST_VERSION_SUPPORTED, 10)),
        IMMEDIATE_RESPAWN(Map.of(V_19W36A, 11));

        private final VersionValueMap<Integer> valueMap;

        Reason(Map<Integer, Integer> values) {
            this.valueMap = new VersionValueMap<>(values);
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
