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

package de.bixilon.minosoft.gui.rendering.gui.hud.hud

import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.block.WorldRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.layout.RowLayout
import de.bixilon.minosoft.gui.rendering.gui.elements.text.AutoTextElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.particle.ParticleRenderer
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil.format
import de.bixilon.minosoft.util.MMath.round10

class DebugHUD(val hudRenderer: HUDRenderer) : HUD<RowLayout> {
    override val renderWindow: RenderWindow = hudRenderer.renderWindow
    override val layout = RowLayout(hudRenderer)


    override fun init() {
        layout += TextElement(hudRenderer, TextComponent(RunConfiguration.VERSION_STRING, ChatColors.RED))
        layout += AutoTextElement(hudRenderer, 1) { "FPS ${renderWindow.renderStats.smoothAvgFPS.round10}" }
        renderWindow[WorldRenderer]?.apply {
            layout += AutoTextElement(hudRenderer, 1) { "C v=${visibleChunks.size}, p=${allChunkSections.size}, q=${queuedChunks.size}, t=${renderWindow.connection.world.chunks.size}" }
        }
        layout += AutoTextElement(hudRenderer, 1) { "E t=${renderWindow.connection.world.entities.size}" }

        renderWindow[ParticleRenderer]?.apply {
            layout += AutoTextElement(hudRenderer, 1) { "P t=$size" }
        }

        layout += TextElement(hudRenderer, "") // ToDo: Spacer element


        renderWindow.connection.player.apply {
            layout += AutoTextElement(hudRenderer, 1) { with(position) { "XYZ ${x.format()} / ${y.format()} / ${z.format()}" } }
            layout += AutoTextElement(hudRenderer, 1) { with(positionInfo.blockPosition) { "Block $x $y $z" } }
        }
    }
}
