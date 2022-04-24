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

package de.bixilon.minosoft.util.account.microsoft

import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.data.accounts.types.microsoft.MicrosoftTokens

class AuthenticationResponse(
    val tokenType: TokenTypes,
    scope: String,
    expiresIn: Int,
    val accessToken: String,
    val idToken: String?,
    val refreshToken: String,
) {
    val expires: Long = (TimeUtil.millis / 1000L) + expiresIn
    val scope = scope.split(' ')

    fun saveTokens(): MicrosoftTokens {
        return MicrosoftTokens(accessToken = accessToken, refreshToken = refreshToken, expires = expires)
    }

    enum class TokenTypes {
        BEARER,
        ;
    }
}
