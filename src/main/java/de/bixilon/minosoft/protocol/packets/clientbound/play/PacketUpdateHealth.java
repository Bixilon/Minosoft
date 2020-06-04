package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketUpdateHealth implements ClientboundPacket {
    float health;
    short food;
    float saturation;


    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                health = (float) (Math.round(buffer.readFloat() * 10) / 10.0);
                food = buffer.readShort();
                saturation = (float) (Math.round(buffer.readFloat() * 10) / 10.0);
                break;
        }
        log();
    }

    @Override
    public void log() {
        Log.protocol(String.format("Health update. Now at %s hearts and %s food level and %s saturation", health, food, saturation));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public short getFood() {
        return food;
    }

    public float getHealth() {
        return health;
    }

    public float getSaturation() {
        return saturation;
    }
}
