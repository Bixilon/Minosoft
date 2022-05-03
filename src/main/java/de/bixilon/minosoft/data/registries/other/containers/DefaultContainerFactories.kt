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

package de.bixilon.minosoft.data.registries.other.containers

import de.bixilon.minosoft.data.container.types.CraftingContainer
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.data.container.types.generic.*
import de.bixilon.minosoft.data.container.types.processing.smelting.BlastFurnaceContainer
import de.bixilon.minosoft.data.container.types.processing.smelting.FurnaceContainer
import de.bixilon.minosoft.data.container.types.processing.smelting.SmokerContainer
import de.bixilon.minosoft.data.registries.factory.DefaultFactory

object DefaultContainerFactories : DefaultFactory<ContainerFactory<*>>(
    PlayerInventory,

    Generic9x1Container,
    Generic9x2Container,
    Generic9x3Container,
    Generic9x4Container,
    Generic9x5Container,
    Generic9x6Container,

    CraftingContainer,

    BlastFurnaceContainer,
    FurnaceContainer,
    SmokerContainer,
)
