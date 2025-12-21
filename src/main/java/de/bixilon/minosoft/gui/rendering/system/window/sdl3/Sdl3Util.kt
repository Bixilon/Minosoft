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

package de.bixilon.minosoft.gui.rendering.system.window.sdl3

import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.system.window.CursorShapes
import org.lwjgl.sdl.SDLMouse.*
import org.lwjgl.sdl.SDLScancode.*

object Sdl3Util {
    val KEY_CODE_MAPPING = mapOf(
        SDL_SCANCODE_SPACE to KeyCodes.KEY_SPACE,
        SDL_SCANCODE_APOSTROPHE to KeyCodes.KEY_APOSTROPHE,
        SDL_SCANCODE_COMMA to KeyCodes.KEY_COMMA,
        SDL_SCANCODE_MINUS to KeyCodes.KEY_MINUS,
        SDL_SCANCODE_PERIOD to KeyCodes.KEY_PERIOD,
        SDL_SCANCODE_SLASH to KeyCodes.KEY_SLASH,
        SDL_SCANCODE_0 to KeyCodes.KEY_0,
        SDL_SCANCODE_1 to KeyCodes.KEY_1,
        SDL_SCANCODE_2 to KeyCodes.KEY_2,
        SDL_SCANCODE_3 to KeyCodes.KEY_3,
        SDL_SCANCODE_4 to KeyCodes.KEY_4,
        SDL_SCANCODE_5 to KeyCodes.KEY_5,
        SDL_SCANCODE_6 to KeyCodes.KEY_6,
        SDL_SCANCODE_7 to KeyCodes.KEY_7,
        SDL_SCANCODE_8 to KeyCodes.KEY_8,
        SDL_SCANCODE_9 to KeyCodes.KEY_9,
        SDL_SCANCODE_SEMICOLON to KeyCodes.KEY_SEMICOLON,
        SDL_SCANCODE_EQUALS to KeyCodes.KEY_EQUAL,
        SDL_SCANCODE_A to KeyCodes.KEY_A,
        SDL_SCANCODE_B to KeyCodes.KEY_B,
        SDL_SCANCODE_C to KeyCodes.KEY_C,
        SDL_SCANCODE_D to KeyCodes.KEY_D,
        SDL_SCANCODE_E to KeyCodes.KEY_E,
        SDL_SCANCODE_F to KeyCodes.KEY_F,
        SDL_SCANCODE_G to KeyCodes.KEY_G,
        SDL_SCANCODE_H to KeyCodes.KEY_H,
        SDL_SCANCODE_I to KeyCodes.KEY_I,
        SDL_SCANCODE_J to KeyCodes.KEY_J,
        SDL_SCANCODE_K to KeyCodes.KEY_K,
        SDL_SCANCODE_L to KeyCodes.KEY_L,
        SDL_SCANCODE_M to KeyCodes.KEY_M,
        SDL_SCANCODE_N to KeyCodes.KEY_N,
        SDL_SCANCODE_O to KeyCodes.KEY_O,
        SDL_SCANCODE_P to KeyCodes.KEY_P,
        SDL_SCANCODE_Q to KeyCodes.KEY_Q,
        SDL_SCANCODE_R to KeyCodes.KEY_R,
        SDL_SCANCODE_S to KeyCodes.KEY_S,
        SDL_SCANCODE_T to KeyCodes.KEY_T,
        SDL_SCANCODE_U to KeyCodes.KEY_U,
        SDL_SCANCODE_V to KeyCodes.KEY_V,
        SDL_SCANCODE_W to KeyCodes.KEY_W,
        SDL_SCANCODE_X to KeyCodes.KEY_X,
        SDL_SCANCODE_Y to KeyCodes.KEY_Y,
        SDL_SCANCODE_Z to KeyCodes.KEY_Z,
        SDL_SCANCODE_LEFTBRACKET to KeyCodes.KEY_LEFT_BRACKET,
        SDL_SCANCODE_BACKSLASH to KeyCodes.KEY_BACKSLASH,
        SDL_SCANCODE_RIGHTBRACKET to KeyCodes.KEY_RIGHT_BRACKET,
        SDL_SCANCODE_GRAVE to KeyCodes.KEY_GRAVE_ACCENT,

        SDL_SCANCODE_ESCAPE to KeyCodes.KEY_ESCAPE,
        SDL_SCANCODE_RETURN to KeyCodes.KEY_ENTER,
        SDL_SCANCODE_TAB to KeyCodes.KEY_TAB,
        SDL_SCANCODE_BACKSPACE to KeyCodes.KEY_BACKSPACE,
        SDL_SCANCODE_INSERT to KeyCodes.KEY_INSERT,
        SDL_SCANCODE_DELETE to KeyCodes.KEY_DELETE,
        SDL_SCANCODE_RIGHT to KeyCodes.KEY_RIGHT,
        SDL_SCANCODE_LEFT to KeyCodes.KEY_LEFT,
        SDL_SCANCODE_DOWN to KeyCodes.KEY_DOWN,
        SDL_SCANCODE_UP to KeyCodes.KEY_UP,
        SDL_SCANCODE_PAGEUP to KeyCodes.KEY_PAGE_UP,
        SDL_SCANCODE_PAGEDOWN to KeyCodes.KEY_PAGE_DOWN,
        SDL_SCANCODE_HOME to KeyCodes.KEY_HOME,
        SDL_SCANCODE_END to KeyCodes.KEY_END,
        SDL_SCANCODE_CAPSLOCK to KeyCodes.KEY_CAPS_LOCK,
        SDL_SCANCODE_SCROLLLOCK to KeyCodes.KEY_SCROLL_LOCK,
        SDL_SCANCODE_NUMLOCKCLEAR to KeyCodes.KEY_NUM_LOCK,
        SDL_SCANCODE_PRINTSCREEN to KeyCodes.KEY_PRINT_SCREEN,
        SDL_SCANCODE_PAUSE to KeyCodes.KEY_PAUSE,
        SDL_SCANCODE_F1 to KeyCodes.KEY_F1,
        SDL_SCANCODE_F2 to KeyCodes.KEY_F2,
        SDL_SCANCODE_F3 to KeyCodes.KEY_F3,
        SDL_SCANCODE_F4 to KeyCodes.KEY_F4,
        SDL_SCANCODE_F5 to KeyCodes.KEY_F5,
        SDL_SCANCODE_F6 to KeyCodes.KEY_F6,
        SDL_SCANCODE_F7 to KeyCodes.KEY_F7,
        SDL_SCANCODE_F8 to KeyCodes.KEY_F8,
        SDL_SCANCODE_F9 to KeyCodes.KEY_F9,
        SDL_SCANCODE_F10 to KeyCodes.KEY_F10,
        SDL_SCANCODE_F11 to KeyCodes.KEY_F11,
        SDL_SCANCODE_F12 to KeyCodes.KEY_F12,
        SDL_SCANCODE_F13 to KeyCodes.KEY_F13,
        SDL_SCANCODE_F14 to KeyCodes.KEY_F14,
        SDL_SCANCODE_F15 to KeyCodes.KEY_F15,
        SDL_SCANCODE_F16 to KeyCodes.KEY_F16,
        SDL_SCANCODE_F17 to KeyCodes.KEY_F17,
        SDL_SCANCODE_F18 to KeyCodes.KEY_F18,
        SDL_SCANCODE_F19 to KeyCodes.KEY_F19,
        SDL_SCANCODE_F20 to KeyCodes.KEY_F20,
        SDL_SCANCODE_F21 to KeyCodes.KEY_F21,
        SDL_SCANCODE_F22 to KeyCodes.KEY_F22,
        SDL_SCANCODE_F23 to KeyCodes.KEY_F23,
        SDL_SCANCODE_F24 to KeyCodes.KEY_F24,
        SDL_SCANCODE_KP_0 to KeyCodes.KEY_KP_0,
        SDL_SCANCODE_KP_1 to KeyCodes.KEY_KP_1,
        SDL_SCANCODE_KP_2 to KeyCodes.KEY_KP_2,
        SDL_SCANCODE_KP_3 to KeyCodes.KEY_KP_3,
        SDL_SCANCODE_KP_4 to KeyCodes.KEY_KP_4,
        SDL_SCANCODE_KP_5 to KeyCodes.KEY_KP_5,
        SDL_SCANCODE_KP_6 to KeyCodes.KEY_KP_6,
        SDL_SCANCODE_KP_7 to KeyCodes.KEY_KP_7,
        SDL_SCANCODE_KP_8 to KeyCodes.KEY_KP_8,
        SDL_SCANCODE_KP_9 to KeyCodes.KEY_KP_9,
        SDL_SCANCODE_KP_PERIOD to KeyCodes.KEY_KP_DECIMAL,
        SDL_SCANCODE_KP_DIVIDE to KeyCodes.KEY_KP_DIVIDE,
        SDL_SCANCODE_KP_MULTIPLY to KeyCodes.KEY_KP_MULTIPLY,
        SDL_SCANCODE_KP_MINUS to KeyCodes.KEY_KP_SUBTRACT,
        SDL_SCANCODE_KP_PLUS to KeyCodes.KEY_KP_ADD,
        SDL_SCANCODE_KP_ENTER to KeyCodes.KEY_KP_ENTER,

        SDL_SCANCODE_LSHIFT to KeyCodes.KEY_LEFT_SHIFT,
        SDL_SCANCODE_LCTRL to KeyCodes.KEY_LEFT_CONTROL,
        SDL_SCANCODE_LALT to KeyCodes.KEY_LEFT_ALT,
        SDL_SCANCODE_LGUI to KeyCodes.KEY_LEFT_SUPER,
        SDL_SCANCODE_RSHIFT to KeyCodes.KEY_RIGHT_SHIFT,
        SDL_SCANCODE_RCTRL to KeyCodes.KEY_RIGHT_CONTROL,
        SDL_SCANCODE_RALT to KeyCodes.KEY_RIGHT_ALT,
        SDL_SCANCODE_RGUI to KeyCodes.KEY_RIGHT_SUPER,
        SDL_SCANCODE_MENU to KeyCodes.KEY_MENU,
    )

