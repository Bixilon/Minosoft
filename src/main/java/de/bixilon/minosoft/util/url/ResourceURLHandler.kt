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

package de.bixilon.minosoft.util.url

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler


object ResourceURLHandler : URLStreamHandler() {

    override fun openConnection(url: URL?): URLConnection {
        return ResourceURLConnection(url)
    }


    private class ResourceURLConnection(url: URL?) : URLConnection(url) {
        override fun connect() {
        }

        override fun getInputStream(): InputStream {
            return Minosoft.MINOSOFT_ASSETS_MANAGER[ResourceLocation.of(url.path)]
        }
    }
}
