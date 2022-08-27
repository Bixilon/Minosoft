/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.*


object YggdrasilUtil {
    lateinit var PUBLIC_KEY: PublicKey
        private set

    fun load() {
        if (RunConfiguration.IGNORE_YGGDRASIL) {
            Log.log(LogMessageType.OTHER, LogLevels.WARN) { "Yggdrasil signature checking is disabled. Servers can pretend that they have valid data from mojang!" }
            return
        }
        check(!this::PUBLIC_KEY.isInitialized) { "Already loaded!" }
        val spec = X509EncodedKeySpec(Minosoft.MINOSOFT_ASSETS_MANAGER["minosoft:mojang/yggdrasil_session_pubkey.der".toResourceLocation()].readAllBytes())
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        PUBLIC_KEY = keyFactory.generatePublic(spec)
    }

    fun verify(data: ByteArray, signature: ByteArray): Boolean {
        if (RunConfiguration.IGNORE_YGGDRASIL) {
            return true
        }
        val signatureInstance = Signature.getInstance("SHA1withRSA")
        signatureInstance.initVerify(PUBLIC_KEY)
        signatureInstance.update(data)
        return signatureInstance.verify(signature)
    }

    fun verify(data: String, signature: String): Boolean {
        if (RunConfiguration.IGNORE_YGGDRASIL) {
            return true
        }
        return verify(data.toByteArray(), Base64.getDecoder().decode(signature))
    }
}
