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

package de.bixilon.minosoft.gui.eros.controller

import de.bixilon.minosoft.gui.eros.modding.events.ErosControllerTerminateEvent
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.util.KUtil.nullCast
import javafx.event.ActionEvent
import javafx.fxml.Initializable
import javafx.scene.control.Labeled
import java.net.URL
import java.util.*


abstract class JavaFXController : Initializable {

    fun openURL(actionEvent: ActionEvent) {
        actionEvent.target?.nullCast<Labeled>()?.text?.let {
            JavaFXUtil.HOST_SERVICES.showDocument(it)
        }
    }

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        init()
    }

    open fun init() = Unit

    open fun postInit() = Unit

    open fun terminate() {
        GlobalEventMaster.fireEvent(ErosControllerTerminateEvent(this))
    }
}
