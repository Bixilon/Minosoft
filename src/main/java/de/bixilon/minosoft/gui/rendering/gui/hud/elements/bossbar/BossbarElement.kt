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
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.bossbar.Bossbar
import de.bixilon.minosoft.data.bossbar.BossbarNotches
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasElement
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions

class BossbarElement(
    guiRenderer: GUIRenderer,
    val bossbar: Bossbar,
    val atlas: Array<Array<Array<AtlasElement?>>>,
) : Element(guiRenderer) {
    private val titleElement = TextElement(guiRenderer, text = bossbar.title, background = null, parent = this)
    private lateinit var progress: BossbarProgressElement

    init {
        tryUpdate()
        updateStyle()
        observe()
    }

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val size = size
        val titleSize = titleElement.size
        titleElement.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, titleSize.x), 0), consumer, options)
        progress.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, progress.size.x), titleSize.y), consumer, options)
    }

    private fun observe() {
        bossbar::color.observe(this) { invalidate() }
        bossbar::notches.observe(this) { invalidate() }
        bossbar::title.observe(this) { this.titleElement.text = it }
        bossbar::progress.observe(this) { invalidate() }
        bossbar::flags.observe(this) { invalidate() }
    }

    private fun updateStyle() {
        val notches = if (bossbar.notches == BossbarNotches.NO_NOTCHES) {
            null
        } else {
            atlas[1][bossbar.notches.ordinal - 1]
        }
        progress = BossbarProgressElement(guiRenderer, atlas[0][bossbar.color.ordinal].unsafeCast(), notches.unsafeCast(), bossbar.progress)
    }

    override fun update() {
        val size = Vec2(BAR_SIZE)
        size.x = maxOf(size.x, titleElement.size.x)
        size.y += titleElement.size.y
        _size = size
    }

    companion object {
        private val BAR_SIZE = Vec2i(182, 5)
    }
}
