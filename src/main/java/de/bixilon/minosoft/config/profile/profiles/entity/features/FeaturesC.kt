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

package de.bixilon.minosoft.config.profile.profiles.entity.features

import de.bixilon.minosoft.config.profile.profiles.entity.EntityProfile
import de.bixilon.minosoft.config.profile.profiles.entity.features.hitbox.HitboxC
import de.bixilon.minosoft.config.profile.profiles.entity.features.name.NameC
import de.bixilon.minosoft.config.profile.profiles.entity.features.player.PlayerC
import de.bixilon.minosoft.config.profile.profiles.entity.features.score.ScoreC

class FeaturesC(profile: EntityProfile) {
    val hitbox = HitboxC(profile)
    val name = NameC(profile)
    val score = ScoreC(profile)
    val player = PlayerC(profile)
}
