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

package de.bixilon.minosoft.data.container.click

@Deprecated("ToDo")
class SlotSwapContainerAction(
    val sourceSlot: Int,
    val target: SwapTargets,
) : ContainerAction {
    private val mode: Int get() = 2
    private val button: Int get() = target.ordinal

    enum class SwapTargets(val button: Int) {
        HOTBAR_1(0),
        HOTBAR_2(1),
        HOTBAR_3(2),
        HOTBAR_4(3),
        HOTBAR_5(4),
        HOTBAR_6(5),
        HOTBAR_7(6),
        HOTBAR_8(7),
        HOTBAR_9(8),

        OFFHAND(40),
        ;
    }
}
