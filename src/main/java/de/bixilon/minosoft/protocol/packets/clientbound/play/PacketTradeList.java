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
import de.bixilon.minosoft.data.entities.entities.npc.villager.data.VillagerLevels;
import de.bixilon.minosoft.data.inventory.ItemStack;
import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_14_3_PRE1;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_14_4_PRE5;

public class PacketTradeList extends PlayClientboundPacket {
    int windowId;
    Trade[] trades;
    VillagerLevels level;
    int experience;
    boolean isRegularVillager;
    boolean canRestock;

    public PacketTradeList(PlayInByteBuffer buffer) {
        this.windowId = buffer.readVarInt();
        this.trades = new Trade[buffer.readByte()];
        for (int i = 0; i < this.trades.length; i++) {
            ItemStack input1 = buffer.readItemStack();
            ItemStack input2 = null;
            if (buffer.readBoolean()) {
                // second input available
                input2 = buffer.readItemStack();
            }
            boolean enabled = !buffer.readBoolean();
            int usages = buffer.readInt();
            int maxUsages = buffer.readInt();
            int xp = buffer.readInt();
            int specialPrice = buffer.readInt();
            float priceMultiplier = buffer.readFloat();
            int demand = 0;
            if (buffer.getVersionId() >= V_1_14_4_PRE5) {
                demand = buffer.readInt();
            }
            this.trades[i] = new Trade(input1, input2, enabled, usages, maxUsages, xp, specialPrice, priceMultiplier, demand);
        }
        this.level = VillagerLevels.Companion.getVALUES()[buffer.readVarInt()];
        this.experience = buffer.readVarInt();
        this.isRegularVillager = buffer.readBoolean();
        if (buffer.getVersionId() >= V_1_14_3_PRE1) {
            this.canRestock = buffer.readBoolean();
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received select trade packet (windowId=%d, tradeLength=%d, level=%s, experience=%d, regularVillager=%s, canRestock=%s)", this.windowId, this.trades.length, this.level, this.experience, this.isRegularVillager, this.canRestock));
    }

    public int getWindowId() {
        return this.windowId;
    }

    public Trade[] getTrades() {
        return this.trades;
    }

    public VillagerLevels getLevel() {
        return this.level;
    }

    public int getExperience() {
        return this.experience;
    }

    public boolean isRegularVillager() {
        return this.isRegularVillager;
    }

    public boolean canRestock() {
        return this.canRestock;
    }
}
