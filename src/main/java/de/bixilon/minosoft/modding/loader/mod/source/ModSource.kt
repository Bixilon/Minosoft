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

package de.bixilon.minosoft.modding.loader.mod.source

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import de.bixilon.minosoft.modding.loader.mod.MinosoftMod
import java.io.File

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = DirectorySource::class, name = "directory"),
    JsonSubTypes.Type(value = SplitDirectorySource::class, name = "split_directory"),
    JsonSubTypes.Type(value = ArchiveSource::class, name = "archive"),
)

interface ModSource {

    fun process(mod: MinosoftMod)

    companion object {

        fun detect(file: File): ModSource? {
            if (file.isDirectory) {
                return DirectorySource(file)
            }
            if (file.isFile && (file.name.endsWith(".jar") || file.name.endsWith(".zip"))) {
                return ArchiveSource(file)
            }
            return null
        }
    }
}
