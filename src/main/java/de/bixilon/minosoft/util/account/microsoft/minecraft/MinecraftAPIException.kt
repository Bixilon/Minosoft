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

package de.bixilon.minosoft.util.account.microsoft.minecraft

import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.minosoft.util.http.HTTPResponse
import de.bixilon.minosoft.util.json.Jackson

class MinecraftAPIException(
    val errorCode: Int,
    val error: MinecraftAPIError?,
) : Exception(error.toString()) {

    constructor(response: HTTPResponse<Map<String, Any>?>) : this(response.statusCode, response.body?.let { catchAll { Jackson.MAPPER.convertValue(it, MinecraftAPIError::class.java) } })
}
