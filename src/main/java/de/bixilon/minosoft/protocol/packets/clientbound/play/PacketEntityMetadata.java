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

import de.bixilon.minosoft.data.entities.meta.EntityMetaData;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

import java.lang.reflect.InvocationTargetException;

public class PacketEntityMetadata implements ClientboundPacket {
    EntityMetaData.MetaDataHashMap sets;
    int entityId;
    int versionId;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.versionId = buffer.getVersionId();
        this.entityId = buffer.readEntityId();

        sets = buffer.readMetaData();
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received entity metadata (entityId=%d)", entityId));
    }

    public int getEntityId() {
        return entityId;
    }

    public EntityMetaData.MetaDataHashMap getSets() {
        return sets;
    }

    public EntityMetaData getEntityData(Class<? extends EntityMetaData> clazz) {
        try {
            return clazz.getConstructor(EntityMetaData.MetaDataHashMap.class, int.class).newInstance(sets, versionId);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
