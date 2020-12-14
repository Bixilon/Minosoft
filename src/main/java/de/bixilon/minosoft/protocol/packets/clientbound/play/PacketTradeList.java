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

import de.bixilon.minosoft.data.Trade;
import de.bixilon.minosoft.data.entities.VillagerData;
import de.bixilon.minosoft.data.inventory.Slot;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketTradeList implements ClientboundPacket {
    int windowId;
    Trade[] trades;
    VillagerData.VillagerLevels level;
    int experience;
    boolean isRegularVillager;
    boolean canRestock;

    @Override
    public boolean read(InByteBuffer buffer) {
        windowId = buffer.readVarInt();
        trades = new Trade[buffer.readByte()];
        for (int i = 0; i < trades.length; i++) {
            Slot input1 = buffer.readSlot();
            Slot input2 = null;
            if (buffer.readBoolean()) {
                // second input available
                input2 = buffer.readSlot();
            }
            boolean enabled = !buffer.readBoolean();
            int usages = buffer.readInt();
            int maxUsages = buffer.readInt();
            int xp = buffer.readInt();
            int specialPrice = buffer.readInt();
            float priceMultiplier = buffer.readFloat();
            int demand = 0;
            if (buffer.getVersionId() >= 495) {
                demand = buffer.readInt();
            }
            trades[i] = new Trade(input1, input2, enabled, usages, maxUsages, xp, specialPrice, priceMultiplier, demand);
        }
        level = VillagerData.VillagerLevels.values()[buffer.readVarInt()];
        experience = buffer.readVarInt();
        isRegularVillager = buffer.readBoolean();
        if (buffer.getVersionId() >= 486) {
            canRestock = buffer.readBoolean();
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received select trade packet (windowId=%d, tradeLength=%d, level=%s, experience=%d, regularVillager=%s, canRestock=%s)", windowId, trades.length, level, experience, isRegularVillager, canRestock));
    }

    public int getWindowId() {
        return windowId;
    }

    public Trade[] getTrades() {
        return trades;
    }

    public VillagerData.VillagerLevels getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    public boolean isRegularVillager() {
        return isRegularVillager;
    }

    public boolean canRestock() {
        return canRestock;
    }
}
