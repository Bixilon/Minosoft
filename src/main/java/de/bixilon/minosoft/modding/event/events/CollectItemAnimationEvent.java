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

package de.bixilon.minosoft.modding.event.events;

import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.entities.entities.item.ItemEntity;
import de.bixilon.minosoft.modding.event.events.annotations.MinimumProtocolVersion;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.s2c.play.PacketCollectItem;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_16W32A;

public class CollectItemAnimationEvent extends CancelableEvent {
    private final Entity item;
    private final Entity collector;
    private final int count;

    public CollectItemAnimationEvent(PlayConnection connection, ItemEntity item, Entity collector, int count) {
        super(connection);
        this.item = item;
        this.collector = collector;
        this.count = count;
    }

    public CollectItemAnimationEvent(PlayConnection connection, PacketCollectItem pkg) {
        super(connection);
        this.item = connection.getWorld().getEntity(pkg.getItemEntityId());
        this.collector = connection.getWorld().getEntity(pkg.getCollectorEntityId());
        this.count = pkg.getCount();
    }

    public Entity getItem() {
        return this.item;
    }

    public Entity getCollector() {
        return this.collector;
    }

    @MinimumProtocolVersion(versionId = V_16W32A)
    public int getCount() {
        return this.count;
    }
}
