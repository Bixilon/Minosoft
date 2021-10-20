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

package de.bixilon.minosoft.gui.rendering.input.interaction

import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.other.containers.PlayerInventory
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.protocol.packets.c2s.play.HotbarSlotSetC2SP
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class HotbarInteractionHandler(
    val renderWindow: RenderWindow,
) {
    private val connection = renderWindow.connection


    fun selectSlot(slot: Int) {
        // ToDo: Rate limit?
        if (connection.player.selectedHotbarSlot == slot) {
            return
        }
        connection.player.selectedHotbarSlot = slot
        connection.sendPacket(HotbarSlotSetC2SP(slot))
    }


    fun init() {
        for (i in 1..PlayerInventory.HOTBAR_SLOTS) {
            renderWindow.inputHandler.registerKeyCallback("minosoft:hotbar_slot_$i".toResourceLocation(), KeyBinding(
                mutableMapOf(
                    KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_CODE_MAP["$i"]!!),
                ),
            )) {
                selectSlot(i - 1)
            }
        }
    }
}
