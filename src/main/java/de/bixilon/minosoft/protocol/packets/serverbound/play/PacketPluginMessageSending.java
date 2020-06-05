package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketPluginMessageSending implements ServerboundPacket {

    public final String channel;
    public final byte[] data;

    public PacketPluginMessageSending(String channel, byte[] data) {
        this.channel = channel;
        this.data = data;
    }


    @Override
    public OutPacketBuffer write(ProtocolVersion v) {
        log();
        OutPacketBuffer buffer = new OutPacketBuffer(v.getPacketCommand(Packets.Serverbound.PLAY_PLUGIN_MESSAGE));
        switch (v) {
            case VERSION_1_7_10:
                buffer.writeString(channel); // name
                buffer.writeShort((short) data.length); // length
                buffer.writeBytes(data); // data
                break;
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending data in plugin channel %s with a length of %s bytes", channel, data.length));
    }
}
