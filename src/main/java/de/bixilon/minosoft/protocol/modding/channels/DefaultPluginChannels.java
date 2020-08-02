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

package de.bixilon.minosoft.protocol.modding.channels;

import de.bixilon.minosoft.game.datatypes.ChangeableIdentifier;

public enum DefaultPluginChannels {
    MC_BRAND(new ChangeableIdentifier("MC|Brand", "minecraft:brand")),
    STOP_SOUND(new ChangeableIdentifier("MC|StopSound")),
    REGISTER(new ChangeableIdentifier("REGISTER", "minecraft:register")),
    UNREGISTER(new ChangeableIdentifier("UNREGISTER", "minecraft:unregister"));

    final ChangeableIdentifier changeableIdentifier;

    DefaultPluginChannels(ChangeableIdentifier changeableIdentifier) {
        this.changeableIdentifier = changeableIdentifier;
    }

    public static DefaultPluginChannels byName(String name, int protocolId) {
        for (DefaultPluginChannels d : values()) {
            if (d.getChangeableIdentifier().get(protocolId).equals(name)) {
                return d;
            }
        }
        return null;
    }

    public ChangeableIdentifier getChangeableIdentifier() {
        return changeableIdentifier;
    }
}
