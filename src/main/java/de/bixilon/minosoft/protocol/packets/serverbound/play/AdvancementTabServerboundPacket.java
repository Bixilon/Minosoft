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

package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.protocol.packets.serverbound.PlayServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPlayByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class AdvancementTabServerboundPacket implements PlayServerboundPacket {
    private final AdvancementTabStatus action;
    private final String tabToOpen;

    public AdvancementTabServerboundPacket(AdvancementTabStatus action) {
        this.action = action;
        this.tabToOpen = null;
    }

    public AdvancementTabServerboundPacket(AdvancementTabStatus action, String tabToOpen) {
        this.action = action;
        this.tabToOpen = tabToOpen;
    }

    @Override
    public void write(OutPlayByteBuffer buffer) {
        buffer.writeVarInt(this.action.ordinal());
        if (this.action == AdvancementTabStatus.OPEN_TAB) {
            buffer.writeString(this.tabToOpen);
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending advancement tab packet (action=%s, tabToOpen=%s)", this.action, this.tabToOpen));
    }

    public enum AdvancementTabStatus {
        OPEN_TAB,
        CLOSE_TAB;

        private static final AdvancementTabStatus[] ADVANCEMENT_TAB_STATUSES = values();

        public static AdvancementTabStatus byId(int id) {
            return ADVANCEMENT_TAB_STATUSES[id];
        }
    }
}
