package de.bixilon.minosoft.gui.rendering.hud

import de.bixilon.minosoft.data.mappings.ModIdentifier
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.hud.elements.HUDElement
import de.bixilon.minosoft.gui.rendering.hud.elements.text.HUDTextElement
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class HUDRenderer(private val connection: Connection, renderWindow: RenderWindow) : Renderer {
    var hudScale = HUDScale.MEDIUM
    val hudElements: MutableMap<ModIdentifier, HUDElement> = mutableMapOf(
        ModIdentifier("minosoft:hud_text_renderer") to HUDTextElement(connection, this, renderWindow),
    )
    var lastTimePrepared = 0L


    override fun init() {
        for (element in hudElements.values) {
            element.init()
        }
    }

    override fun screenChangeResizeCallback(width: Int, height: Int) {
        for (element in hudElements.values) {
            element.screenChangeResizeCallback(width, height)
        }
    }

    override fun draw() {
        if (System.currentTimeMillis() - lastTimePrepared > ProtocolDefinition.TICK_TIME) {
            prepare()
            update()
            lastTimePrepared = System.currentTimeMillis()
        }

        for (element in hudElements.values) {
            element.draw()
        }
    }

    fun prepare() {
        for (element in hudElements.values) {
            element.prepare()
        }
    }

    fun update() {
        for (element in hudElements.values) {
            element.update()
        }
    }
}
