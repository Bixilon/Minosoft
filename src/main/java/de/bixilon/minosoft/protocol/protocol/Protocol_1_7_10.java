package de.bixilon.minosoft.protocol.protocol;


import java.util.HashMap;
import java.util.Map;

public class Protocol_1_7_10 implements Protocol {

    public HashMap<Packets.Serverbound, Integer> serverboundPacketMapping;
    public HashMap<Packets.Clientbound, Integer> clientboundPacketMapping;

    Protocol_1_7_10() {
        // serverbound
        serverboundPacketMapping = new HashMap<>();
        // handshake
        serverboundPacketMapping.put(Packets.Serverbound.HANDSHAKING_HANDSHAKE, 0x00);
        // status
        serverboundPacketMapping.put(Packets.Serverbound.STATUS_REQUEST, 0x00);
        serverboundPacketMapping.put(Packets.Serverbound.STATUS_PING, 0x01);
        // login
        serverboundPacketMapping.put(Packets.Serverbound.LOGIN_LOGIN_START, 0x00);
        serverboundPacketMapping.put(Packets.Serverbound.LOGIN_ENCRYPTION_RESPONSE, 0x01);
        // play
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_KEEP_ALIVE, 0x00);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_CHAT_MESSAGE, 0x01);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_INTERACT_ENTITY, 0x02);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_PLAYER_POSITION, 0x04);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_PLAYER_ROTATION, 0x05);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_PLAYER_POSITION_AND_ROTATION, 0x06);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_PLAYER_DIGGING, 0x07);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_PLAYER_BLOCK_PLACEMENT, 0x08);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_HELD_ITEM_CHANGE, 0x09);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_ANIMATION, 0x0A);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_ENTITY_ACTION, 0x0B);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_STEER_VEHICLE, 0x0C);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_CLOSE_WINDOW, 0x0D);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_CLICK_WINDOW, 0x0E);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_WINDOW_CONFIRMATION, 0x0F);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_CREATIVE_INVENTORY_ACTION, 0x10);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_CLICK_WINDOW_BUTTON, 0x11);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_UPDATE_SIGN, 0x12);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_PLAYER_ABILITIES, 0x13);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_TAB_COMPLETE, 0x14);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_CLIENT_SETTINGS, 0x15);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_CLIENT_STATUS, 0x16);
        serverboundPacketMapping.put(Packets.Serverbound.PLAY_PLUGIN_MESSAGE, 0x17);


        clientboundPacketMapping = new HashMap<>();
        // status
        clientboundPacketMapping.put(Packets.Clientbound.STATUS_RESPONSE, 0x00);
        clientboundPacketMapping.put(Packets.Clientbound.STATUS_PONG, 0x01);
        // login
        clientboundPacketMapping.put(Packets.Clientbound.LOGIN_DISCONNECT, 0x00);
        clientboundPacketMapping.put(Packets.Clientbound.LOGIN_ENCRYPTION_REQUEST, 0x01);
        clientboundPacketMapping.put(Packets.Clientbound.LOGIN_LOGIN_SUCCESS, 0x02);
        // play
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_KEEP_ALIVE, 0x00);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_JOIN_GAME, 0x01);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_CHAT_MESSAGE, 0x02);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_TIME_UPDATE, 0x03);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_ENTITY_EQUIPMENT, 0x04);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_SPAWN_POSITION, 0x05);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_UPDATE_HEALTH, 0x06);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_RESPAWN, 0x07);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_PLAYER_POSITION_AND_LOOK, 0x08);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_HELD_ITEM_CHANGE, 0x09);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_USE_BED, 0x0A);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_ENTITY_ANIMATION, 0x0B);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_SPAWN_PLAYER, 0x0C);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_COLLECT_ITEM, 0x0E);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_SPAWN_ENTITY, 0x0F);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_SPAWN_PAINTING, 0x10);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_SPAWN_EXPERIENCE_ORB, 0x11);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_ENTITY_VELOCITY, 0x12);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_DESTROY_ENTITIES, 0x13);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_ENTITY_POSITION, 0x15);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_ENTITY_ROTATION, 0x16);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_ENTITY_POSITION_AND_ROTATION, 0x17);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_ENTITY_TELEPORT, 0x18);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_ENTITY_HEAD_LOOK, 0x19);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_ENTITY_STATUS, 0x1A);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_ATTACH_ENTITY, 0x1B);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_ENTITY_METADATA, 0x1C);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_ENTITY_EFFECT, 0x1D);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_REMOVE_ENTITY_EFFECT, 0x1E);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_SET_EXPERIENCE, 0x1F);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_ENTITY_PROPERTIES, 0x20);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_CHUNK_DATA, 0x21);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_MULTIBLOCK_CHANGE, 0x22);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_BLOCK_CHANGE, 0x23);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_BLOCK_ACTION, 0x24);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_BLOCK_BREAK_ANIMATION, 0x25);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_CHUNK_BULK, 0x26);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_EXPLOSION, 0x27);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_EFFECT, 0x28);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_SOUND_EFFECT, 0x29);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_PARTICLE, 0x2A);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_CHANGE_GAME_STATE, 0x2B);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_SPAWN_WEATHER_ENTITY, 0x2C);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_OPEN_WINDOW, 0x2D);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_CLOSE_WINDOW, 0x2F);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_WINDOW_ITEMS, 0x30);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_WINDOW_PROPERTY, 0x31);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_WINDOW_CONFIRMATION, 0x32);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_UPDATE_SIGN, 0x33);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_MAP_DATA, 0x34);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_BLOCK_ENTITY_DATA, 0x35);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_OPEN_SIGN_EDITOR, 0x36);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_STATISTICS, 0x37);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_PLAYER_INFO, 0x38);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_PLAYER_ABILITIES, 0x39);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_TAB_COMPLETE, 0x3A);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_SCOREBOARD_OBJECTIVE, 0x3B);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_UPDATE_SCORE, 0x3C);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_DISPLAY_SCOREBOARD, 0x3D);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_TEAMS, 0x3E);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_PLUGIN_MESSAGE, 0x3F);
        clientboundPacketMapping.put(Packets.Clientbound.PLAY_DISCONNECT, 0x40);

    }

    public int getProtocolVersion() {
        return 5;
    }

    public int getPacketCommand(Packets.Serverbound p) {
        return serverboundPacketMapping.get(p);
    }

    @Override
    public String getName() {
        return "1.7.10";
    }

    public Packets.Clientbound getPacketByCommand(ConnectionState s, int command) {
        for (Map.Entry<Packets.Clientbound, Integer> set : clientboundPacketMapping.entrySet()) {
            if (set.getValue() == command && set.getKey().name().startsWith(s.name())) {
                return set.getKey();
            }
        }
        return null;
    }

}
