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

import de.bixilon.minosoft.game.datatypes.text.BaseComponent;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.util.BitByte;

import java.util.UUID;

public class PacketBossBar implements ClientboundPacket {
    UUID uuid;
    BossBarActions action;

    //fields depend on action
    BaseComponent title;
    float health;
    BossBarColors color;
    BossBarDivisions divisions;
    byte flags;

    @Override
    public boolean read(InByteBuffer buffer) {
        uuid = buffer.readUUID();
        action = BossBarActions.byId(buffer.readVarInt());
        switch (action) {
            case ADD -> {
                title = buffer.readTextComponent();
                health = buffer.readFloat();
                color = BossBarColors.byId(buffer.readVarInt());
                divisions = BossBarDivisions.byId(buffer.readVarInt());
                flags = buffer.readByte();
            }
            case UPDATE_HEALTH -> health = buffer.readFloat();
            case UPDATE_TITLE -> title = buffer.readTextComponent();
            case UPDATE_STYLE -> {
                color = BossBarColors.byId(buffer.readVarInt());
                divisions = BossBarDivisions.byId(buffer.readVarInt());
            }
            case UPDATE_FLAGS -> flags = buffer.readByte();
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        switch (action) {
            case ADD -> Log.protocol(String.format("Received boss bar (action=%s, uuid=%s, title=\"%s\", health=%s, color=%s, divisions=%s, dragonBar=%s, darkenSky=%s)", action, uuid.toString(), title.getANSIColoredMessage(), health, color, divisions, isDragonBar(), shouldDarkenSky()));
            case REMOVE -> Log.protocol(String.format("Received boss bar (action=%s, uuid=%s)", action, uuid.toString()));
            case UPDATE_HEALTH -> Log.protocol(String.format("Received boss bar (action=%s, uuid=%s, health=%s)", action, uuid.toString(), health));
            case UPDATE_TITLE -> Log.protocol(String.format("Received boss bar (action=%s, uuid=%s, title=\"%s\")", action, uuid.toString(), title.getANSIColoredMessage()));
            case UPDATE_STYLE -> Log.protocol(String.format("Received boss bar (action=%s, uuid=%s, color=%s, divisions=%s)", action, uuid.toString(), color, divisions));
            case UPDATE_FLAGS -> Log.protocol(String.format("Received boss bar (action=%s, uuid=%s, dragonBar=%s, darkenSky=%s)", action, uuid.toString(), isDragonBar(), shouldDarkenSky()));
        }
    }

    public boolean isDragonBar() {
        return BitByte.isBitMask(flags, 0x02);
    }

    public boolean shouldDarkenSky() {
        return BitByte.isBitMask(flags, 0x01);
    }

    public UUID getUUID() {
        return uuid;
    }

    public BossBarActions getAction() {
        return action;
    }

    public BossBarDivisions getDivisions() {
        return divisions;
    }

    public BossBarColors getColor() {
        return color;
    }

    public float getHealth() {
        return health;
    }

    public BaseComponent getTitle() {
        return title;
    }

    public byte getFlags() {
        return flags;
    }

    public boolean createFog() {
        return BitByte.isBitMask(flags, 0x04);
    }

    public enum BossBarActions {
        ADD,
        REMOVE,
        UPDATE_HEALTH,
        UPDATE_TITLE,
        UPDATE_STYLE,
        UPDATE_FLAGS;

        public static BossBarActions byId(int id) {
            return values()[id];
        }

        public int getId() {
            return ordinal();
        }
    }

    public enum BossBarColors {
        PINK,
        BLUE,
        RED,
        GREEN,
        YELLOW,
        PURPLE,
        WHITE;

        public static BossBarColors byId(int id) {
            return values()[id];
        }

        public int getId() {
            return ordinal();
        }
    }

    public enum BossBarDivisions {
        NO_DIVISIONS,
        NOTCHES_6,
        NOTCHES_10,
        NOTCHES_12,
        NOTCHES_20;

        public static BossBarDivisions byId(int id) {
            return values()[id];
        }

        public int getId() {
            return ordinal();
        }
    }
}
