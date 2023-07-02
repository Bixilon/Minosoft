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
import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.observer.map.MapObserver.Companion.observeMap
import de.bixilon.minosoft.data.bossbar.Bossbar
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.layout.RowLayout
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.util.Initializable
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class BossbarLayout(guiRenderer: GUIRenderer) : RowLayout(guiRenderer, HorizontalAlignments.CENTER, 2.0f), LayoutedElement, Initializable {
    private val connection = context.connection
    private val bossbars: MutableMap<Bossbar, BossbarElement> = synchronizedMapOf()

    override val layoutOffset: Vec2
        get() = Vec2((guiRenderer.screen.scaled.x - super.size.x) / 2, 2)

    val atlasManager = guiRenderer.atlasManager

    /**
     * [bar|notches][color.ordinal|notches.ordinal-1][empty|full]
     */
    private val atlas = arrayOf(
        arrayOf(
            arrayOf(
                atlasManager["minecraft:bossbar_pink_empty"],
                atlasManager["minecraft:bossbar_pink_full"],
            ),
            arrayOf(
                atlasManager["minecraft:bossbar_blue_empty"],
                atlasManager["minecraft:bossbar_blue_full"],
            ),
            arrayOf(
                atlasManager["minecraft:bossbar_red_empty"],
                atlasManager["minecraft:bossbar_red_full"],
            ),
            arrayOf(
                atlasManager["minecraft:bossbar_green_empty"],
                atlasManager["minecraft:bossbar_green_full"],
            ),
            arrayOf(
                atlasManager["minecraft:bossbar_yellow_empty"],
                atlasManager["minecraft:bossbar_yellow_full"],
            ),
            arrayOf(
                atlasManager["minecraft:bossbar_purple_empty"],
                atlasManager["minecraft:bossbar_purple_full"],
            ),
            arrayOf(
                atlasManager["minecraft:bossbar_white_empty"],
                atlasManager["minecraft:bossbar_white_full"],
            ),
        ),
        arrayOf(
            arrayOf(
                atlasManager["minecraft:bossbar_notches_6_empty"],
                atlasManager["minecraft:bossbar_notches_6_full"],
            ),
            arrayOf(
                atlasManager["minecraft:bossbar_notches_10_empty"],
                atlasManager["minecraft:bossbar_notches_10_full"],
            ),
            arrayOf(
                atlasManager["minecraft:bossbar_notches_12_empty"],
                atlasManager["minecraft:bossbar_notches_12_empty"],
            ),
            arrayOf(
                atlasManager["minecraft:bossbar_notches_20_empty"],
                atlasManager["minecraft:bossbar_notches_20_full"],
            ),
        ),
    )

    override fun postInit() {
        connection.bossbars::bossbars.observeMap(this) {
            for ((_, bossbar) in it.adds) {
                val element = BossbarElement(guiRenderer, bossbar, atlas)
                this += element
                val previous = bossbars.put(bossbar, element) ?: continue
                this -= previous
            }
            for ((_, bossbar) in it.removes) {
                val element = bossbars.remove(bossbar) ?: continue
                this -= element
            }
        }
    }


    companion object : HUDBuilder<LayoutedGUIElement<BossbarLayout>> {
        override val identifier: ResourceLocation = "minosoft:bossbar".toResourceLocation()

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<BossbarLayout> {
            return LayoutedGUIElement(BossbarLayout(guiRenderer))
        }
    }
}
