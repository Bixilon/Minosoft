/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.protocol.versions;


import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.Protocol;

public class Protocol_1_14_4 extends Protocol {

    public Protocol_1_14_4() {
        registerPacket(Packets.Clientbound.PLAY_SPAWN_ENTITY);
        registerPacket(Packets.Clientbound.PLAY_SPAWN_EXPERIENCE_ORB);
        registerPacket(Packets.Clientbound.PLAY_SPAWN_WEATHER_ENTITY);
        registerPacket(Packets.Clientbound.PLAY_SPAWN_MOB);
        registerPacket(Packets.Clientbound.PLAY_SPAWN_PAINTING);
        registerPacket(Packets.Clientbound.PLAY_SPAWN_PLAYER);
        registerPacket(Packets.Clientbound.PLAY_ENTITY_ANIMATION);
        registerPacket(Packets.Clientbound.PLAY_STATISTICS);
        registerPacket(Packets.Clientbound.PLAY_BLOCK_BREAK_ANIMATION);
        registerPacket(Packets.Clientbound.PLAY_BLOCK_ENTITY_DATA);
        registerPacket(Packets.Clientbound.PLAY_BLOCK_ACTION);
        registerPacket(Packets.Clientbound.PLAY_BLOCK_CHANGE);
        registerPacket(Packets.Clientbound.PLAY_BOSS_BAR);
        registerPacket(Packets.Clientbound.PLAY_SERVER_DIFFICULTY);
        registerPacket(Packets.Clientbound.PLAY_CHAT_MESSAGE);
        registerPacket(Packets.Clientbound.PLAY_MULTIBLOCK_CHANGE);
        registerPacket(Packets.Clientbound.PLAY_TAB_COMPLETE);
        registerPacket(Packets.Clientbound.PLAY_DECLARE_COMMANDS);
        registerPacket(Packets.Clientbound.PLAY_WINDOW_CONFIRMATION);
        registerPacket(Packets.Clientbound.PLAY_CLOSE_WINDOW);
        registerPacket(Packets.Clientbound.PLAY_WINDOW_ITEMS);
        registerPacket(Packets.Clientbound.PLAY_WINDOW_PROPERTY);
        registerPacket(Packets.Clientbound.PLAY_SET_SLOT);
        registerPacket(Packets.Clientbound.PLAY_SET_COOLDOWN);
        registerPacket(Packets.Clientbound.PLAY_PLUGIN_MESSAGE);
        registerPacket(Packets.Clientbound.PLAY_NAMED_SOUND_EFFECT);
        registerPacket(Packets.Clientbound.PLAY_DISCONNECT);
        registerPacket(Packets.Clientbound.PLAY_ENTITY_STATUS);
        registerPacket(Packets.Clientbound.PLAY_EXPLOSION);
        registerPacket(Packets.Clientbound.PLAY_UNLOAD_CHUNK);
        registerPacket(Packets.Clientbound.PLAY_CHANGE_GAME_STATE);
        registerPacket(Packets.Clientbound.PLAY_OPEN_HORSE_WINDOW);
        registerPacket(Packets.Clientbound.PLAY_KEEP_ALIVE);
        registerPacket(Packets.Clientbound.PLAY_CHUNK_DATA);
        registerPacket(Packets.Clientbound.PLAY_EFFECT);
        registerPacket(Packets.Clientbound.PLAY_PARTICLE);
        registerPacket(Packets.Clientbound.PLAY_UPDATE_LIGHT);
        registerPacket(Packets.Clientbound.PLAY_JOIN_GAME);
        registerPacket(Packets.Clientbound.PLAY_MAP_DATA);
        registerPacket(Packets.Clientbound.PLAY_TRADE_LIST);
        registerPacket(Packets.Clientbound.PLAY_ENTITY_MOVEMENT);
        registerPacket(Packets.Clientbound.PLAY_ENTITY_MOVEMENT_AND_ROTATION);
        registerPacket(Packets.Clientbound.PLAY_ENTITY_ROTATION);
        increasePacketCounter(Packets.Clientbound.class);
        registerPacket(Packets.Clientbound.PLAY_VEHICLE_MOVEMENT);
        registerPacket(Packets.Clientbound.PLAY_OPEN_BOOK);
        registerPacket(Packets.Clientbound.PLAY_OPEN_WINDOW);
        registerPacket(Packets.Clientbound.PLAY_OPEN_SIGN_EDITOR);
        registerPacket(Packets.Clientbound.PLAY_CRAFT_RECIPE_RESPONSE);
        registerPacket(Packets.Clientbound.PLAY_PLAYER_ABILITIES);
        registerPacket(Packets.Clientbound.PLAY_COMBAT_EVENT);
        registerPacket(Packets.Clientbound.PLAY_PLAYER_INFO);
        registerPacket(Packets.Clientbound.PLAY_FACE_PLAYER);
        registerPacket(Packets.Clientbound.PLAY_PLAYER_POSITION_AND_ROTATION);
        registerPacket(Packets.Clientbound.PLAY_UNLOCK_RECIPES);
        registerPacket(Packets.Clientbound.PLAY_DESTROY_ENTITIES);
        registerPacket(Packets.Clientbound.PLAY_REMOVE_ENTITY_EFFECT);
        registerPacket(Packets.Clientbound.PLAY_RESOURCE_PACK_SEND);
        registerPacket(Packets.Clientbound.PLAY_RESPAWN);
        registerPacket(Packets.Clientbound.PLAY_ENTITY_HEAD_ROTATION);
        registerPacket(Packets.Clientbound.PLAY_SELECT_ADVANCEMENT_TAB);
        registerPacket(Packets.Clientbound.PLAY_WORLD_BORDER);
        registerPacket(Packets.Clientbound.PLAY_CAMERA);
        registerPacket(Packets.Clientbound.PLAY_HELD_ITEM_CHANGE);
        registerPacket(Packets.Clientbound.PLAY_UPDATE_VIEW_POSITION);
        registerPacket(Packets.Clientbound.PLAY_UPDATE_VIEW_DISTANCE);
        registerPacket(Packets.Clientbound.PLAY_DISPLAY_SCOREBOARD);
        registerPacket(Packets.Clientbound.PLAY_ENTITY_METADATA);
        registerPacket(Packets.Clientbound.PLAY_ATTACH_ENTITY);
        registerPacket(Packets.Clientbound.PLAY_ENTITY_VELOCITY);
        registerPacket(Packets.Clientbound.PLAY_ENTITY_EQUIPMENT);
        registerPacket(Packets.Clientbound.PLAY_SET_EXPERIENCE);
        registerPacket(Packets.Clientbound.PLAY_UPDATE_HEALTH);
        registerPacket(Packets.Clientbound.PLAY_SCOREBOARD_OBJECTIVE);
        registerPacket(Packets.Clientbound.PLAY_SET_PASSENGERS);
        registerPacket(Packets.Clientbound.PLAY_TEAMS);
        registerPacket(Packets.Clientbound.PLAY_UPDATE_SCORE);
        registerPacket(Packets.Clientbound.PLAY_SPAWN_POSITION);
        registerPacket(Packets.Clientbound.PLAY_TIME_UPDATE);
        registerPacket(Packets.Clientbound.PLAY_TITLE);
        registerPacket(Packets.Clientbound.PLAY_ENTITY_SOUND_EFFECT);
        registerPacket(Packets.Clientbound.PLAY_SOUND_EFFECT);
        registerPacket(Packets.Clientbound.PLAY_STOP_SOUND);
        registerPacket(Packets.Clientbound.PLAY_LIST_HEADER_AND_FOOTER);
        registerPacket(Packets.Clientbound.PLAY_NBT_QUERY_RESPONSE);
        increasePacketCounter(Packets.Clientbound.class);
        registerPacket(Packets.Clientbound.PLAY_ENTITY_TELEPORT);
        registerPacket(Packets.Clientbound.PLAY_ADVANCEMENTS);
        registerPacket(Packets.Clientbound.PLAY_ENTITY_PROPERTIES);
        registerPacket(Packets.Clientbound.PLAY_ENTITY_EFFECT);
        registerPacket(Packets.Clientbound.PLAY_DECLARE_RECIPES);
        registerPacket(Packets.Clientbound.PLAY_TAGS);
        registerPacket(Packets.Clientbound.PLAY_ACKNOWLEDGE_PLAYER_DIGGING);


        registerPacket(Packets.Serverbound.PLAY_TELEPORT_CONFIRM);
        registerPacket(Packets.Serverbound.PLAY_QUERY_BLOCK_NBT);
        registerPacket(Packets.Serverbound.PLAY_SET_DIFFICULTY);
        registerPacket(Packets.Serverbound.PLAY_CHAT_MESSAGE);
        registerPacket(Packets.Serverbound.PLAY_CLIENT_STATUS);
        registerPacket(Packets.Serverbound.PLAY_CLIENT_SETTINGS);
        registerPacket(Packets.Serverbound.PLAY_TAB_COMPLETE);
        registerPacket(Packets.Serverbound.PLAY_WINDOW_CONFIRMATION);
        registerPacket(Packets.Serverbound.PLAY_CLICK_WINDOW_BUTTON);
        registerPacket(Packets.Serverbound.PLAY_CLICK_WINDOW);
        registerPacket(Packets.Serverbound.PLAY_CLOSE_WINDOW);
        registerPacket(Packets.Serverbound.PLAY_PLUGIN_MESSAGE);
        registerPacket(Packets.Serverbound.PLAY_EDIT_BOOK);
        registerPacket(Packets.Serverbound.PLAY_ENTITY_NBT_REQUEST);
        registerPacket(Packets.Serverbound.PLAY_INTERACT_ENTITY);
        registerPacket(Packets.Serverbound.PLAY_KEEP_ALIVE);
        registerPacket(Packets.Serverbound.PLAY_LOCK_DIFFICULTY);
        registerPacket(Packets.Serverbound.PLAY_PLAYER_POSITION);
        registerPacket(Packets.Serverbound.PLAY_PLAYER_POSITION_AND_ROTATION);
        registerPacket(Packets.Serverbound.PLAY_PLAYER_ROTATION);
        increasePacketCounter(Packets.Serverbound.class);
        registerPacket(Packets.Serverbound.PLAY_VEHICLE_MOVE);
        registerPacket(Packets.Serverbound.PLAY_STEER_BOAT);
        registerPacket(Packets.Serverbound.PLAY_PICK_ITEM);
        registerPacket(Packets.Serverbound.PLAY_CRAFT_RECIPE_REQUEST);
        registerPacket(Packets.Serverbound.PLAY_PLAYER_ABILITIES);
        registerPacket(Packets.Serverbound.PLAY_PLAYER_DIGGING);
        registerPacket(Packets.Serverbound.PLAY_ENTITY_ACTION);
        registerPacket(Packets.Serverbound.PLAY_STEER_VEHICLE);
        registerPacket(Packets.Serverbound.PLAY_RECIPE_BOOK_DATA);
        registerPacket(Packets.Serverbound.PLAY_NAME_ITEM);
        registerPacket(Packets.Serverbound.PLAY_RESOURCE_PACK_STATUS);
        registerPacket(Packets.Serverbound.PLAY_ADVANCEMENT_TAB);
        registerPacket(Packets.Serverbound.PLAY_SELECT_TRADE);
        registerPacket(Packets.Serverbound.PLAY_SET_BEACON_EFFECT);
        registerPacket(Packets.Serverbound.PLAY_HELD_ITEM_CHANGE);
        registerPacket(Packets.Serverbound.PLAY_UPDATE_COMMAND_BLOCK);
        registerPacket(Packets.Serverbound.PLAY_UPDATE_COMMAND_BLOCK_MINECART);
        registerPacket(Packets.Serverbound.PLAY_CREATIVE_INVENTORY_ACTION);
        registerPacket(Packets.Serverbound.PLAY_UPDATE_JIGSAW_BLOCK);
        registerPacket(Packets.Serverbound.PLAY_UPDATE_STRUCTURE_BLOCK);
        registerPacket(Packets.Serverbound.PLAY_UPDATE_SIGN);
        registerPacket(Packets.Serverbound.PLAY_ANIMATION);
        registerPacket(Packets.Serverbound.PLAY_SPECTATE);
        registerPacket(Packets.Serverbound.PLAY_PLAYER_BLOCK_PLACEMENT);
        registerPacket(Packets.Serverbound.PLAY_USE_ITEM);
    }

    public int getProtocolVersionNumber() {
        return 498;
    }

    @Override
    public String getVersionString() {
        return "1.14.4";
    }

    @Override
    public String getReleaseName() {
        return "Village and Pillage";
    }
}
