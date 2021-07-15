/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.base

enum class BlendingFunctions {
    ZERO,
    ONE,
    SOURCE_COLOR,
    ONE_MINUS_SOURCE_COLOR,
    DESTINATION_COLOR,
    ONE_MINUS_DESTINATION_COLOR,
    SOURCE_ALPHA,
    ONE_MINUS_SOURCE_ALPHA,
    DESTINATION_ALPHA,
    ONE_MINUS_DESTINATION_ALPHA,
    CONSTANT_COLOR,
    ONE_MINUS_CONSTANT_COLOR,
    CONSTANT_ALPHA,
    ONE_MINUS_CONSTANT_ALPHA,
    ;
}
