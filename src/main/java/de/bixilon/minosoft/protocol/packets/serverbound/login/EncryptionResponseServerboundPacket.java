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

package de.bixilon.minosoft.protocol.packets.serverbound.login;

import de.bixilon.minosoft.protocol.packets.serverbound.PlayServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.CryptManager;
import de.bixilon.minosoft.protocol.protocol.OutPlayByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import javax.crypto.SecretKey;
import java.security.PublicKey;

public class EncryptionResponseServerboundPacket implements PlayServerboundPacket {
    private final byte[] secret;
    private final byte[] token;
    private final SecretKey secretKey;

    public EncryptionResponseServerboundPacket(SecretKey secret, byte[] token, PublicKey key) {
        this.secretKey = secret;
        this.secret = CryptManager.encryptData(key, secret.getEncoded());
        this.token = CryptManager.encryptData(key, token);
    }

    public SecretKey getSecretKey() {
        return this.secretKey;
    }

    @Override
    public void write(OutPlayByteBuffer buffer) {
        buffer.writeByteArray(this.secret);
        buffer.writeByteArray(this.token);
    }

    @Override
    public void log() {
        Log.protocol("[OUT] Sending encryption response");
    }
}
