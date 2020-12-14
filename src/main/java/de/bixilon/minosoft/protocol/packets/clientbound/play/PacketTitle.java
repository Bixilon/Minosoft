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

import de.bixilon.minosoft.data.MapSet;
import de.bixilon.minosoft.data.VersionValueMap;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketTitle implements ClientboundPacket {
    TitleActions action;

    // fields depend on action
    ChatComponent text;
    ChatComponent subText;
    int fadeInTime;
    int stayTime;
    int fadeOutTime;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.action = TitleActions.byId(buffer.readVarInt(), buffer.getVersionId());
        switch (this.action) {
            case SET_TITLE -> this.text = buffer.readChatComponent();
            case SET_SUBTITLE -> this.subText = buffer.readChatComponent();
            case SET_TIMES_AND_DISPLAY -> {
                this.fadeInTime = buffer.readInt();
                this.stayTime = buffer.readInt();
                this.fadeOutTime = buffer.readInt();
            }
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        switch (this.action) {
            case SET_TITLE -> Log.protocol(String.format("[IN] Received title (action=%s, text=%s)", this.action, this.text.getANSIColoredMessage()));
            case SET_SUBTITLE -> Log.protocol(String.format("[IN] Received title (action=%s, subText=%s)", this.action, this.subText.getANSIColoredMessage()));
            case SET_TIMES_AND_DISPLAY -> Log.protocol(String.format("[IN] Received title (action=%s, fadeInTime=%d, stayTime=%d, fadeOutTime=%d)", this.action, this.fadeInTime, this.stayTime, this.fadeOutTime));
            case HIDE, RESET -> Log.protocol(String.format("[IN] Received title (action=%s)", this.action));
        }
    }

    public int getFadeInTime() {
        return this.fadeInTime;
    }

    public int getFadeOutTime() {
        return this.fadeOutTime;
    }

    public int getStayTime() {
        return this.stayTime;
    }

    public ChatComponent getSubText() {
        return this.subText;
    }

    public ChatComponent getText() {
        return this.text;
    }

    public TitleActions getAction() {
        return this.action;
    }

    public enum TitleActions {
        SET_TITLE(0),
        SET_SUBTITLE(1),
        SET_ACTION_BAR(new MapSet[]{new MapSet<>(302, 2)}),
        SET_TIMES_AND_DISPLAY(new MapSet[]{new MapSet<>(18, 2), new MapSet<>(302, 3)}),
        HIDE(new MapSet[]{new MapSet<>(18, 3), new MapSet<>(302, 4)}),
        RESET(new MapSet[]{new MapSet<>(18, 4), new MapSet<>(302, 5)});

        final VersionValueMap<Integer> valueMap;

        TitleActions(MapSet<Integer, Integer>[] values) {
            this.valueMap = new VersionValueMap<>(values, true);
        }

        TitleActions(int id) {
            this.valueMap = new VersionValueMap<>(id);
        }

        public static TitleActions byId(int id, int versionId) {
            for (TitleActions action : values()) {
                if (action.getId(versionId) == id) {
                    return action;
                }
            }
            return null;
        }

        public int getId(int versionId) {
            Integer ret = this.valueMap.get(versionId);
            if (ret == null) {
                return -2;
            }
            return ret;
        }
    }
}
