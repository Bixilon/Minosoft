/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later protocolId.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.entities.StatusEffect;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.util.BitByte;

public class PacketEntityEffect implements ClientboundPacket {
    int entityId;
    StatusEffect effect;
    boolean isAmbient;
    boolean hideParticles = false;
    boolean showIcon = true;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.entityId = buffer.readEntityId();
        if (buffer.getProtocolId() < 7) {
            effect = new StatusEffect(buffer.getConnection().getMapping().getMobEffectById(buffer.readByte()), buffer.readByte() + 1, buffer.readShort());
            return true;
        }
        effect = new StatusEffect(buffer.getConnection().getMapping().getMobEffectById(buffer.readByte()), buffer.readByte() + 1, buffer.readVarInt());
        if (buffer.getProtocolId() < 110) { //ToDo
            if (buffer.getProtocolId() >= 10) {
                hideParticles = buffer.readBoolean();
                return true;
            }
        }
        byte flags = buffer.readByte();
        isAmbient = BitByte.isBitMask(flags, 0x01);
        hideParticles = !BitByte.isBitMask(flags, 0x02);
        if (buffer.getProtocolId() >= 498) { //ToDo
            showIcon = BitByte.isBitMask(flags, 0x04);
        }
        return true;
    }

    @Override
    public void log() {
        Log.game(String.format("Entity effect added: %d %s", entityId, effect.toString()));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public int getEntityId() {
        return entityId;
    }

    public StatusEffect getEffect() {
        return effect;
    }

    public boolean hideParticles() {
        return hideParticles;
    }

    public boolean showIcon() {
        return showIcon;
    }

    public boolean isAmbient() {
        return isAmbient;
    }
}
