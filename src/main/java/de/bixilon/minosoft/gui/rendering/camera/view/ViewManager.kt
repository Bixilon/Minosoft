/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.camera.view

import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.util.KUtil.format
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class ViewManager(private val camera: Camera) {
    private val debug = DebugView(camera)
    private val firstPerson = FirstPersonView(camera)
    private val thirdPerson = FirstPersonView(camera) // TODO

    var view: CameraView = firstPerson
        private set

    private var isDebug = false
    private var isThirdPerson = false


    fun init() {
        camera.renderWindow.inputHandler.registerKeyCallback(
            "minosoft:camera_debug_view".toResourceLocation(),
            KeyBinding(
                KeyActions.MODIFIER to setOf(KeyCodes.KEY_F4),
                KeyActions.STICKY to setOf(KeyCodes.KEY_V),
            )
        ) {
            this.isDebug = it
            updateView()
            camera.renderWindow.connection.util.sendDebugMessage("Camera debug view: ${it.format()}")
        }

        camera.renderWindow.inputHandler.registerKeyCallback(
            "minosoft:camera_third_person".toResourceLocation(),
            KeyBinding(
                KeyActions.STICKY to setOf(KeyCodes.KEY_F5),
            )
        ) {
            this.isThirdPerson = it
            updateView()
        }

        view.onAttach(null)
    }


    private fun updateView(debug: Boolean = isDebug, thirdPerson: Boolean = isThirdPerson) {
        val next = getView(debug, thirdPerson)
        if (next == this.view) {
            return
        }
        val previous = this.view
        previous.onDetach(next)
        this.view = next
        this.view.onAttach(previous)
    }

    private fun getView(debug: Boolean, thirdPerson: Boolean): CameraView {
        if (debug) return this.debug
        if (thirdPerson) return this.thirdPerson
        return this.firstPerson
    }

    fun draw() {
        view.draw()
    }
}
