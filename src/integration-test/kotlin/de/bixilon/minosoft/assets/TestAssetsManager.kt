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

package de.bixilon.minosoft.assets

import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.assets.connection.ConnectionAssetsManager
import de.bixilon.minosoft.assets.properties.manager.AssetsManagerProperties
import de.bixilon.minosoft.assets.properties.manager.pack.PackProperties
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import java.io.FileNotFoundException
import java.io.InputStream

object TestAssetsManager : AssetsManager {
    override val loaded: Boolean = true

    override fun get(path: ResourceLocation): InputStream {
        throw FileNotFoundException(path.toString())
    }

    override fun getOrNull(path: ResourceLocation): InputStream? {
        return null
    }

    override fun load(latch: AbstractLatch?) {
    }

    override fun unload() {
    }

    override fun getAssetsManager(path: ResourceLocation) = null


    fun AssetsManager.box(packFormat: Int = 0): ConnectionAssetsManager {
        val manager = ConnectionAssetsManager(AssetsManagerProperties(PackProperties(packFormat)))
        manager += this

        return manager
    }
}
