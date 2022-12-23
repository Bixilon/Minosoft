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

package de.bixilon.minosoft.assets

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.data.registries.ResourceLocation
import java.io.FileNotFoundException
import java.io.InputStream

object TestAssetsManager : AssetsManager {
    override val namespaces: Set<String> = emptySet()
    override val loaded: Boolean = true

    override fun get(path: ResourceLocation): InputStream {
        throw FileNotFoundException(path.toString())
    }

    override fun getOrNull(path: ResourceLocation): InputStream? {
        return null
    }

    override fun load(latch: CountUpAndDownLatch) {
    }

    override fun unload() {
    }

    override fun contains(path: ResourceLocation): Boolean {
        return false
    }
}
