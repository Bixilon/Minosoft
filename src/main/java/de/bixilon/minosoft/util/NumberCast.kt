/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util

// TODO: Remove in kutil 1.27.4

// ported from glm
// using number is okay, because it gets inlined and does not allocate a wrapper object
inline val Number.b get() = this.toByte()
inline val Number.s get() = this.toShort()
inline val Number.i get() = this.toInt()
inline val Number.f get() = this.toFloat()
inline val Number.d get() = this.toDouble()
inline val Number.l get() = this.toLong()
