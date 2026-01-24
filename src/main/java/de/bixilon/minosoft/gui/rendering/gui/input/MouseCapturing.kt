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

package de.bixilon.minosoft.gui.rendering.gui.input

import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons

/**
 * Interface for GUI elements that can capture mouse input.
 * When an element is capturing mouse, the parent layout should continue 
 * forwarding mouse events to it even when the mouse position is outside its bounds.
 * 
 * This is useful for elements like sliders that need to track mouse movement
 * during drag operations even when the cursor moves outside the element.
 *
 * This file exists purely for sliders in menus to be able to capture mouse outside their bounds. 
 * Could have more uses in future...
 */
interface MouseCapturing {
    val isCapturingMouse: Boolean
    
    /**
     * Handle mouse action from parent when mouse is outside this element.
     * Called by parent layouts when mouse action occurs outside the element's bounds
     * but the element is still capturing mouse.
     * 
     * @param relativeX The x position relative to this element's position
     * @param button The mouse button that was pressed/released
     * @param action The type of mouse action (press/release)
     * @return true if the event was handled
     */
    fun onMouseActionOutside(relativeX: Float, button: MouseButtons, action: MouseActions): Boolean
    
    /**
     * Handle mouse move from parent when mouse is outside this element.
     * Called by parent layouts during mouse movement when the element is capturing mouse.
     * 
     * @param relativeX The x position relative to this element's position
     * @return true if the event was handled
     */
    fun onMouseMoveOutside(relativeX: Float): Boolean
}
