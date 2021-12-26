package de.bixilon.minosoft.gui.rendering.input.interaction

import de.bixilon.kutil.rate.RateLimiter
import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.protocol.packets.c2s.play.PlayerActionC2SP
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class DropInteractionManager(
    private val renderWindow: RenderWindow,
) {
    private val connection = renderWindow.connection
    private val rateLimiter = RateLimiter()

    fun init() {
        // ToDo: This creates a weird condition, because we first drop the stack and then the single item
        // ToDo: Does this swing the arm?
        renderWindow.inputHandler.registerKeyCallback(DROP_ITEM_STACK_KEYBINDING, KeyBinding(
            mapOf(
                KeyAction.PRESS to setOf(KeyCodes.KEY_Q),
                KeyAction.MODIFIER to setOf(KeyCodes.KEY_LEFT_CONTROL)
            ),
        )) { dropItem(true) }
        renderWindow.inputHandler.registerKeyCallback(DROP_ITEM_KEYBINDING, KeyBinding(
            mapOf(
                KeyAction.PRESS to setOf(KeyCodes.KEY_Q),
            ),
        )) { dropItem(false) }
    }


    fun dropItem(stack: Boolean) {
        val time = KUtil.time
        val type = if (stack) {
            connection.player.inventory.getHotbarSlot()?.count = 0
            PlayerActionC2SP.Actions.DROP_ITEM_STACK
        } else {
            connection.player.inventory.getHotbarSlot()?.let {
                it.count--
            }
            PlayerActionC2SP.Actions.DROP_ITEM
        }
        rateLimiter += { connection.sendPacket(PlayerActionC2SP(type)) }
    }

    fun draw(delta: Double) {
        rateLimiter.work()
    }

    companion object {
        private val DROP_ITEM_KEYBINDING = "minosoft:drop_item".toResourceLocation()
        private val DROP_ITEM_STACK_KEYBINDING = "minosoft:drop_item_stack".toResourceLocation()
    }
}
