/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config.profile.profiles.rendering.quality.resolution

import de.bixilon.minosoft.config.profile.delegate.primitive.FloatDelegate
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfile

class ResolutionC(profile: RenderingProfile) {

    /**
     * Scale of the gui (and hud) framebuffer. If you change this, the font will look weird and maybe blurry. Do not change this.
     */
    var guiScale by FloatDelegate(profile, 1.0f, ranges = arrayOf(0.0001f..4.0f))

    /**
     * Scale of the world (blocks, entities, particles, ...) framebuffer.
     * If you lower this, the framebuffer will be smaller and thus performance should go up.
     * This is only useful when your gpu is bottlenecked by the number of pixels/texels
     * (e.g. if you make the window smaller, your fps go up)
     * Otherwise do not change this, this will not reduce cpu load or gpu load of other stages.
     */
    var worldScale by FloatDelegate(profile, 1.0f, ranges = arrayOf(0.0001f..4.0f))
}
