/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config.config.game.controls

import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames.DEBUG_CLEAR_CHUNK_CACHE
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames.DEBUG_MOUSE_CATCH
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames.DEBUG_POLYGEN
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames.DEBUG_SCREEN
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames.MOVE_BACKWARDS
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames.MOVE_FLY_DOWN
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames.MOVE_FLY_UP
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames.MOVE_FORWARD
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames.MOVE_LEFT
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames.MOVE_RIGHT
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames.MOVE_SPRINT
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames.QUIT_RENDERING
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames.WHEN_IN_GAME
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames.WHEN_PLAYER_IS_FLYING
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames.ZOOM
import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.mappings.ResourceLocation

data class KeyBindingsGameConfig(
    val entries: MutableMap<ResourceLocation, KeyBinding> = mutableMapOf(
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
        DEBUG_SCREEN to KeyBinding(
            mutableMapOf(
                KeyAction.RELEASE to mutableSetOf(KeyCodes.KEY_F3)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        DEBUG_POLYGEN to KeyBinding(
            mutableMapOf(
                KeyAction.MODIFIER to mutableSetOf(KeyCodes.KEY_F4),
                KeyAction.RELEASE to mutableSetOf(KeyCodes.KEY_P)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
        DEBUG_MOUSE_CATCH to KeyBinding(
            mutableMapOf(
                KeyAction.MODIFIER to mutableSetOf(KeyCodes.KEY_F4),
                KeyAction.RELEASE to mutableSetOf(KeyCodes.KEY_M)
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
                KeyAction.RELEASE to mutableSetOf(KeyCodes.KEY_A)
            ),
            mutableSetOf(mutableSetOf(WHEN_IN_GAME))
        ),
    ),
)

object KeyBindingsNames {
    val MOVE_FORWARD = ResourceLocation("minosoft:move_forward")
    val MOVE_BACKWARDS = ResourceLocation("minosoft:move_backwards")
    val MOVE_LEFT = ResourceLocation("minosoft:move_left")
    val MOVE_RIGHT = ResourceLocation("minosoft:move_right")
    val MOVE_SPRINT = ResourceLocation("minosoft:move_sprint")
    val MOVE_FLY_UP = ResourceLocation("minosoft:move_fly_up")
    val MOVE_FLY_DOWN = ResourceLocation("minosoft:move_fly_down")

    val ZOOM = ResourceLocation("minosoft:zoom")

    val QUIT_RENDERING = ResourceLocation("minosoft:quit_rendering")

    val DEBUG_SCREEN = ResourceLocation("minosoft:debug_screen")
    val DEBUG_CLEAR_CHUNK_CACHE = ResourceLocation("minosoft:debug_clear_chunk_cache")
    val DEBUG_POLYGEN = ResourceLocation("minosoft:debug_polygen")
    val DEBUG_MOUSE_CATCH = ResourceLocation("minosoft:debug_mouse_catch")

    val WHEN_IN_GAME = ResourceLocation("minosoft:in_game")
    val WHEN_PLAYER_IS_FLYING = ResourceLocation("minosoft:is_flying")
}
