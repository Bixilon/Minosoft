/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.s2c.play;

import de.bixilon.minosoft.data.registries.ResourceLocation;
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import java.util.HashMap;

public class PacketSelectAdvancementTab extends PlayS2CPacket {
    private AdvancementTabs tab;

    public PacketSelectAdvancementTab(PlayInByteBuffer buffer) {
        if (buffer.readBoolean()) {
            this.tab = AdvancementTabs.VALUES.get(buffer.readResourceLocation());
        }
    }

    @Override
    public void log(boolean reducedLog) {
        Log.protocol(String.format("[IN] Received select advancement tab (tab=%s)", this.tab));
    }

    public AdvancementTabs getTab() {
        return this.tab;
    }

    public enum AdvancementTabs {
        STORY(new ResourceLocation("story/root")),
        NETHER(new ResourceLocation("nether/root")),
        END(new ResourceLocation("end/root")),
        ADVENTURE(new ResourceLocation("adventure/root")),
        HUSBANDRY(new ResourceLocation("husbandry/root"));

        public static final HashMap<ResourceLocation, AdvancementTabs> VALUES = new HashMap<>();

        static {
            for (AdvancementTabs tab : values()) {
                VALUES.put(tab.resourceLocation, tab);
            }
        }

        private final ResourceLocation resourceLocation;

        AdvancementTabs(ResourceLocation resourceLocation) {
            this.resourceLocation = resourceLocation;
        }

        public ResourceLocation getResourceLocation() {
            return this.resourceLocation;
        }
    }
}
