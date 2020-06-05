package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.GameMode;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketChangeGameState implements ClientboundPacket {
    Reason reason;
    float value;

    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                reason = Reason.byId(buffer.readByte());
                value = buffer.readFloat();
                break;
        }
        log();
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
                Log.game(String.format("Received game mode change: Now in %s", GameMode.byId(getValue().intValue()).name()));
                break;
            default:
                Log.protocol(String.format("Received game status change (%s)", getReason().name()));
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
        INVALID_BED(0),
        END_RAIN(1),
        START_RAIN(2),
        CHANGE_GAMEMODE(3),
        ENTER_CREDITS(4),
        DEMO_MESSAGES(5),
        ARROW_HITTING_PLAYER(6),
        FADE_VALUE(7),
        FADE_TIME(8);

        byte id;

        Reason(byte id) {
            this.id = id;
        }

        Reason(int id) {
            this.id = (byte) id;
        }

        public static Reason byId(byte id) {
            for (Reason g : values()) {
                if (g.getId() == id) {
                    return g;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
