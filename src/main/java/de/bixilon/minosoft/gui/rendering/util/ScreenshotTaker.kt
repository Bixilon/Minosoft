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

package de.bixilon.minosoft.gui.rendering.util

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.logging.Log
import de.matthiasmann.twl.utils.PNGDecoder
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import java.awt.image.BufferedImage
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import javax.imageio.ImageIO


class ScreenshotTaker(
    private val renderWindow: RenderWindow,
) {
    fun takeScreenshot() {
        try {
            val basePath = "${StaticConfiguration.HOME_DIRECTORY}/screenshots/${renderWindow.connection.address.hostname}/${DATE_FORMATTER.format(System.currentTimeMillis())}"
            var path = "$basePath.png"
            var i = 1
            while (File(path).exists()) {
                path = "${basePath}_${i++}.png"
            }

            val width = renderWindow.screenDimensions.x.toInt()
            val height = renderWindow.screenDimensions.y.toInt()
            val buffer: ByteBuffer = BufferUtils.createByteBuffer(width * height * PNGDecoder.Format.RGBA.numComponents)
            glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer)

            Minosoft.THREAD_POOL.execute {
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

                    val message = "§aScreenshot saved to §f${file.name}"
                    renderWindow.sendDebugMessage(message)
                    Log.game(message)
                } catch (exception: Exception) {
                    screenshotFail(exception)
                }
            }
        } catch (exception: Exception) {
            screenshotFail(exception)
        }
    }

    private fun screenshotFail(exception: Exception?) {
        exception?.printStackTrace()
        renderWindow.sendDebugMessage("§cFailed to make a screenshot!")
    }

    companion object {
        private val DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")
    }
}
