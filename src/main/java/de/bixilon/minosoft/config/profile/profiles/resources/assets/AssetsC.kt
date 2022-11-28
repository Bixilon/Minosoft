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

package de.bixilon.minosoft.config.profile.profiles.resources.assets

import de.bixilon.minosoft.assets.minecraft.index.IndexAssetsType
import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.delegate.types.list.ListDelegate
import de.bixilon.minosoft.config.profile.delegate.types.set.SetDelegate
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.config.profile.profiles.resources.assets.packs.ResourcePack

class AssetsC(profile: ResourcesProfile) {
    var disableJarAssets by BooleanDelegate(profile, false)
    var disableIndexAssets by BooleanDelegate(profile, false)
    val indexAssetsTypes: MutableSet<IndexAssetsType> by SetDelegate(profile, mutableSetOf(*IndexAssetsType.VALUES), "")

    val resourcePacks: List<ResourcePack> by ListDelegate(profile, mutableListOf(), "")
}
