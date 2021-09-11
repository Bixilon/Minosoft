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

package de.bixilon.minosoft.config.config.game.controls

import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.registries.ResourceLocation


@Deprecated(message = "Use new system with providing default key bindings when registering instead of \"pre registering\" the key")
object KeyBindingsNames {
    val TOGGLE_DEBUG_SCREEN = ResourceLocation("minosoft:toggle_debug_screen")
    val DEBUG_MOUSE_CATCH = ResourceLocation("minosoft:debug_mouse_catch")

    val TOGGLE_HUD = ResourceLocation("minosoft:toggle_hud")

    val OPEN_CHAT = ResourceLocation("minosoft:open_chat")

    val CLOSE = ResourceLocation("minosoft:close")


    val DEFAULT_KEY_BINDINGS: Map<ResourceLocation, KeyBinding> = mapOf(
        TOGGLE_DEBUG_SCREEN to KeyBinding(
            mutableMapOf(
                KeyAction.STICKY to mutableSetOf(KeyCodes.KEY_F3),
            ),
        ),
        TOGGLE_HUD to KeyBinding(
            mutableMapOf(
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_F1),
            ),
        ),
        OPEN_CHAT to KeyBinding(
            mutableMapOf(
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_T),
            ),
        ),
        CLOSE to KeyBinding(
            mutableMapOf(
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_ESCAPE),
            ),
            ignoreConsumer = true,
        ),
    )
}
