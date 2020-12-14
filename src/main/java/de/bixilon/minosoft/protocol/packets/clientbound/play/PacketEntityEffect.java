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

import de.bixilon.minosoft.data.entities.StatusEffect;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.util.BitByte;

public class PacketEntityEffect implements ClientboundPacket {
    int entityId;
    StatusEffect effect;
    boolean isAmbient;
    boolean hideParticles;
    boolean showIcon = true;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.entityId = buffer.readEntityId();
        if (buffer.getVersionId() < 7) {
            this.effect = new StatusEffect(buffer.getConnection().getMapping().getMobEffectById(buffer.readByte()), buffer.readByte() + 1, buffer.readShort());
            return true;
        }
        this.effect = new StatusEffect(buffer.getConnection().getMapping().getMobEffectById(buffer.readByte()), buffer.readByte() + 1, buffer.readVarInt());
        if (buffer.getVersionId() < 110) { // ToDo
            if (buffer.getVersionId() >= 10) {
                this.hideParticles = buffer.readBoolean();
                return true;
            }
        }
        byte flags = buffer.readByte();
        this.isAmbient = BitByte.isBitMask(flags, 0x01);
        this.hideParticles = !BitByte.isBitMask(flags, 0x02);
        if (buffer.getVersionId() >= 498) { // ToDo
            this.showIcon = BitByte.isBitMask(flags, 0x04);
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Entity effect added: %d %s", this.entityId, this.effect.toString()));
    }

    public int getEntityId() {
        return this.entityId;
    }

    public StatusEffect getEffect() {
        return this.effect;
    }

    public boolean hideParticles() {
        return this.hideParticles;
    }

    public boolean showIcon() {
        return this.showIcon;
    }

    public boolean isAmbient() {
        return this.isAmbient;
    }
}
