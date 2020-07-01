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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.TextComponent;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.util.BitByte;

import java.util.UUID;

public class PacketBossBar implements ClientboundPacket {
    UUID uuid;
    BossBarAction action;

    //fields depend on action
    TextComponent title;
    float health;
    BossBarColor color;
    BossBarDivisions divisions;
    byte flags;


    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_9_4:
                uuid = buffer.readUUID();
                action = BossBarAction.byId(buffer.readVarInt());
                switch (action) {
                    case ADD:
                        title = buffer.readTextComponent();
                        health = buffer.readFloat();
                        color = BossBarColor.byId(buffer.readVarInt());
                        divisions = BossBarDivisions.byId(buffer.readVarInt());
                        flags = buffer.readByte();
                        break;
                    case REMOVE:
                        break;
                    case UPDATE_HEALTH:
                        health = buffer.readFloat();
                        break;
                    case UPDATE_TITLE:
                        title = buffer.readTextComponent();
                        break;
                    case UPDATE_STYLE:
                        color = BossBarColor.byId(buffer.readVarInt());
                        divisions = BossBarDivisions.byId(buffer.readVarInt());
                        break;
                    case UPDATE_FLAGS:
                        flags = buffer.readByte();
                        break;
                }
                return true;
        }

        return false;
    }

    @Override
    public void log() {
        switch (action) {
            case ADD:
                Log.protocol(String.format("Received boss bar (action=%s, uuid=%s, title=\"%s\", health=%s, color=%s, divisions=%s, dragonBar=%s, darkenSky=%s)", action.name(), uuid.toString(), title.getColoredMessage(), health, color.name(), divisions.name(), isDragonBar(), shouldDarkenSky()));
                break;
            case REMOVE:
                Log.protocol(String.format("Received boss bar (action=%s, uuid=%s)", action.name(), uuid.toString()));
                break;
            case UPDATE_HEALTH:
                Log.protocol(String.format("Received boss bar (action=%s, uuid=%s, health=%s)", action.name(), uuid.toString(), health));
                break;
            case UPDATE_TITLE:
                Log.protocol(String.format("Received boss bar (action=%s, uuid=%s, title=\"%s\")", action.name(), uuid.toString(), title.getColoredMessage()));
                break;
            case UPDATE_STYLE:
                Log.protocol(String.format("Received boss bar (action=%s, uuid=%s, color=%s, divisions=%s)", action.name(), uuid.toString(), color.name(), divisions.name()));
                break;
            case UPDATE_FLAGS:
                Log.protocol(String.format("Received boss bar (action=%s, uuid=%s, dragonBar=%s, darkenSky=%s)", action.name(), uuid.toString(), isDragonBar(), shouldDarkenSky()));
                break;
        }
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public UUID getUUID() {
        return uuid;
    }

    public BossBarAction getAction() {
        return action;
    }

    public BossBarDivisions getDivisions() {
        return divisions;
    }

    public BossBarColor getColor() {
        return color;
    }

    public float getHealth() {
        return health;
    }

    public TextComponent getTitle() {
        return title;
    }


    public byte getFlags() {
        return flags;
    }

    public boolean shouldDarkenSky() {
        return BitByte.isBitMask(flags, 0x01);
    }

    public boolean isDragonBar() {
        return BitByte.isBitMask(flags, 0x02);
    }

    public enum BossBarAction {
        ADD(0),
        REMOVE(1),
        UPDATE_HEALTH(2),
        UPDATE_TITLE(3),
        UPDATE_STYLE(4),
        UPDATE_FLAGS(5);


        final int id;

        BossBarAction(int id) {
            this.id = id;
        }

        public static BossBarAction byId(int id) {
            for (BossBarAction a : values()) {
                if (a.getId() == id) {
                    return a;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }

    public enum BossBarColor {
        PINK(0),
        BLUE(1),
        RED(2),
        GREEN(3),
        YELLOW(4),
        PURPLE(5),
        WHITE(6);


        final int id;

        BossBarColor(int id) {
            this.id = id;
        }

        public static BossBarColor byId(int id) {
            for (BossBarColor c : values()) {
                if (c.getId() == id) {
                    return c;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }

    public enum BossBarDivisions {
        NO_DIVISIONS(0),
        NOTCHES_6(1),
        NOTCHES_10(2),
        NOTCHES_12(3),
        NOTCHES_20(4);


        final int id;

        BossBarDivisions(int id) {
            this.id = id;
        }

        public static BossBarDivisions byId(int id) {
            for (BossBarDivisions d : values()) {
                if (d.getId() == id) {
                    return d;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
