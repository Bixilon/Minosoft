#!/bin/ash
#
# Minosoft
# Copyright (C) 2020-2024 Moritz Zwerger
#
# This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
#
# This software is not affiliated with Mojang AB, the original developer of Minecraft.
#

curl \
  --form-string "stable=false" \
  -F "page=" \
  -F "release_notes=abc" \
  -F "channel=test" \
  -H "Authorization: Bearer ..Hs6dpK_aHBWeuasq2MA2c_zBw1JCJsgk4Lcw2fm1PoI" \
  localhost:8080/api/v1/releases/create/test
#  https://minosoft.bixilon.de/api/v1/releases/create/test
