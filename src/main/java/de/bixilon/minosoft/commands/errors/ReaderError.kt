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

package de.bixilon.minosoft.commands.errors

import de.bixilon.kutil.exception.FastException
import de.bixilon.minosoft.commands.util.StringReader

abstract class ReaderError(
    val reader: StringReader,
    val start: Int,
    val end: Int,
) : FastException() {
    override val message: String = "${this::class.simpleName} at $start-$end: ${reader.string} (at ${reader.string.substring(start, end)})"
}
