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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.data.SoundCategories;
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketSoundEffect implements ClientboundPacket {
    static final float pitchCalc = 100.0F / 63.0F;
    Location location;
    SoundCategories category;
    int soundId;
    float volume;
    float pitch;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getProtocolId() >= 321 && buffer.getProtocolId() < 326) {
            // category was moved to the top
            category = SoundCategories.byId(buffer.readVarInt());
        }
        soundId = buffer.readVarInt();

        if (buffer.getProtocolId() >= 321 && buffer.getProtocolId() < 326) {
            buffer.readString(); // parrot entity type
        }
        if (buffer.getProtocolId() >= 95 && (buffer.getProtocolId() < 321 || buffer.getProtocolId() >= 326)) {
            category = SoundCategories.byId(buffer.readVarInt());
        }
        location = new Location(buffer.readFixedPointNumberInteger() * 4, buffer.readFixedPointNumberInteger() * 4, buffer.readFixedPointNumberInteger() * 4);
        volume = buffer.readFloat();
        if (buffer.getProtocolId() < 201) {
            pitch = (buffer.readByte() * pitchCalc) / 100F;
        } else {
            pitch = buffer.readFloat();
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("Play sound effect (soundId=%d, category=%s, volume=%s, pitch=%s, location=%s)", soundId, category, volume, pitch, location));
    }

    public Location getLocation() {
        return location;
    }

    /**
     * @return Pitch in Percent
     */
    public float getPitch() {
        return pitch;
    }

    public int getSoundId() {
        return soundId;
    }

    public float getVolume() {
        return volume;
    }

    public SoundCategories getCategory() {
        return category;
    }
}
