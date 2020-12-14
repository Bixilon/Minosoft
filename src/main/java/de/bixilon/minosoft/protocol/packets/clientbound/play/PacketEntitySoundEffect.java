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

public class PacketEntitySoundEffect extends ClientboundPacket {
    int soundId;
    SoundCategories category;
    int entityId;
    float volume;
    float pitch;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.soundId = buffer.readVarInt();
        this.category = SoundCategories.byId(buffer.readVarInt());
        this.entityId = buffer.readVarInt();
        this.volume = buffer.readFloat();
        this.pitch = buffer.readFloat();
        return true;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Play sound entity effect (soundId=%d, category=%s, entityId=%d, volume=%s, pitch=%s)", this.soundId, this.category, this.entityId, this.volume, this.pitch));
    }

    public int getSoundId() {
        return this.soundId;
    }

    public SoundCategories getCategory() {
        return this.category;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public float getVolume() {
        return this.volume;
    }

    public float getPitch() {
        return this.pitch;
    }
}
