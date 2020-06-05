package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketHeldItemChangeReceiving implements ClientboundPacket {
    byte slot;

    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                slot = buffer.readByte();
                break;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Slot change received. Now on slot %s", slot));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public byte getSlot() {
        return slot;
    }
}
