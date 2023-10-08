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

package de.bixilon.minosoft.gui.rendering.gui.atlas

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext

class AtlasManager(val context: RenderContext) {
    private val atlas: Map<ResourceLocation, Atlas> = unsafeNull()
    private var loader: AtlasLoader? = AtlasLoader(context)

    fun load() {
        this::atlas.forceSet(loader!!.load())
        this.loader = null
    }

    fun load(name: ResourceLocation) {
        val loader = this.loader ?: throw IllegalStateException("Can not load anymore! Is init stage done?")
        loader.load(name)
    }

    operator fun get(atlas: ResourceLocation): Atlas? {
        if (loader != null) throw IllegalStateException("Atlas is still in loading phase!")
        return this.atlas[atlas]
    }
}
