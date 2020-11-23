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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.data.SoundCategories;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketEntitySoundEffect implements ClientboundPacket {
    int soundId;
    SoundCategories category;
    int entityId;
    float volume;
    float pitch;

    @Override
    public boolean read(InByteBuffer buffer) {
        soundId = buffer.readVarInt();
        category = SoundCategories.byId(buffer.readVarInt());
        entityId = buffer.readVarInt();
        volume = buffer.readFloat();
        pitch = buffer.readFloat();
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Play sound entity effect (soundId=%d, category=%s, entityId=%d, volume=%s, pitch=%s)", soundId, category, entityId, volume, pitch));
    }

    public int getSoundId() {
        return soundId;
    }

    public SoundCategories getCategory() {
        return category;
    }

    public int getEntityId() {
        return entityId;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }
}
