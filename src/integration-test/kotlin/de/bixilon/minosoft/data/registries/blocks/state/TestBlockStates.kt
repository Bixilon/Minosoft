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

package de.bixilon.minosoft.data.registries.blocks.state

import de.bixilon.minosoft.data.registries.blocks.TestBlocks

object TestBlockStates {
    val TEST1 = TestBlocks.TEST1.states.default
    val TEST2 = TestBlocks.TEST2.states.default
    val TEST3 = TestBlocks.TEST3.states.default

    val OPAQUE1 = TestBlocks.OPAQUE1.states.default
    val OPAQUE2 = TestBlocks.OPAQUE2.states.default
    val OPAQUE3 = TestBlocks.OPAQUE3.states.default

    val ENTITY1 = TestBlocks.ENTITY1.states.default
    val ENTITY2 = TestBlocks.ENTITY2.states.default

    val TORCH14 = TestBlocks.TORCH14.states.default
}
