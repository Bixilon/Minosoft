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

package de.bixilon.minosoft.data.bossbar

import de.bixilon.kutil.collections.CollectionUtil.lockMapOf
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.kutil.watcher.DataWatcher.Companion.observe
import de.bixilon.kutil.watcher.DataWatcher.Companion.watched
import de.bixilon.kutil.watcher.map.MapChange.Companion.values
import de.bixilon.kutil.watcher.map.MapDataWatcher.Companion.observeMap
import de.bixilon.kutil.watcher.map.MapDataWatcher.Companion.watchedMap
import java.util.*

class BossbarManager {
    private val _bossbar: LockMap<UUID, Bossbar> = lockMapOf()
    val bossbars by watchedMap(_bossbar)
    var darkSky by watched(false)
    var fog by watched(false)

    init {
        this::bossbars.observeMap(this) {
            for (bossbar in it.adds.values()) {
                processFlags(bossbar.flags, ignoreRecalculate = true)
                bossbar::flags.observe(this) { flags -> processFlags(flags) }
            }
        }
    }

    private fun processFlags(flags: BossbarFlags, ignoreRecalculate: Boolean = false) {
        var recalculate = false
        if (flags.darkSky) {
            this.darkSky = true
        } else if (this.darkSky) {
            recalculate = true
        }
        if (flags.fog) {
            this.fog = true
        } else if (this.fog) {
            recalculate = true
        }
        if (!ignoreRecalculate && recalculate) {
            calculateFlags()
        }
    }

    private fun calculateFlags() {
        _bossbar.lock.acquire()
        var darkSky = false
        var fog = false
        for (bossbar in _bossbar.values) {
            if (bossbar.flags.darkSky) {
                darkSky = true
            }
            if (bossbar.flags.fog) {
                fog = true
            }
        }
        _bossbar.lock.release()
        this.darkSky = darkSky
        this.fog = fog
    }
}
