/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.account.microsoft.code

import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.data.text.URLProtocols
import java.net.URL

data class MicrosoftDeviceCode(
    val deviceCode: String,
    val userCode: String,
    val verificationURI: URL,
    val expiresIn: Int,
    val interval: Int,
    val message: String,
) {
    val expires = (millis() / 1000) + expiresIn

    init {
        check(verificationURI.protocol == URLProtocols.HTTPS.protocol) { "Insecure url: $verificationURI" }
        check(verificationURI.host == "login.microsoftonline.com" || verificationURI.host == "www.microsoft.com" || verificationURI.host == "microsoft.com") { "Invalid verification host: $verificationURI" }

        check(interval in 1..20) { "Polling interval out of range: $interval" }
    }
}
