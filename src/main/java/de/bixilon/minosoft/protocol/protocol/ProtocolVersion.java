package de.bixilon.minosoft.protocol.protocol;

public enum ProtocolVersion {
    VERSION_1_7_10(new Protocol_1_7_10());

    private int version;
    private Protocol protocol;

    ProtocolVersion(Protocol protocol) {
        this.protocol = protocol;
        this.version = protocol.getProtocolVersion();
    }

    public int getVersion() {
        return version;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public int getPacketCommand(Packets.Serverbound p) {
        return protocol.getPacketCommand(p);
    }

}
