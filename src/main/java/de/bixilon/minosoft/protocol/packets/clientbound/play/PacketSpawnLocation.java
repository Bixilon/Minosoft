package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.player.Location;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketSpawnLocation implements ClientboundPacket {
    Location loc;


    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                int x = buffer.readInteger();
                int y = buffer.readInteger();
                int z = buffer.readInteger();
                loc = new Location(x, y, z);
                break;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received spawn location %s %s %s", loc.getX(), loc.getY(), loc.getZ()));
    }

    public Location getSpawnLocation() {
        return loc;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
