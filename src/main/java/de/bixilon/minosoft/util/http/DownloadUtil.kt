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

package de.bixilon.minosoft.util.http

import de.bixilon.kutil.json.JsonObject

object DownloadUtil {

    fun retry(urls: List<String>, loader: (String) -> JsonObject): Map<String, Any> {
        if (urls.isEmpty()) throw IllegalArgumentException("No urls provided!")

        var first: Throwable? = null
        for (url in urls) {
            try {
                return loader.invoke(url)
            } catch (error: Throwable) {
                first = error
                error.printStackTrace()
            }
        }
        throw first!!
    }
}
