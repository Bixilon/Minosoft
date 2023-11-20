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

package de.bixilon.minosoft.config.profile.storage

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.exception.ExceptionUtil.ignoreAll
import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.minosoft.config.profile.profiles.Profile

object ProfileIOManager {
    private val lock = SimpleLock()
    private val notify = SimpleLatch(0)
    private val save: MutableSet<FileStorage> = mutableSetOf()
    private val delete: MutableSet<FileStorage> = mutableSetOf()
    private val reload: MutableSet<FileStorage> = mutableSetOf()


    fun init() {
        Thread({ while (true) observe() }, "ProfileIO").start()
    }

    private fun observe() {
        lock.lock()
        ignoreAll { delete() }
        ignoreAll { save() }
        ignoreAll { reload() }


        lock.unlock()
        notify.awaitOrChange()
        Thread.sleep(50L) // sometimes changes happen very quickly, we don't want to save 10 times while in change
    }

    private fun MutableSet<FileStorage>.work(worker: (Profile, FileStorage, StorageProfileManager<Profile>) -> Unit) {
        if (isEmpty()) return
        val iterator = iterator()
        while (iterator.hasNext()) {
            val storage = iterator.next()
            iterator.remove()
            val profile = storage.profile ?: throw IllegalArgumentException("Storage has no profile set!")
            val manager = storage.manager.unsafeCast<StorageProfileManager<Profile>>()
            ignoreAll { worker.invoke(profile, storage, manager) }
        }
    }

    private fun delete() {
        delete.work { profile, storage, manager ->
            storage.path.delete()
            storage.profile = null
        }
    }

    private fun save() {
        save.work { profile, storage, manager ->
            manager.save(profile)
        }
    }

    private fun reload() {
        // TODO
    }


    fun save(storage: FileStorage) {
        lock.lock()
        save += storage
        lock.unlock()
        notify.countUp()
    }

    fun delete(storage: FileStorage) {
        lock.lock()
        delete += storage
        lock.unlock()
        notify.countUp()
    }

    fun reload(storage: FileStorage) {
        lock.lock()
        reload += storage
        lock.unlock()
        notify.countUp()
    }
}
