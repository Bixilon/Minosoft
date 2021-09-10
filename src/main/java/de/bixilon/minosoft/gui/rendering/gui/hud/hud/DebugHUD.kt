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
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.ElementAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.layout.RowLayout
import de.bixilon.minosoft.gui.rendering.gui.elements.layout.grid.GridGrow
import de.bixilon.minosoft.gui.rendering.gui.elements.layout.grid.GridLayout
import de.bixilon.minosoft.gui.rendering.gui.elements.spacer.LineSpacerElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.AutoTextElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.modding.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.particle.ParticleRenderer
import de.bixilon.minosoft.modding.event.events.DifficultyChangeEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.modding.loading.ModLoader
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.GitInfo
import de.bixilon.minosoft.util.KUtil.decide
import de.bixilon.minosoft.util.KUtil.format
import de.bixilon.minosoft.util.MMath.round10
import de.bixilon.minosoft.util.SystemInformation
import glm_.vec2.Vec2i
import glm_.vec4.Vec4i

class DebugHUD(val hudRenderer: HUDRenderer) : HUD<GridLayout> {
    override val renderWindow: RenderWindow = hudRenderer.renderWindow
    private val connection = renderWindow.connection
    override val layout = GridLayout(hudRenderer, Vec2i(3, 1)).apply {
        columnConstraints[0].apply {
            grow = GridGrow.NEVER
        }
        columnConstraints[2].apply {
            grow = GridGrow.NEVER
            alignment = ElementAlignments.RIGHT
        }

        apply()
    }


    override fun init() {
        layout[Vec2i(0, 0)] = initLeft()
        layout[Vec2i(2, 0)] = initRight()
    }

    private fun initLeft(): Element {
        val layout = RowLayout(hudRenderer)
        layout.margin = Vec4i(5)
        layout += TextElement(hudRenderer, TextComponent(RunConfiguration.VERSION_STRING, ChatColors.RED))
        layout += AutoTextElement(hudRenderer, 1) { "FPS ${renderWindow.renderStats.smoothAvgFPS.round10}" }
        renderWindow[WorldRenderer]?.apply {
            layout += AutoTextElement(hudRenderer, 1) { "C v=${visibleChunks.size}, p=${allChunkSections.size}, q=${queuedChunks.size}, t=${connection.world.chunks.size}" }
        }
        layout += AutoTextElement(hudRenderer, 1) { "E t=${connection.world.entities.size}" }

        renderWindow[ParticleRenderer]?.apply {
            layout += AutoTextElement(hudRenderer, 1) { "P t=$size" }
        }

        layout += LineSpacerElement(hudRenderer)


        connection.player.apply {
            layout += AutoTextElement(hudRenderer, 1) { with(position) { "XYZ ${x.format()} / ${y.format()} / ${z.format()}" } }
            layout += AutoTextElement(hudRenderer, 1) { with(positionInfo.blockPosition) { "Block $x $y $z" } }
        }

        layout += LineSpacerElement(hudRenderer)

        val chunk = connection.world[connection.player.positionInfo.chunkPosition]

        if (chunk == null) {
            layout += TextElement(hudRenderer, "Waiting for chunk...") // ToDo
        }

        layout += LineSpacerElement(hudRenderer)

        layout += TextElement(hudRenderer, "Difficulty ${connection.world.difficulty.format()}, ${connection.world.difficultyLocked.decide("locked", "unlocked")}", ElementAlignments.RIGHT).apply {
            connection.registerEvent(CallbackEventInvoker.of<DifficultyChangeEvent> {
                text = "Difficulty ${it.difficulty.format()}, ${it.locked.decide("locked", "unlocked")}"
            })
        }

        return layout
    }

    private fun initRight(): Element {
        val layout = RowLayout(hudRenderer, ElementAlignments.RIGHT)
        layout.margin = Vec4i(5)
        layout += TextElement(hudRenderer, "Java ${Runtime.version()} ${System.getProperty("sun.arch.data.model")}bit", ElementAlignments.RIGHT)

        layout += LineSpacerElement(hudRenderer)

        layout += TextElement(hudRenderer, "OS ${SystemInformation.OS_TEXT}", ElementAlignments.RIGHT)
        layout += TextElement(hudRenderer, "CPU ${SystemInformation.PROCESSOR_TEXT}", ElementAlignments.RIGHT)

        layout += LineSpacerElement(hudRenderer)

        layout += TextElement(hudRenderer, "Display TBA", ElementAlignments.RIGHT).apply {
            hudRenderer.connection.registerEvent(CallbackEventInvoker.of<ResizeWindowEvent> {
                text = "Display ${it.size.x}x${it.size.y}"
            })
        }

        renderWindow.renderSystem.apply {
            layout += TextElement(hudRenderer, "GPU $gpuType", ElementAlignments.RIGHT)
            layout += TextElement(hudRenderer, "Version $version", ElementAlignments.RIGHT)
        }

        if (GitInfo.IS_INITIALIZED) {
            layout += LineSpacerElement(hudRenderer)

            GitInfo.apply {
                layout += TextElement(hudRenderer, "Git $GIT_COMMIT_ID_ABBREV: $GIT_COMMIT_MESSAGE_SHORT", ElementAlignments.RIGHT)
            }
        }

        layout += LineSpacerElement(hudRenderer)

        layout += TextElement(hudRenderer, "Mods ${ModLoader.MOD_MAP.size}x loaded, ${connection.size}x listeners", ElementAlignments.RIGHT)

        return layout
    }
}
