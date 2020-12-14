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

package de.bixilon.minosoft.modding.event.events;

import de.bixilon.minosoft.modding.event.events.annotations.MaximumProtocolVersion;
import de.bixilon.minosoft.modding.event.events.annotations.MinimumProtocolVersion;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketResourcePackSend;

@MinimumProtocolVersion(versionId = 32)
public class ResourcePackChangeEvent extends CancelableEvent {
    private String url;
    private String hash;

    public ResourcePackChangeEvent(Connection connection, String url, String hash) {
        super(connection);
        this.url = url;
        this.hash = hash;
    }

    public ResourcePackChangeEvent(Connection connection, PacketResourcePackSend pkg) {
        super(connection);
        this.url = pkg.getUrl();
        this.hash = pkg.getHash();
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @MaximumProtocolVersion(versionId = 204)
    public String getHash() {
        return this.hash;
    }

    @MaximumProtocolVersion(versionId = 204)
    public void setHash(String hash) {
        this.hash = hash;
    }
}
