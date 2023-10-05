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

package de.bixilon.minosoft.config.profile.profiles.resources.source

import de.bixilon.minosoft.config.profile.delegate.types.StringDelegate
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile

class SourceC(profile: ResourcesProfile) {
    var pixlyzer by StringDelegate(profile, "https://gitlab.bixilon.de/bixilon/pixlyzer-data/-/raw/master/hash/\${hashPrefix}/\${fullHash}.mbf?inline=false")
    var minosoftMeta by StringDelegate(profile, "https://gitlab.bixilon.de/bixilon/minosoft-meta-bin/-/raw/master/hash/\${hashPrefix}/\${fullHash}.mbf?inline=false")
    var minecraftResources by StringDelegate(profile, "https://resources.download.minecraft.net/\${hashPrefix}/\${fullHash}")
    var mojangPackages by StringDelegate(profile, "https://launchermeta.mojang.com/v1/packages/\${fullHash}/\${filename}")
    var pistonObjects by StringDelegate(profile, "https://piston-data.mojang.com/v1/objects/\${fullHash}/\${filename}")
}
