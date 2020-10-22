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

package de.bixilon.minosoft.modding.event.events;

import de.bixilon.minosoft.data.mappings.Item;
import de.bixilon.minosoft.modding.event.events.annotations.MinimumProtocolVersion;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketCollectItem;

public class CollectItemAnimationEvent extends CancelableEvent {
    private final Item item;
    private final int collectorEntityId;
    private final int count;

    public CollectItemAnimationEvent(Connection connection, Item item, int collectorEntityId, int count) {
        super(connection);
        this.item = item;
        this.collectorEntityId = collectorEntityId;
        this.count = count;
    }

    public CollectItemAnimationEvent(Connection connection, PacketCollectItem pkg) {
        super(connection);
        this.item = pkg.getItem();
        this.collectorEntityId = pkg.getCollectorId();
        this.count = pkg.getCount();
    }

    public Item getItem() {
        return item;
    }

    public int getCollectorEntityId() {
        return collectorEntityId;
    }

    @MinimumProtocolVersion(versionId = 301)
    public int getCount() {
        return count;
    }
}
