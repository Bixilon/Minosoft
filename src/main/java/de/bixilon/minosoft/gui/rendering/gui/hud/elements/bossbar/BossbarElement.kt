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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.bossbar

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.array.ArrayUtil.cast
import de.bixilon.minosoft.data.bossbar.Bossbar
import de.bixilon.minosoft.data.bossbar.BossbarColors
import de.bixilon.minosoft.data.bossbar.BossbarNotches
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions

class BossbarElement(
    guiRenderer: GUIRenderer,
    val bossbar: Bossbar,
    val atlas: BossbarAtlas?,
) : Element(guiRenderer), Pollable {
    private var color: BossbarColors = bossbar.color
    private var notches: BossbarNotches = bossbar.notches

    private val titleElement = TextElement(guiRenderer, text = bossbar.title, background = null, parent = this)
    private lateinit var progress: BossbarProgressElement

    init {
        forceSilentApply()
        setStyle()
    }

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val size = size
        val titleSize = titleElement.size
        titleElement.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, titleSize.x), 0), consumer, options)
        progress.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, progress.size.x), titleSize.y), consumer, options)
    }

    override fun poll(): Boolean {
        var changes = 0

        val title = bossbar.title
        if (titleElement.chatComponent != title) {
            titleElement.text = title
            changes++
        }

        val changesBeforeStyle = changes
        val color = bossbar.color
        if (this.color != color) {
            this.color = color
            changes++
        }

        val notches = bossbar.notches
        if (this.notches != notches) {
            this.notches = notches
            changes++
        }

        if (changes < changesBeforeStyle) {
            setStyle()
        }

        val value = bossbar.progress
        if (progress.progress != value) {
            progress.progress = value
            changes++
        }

        return changes > 0
    }

    private fun setStyle() {
        // ToDo: Cache progress
        val notches = if (notches == BossbarNotches.NO_NOTCHES) null else atlas?.get(notches)
        progress = BossbarProgressElement(guiRenderer, atlas?.get(color)!!.cast(), notches?.cast(), 0.0f)
    }

    override fun forceSilentApply() {
        val size = Vec2(BAR_SIZE)
        size.x = maxOf(size.x, titleElement.size.x)
        size.y += titleElement.size.y
        _size = size

        cacheUpToDate = false
    }

    override fun onChildChange(child: Element) = Unit


    companion object {
        private val BAR_SIZE = Vec2i(182, 5)
    }
}
