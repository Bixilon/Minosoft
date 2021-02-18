/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

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

    override fun postInit() {
        for (element in hudElements.values) {
            element.postInit()
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
