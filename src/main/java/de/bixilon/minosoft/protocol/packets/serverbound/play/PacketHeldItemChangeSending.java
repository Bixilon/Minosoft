package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketHeldItemChangeSending implements ServerboundPacket {

    private final byte slot;

    public PacketHeldItemChangeSending(byte slot) {
        this.slot = slot;
        log();
    }

    public PacketHeldItemChangeSending(int slot) {
        this.slot = (byte) slot;
        log();
    }


    @Override
    public OutPacketBuffer write(ProtocolVersion v) {
        OutPacketBuffer buffer = new OutPacketBuffer(v.getPacketCommand(Packets.Serverbound.PLAY_HELD_ITEM_CHANGE));
        switch (v) {
            case VERSION_1_7_10:
                buffer.writeByte(slot);
                break;
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending slot selection: (%s)", slot));
    }
}
