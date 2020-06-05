package de.bixilon.minosoft.protocol.protocol;

import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.clientbound.login.PacketEncryptionKeyRequest;
import de.bixilon.minosoft.protocol.packets.clientbound.login.PacketLoginDisconnect;
import de.bixilon.minosoft.protocol.packets.clientbound.login.PacketLoginSuccess;
import de.bixilon.minosoft.protocol.packets.clientbound.play.*;
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusPong;
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusResponse;

import java.util.HashMap;

public interface Protocol {
    HashMap<Packets.Clientbound, Class<? extends ClientboundPacket>> packetClassMapping = new HashMap<>();

    int getProtocolVersion();

    int getPacketCommand(Packets.Serverbound p);

    String getName();

    Packets.Clientbound getPacketByCommand(ConnectionState s, int command);

    static Class<? extends ClientboundPacket> getPacketByPacket(Packets.Clientbound p) {
        if (packetClassMapping.size() == 0) {
            // init
            initPacketClassMapping();
        }
        return packetClassMapping.get(p);
    }

    private static void initPacketClassMapping() {
        packetClassMapping.put(Packets.Clientbound.STATUS_RESPONSE, PacketStatusResponse.class);
        packetClassMapping.put(Packets.Clientbound.STATUS_PONG, PacketStatusPong.class);
        packetClassMapping.put(Packets.Clientbound.LOGIN_ENCRYPTION_REQUEST, PacketEncryptionKeyRequest.class);
        packetClassMapping.put(Packets.Clientbound.LOGIN_LOGIN_SUCCESS, PacketLoginSuccess.class);
        packetClassMapping.put(Packets.Clientbound.LOGIN_DISCONNECT, PacketLoginDisconnect.class);

        packetClassMapping.put(Packets.Clientbound.PLAY_JOIN_GAME, PacketJoinGame.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_PLAYER_INFO, PacketPlayerInfo.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_TIME_UPDATE, PacketTimeUpdate.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_KEEP_ALIVE, PacketKeepAlive.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_CHUNK_BULK, PacketChunkBulk.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_UPDATE_HEALTH, PacketUpdateHealth.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_PLUGIN_MESSAGE, PacketPluginMessageReceiving.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SPAWN_POSITION, PacketSpawnLocation.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_CHAT_MESSAGE, PacketChatMessage.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_DISCONNECT, PacketDisconnect.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_HELD_ITEM_CHANGE, PacketHeldItemChangeReceiving.class);
        packetClassMapping.put(Packets.Clientbound.PLAY_SET_EXPERIENCE, PacketSetExperience.class);
    }
}