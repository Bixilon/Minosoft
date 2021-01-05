/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.modding.event.events.BossBarChangeEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.BitByte;
import de.bixilon.minosoft.util.logging.Log;

import java.util.UUID;

public class PacketBossBar extends ClientboundPacket {
    UUID uuid;
    BossBarActions action;

    // fields depend on action
    ChatComponent title;
    float health;
    BossBarColors color;
    BossBarDivisions divisions;
    byte flags;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.uuid = buffer.readUUID();
        this.action = BossBarActions.byId(buffer.readVarInt());
        switch (this.action) {
            case ADD -> {
                this.title = buffer.readChatComponent();
                this.health = buffer.readFloat();
                this.color = BossBarColors.byId(buffer.readVarInt());
                this.divisions = BossBarDivisions.byId(buffer.readVarInt());
                this.flags = buffer.readByte();
            }
            case UPDATE_HEALTH -> this.health = buffer.readFloat();
            case UPDATE_TITLE -> this.title = buffer.readChatComponent();
            case UPDATE_STYLE -> {
                this.color = BossBarColors.byId(buffer.readVarInt());
                this.divisions = BossBarDivisions.byId(buffer.readVarInt());
            }
            case UPDATE_FLAGS -> this.flags = buffer.readByte();
        }
        return true;
    }

    @Override
    public void handle(Connection connection) {
        BossBarChangeEvent event = new BossBarChangeEvent(connection, this);
        if (connection.fireEvent(event)) {
            return;
        }
    }

    @Override
    public void log() {
        switch (this.action) {
            case ADD -> Log.protocol(String.format("[IN] Received boss bar (action=%s, uuid=%s, title=\"%s\", health=%s, color=%s, divisions=%s, dragonBar=%s, darkenSky=%s)", this.action, this.uuid.toString(), this.title.getANSIColoredMessage(), this.health, this.color, this.divisions, isDragonBar(), shouldDarkenSky()));
            case REMOVE -> Log.protocol(String.format("[IN] Received boss bar (action=%s, uuid=%s)", this.action, this.uuid.toString()));
            case UPDATE_HEALTH -> Log.protocol(String.format("[IN] Received boss bar (action=%s, uuid=%s, health=%s)", this.action, this.uuid.toString(), this.health));
            case UPDATE_TITLE -> Log.protocol(String.format("[IN] Received boss bar (action=%s, uuid=%s, title=\"%s\")", this.action, this.uuid.toString(), this.title.getANSIColoredMessage()));
            case UPDATE_STYLE -> Log.protocol(String.format("[IN] Received boss bar (action=%s, uuid=%s, color=%s, divisions=%s)", this.action, this.uuid.toString(), this.color, this.divisions));
            case UPDATE_FLAGS -> Log.protocol(String.format("[IN] Received boss bar (action=%s, uuid=%s, dragonBar=%s, darkenSky=%s)", this.action, this.uuid.toString(), isDragonBar(), shouldDarkenSky()));
        }
    }

    public boolean isDragonBar() {
        return BitByte.isBitMask(this.flags, 0x02);
    }

    public boolean shouldDarkenSky() {
        return BitByte.isBitMask(this.flags, 0x01);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public BossBarActions getAction() {
        return this.action;
    }

    public BossBarDivisions getDivisions() {
        return this.divisions;
    }

    public BossBarColors getColor() {
        return this.color;
    }

    public float getHealth() {
        return this.health;
    }

    public ChatComponent getTitle() {
        return this.title;
    }

    public byte getFlags() {
        return this.flags;
    }

    public boolean createFog() {
        return BitByte.isBitMask(this.flags, 0x04);
    }

    public enum BossBarActions {
        ADD,
        REMOVE,
        UPDATE_HEALTH,
        UPDATE_TITLE,
        UPDATE_STYLE,
        UPDATE_FLAGS;

        private static final BossBarActions[] BOSS_BAR_ACTIONS = values();

        public static BossBarActions byId(int id) {
            return BOSS_BAR_ACTIONS[id];
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

        private static final BossBarColors[] BOSS_BAR_COLORS = values();

        public static BossBarColors byId(int id) {
            return BOSS_BAR_COLORS[id];
        }
    }

    public enum BossBarDivisions {
        NO_DIVISIONS,
        NOTCHES_6,
        NOTCHES_10,
        NOTCHES_12,
        NOTCHES_20;

        private static final BossBarDivisions[] BOSS_BAR_DIVISIONS = values();

        public static BossBarDivisions byId(int id) {
            return BOSS_BAR_DIVISIONS[id];
        }
    }
}
