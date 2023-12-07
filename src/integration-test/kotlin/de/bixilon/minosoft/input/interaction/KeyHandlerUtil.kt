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

package de.bixilon.minosoft.input.interaction

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.concurrent.schedule.RepeatedTask
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.kutil.time.TimeUtil.millis
import org.testng.SkipException

object KeyHandlerUtil {
    private val FIELD = KeyHandler::class.java.getFieldOrNull("task")!!

    private fun KeyHandler.getTask(): RepeatedTask {
        return FIELD.get(this).unsafeCast() ?: throw IllegalStateException("Not pressed!")
    }

    fun KeyHandler.awaitTicks(count: Int) {
        val start = millis()
        val task = getTask()
        val executions = task.executions
        while (true) {
            val time = millis()
            if (time - start > (count + 1) * 50 - 1) throw SkipException("busy") // wait max one tick longer

            if (time - start < count * 50) {
                Thread.sleep(10)
                continue
            }

            if (task.executions - executions == count) break
            if (task.executions - executions > count) throw SkipException("Ran too often!")
            Thread.sleep(5)
        }
    }
}
