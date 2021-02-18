package de.bixilon.minosoft.config.config.game.controls

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
import de.bixilon.minosoft.data.mappings.ModIdentifier

data class KeyBindingsGameConfig(
    val entries: MutableMap<ModIdentifier, KeyBinding> = mutableMapOf(
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
    ),
)

object KeyBindingsNames {
    val MOVE_FORWARD = ModIdentifier("minosoft:move_forward")
    val MOVE_BACKWARDS = ModIdentifier("minosoft:move_backwards")
    val MOVE_LEFT = ModIdentifier("minosoft:move_left")
    val MOVE_RIGHT = ModIdentifier("minosoft:move_right")
    val MOVE_SPRINT = ModIdentifier("minosoft:move_sprint")
    val MOVE_FLY_UP = ModIdentifier("minosoft:move_fly_up")
    val MOVE_FLY_DOWN = ModIdentifier("minosoft:move_fly_down")

    val ZOOM = ModIdentifier("minosoft:zoom")

    val QUIT_RENDERING = ModIdentifier("minosoft:quit_rendering")

    val DEBUG_SCREEN = ModIdentifier("minosoft:debug_screen")
    val DEBUG_POLYGEN = ModIdentifier("minosoft:debug_polygen")
    val DEBUG_MOUSE_CATCH = ModIdentifier("minosoft:debug_mouse_catch")

    val WHEN_IN_GAME = ModIdentifier("minosoft:in_game")
    val WHEN_PLAYER_IS_FLYING = ModIdentifier("minosoft:is_flying")
}
