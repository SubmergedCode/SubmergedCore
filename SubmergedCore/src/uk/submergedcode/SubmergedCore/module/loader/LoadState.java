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

import uk.submergedcode.SubmergedCore.module.Module;

/**
 * Created by James on 05/05/2014.
 */
public enum LoadState {

    PRE_SETUP,

    /**
     * Setup phase
     * <p />
     * Loads all modules and their required information from disk
     */
    SETUP,

    /**
     * Load phase
     * <p />
     * Loads the modules main class into memory and calls the
     * {@link Module#onLoad()} method. As well as this, help pages for the
     * module are created and added to the bukkit help page.
     */
    LOAD,

    /**
     * Enable phase
     * <p />
     * Calls {@link Module#onEnable()} on all modules.
     */
    ENABLE,

    /**
     * Setup phase
     * <p />
     * Calls {@link Module#onPostEnable()} on all modules.
     */
    POST_ENABLE,

    /**
     * Loaded phase
     * <p />
     * Signifies that the module loader has finished loading all modules from
     * the specified directories
     */
    LOADED;


    public boolean after(LoadState state) {
        return this.ordinal() > state.ordinal();
    }
}
