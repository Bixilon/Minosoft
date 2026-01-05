/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.elements.text

import de.bixilon.minosoft.data.text.EmptyComponent
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.protocol.network.session.play.tick.Ticks
import de.bixilon.minosoft.protocol.network.session.play.tick.Ticks.Companion.ticks

class AutoTextElement(
    guiRenderer: GUIRenderer,
    var interval: Ticks = 1.ticks,
    properties: TextRenderProperties = TextRenderProperties.DEFAULT,
    private val updater: () -> Any,
) : TextElement(guiRenderer, EmptyComponent, properties = properties) {
    private var remainingTicks = 0.ticks

    init {
        text = updater()
    }

    override fun tick() {
        super.tick()

        if (remainingTicks-- > 0.ticks) {
            return
        }

        text = updater()

        remainingTicks = interval
    }
}
