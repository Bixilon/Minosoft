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

package de.bixilon.minosoft.protocol.protocol;

public class Packets {

    public enum Serverbound {
        HANDSHAKING_HANDSHAKE,
        STATUS_PING,
        STATUS_REQUEST,
        LOGIN_LOGIN_START,
        LOGIN_ENCRYPTION_RESPONSE,
        LOGIN_PLUGIN_RESPONSE,
        PLAY_TELEPORT_CONFIRM,
        PLAY_QUERY_BLOCK_NBT,
        PLAY_SET_DIFFICULTY,
        PLAY_CHAT_MESSAGE,
        PLAY_CLIENT_STATUS,
        PLAY_CLIENT_SETTINGS,
        PLAY_TAB_COMPLETE,
        PLAY_WINDOW_CONFIRMATION,
        PLAY_CLICK_WINDOW_BUTTON,
        PLAY_CLICK_WINDOW,
        PLAY_CLOSE_WINDOW,
        PLAY_PLUGIN_MESSAGE,
        PLAY_EDIT_BOOK,
        PLAY_ENTITY_NBT_REQUEST,
        PLAY_INTERACT_ENTITY,
        PLAY_KEEP_ALIVE,
        PLAY_LOCK_DIFFICULTY,
        PLAY_PLAYER_POSITION,
        PLAY_PLAYER_POSITION_AND_ROTATION,
        PLAY_PLAYER_ROTATION,
        PLAY_VEHICLE_MOVE,
        PLAY_STEER_BOAT,
        PLAY_PICK_ITEM,
        PLAY_CRAFT_RECIPE_REQUEST,
        PLAY_PLAYER_ABILITIES,
        PLAY_PLAYER_DIGGING,
        PLAY_ENTITY_ACTION,
        PLAY_STEER_VEHICLE,
        PLAY_RECIPE_BOOK_DATA,
        PLAY_NAME_ITEM,
        PLAY_RESOURCE_PACK_STATUS,
        PLAY_ADVANCEMENT_TAB,
        PLAY_SELECT_TRADE,
        PLAY_SET_BEACON_EFFECT,
        PLAY_HELD_ITEM_CHANGE,
        PLAY_UPDATE_COMMAND_BLOCK,
        PLAY_CREATIVE_INVENTORY_ACTION,
        PLAY_UPDATE_JIGSAW_BLOCK,
        PLAY_UPDATE_STRUCTURE_BLOCK,
        PLAY_UPDATE_SIGN,
        PLAY_ANIMATION,
        PLAY_SPECTATE,
        PLAY_PLAYER_BLOCK_PLACEMENT,
        PLAY_USE_ITEM,
        PLAY_UPDATE_COMMAND_BLOCK_MINECART;

        final ConnectionState state;

        Serverbound() {
            this.state = ConnectionState.valueOf(name().split("_")[0]);

        }

        public ConnectionState getState() {
            return state;
        }

    }

