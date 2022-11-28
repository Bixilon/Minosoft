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

package de.bixilon.minosoft.config.profile.delegate

import de.bixilon.kutil.observer.DataObserver
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.util.KUtil.minosoft
import kotlin.reflect.KProperty

open class SimpleDelegate<T>(
    override val profile: Profile,
    default: T,
    name: String,
    private val verify: ((T) -> Unit)? = null,
) : DataObserver<T>(default), AbstractDelegate<T> {
    override val name = minosoft(name)
    override val description = minosoft("$name.description")

    override fun get() = value
    override fun set(value: T) {
        validate(value)
        this.value = value
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        super.setValue(thisRef, property, value)
        queueSave()
    }

    override fun validate(value: T) {
        verify?.invoke(value)
    }
}
