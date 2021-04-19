/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
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

import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames.DEFAULT_KEY_BINDINGS
import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.mappings.ResourceLocation

data class KeyBindingsGameConfig(
    val entries: MutableMap<ResourceLocation, KeyBinding> = mutableMapOf(),
) {
    init {
        for ((resourceLocation, keyBinding) in DEFAULT_KEY_BINDINGS) {
            if (!entries.containsKey(resourceLocation)) {
                // add key binding
                entries[resourceLocation] = KeyBinding(keyBinding)
            }
        }
    }
}

object KeyBindingsNames {
    val MOVE_FORWARD = ResourceLocation("minosoft:move_forward")
    val MOVE_BACKWARDS = ResourceLocation("minosoft:move_backwards")
    val MOVE_LEFT = ResourceLocation("minosoft:move_left")
    val MOVE_RIGHT = ResourceLocation("minosoft:move_right")
    val MOVE_SPRINT = ResourceLocation("minosoft:move_sprint")
    val MOVE_FLY_UP = ResourceLocation("minosoft:move_fly_up")
    val MOVE_FLY_DOWN = ResourceLocation("minosoft:move_fly_down")
    val MOVE_JUMP = ResourceLocation("minosoft:move_jump")

    val ZOOM = ResourceLocation("minosoft:zoom")

    val QUIT_RENDERING = ResourceLocation("minosoft:quit_rendering")

    val TOGGLE_DEBUG_SCREEN = ResourceLocation("minosoft:toggle_debug_screen")
    val DEBUG_CLEAR_CHUNK_CACHE = ResourceLocation("minosoft:debug_clear_chunk_cache")
    val DEBUG_POLYGON = ResourceLocation("minosoft:debug_polygon")
    val DEBUG_MOUSE_CATCH = ResourceLocation("minosoft:debug_mouse_catch")

    val WHEN_IN_GAME = ResourceLocation("minosoft:in_game")
    val WHEN_PLAYER_IS_FLYING = ResourceLocation("minosoft:is_flying")
    val WHEN_IN_CHAT = ResourceLocation("minosoft:in_chat")

    val TAKE_SCREENSHOT = ResourceLocation("minosoft:take_screenshot")

    val TOGGLE_HUD = ResourceLocation("minosoft:toggle_hud")

    val OPEN_CHAT = ResourceLocation("minosoft:open_chat")

    val CLOSE_CHAT = ResourceLocation("minosoft:close_chat")


    val SELECT_HOTBAR_SLOTS = arrayOf(ResourceLocation("minosoft:select_hotbar_slot_1"),
        ResourceLocation("minosoft:select_hotbar_slot_2"),
        ResourceLocation("minosoft:select_hotbar_slot_3"),
        ResourceLocation("minosoft:select_hotbar_slot_4"),
        ResourceLocation("minosoft:select_hotbar_slot_5"),
        ResourceLocation("minosoft:select_hotbar_slot_6"),
        ResourceLocation("minosoft:select_hotbar_slot_7"),
        ResourceLocation("minosoft:select_hotbar_slot_8"),
        ResourceLocation("minosoft:select_hotbar_slot_9")
    )

    val DEFAULT_KEY_BINDINGS: Map<ResourceLocation, KeyBinding> = mapOf(
        MOVE_FORWARD to KeyBinding(
            mutableMapOf(
                KeyAction.CHANGE to mutableSetOf(KeyCodes.KEY_W)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        MOVE_LEFT to KeyBinding(
            mutableMapOf(
                KeyAction.CHANGE to mutableSetOf(KeyCodes.KEY_A)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        MOVE_BACKWARDS to KeyBinding(
            mutableMapOf(
                KeyAction.CHANGE to mutableSetOf(KeyCodes.KEY_S)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        MOVE_RIGHT to KeyBinding(
            mutableMapOf(
                KeyAction.CHANGE to mutableSetOf(KeyCodes.KEY_D)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        MOVE_SPRINT to KeyBinding(
            mutableMapOf(
                KeyAction.CHANGE to mutableSetOf(KeyCodes.KEY_LEFT_CONTROL)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        MOVE_FLY_UP to KeyBinding(
            mutableMapOf(
                KeyAction.CHANGE to mutableSetOf(KeyCodes.KEY_SPACE)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME, WHEN_PLAYER_IS_FLYING))
        ),
        MOVE_JUMP to KeyBinding(
            mutableMapOf(
                KeyAction.CHANGE to mutableSetOf(KeyCodes.KEY_SPACE)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        MOVE_FLY_DOWN to KeyBinding(
            mutableMapOf(
                KeyAction.CHANGE to mutableSetOf(KeyCodes.KEY_LEFT_SHIFT)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME, WHEN_PLAYER_IS_FLYING))
        ),
        ZOOM to KeyBinding(
            mutableMapOf(
                KeyAction.CHANGE to mutableSetOf(KeyCodes.KEY_C)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        TOGGLE_DEBUG_SCREEN to KeyBinding(
            mutableMapOf(
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_F3)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        DEBUG_POLYGON to KeyBinding(
            mutableMapOf(
                KeyAction.MODIFIER to mutableSetOf(KeyCodes.KEY_F4),
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_P)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        DEBUG_MOUSE_CATCH to KeyBinding(
            mutableMapOf(
                KeyAction.MODIFIER to mutableSetOf(KeyCodes.KEY_F4),
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_M)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        QUIT_RENDERING to KeyBinding(
            mutableMapOf(
                KeyAction.RELEASE to mutableSetOf(KeyCodes.KEY_ESCAPE)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        DEBUG_CLEAR_CHUNK_CACHE to KeyBinding(
            mutableMapOf(
                KeyAction.MODIFIER to mutableSetOf(KeyCodes.KEY_F3),
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_A)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        TAKE_SCREENSHOT to KeyBinding(
            mutableMapOf(
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_F2)
            ),
            mutableSetOf()
        ),
        SELECT_HOTBAR_SLOTS[0] to KeyBinding(
            mutableMapOf(
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_1)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        SELECT_HOTBAR_SLOTS[1] to KeyBinding(
            mutableMapOf(
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_2)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        SELECT_HOTBAR_SLOTS[2] to KeyBinding(
            mutableMapOf(
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_3)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        SELECT_HOTBAR_SLOTS[3] to KeyBinding(
            mutableMapOf(
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_4)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        SELECT_HOTBAR_SLOTS[4] to KeyBinding(
            mutableMapOf(
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_5)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        SELECT_HOTBAR_SLOTS[5] to KeyBinding(
            mutableMapOf(
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_6)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        SELECT_HOTBAR_SLOTS[6] to KeyBinding(
            mutableMapOf(
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_7)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        SELECT_HOTBAR_SLOTS[7] to KeyBinding(
            mutableMapOf(
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_8)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        SELECT_HOTBAR_SLOTS[8] to KeyBinding(
            mutableMapOf(
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_9)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        TOGGLE_HUD to KeyBinding(
            mutableMapOf(
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_F1)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        OPEN_CHAT to KeyBinding(
            mutableMapOf(
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_T)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        CLOSE_CHAT to KeyBinding(
            mutableMapOf(
                KeyAction.PRESS to mutableSetOf(KeyCodes.KEY_ESCAPE)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_CHAT))
        ),
    )
}
