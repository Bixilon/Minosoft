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

package de.bixilon.minosoft.config.key

import com.fasterxml.jackson.annotation.JsonInclude
import de.bixilon.minosoft.util.KUtil.synchronizedDeepCopy

class KeyBinding(
    val action: MutableMap<KeyActions, MutableSet<KeyCodes>>,
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) var ignoreConsumer: Boolean = false,
    ignored: Boolean = true, // to prevent constructor overloading
) {
    constructor(keyBinding: KeyBinding) : this(keyBinding.action.synchronizedDeepCopy())
    constructor(action: Map<KeyActions, Set<KeyCodes>>, ignoreConsumer: Boolean = false) : this(action.copy(), ignoreConsumer)
    constructor(vararg action: Pair<KeyActions, Set<KeyCodes>>, ignoreConsumer: Boolean = false) : this(mapOf(*action), ignoreConsumer)


    companion object {
        private fun Map<KeyActions, Set<KeyCodes>>.copy(): MutableMap<KeyActions, MutableSet<KeyCodes>> {
            val next: MutableMap<KeyActions, MutableSet<KeyCodes>> = mutableMapOf()
            for ((action, codes) in this) {
                next[action] = codes.toMutableSet()
            }
            return next
        }
    }
}
