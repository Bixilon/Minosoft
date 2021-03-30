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

import de.bixilon.minosoft.data.inventory.ItemStack;
import de.bixilon.minosoft.data.player.advancements.Advancement;
import de.bixilon.minosoft.data.player.advancements.AdvancementDisplay;
import de.bixilon.minosoft.data.player.advancements.AdvancementProgress;
import de.bixilon.minosoft.data.player.advancements.CriterionProgress;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.BitByte;
import de.bixilon.minosoft.util.logging.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class PacketAdvancements extends ClientboundPacket {
    private final HashMap<String, Advancement> advancements = new HashMap<>();
    private final HashMap<String, AdvancementProgress> progresses = new HashMap<>();
    boolean reset;
    String[] toRemove;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.reset = buffer.readBoolean();
        int length = buffer.readVarInt();
        for (int i = 0; i < length; i++) {
            String advancementKey = buffer.readString();

            String parentName = null;
            if (buffer.readBoolean()) {
                parentName = buffer.readString();
            }
            AdvancementDisplay display = null;
            if (buffer.readBoolean()) {
                ChatComponent title = buffer.readChatComponent();
                ChatComponent description = buffer.readChatComponent();
                ItemStack icon = buffer.readItemStack();
                AdvancementDisplay.AdvancementFrameTypes frameType = AdvancementDisplay.AdvancementFrameTypes.byId(buffer.readVarInt());
                int flags = buffer.readInt();
                String backgroundTexture = null;
                if (BitByte.isBitMask(flags, 0x01)) {
                    backgroundTexture = buffer.readString();
                }
                float x = buffer.readFloat();
                float y = buffer.readFloat();
                display = new AdvancementDisplay(title, description, icon, frameType, flags, backgroundTexture, x, y);
            }
            int criteriaCount = buffer.readVarInt();
            ArrayList<String> criteria = new ArrayList<>();
            for (int ii = 0; ii < criteriaCount; ii++) {
                criteria.add(buffer.readString());
            }
            int requirementsCount = buffer.readVarInt();
            ArrayList<String[]> requirements = new ArrayList<>();
            for (int ii = 0; ii < requirementsCount; ii++) {
                String[] requirement = new String[buffer.readVarInt()];
                for (int iii = 0; iii < requirement.length; iii++) {
                    requirement[iii] = buffer.readString();
                }
                requirements.add(requirement);
            }
            this.advancements.put(advancementKey, new Advancement(parentName, display, criteria, requirements));
        }
        this.toRemove = new String[buffer.readVarInt()];
        for (int i = 0; i < this.toRemove.length; i++) {
            this.toRemove[i] = buffer.readString();
        }
        int progressesLength = buffer.readVarInt();
        for (int i = 0; i < progressesLength; i++) {
            HashMap<String, CriterionProgress> progress = new HashMap<>();
            String progressName = buffer.readString();
            int criterionLength = buffer.readVarInt();
            for (int ii = 0; ii < criterionLength; ii++) {
                String criterionName = buffer.readString();
                boolean archived = buffer.readBoolean();
                Long archiveTime = null;
                if (archived) {
                    archiveTime = buffer.readLong();
                }
                CriterionProgress criterionProgress = new CriterionProgress(archived, archiveTime);
                progress.put(criterionName, criterionProgress);
            }
            this.progresses.put(progressName, new AdvancementProgress(progress));
        }

        return true;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Receiving advancements (reset=%s, advancements=%s, progresses=%s)", this.reset, this.advancements.size(), this.progresses.size()));
    }

    public boolean isReset() {
        return this.reset;
    }

    public HashMap<String, Advancement> getAdvancements() {
        return this.advancements;
    }

    public HashMap<String, AdvancementProgress> getProgresses() {
        return this.progresses;
    }
}