    val MOUSE_CODE_MAPPING = mapOf(
        SDL_BUTTON_LEFT to KeyCodes.MOUSE_BUTTON_LEFT,
        SDL_BUTTON_MIDDLE to KeyCodes.MOUSE_BUTTON_MIDDLE,
        SDL_BUTTON_RIGHT to KeyCodes.MOUSE_BUTTON_RIGHT,
        SDL_BUTTON_X1 to KeyCodes.MOUSE_BUTTON_1, // TODO
        SDL_BUTTON_X2 to KeyCodes.MOUSE_BUTTON_2, // TODO
    )

    val CursorShapes.sdl3
        get() = when (this) {
            CursorShapes.ARROW -> SDL_SYSTEM_CURSOR_DEFAULT
            CursorShapes.IBEAM -> SDL_SYSTEM_CURSOR_TEXT
            CursorShapes.CROSSHAIR -> SDL_SYSTEM_CURSOR_CROSSHAIR
            CursorShapes.HAND -> SDL_SYSTEM_CURSOR_POINTER
            CursorShapes.HORIZONTAL_RESIZE -> SDL_SYSTEM_CURSOR_EW_RESIZE
            CursorShapes.VERTICAL_RESIZE -> SDL_SYSTEM_CURSOR_NS_RESIZE
        }
}
