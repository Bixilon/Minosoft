package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.game.datatypes.Difficulty;
import de.bixilon.minosoft.game.datatypes.Locale;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketClientSettings implements ServerboundPacket {

    public final Locale locale;
    public final byte renderDistance;

    public PacketClientSettings(Locale locale, int renderDistance) {
        this.locale = locale;
        this.renderDistance = (byte) renderDistance;
        log();
    }


    @Override
    public OutPacketBuffer write(ProtocolVersion v) {
        OutPacketBuffer buffer = new OutPacketBuffer(v.getPacketCommand(Packets.Serverbound.PLAY_CLIENT_SETTINGS));
        switch (v) {
            case VERSION_1_7_10:
                buffer.writeString(locale.getName()); // locale
                buffer.writeByte(renderDistance); // render Distance
                buffer.writeByte((byte) 0x00); // chat settings (nobody uses them)
                buffer.writeBoolean(true); // chat colors
                buffer.writeByte((byte) Difficulty.NORMAL.getId()); // difficulty
                buffer.writeBoolean(true); // cape
                break;
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending settings (%s, render distance: %s)", locale.getName(), renderDistance));
    }
}
