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

package de.bixilon.minosoft.gui.rendering.util

import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.events.ClickEvent
import de.bixilon.minosoft.data.text.events.HoverEvent
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.PixelTypes
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool
import de.bixilon.minosoft.util.task.pool.ThreadPool
import de.bixilon.minosoft.util.task.pool.ThreadPoolRunnable
import glm_.vec2.Vec2i
import java.awt.image.BufferedImage
import java.io.File
import java.text.SimpleDateFormat
import javax.imageio.ImageIO


class ScreenshotTaker(
    private val renderWindow: RenderWindow,
) {
    fun takeScreenshot() {
        try {
            val width = renderWindow.window.size.x
            val height = renderWindow.window.size.y
            val buffer = renderWindow.renderSystem.readPixels(Vec2i(0, 0), Vec2i(width, height), PixelTypes.RGBA)

            val basePath = "${RunConfiguration.HOME_DIRECTORY}/screenshots/${renderWindow.connection.address.hostname}/${DATE_FORMATTER.format(System.currentTimeMillis())}"
            var path = "$basePath.png"
            var i = 1
            while (File(path).exists()) {
                path = "${basePath}_${i++}.png"
                if (i > MAX_FILES_CHECK) {
                    throw StackOverflowError("There are already > $MAX_FILES_CHECK screenshots with this date! Please try again!")
                }
            }

            DefaultThreadPool += ThreadPoolRunnable(priority = ThreadPool.HIGHER) {
                try {
                    val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            val index: Int = (x + width * y) * 4
                            val red: Int = buffer[index].toInt() and 0xFF
                            val green: Int = buffer[index + 1].toInt() and 0xFF
                            val blue: Int = buffer[index + 2].toInt() and 0xFF
                            bufferedImage.setRGB(x, height - (y + 1), 0xFF shl 24 or (red shl 16) or (green shl 8) or blue)
                        }
                    }

                    val file = File(path)
                    Util.createParentFolderIfNotExist(file)

                    ImageIO.write(bufferedImage, "png", file)

                    renderWindow.sendDebugMessage(BaseComponent(
                        "§aScreenshot saved to ",
                        TextComponent(file.name).apply {
                            color = ChatColors.WHITE
                            underline()
                            clickEvent = ClickEvent(ClickEvent.ClickEventActions.OPEN_URL, "file:${file.absolutePath}")
                            hoverEvent = HoverEvent(HoverEvent.HoverEventActions.SHOW_TEXT, "Click to open")
                        },
                        // "\n",
                        // TextComponent("[DELETE]").apply {
                        //     color = ChatColors.RED
                        //     bold()
                        //     clickEvent = ClickEvent(ClickEvent.ClickEventActions.OPEN_CONFIRMATION, {
                        //         TODO()
                        //     })
                        //     hoverEvent = HoverEvent(HoverEvent.HoverEventActions.SHOW_TEXT, "Click to delete screenshot")
                        // },
                    ))
                } catch (exception: Exception) {
                    exception.fail()
                }
            }
        } catch (exception: Exception) {
            exception.fail()
        }
    }

    private fun Throwable?.fail() {
        this?.printStackTrace()
        renderWindow.sendDebugMessage("§cFailed to make a screenshot: ${this?.message}")
    }

    companion object {
        private val DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")
        private const val MAX_FILES_CHECK = 10
    }
}
