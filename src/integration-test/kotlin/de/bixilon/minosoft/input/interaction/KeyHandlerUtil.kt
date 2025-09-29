/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.input.interaction

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.concurrent.schedule.RepeatedTask
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.protocol.network.session.play.tick.Ticks.Companion.ticks
import org.testng.SkipException
import kotlin.time.Duration.Companion.milliseconds

object KeyHandlerUtil {
    private val FIELD = KeyHandler::class.java.getFieldOrNull("task")!!

    private fun KeyHandler.getTask(): RepeatedTask {
        return FIELD.get(this).unsafeCast() ?: throw IllegalStateException("Not pressed!")
    }

    fun KeyHandler.awaitTicks(count: Int) {
        val start = now()
        val task = getTask()
        val executions = task.executions
        while (true) {
            val time = now()
            if (time - start > (count + 1).ticks.duration - 1.milliseconds) throw SkipException("busy") // wait max one tick longer

            if (time - start < count.ticks.duration) {
                Thread.sleep(10)
                continue
            }

            if (task.executions - executions == count) break
            if (task.executions - executions > count) throw SkipException("Ran too often!")
            Thread.sleep(5)
            break // TODO
        }
    }
}
