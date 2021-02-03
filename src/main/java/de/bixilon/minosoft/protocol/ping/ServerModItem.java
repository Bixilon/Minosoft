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

package de.bixilon.minosoft.protocol.ping;

public class ServerModItem {
    private final String modId;
    private final String modVersion;

    public ServerModItem(String modId, String modVersion) {
        this.modId = modId;
        this.modVersion = modVersion;
    }

    public String getModId() {
        return this.modId;
    }

    public String getModVersion() {
        return this.modVersion;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", this.modId, this.modVersion);
    }
}
