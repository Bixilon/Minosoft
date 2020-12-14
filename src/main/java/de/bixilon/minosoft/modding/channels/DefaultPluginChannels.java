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

package de.bixilon.minosoft.modding.channels;

import de.bixilon.minosoft.data.ChangeableIdentifier;
import de.bixilon.minosoft.data.IdentifierSet;

public enum DefaultPluginChannels {
    MC_BRAND(new ChangeableIdentifier(new IdentifierSet(0, "MC|Brand"), new IdentifierSet(385, "minecraft:brand"))),
    STOP_SOUND(new ChangeableIdentifier("MC|StopSound")),
    REGISTER(new ChangeableIdentifier(new IdentifierSet(0, "REGISTER"), new IdentifierSet(385, "minecraft:register"))),
    UNREGISTER(new ChangeableIdentifier(new IdentifierSet(0, "UNREGISTER"), new IdentifierSet(385, "minecraft:unregister")));

    final ChangeableIdentifier changeableIdentifier;

    DefaultPluginChannels(ChangeableIdentifier changeableIdentifier) {
        this.changeableIdentifier = changeableIdentifier;
    }

    public static DefaultPluginChannels byName(String name, int versionId) {
        for (DefaultPluginChannels channel : values()) {
            if (channel.getChangeableIdentifier().get(versionId).equals(name)) {
                return channel;
            }
        }
        return null;
    }

    public ChangeableIdentifier getChangeableIdentifier() {
        return this.changeableIdentifier;
    }
}
