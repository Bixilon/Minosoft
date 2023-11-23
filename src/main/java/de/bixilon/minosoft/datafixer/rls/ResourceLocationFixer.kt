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

package de.bixilon.minosoft.datafixer.rls

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.assets.IntegratedAssets
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJson
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.datafixer.Fixer

abstract class ResourceLocationFixer(private val path: ResourceLocation) : Fixer {
    private val renames: Map<ResourceLocation, ResourceLocation> = unsafeNull()


    override fun load() {
        this::renames.forceSet(IntegratedAssets.DEFAULT[this.path.fixer()].readJson())
    }

    open fun fix(resourceLocation: ResourceLocation): ResourceLocation {
        return renames[resourceLocation] ?: resourceLocation
    }


    companion object {
        private fun ResourceLocation.fixer(): ResourceLocation {
            return this.prefix("fixer/").suffix(".json")
        }
    }
}
