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

import de.bixilon.minosoft.game.datatypes.inventory.InventoryProperties;
import de.bixilon.minosoft.game.datatypes.inventory.InventoryTypes;
import de.bixilon.minosoft.game.datatypes.text.BaseComponent;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketOpenWindow implements ClientboundPacket {
    byte windowId;
    InventoryTypes type;
    BaseComponent title;
    byte slotCount;
    int entityId;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getProtocolId() < 6) {
            this.windowId = buffer.readByte();
            this.type = InventoryTypes.byId(buffer.readByte());
            this.title = buffer.readTextComponent();
            slotCount = buffer.readByte();
            if (!buffer.readBoolean()) {
                // no custom name
                title = null;
            }
            this.entityId = buffer.readInt();
            return true;
        }
        this.windowId = buffer.readByte();
        this.type = InventoryTypes.byName(buffer.readString());
        this.title = buffer.readTextComponent();
        if (buffer.getProtocolId() < 452 || buffer.getProtocolId() >= 464) {
            slotCount = buffer.readByte();
        }
        if (type == InventoryTypes.HORSE) {
            this.entityId = buffer.readInt();
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received inventory open packet (windowId=%d, type=%s, title=%s, entityId=%d, slotCount=%d)", windowId, type, title, entityId, slotCount));
    }

    public byte getSlotCount() {
        return slotCount;
    }

    public int getEntityId() {
        return entityId;
    }

    public BaseComponent getTitle() {
        return title;
    }

    public InventoryProperties getInventoryProperties() {
        return new InventoryProperties(getWindowId(), getType(), title, slotCount);
    }

    public byte getWindowId() {
        return windowId;
    }

    public InventoryTypes getType() {
        return type;
    }
}
