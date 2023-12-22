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

package de.bixilon.minosoft.gui.rendering.system.base.driver

import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.enums.ValuesEnum.Companion.names

// Than name is the problem in itself
enum class DriverHacks {
    /**
     * OpenGL implements uniform arrays (used for textures) not as a real array. It just names them like it. Hence non constant array accessing is forbidden.
     * If enabled, it allows (potentially faster and cleaner) access as an array.
     * Enabled by default on NVIDIA
     */
    UNIFORM_ARRAY_AS_ARRAY,

    /**
     * GPUs can't really do quads, its a hack in most drivers. Some of them still disallow using them, when binding to modern context.
     * Will only work and AMD and NVIDIA. Disabled by default.
     */
    @Deprecated("implement index buffers")
    USE_QUADS_OVER_TRIANGLE,
    ;


    companion object : ValuesEnum<DriverHacks> {
        override val VALUES = values()
        override val NAME_MAP = names()
    }
}
