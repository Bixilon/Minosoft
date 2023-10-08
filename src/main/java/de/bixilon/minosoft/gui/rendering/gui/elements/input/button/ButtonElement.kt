/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.elements.input.button

import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer

open class ButtonElement(
    guiRenderer: GUIRenderer,
    text: Any,
    disabled: Boolean = false,
    var onSubmit: () -> Unit,
) : AbstractButtonElement(guiRenderer, text, disabled) {
    private val atlas = guiRenderer.atlas[ATLAS]
    override val disabledAtlas = atlas?.get("disabled")
    override val normalAtlas = atlas?.get("normal")
    override val hoveredAtlas = atlas?.get("hovered")

    override fun submit() {
        onSubmit()
    }

    companion object {
        val ATLAS = minecraft("elements/button")
    }
}
