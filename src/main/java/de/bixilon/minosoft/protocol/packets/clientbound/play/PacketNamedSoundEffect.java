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
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;

public class PacketNamedSoundEffect extends ClientboundPacket {
    Location location;
    String sound;
    float volume;
    float pitch;
    SoundCategories category;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getVersionId() >= 321 && buffer.getVersionId() < 326) {
            // category was moved to the top
            this.category = SoundCategories.byId(buffer.readVarInt());
        }
        this.sound = buffer.readString();

        if (buffer.getVersionId() >= 321 && buffer.getVersionId() < 326) {
            buffer.readString(); // parrot entity type
        }
        if (buffer.getVersionId() < 95) {
            this.location = new Location(buffer.readInt() * 8, buffer.readInt() * 8, buffer.readInt() * 8); // ToDo: check if it is not * 4
        }

        if (buffer.getVersionId() >= 95 && (buffer.getVersionId() < 321 || buffer.getVersionId() >= 326)) {
            this.category = SoundCategories.byId(buffer.readVarInt());
        }
        if (buffer.getVersionId() >= 95) {
            this.location = new Location(buffer.readFixedPointNumberInt() * 4, buffer.readFixedPointNumberInt() * 4, buffer.readFixedPointNumberInt() * 4);
        }
        this.volume = buffer.readFloat();
        if (buffer.getVersionId() < 201) {
            this.pitch = (buffer.readByte() * ProtocolDefinition.PITCH_CALCULATION_CONSTANT) / 100F;
        } else {
            this.pitch = buffer.readFloat();
        }
        return true;
    }


    @Override
    public void log() {
        Log.protocol(String.format("[IN] Play sound effect (sound=%s, category=%s, volume=%s, pitch=%s, location=%s)", this.sound, this.category, this.volume, this.pitch, this.location));
    }

    public Location getLocation() {
        return this.location;
    }

    /**
     * @return Pitch in Percent
     */
    public float getPitch() {
        return this.pitch;
    }

    public String getSound() {
        return this.sound;
    }

    public float getVolume() {
        return this.volume;
    }

    public SoundCategories getCategory() {
        return this.category;
    }
}
