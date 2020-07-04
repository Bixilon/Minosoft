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
import de.bixilon.minosoft.game.datatypes.inventory.InventoryProperties;
import de.bixilon.minosoft.game.datatypes.inventory.InventoryType;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketOpenWindow implements ClientboundPacket {
    byte windowId;
    InventoryType type;
    TextComponent title;
    byte slotCount;
    int entityId;


    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
                this.windowId = buffer.readByte();
                this.type = InventoryType.byId(buffer.readByte());
                this.title = buffer.readTextComponent();
                slotCount = buffer.readByte();
                if (!buffer.readBoolean()) {
                    // no custom name
                    title = null;
                }
                this.entityId = buffer.readInt();
                return true;
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
                this.windowId = buffer.readByte();
                this.type = InventoryType.byName(buffer.readString());
                this.title = buffer.readTextComponent();
                slotCount = buffer.readByte();
                if (type == InventoryType.HORSE) {
                    this.entityId = buffer.readInt();
                }
                return true;
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received inventory open packet (windowId=%d, type=%s, title=%s, entityId=%d, slotCount=%d)", windowId, type.name(), ((title == null) ? "null" : title), entityId, slotCount));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public byte getSlotCount() {
        return slotCount;
    }

    public byte getWindowId() {
        return windowId;
    }

    public int getEntityId() {
        return entityId;
    }

    public TextComponent getTitle() {
        return title;
    }

    public InventoryType getType() {
        return type;
    }

    public InventoryProperties getInventoryProperties() {
        return new InventoryProperties(getWindowId(), getType(), title, slotCount);
    }
}
