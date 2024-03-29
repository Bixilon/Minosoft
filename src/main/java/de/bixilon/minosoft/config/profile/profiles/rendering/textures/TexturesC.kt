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

package de.bixilon.minosoft.config.profile.profiles.rendering.textures

import de.bixilon.minosoft.config.profile.delegate.primitive.IntDelegate
import de.bixilon.minosoft.config.profile.delegate.types.EnumDelegate
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfile
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.font.FontCompressions

class TexturesC(profile: RenderingProfile) {

    /**
     * Weather to use mipmaps for all static textures
     */
    var mipmaps by IntDelegate(profile, 4, arrayOf(0..4))

    /**
     * Font texture can be compressed on the gpu which massively reduces vram usage.
     * It may not work with all drivers well, so disable it when font is just black.
     */
    var fontCompression by EnumDelegate(profile, FontCompressions.COMPRESSED_ALPHA, FontCompressions)
}
