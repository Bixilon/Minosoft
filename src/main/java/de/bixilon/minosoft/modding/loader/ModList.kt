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

package de.bixilon.minosoft.modding.loader

import de.bixilon.minosoft.modding.loader.error.NoManifestError
import de.bixilon.minosoft.modding.loader.mod.MinosoftMod

class ModList(
    val mods: MutableMap<String, MinosoftMod> = mutableMapOf(),
    val virtual: MutableMap<String, MinosoftMod> = mutableMapOf(),
) : Iterable<MinosoftMod> {

    inline operator fun minusAssign(mods: Collection<MinosoftMod>) = remove(mods)
    inline operator fun minusAssign(mod: MinosoftMod) = remove(mod)

    fun remove(mods: Collection<MinosoftMod>) {
        for (mod in mods) {
            remove(mod)
        }
    }

    @Synchronized
    fun remove(mod: MinosoftMod) {
        val manifest = mod.manifest ?: throw NoManifestError(mod.source)
        this.mods -= manifest.name
        this.virtual -= manifest.name

        manifest.packages?.provides?.let { this.virtual -= it }

        try {
            mod.unload()
        } catch (error: Throwable) {
            error.printStackTrace()
        }
    }

    inline operator fun plusAssign(mod: MinosoftMod) = add(mod)

    @Synchronized
    fun add(mod: MinosoftMod) {
        val manifest = mod.manifest ?: throw NoManifestError(mod.source)

        this.mods[manifest.name] = mod
        this.virtual[manifest.name] = mod

        manifest.packages?.provides?.let {
            for (name in it) {
                this.virtual[name] = mod
            }
        }
    }

    operator fun contains(name: String): Boolean = get(name) != null

    @Synchronized
    operator fun get(name: String): MinosoftMod? {
        return mods[name] ?: virtual[name]
    }

    @Synchronized
    operator fun plusAssign(list: ModList) {
        this.mods += list.mods
        this.virtual += list.virtual
    }

    override fun iterator(): Iterator<MinosoftMod> {
        return mods.values.iterator()
    }
}
