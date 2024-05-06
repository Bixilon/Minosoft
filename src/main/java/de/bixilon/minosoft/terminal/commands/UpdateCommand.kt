/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.terminal.commands

import de.bixilon.minosoft.commands.nodes.LiteralNode
import de.bixilon.minosoft.commands.stack.print.PrintTarget
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.terminal.CommandUtil.commandText
import de.bixilon.minosoft.updater.MinosoftUpdate
import de.bixilon.minosoft.updater.MinosoftUpdater
import de.bixilon.minosoft.updater.UpdateProgress

object UpdateCommand : Command {
    override var node: LiteralNode = LiteralNode("update", executor = { it.print.check() }).addChild(
        LiteralNode("notes", executor = {
            MinosoftUpdater.check { update ->
                if (update == null) {
                    print("No update available!")
                } else {
                    update.printNotes(it.print)
                }
            }
        }),
        LiteralNode("update", executor = {
            MinosoftUpdater.check(false) { update ->
                if (update == null) {
                    print("No update available!")
                }
                if (update != null) {
                    val progress = UpdateProgress(log = it.print)
                    MinosoftUpdater.download(update, progress)
                    return@check
                }
            }
        }),
        LiteralNode("dismiss", executor = {
            val update = MinosoftUpdater.update
            if (update == null) {
                it.print.print("§cNot checked for updates!")
                return@LiteralNode
            }
            it.print.print("Dismissed update ${update.name} (${update.id})")
            OtherProfileManager.selected.updater.dismiss = update.id
        }),
    )


    private fun PrintTarget.check() {
        print("Checking for updates...")
        MinosoftUpdater.check(true) {
            if (it == null) {
                print("No update available!")
            } else {
                print("There is a new update available:")
                print("Version: ${it.name} (${it.id})")
                print(BaseComponent("Run ", commandText("update notes"), " to see the release notes."))
                print(BaseComponent("Run ", commandText("update update"), " to download and update."))
            }
        }
    }

    private fun MinosoftUpdate.printNotes(target: PrintTarget) {
        if (releaseNotes == null) {
            target.print("No release notes for $name ($id) available.")
            return
        }
        target.print("Release notes for update $name ($id):")
        target.print(releaseNotes)
    }
}
