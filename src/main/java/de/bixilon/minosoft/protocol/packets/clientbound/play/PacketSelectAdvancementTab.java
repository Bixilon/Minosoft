/*
 * Minosoft
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

import de.bixilon.minosoft.data.ChangeableIdentifier;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketSelectAdvancementTab implements ClientboundPacket {
    AdvancementTabs tab;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.readBoolean()) {
            tab = AdvancementTabs.byName(buffer.readString(), buffer.getVersionId());
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received select advancement tab (tab=%s)", tab));
    }

    public AdvancementTabs getTab() {
        return tab;
    }

    public enum AdvancementTabs {
        STORY(new ChangeableIdentifier("story/root")),
        NETHER(new ChangeableIdentifier("nether/root")),
        END(new ChangeableIdentifier("end/root")),
        ADVENTURE(new ChangeableIdentifier("adventure/root")),
        HUSBANDRY(new ChangeableIdentifier("husbandry/root"));

        final ChangeableIdentifier changeableIdentifier;

        AdvancementTabs(ChangeableIdentifier changeableIdentifier) {
            this.changeableIdentifier = changeableIdentifier;
        }

        public static AdvancementTabs byName(String name, int versionId) {
            for (AdvancementTabs advancementTab : values()) {
                if (advancementTab.getChangeableIdentifier().get(versionId).equals(name)) {
                    return advancementTab;
                }
            }
            return null;
        }

        public ChangeableIdentifier getChangeableIdentifier() {
            return changeableIdentifier;
        }
    }
}
