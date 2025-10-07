/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.yggdrasil

import de.bixilon.minosoft.data.registries.identified.Namespaces.mojang
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.signature.SignatureSigner


object YggdrasilUtil : SignatureSigner(mojang("yggdrasil/pubkey.der"), "SHA1withRSA") {
    var ignore = false

    override fun load() {
        if (ignore) {
            Log.log(LogMessageType.LOADING, LogLevels.WARN) { "Yggdrasil signature checking is disabled. Servers can pretend that they have valid data from mojang!" }
            return
        }
        super.load()
    }

    override fun verify(data: ByteArray, signature: ByteArray?): Boolean {
        if (ignore) return true
        return super.verify(data, signature)
    }
}
