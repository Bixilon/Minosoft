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

package de.bixilon.minosoft.gui.rendering.util

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.pool.runnable.ForcePooledRunnable
import de.bixilon.kutil.file.FileUtil.createParent
import de.bixilon.kutil.file.FileUtil.slashPath
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.assets.util.AssetsOptions
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.events.click.ClickCallbackClickEvent
import de.bixilon.minosoft.data.text.events.click.OpenFileClickEvent
import de.bixilon.minosoft.data.text.events.hover.TextHoverEvent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.confirmation.DeleteScreenshotDialog
import de.bixilon.minosoft.gui.rendering.system.base.PixelTypes
import de.bixilon.minosoft.terminal.RunConfiguration
import java.awt.image.BufferedImage
import java.text.SimpleDateFormat
import javax.imageio.ImageIO


class ScreenshotTaker(
    private val context: RenderContext,
) {
    fun takeScreenshot() {
        try {
            val width = context.window.size.x
            val height = context.window.size.y
            val buffer = context.renderSystem.readPixels(Vec2i(0, 0), Vec2i(width, height), PixelTypes.RGBA)

            val path = RunConfiguration.HOME_DIRECTORY.resolve("screenshots").resolve(context.connection.address.hostname)

            val timestamp = DATE_FORMATTER.format(millis())
            var filename = "$timestamp.png"
            var i = 1
            while (path.resolve(filename).toFile().exists()) {
                filename = "${timestamp}_${i++}.png"
                if (i > AssetsOptions.MAX_FILE_CHECKING) {
                    throw StackOverflowError("There are already > ${AssetsOptions.MAX_FILE_CHECKING} screenshots with this date! Please try again later!")
                }
            }

            DefaultThreadPool += ForcePooledRunnable(priority = ThreadPool.HIGHER) {
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

                    val file = path.resolve(filename).toFile()
                    file.createParent()

                    ImageIO.write(bufferedImage, "png", file)
                    var deleted = false

                    val component = BaseComponent()
                    component += "§aScreenshot saved: "
                    component += TextComponent(file.name).apply {
                        color = ChatColors.WHITE
                        underline()
                        clickEvent = OpenFileClickEvent(file.slashPath)
                        hoverEvent = TextHoverEvent("Click to open")
                    }
                    component += " "
                    component += TextComponent("[DELETE]").apply {
                        color = ChatColors.RED
                        bold()
                        clickEvent = ClickCallbackClickEvent {
                            if (deleted) {
                                return@ClickCallbackClickEvent
                            }
                            DeleteScreenshotDialog(context.renderer[GUIRenderer] ?: return@ClickCallbackClickEvent, file) {
                                deleted = true
                                hoverEvent = TextHoverEvent("§cAlready deleted!")
                                clickEvent = null
                                component.strikethrough() // ToDo: TextComponents are non mutable when passed to the renderer
                            }.show()
                        }
                        hoverEvent = TextHoverEvent("Click to delete screenshot")
                    }
                    context.connection.util.sendInternal(component)
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
        context.connection.util.sendInternal("§cFailed to make a screenshot: ${this?.message}")
    }

    companion object {
        private val DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")
    }
}
