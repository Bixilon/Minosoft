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
import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3i;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketSoundEffect extends PlayClientboundPacket {
    private final Vec3i position;
    private final int soundId;
    private final float volume;
    private final float pitch;
    private SoundCategories category;

    public PacketSoundEffect(PlayInByteBuffer buffer) {
        if (buffer.getVersionId() >= V_17W15A && buffer.getVersionId() < V_17W18A) {
            // category was moved to the top
            this.category = SoundCategories.byId(buffer.readVarInt());
        }
        this.soundId = buffer.readVarInt();

        if (buffer.getVersionId() >= V_17W15A && buffer.getVersionId() < V_17W18A) {
            buffer.readString(); // parrot entity type
        }
        if (buffer.getVersionId() >= V_16W02A && (buffer.getVersionId() < V_17W15A || buffer.getVersionId() >= V_17W18A)) {
            this.category = SoundCategories.byId(buffer.readVarInt());
        }
        this.position = new Vec3i(buffer.readFixedPointNumberInt() * 4, buffer.readFixedPointNumberInt() * 4, buffer.readFixedPointNumberInt() * 4);
        this.volume = buffer.readFloat();
        if (buffer.getVersionId() < V_16W20A) {
            this.pitch = (buffer.readByte() * ProtocolDefinition.PITCH_CALCULATION_CONSTANT) / 100F;
        } else {
            this.pitch = buffer.readFloat();
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Play sound effect (soundId=%d, category=%s, volume=%s, pitch=%s, position=%s)", this.soundId, this.category, this.volume, this.pitch, this.position));
    }

    public Vec3i getPosition() {
        return this.position;
    }

    /**
     * @return Pitch in Percent
     */
    public float getPitch() {
        return this.pitch;
    }

    public int getSoundId() {
        return this.soundId;
    }

    public float getVolume() {
        return this.volume;
    }

    public SoundCategories getCategory() {
        return this.category;
    }
}
