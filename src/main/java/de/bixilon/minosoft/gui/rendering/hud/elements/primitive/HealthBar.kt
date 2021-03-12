/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.hud.elements.primitive

import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.hud.atlas.HUDAtlasElement
import de.bixilon.minosoft.util.MMath
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2

class HealthBar(
    start: Vec2 = Vec2(0, 0),
    var blackHeartContainerAtlasElement: HUDAtlasElement,
    var whiteHeartContainerAtlasElement: HUDAtlasElement,
    var halfHartAtlasElement: HUDAtlasElement,
    var hartAtlasElement: HUDAtlasElement,
    var maxValue: Float,
    var textReplaceValue: Float,
    var textColor: RGBColor,
    var font: Font,
    z: Int = 1,
) : Layout(start, z) {
    private val singleHeartSize = blackHeartContainerAtlasElement.binding.size
    private val width = singleHeartSize.x * MAX_HEARTS_IN_ROW

    private val alternativeText = TextElement(font = font, start = Vec2(), background = false)
    private var _value = 0.0f
    var value: Float
        get() = _value
        set(value) {
            _value = if (value < 0.0f) {
                0.0f
            } else {
                value
            }
            cache.clear()
        }

    fun prepare() {
        clear()

        if (value >= textReplaceValue) {
            alternativeText.text = TextComponent(value.toString()).setColor(textColor)
            alternativeText.start = Vec2((width - alternativeText.size.x) / 2, 0)

            addChild(alternativeText)
            return
        }

        val offset = Vec2(0, 0)
        val containerCount = (maxValue + 1.0f).toInt() / 2

        // heart container
        val rows = MMath.divideUp(containerCount, MAX_HEARTS_IN_ROW)
        for (row in 0 until rows) {
            val heartsToDraw = if (row == 0 && containerCount % MAX_HEARTS_IN_ROW != 0) {
                containerCount % MAX_HEARTS_IN_ROW
            } else {
                MAX_HEARTS_IN_ROW
            }
            for (i in 0 until heartsToDraw) {
                drawHeart(this.start + offset, blackHeartContainerAtlasElement, z)

                offset.x += singleHeartSize.x - 1
            }
            offset.y += singleHeartSize.y
            offset.x = 0.0f
        }


        offset.x = 0.0f
        offset.y = 0.0f
        val halfHeartCount = MMath.round10Up(value)
        val fullHeartCount = halfHeartCount / 2

        val addHalfHeart = halfHeartCount % 2 == 1

        var currentHeart = fullHeartCount - 1
        if (addHalfHeart) {
            currentHeart += 1
        }

        for (row in rows - 1 downTo 0) {
            val heartsInRow = if (row == 0 && containerCount % MAX_HEARTS_IN_ROW != 0) {
                containerCount % MAX_HEARTS_IN_ROW
            } else {
                MAX_HEARTS_IN_ROW
            }
            for (i in 0 until heartsInRow) {

                if (currentHeart < 0) {
                    break
                }

                if (currentHeart == 0 && addHalfHeart) {
                    drawHeart(this.start + offset, halfHartAtlasElement, z + 1)
                } else {
                    drawHeart(this.start + offset, hartAtlasElement, z + 1)
                }

                currentHeart--
                offset.x += singleHeartSize.x - 1
            }
            offset.y += singleHeartSize.y
            offset.x = 0.0f
        }
    }

    override fun prepareCache(start: Vec2, scaleFactor: Float, matrix: Mat4, z: Int) {
        prepare()
        super.prepareCache(start, scaleFactor, matrix, z)
    }


    private fun drawHeart(elementStart: Vec2, element: HUDAtlasElement, z: Int) {
        addChild(ImageElement(elementStart, element, elementStart + singleHeartSize, this.z + z))
    }

    companion object {
        const val MAX_HEARTS_IN_ROW = 10
    }
}