    public enum Clientbound {
        STATUS_RESPONSE,
        STATUS_PONG,
        LOGIN_DISCONNECT,
        LOGIN_ENCRYPTION_REQUEST,
        LOGIN_LOGIN_SUCCESS,
        LOGIN_SET_COMPRESSION,
        LOGIN_PLUGIN_REQUEST,
        PLAY_SPAWN_MOB,
        PLAY_SPAWN_EXPERIENCE_ORB,
        PLAY_SPAWN_WEATHER_ENTITY,
        PLAY_SPAWN_LIVING_ENTITY,
        PLAY_SPAWN_PAINTING,
        PLAY_SPAWN_PLAYER,
        PLAY_ENTITY_ANIMATION,
        PLAY_STATS_RESPONSE,
        PLAY_ACKNOWLEDGE_PLAYER_DIGGING,
        PLAY_BLOCK_BREAK_ANIMATION,
        PLAY_BLOCK_ENTITY_DATA,
        PLAY_BLOCK_ACTION,
        PLAY_BLOCK_CHANGE,
        PLAY_BOSS_BAR,
        PLAY_SERVER_DIFFICULTY,
        PLAY_CHAT_MESSAGE,
        PLAY_MULTIBLOCK_CHANGE,
        PLAY_TAB_COMPLETE,
        PLAY_DECLARE_COMMANDS,
        PLAY_WINDOW_CONFIRMATION,
        PLAY_CLOSE_WINDOW,
        PLAY_WINDOW_ITEMS,
        PLAY_WINDOW_PROPERTY,
        PLAY_SET_SLOT,
        PLAY_SET_COOLDOWN,
        PLAY_PLUGIN_MESSAGE,
        PLAY_NAMED_SOUND_EFFECT,
        PLAY_DISCONNECT,
        PLAY_ENTITY_STATUS,
        PLAY_EXPLOSION,
        PLAY_UNLOAD_CHUNK,
        PLAY_CHANGE_GAME_STATE,
        PLAY_OPEN_HORSE_WINDOW,
        PLAY_KEEP_ALIVE,
        PLAY_CHUNK_DATA,
        PLAY_EFFECT,
        PLAY_PARTICLE,
        PLAY_UPDATE_LIGHT,
        PLAY_JOIN_GAME,
        PLAY_MAP_DATA,
        PLAY_TRADE_LIST,
        PLAY_ENTITY_MOVEMENT_AND_ROTATION,
        PLAY_ENTITY_ROTATION,
        PLAY_ENTITY_MOVEMENT,
        PLAY_VEHICLE_MOVEMENT,
        PLAY_OPEN_BOOK,
        PLAY_OPEN_WINDOW,
        PLAY_OPEN_SIGN_EDITOR,
        PLAY_CRAFT_RECIPE_RESPONSE,
        PLAY_PLAYER_ABILITIES,
        PLAY_COMBAT_EVENT,
        PLAY_PLAYER_INFO,
        PLAY_FACE_PLAYER,
        PLAY_PLAYER_POSITION_AND_ROTATION,
        PLAY_UNLOCK_RECIPES,
        PLAY_DESTROY_ENTITIES,
        PLAY_REMOVE_ENTITY_EFFECT,
        PLAY_RESOURCE_PACK_SEND,
        PLAY_RESPAWN,
        PLAY_ENTITY_HEAD_ROTATION,
        PLAY_SELECT_ADVANCEMENT_TAB,
        PLAY_WORLD_BORDER,
        PLAY_CAMERA,
        PLAY_HELD_ITEM_CHANGE,
        PLAY_UPDATE_VIEW_POSITION,
        PLAY_DISPLAY_SCOREBOARD,
        PLAY_ENTITY_METADATA,
        PLAY_ATTACH_ENTITY,
        PLAY_ENTITY_VELOCITY,
        PLAY_ENTITY_EQUIPMENT,
        PLAY_SET_EXPERIENCE,
        PLAY_UPDATE_HEALTH,
        PLAY_SCOREBOARD_OBJECTIVE,
        PLAY_SET_PASSENGERS,
        PLAY_TEAMS,
        PLAY_UPDATE_SCORE,
        PLAY_SPAWN_POSITION,
        PLAY_TIME_UPDATE,
        PLAY_ENTITY_SOUND_EFFECT,
        PLAY_SOUND_EFFECT,
        PLAY_STOP_SOUND,
        PLAY_LIST_HEADER_AND_FOOTER,
        PLAY_NBT_QUERY_RESPONSE,
        PLAY_COLLECT_ITEM,
        PLAY_ENTITY_TELEPORT,
        PLAY_ADVANCEMENTS,
        PLAY_ENTITY_PROPERTIES,
        PLAY_ENTITY_EFFECT,
        PLAY_DECLARE_RECIPES,
        PLAY_TAGS,
        PLAY_USE_BED,
        PLAY_UPDATE_VIEW_DISTANCE,
        PLAY_CHUNK_BULK,
        PLAY_UPDATE_SIGN,
        PLAY_STATISTICS,
        PLAY_SPAWN_OBJECT,
        PLAY_TITLE;

        final ConnectionState state;

        Clientbound() {
            this.state = ConnectionState.valueOf(name().split("_")[0]);

        }

        public ConnectionState getState() {
            return state;
        }
    }
}
