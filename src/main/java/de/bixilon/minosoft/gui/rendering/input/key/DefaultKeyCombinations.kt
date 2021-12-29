package de.bixilon.minosoft.gui.rendering.input.key

import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.PolygonModes
import de.bixilon.minosoft.util.KUtil.format
import de.bixilon.minosoft.util.KUtil.toResourceLocation

object DefaultKeyCombinations {

    fun registerAll(renderWindow: RenderWindow) {
        val inputHandler = renderWindow.inputHandler
        val window = renderWindow.window
        val connection = renderWindow.connection

        inputHandler.registerKeyCallback("minosoft:enable_debug_polygon".toResourceLocation(),
            KeyBinding(
                mapOf(
                    KeyAction.MODIFIER to setOf(KeyCodes.KEY_F4),
                    KeyAction.STICKY to setOf(KeyCodes.KEY_P),
                ),
            )) {
            val nextMode = it.decide(PolygonModes.LINE, PolygonModes.FILL)
            renderWindow.framebufferManager.world.polygonMode = nextMode
            connection.util.sendDebugMessage("Polygon mode: ${nextMode.format()}")
        }

        inputHandler.registerKeyCallback("minosoft:quit_rendering".toResourceLocation(),
            KeyBinding(
                mapOf(
                    KeyAction.RELEASE to setOf(KeyCodes.KEY_ESCAPE),
                ),
            )) { window.close() }

        inputHandler.registerKeyCallback("minosoft:take_screenshot".toResourceLocation(),
            KeyBinding(
                mapOf(
                    KeyAction.PRESS to setOf(KeyCodes.KEY_F2),
                ),
                ignoreConsumer = true,
            )) { renderWindow.screenshotTaker.takeScreenshot() }

        inputHandler.registerKeyCallback("minosoft:pause_incoming_packets".toResourceLocation(),
            KeyBinding(
                mapOf(
                    KeyAction.MODIFIER to setOf(KeyCodes.KEY_F4),
                    KeyAction.STICKY to setOf(KeyCodes.KEY_I),
                ),
                ignoreConsumer = true,
            )) {
            connection.util.sendDebugMessage("Pausing incoming packets: ${it.format()}")
            connection.network.pauseReceiving(it)
        }

        inputHandler.registerKeyCallback("minosoft:pause_outgoing_packets".toResourceLocation(),
            KeyBinding(
                mapOf(
                    KeyAction.MODIFIER to setOf(KeyCodes.KEY_F4),
                    KeyAction.STICKY to setOf(KeyCodes.KEY_O),
                ),
                ignoreConsumer = true,
            )) {
            connection.util.sendDebugMessage("Pausing outgoing packets: ${it.format()}")
            connection.network.pauseSending(it)
        }

        inputHandler.registerKeyCallback("minosoft:toggle_fullscreen".toResourceLocation(),
            KeyBinding(
                mapOf(
                    KeyAction.PRESS to setOf(KeyCodes.KEY_F11),
                ),
                ignoreConsumer = true,
            )) {
            window.fullscreen = !window.fullscreen
        }
    }
}
