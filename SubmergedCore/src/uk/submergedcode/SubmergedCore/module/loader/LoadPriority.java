/**
 * SubmergedCore 1.0
 * Copyright (C) 2014 CodingBadgers <plugins@mcbadgercraft.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.submergedcode.SubmergedCore.module.loader;

/**
 * The priority that each module will be loaded with, {@code HIGHEST} priority
 * modules will be loaded first, going down to {@code LOWEST} being loaded last
 * <p />
 * Note - that dependencies take precedence over priority so if a module
 * depends on another this will be taken into account before its load priority
 * is used. The LoadPriority is only used as a fallback.
 */
public enum LoadPriority {

    HIGHEST,
    HIGH,
    NORMAL,
    LOW,
    LOWEST;

}
