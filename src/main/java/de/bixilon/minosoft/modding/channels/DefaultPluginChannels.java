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

import de.bixilon.minosoft.data.ChangeableResourceLocation;
import de.bixilon.minosoft.data.mappings.ResourceLocation;

import java.util.Map;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.LOWEST_VERSION_SUPPORTED;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_13_PRE3;

public enum DefaultPluginChannels {
    MC_BRAND(new ChangeableResourceLocation(Map.of(LOWEST_VERSION_SUPPORTED, "MC|Brand", V_1_13_PRE3, "minecraft:brand"))),
    STOP_SOUND(new ChangeableResourceLocation("MC|StopSound")),
    REGISTER(new ChangeableResourceLocation(Map.of(LOWEST_VERSION_SUPPORTED, "REGISTER", V_1_13_PRE3, "minecraft:register"))),
    UNREGISTER(new ChangeableResourceLocation(Map.of(LOWEST_VERSION_SUPPORTED, "UNREGISTER", V_1_13_PRE3, "minecraft:unregister")));

    private final ChangeableResourceLocation changeableResourceLocation;

    DefaultPluginChannels(ChangeableResourceLocation changeableResourceLocation) {
        this.changeableResourceLocation = changeableResourceLocation;
    }

    public static DefaultPluginChannels byResourceLocation(ResourceLocation resourceLocation, int versionId) {
        for (DefaultPluginChannels channel : values()) {
            if (channel.getChangeableResourceLocation().get(versionId).equals(resourceLocation)) {
                return channel;
            }
        }
        return null;
    }

    public ChangeableResourceLocation getChangeableResourceLocation() {
        return this.changeableResourceLocation;
    }
}
