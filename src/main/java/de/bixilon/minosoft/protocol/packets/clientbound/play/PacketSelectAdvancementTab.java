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

import de.bixilon.minosoft.data.ChangeableResourceLocation;
import de.bixilon.minosoft.data.mappings.ResourceLocation;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class PacketSelectAdvancementTab extends ClientboundPacket {
    private AdvancementTabs tab;

    public PacketSelectAdvancementTab(InByteBuffer buffer) {
        if (buffer.readBoolean()) {
            this.tab = AdvancementTabs.byResourceLocation(buffer.readResourceLocation(), buffer.getVersionId());
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received select advancement tab (tab=%s)", this.tab));
    }

    public AdvancementTabs getTab() {
        return this.tab;
    }

    public enum AdvancementTabs {
        STORY(new ChangeableResourceLocation("story/root")),
        NETHER(new ChangeableResourceLocation("nether/root")),
        END(new ChangeableResourceLocation("end/root")),
        ADVENTURE(new ChangeableResourceLocation("adventure/root")),
        HUSBANDRY(new ChangeableResourceLocation("husbandry/root"));

        private final ChangeableResourceLocation changeableResourceLocation;

        AdvancementTabs(ChangeableResourceLocation changeableResourceLocation) {
            this.changeableResourceLocation = changeableResourceLocation;
        }

        public static AdvancementTabs byResourceLocation(ResourceLocation resourceLocation, int versionId) {
            for (AdvancementTabs advancementTab : values()) {
                if (advancementTab.getChangeableResourceLocation().get(versionId).equals(resourceLocation)) {
                    return advancementTab;
                }
            }
            return null;
        }

        public ChangeableResourceLocation getChangeableResourceLocation() {
            return this.changeableResourceLocation;
        }
    }
}
