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
import uk.submergedcode.SubmergedCore.module.loader.exception.LoadException;

import java.io.File;
import java.util.List;

/**
 * A module loader takes a list of directories and loads Modules to execute
 * custom code into the server.
 */
public interface ModuleLoader {

    /**
     * Add a directory for modules to be loaded from.
     * <p />
     * <b>Note - this is not to be used by modules as it will have no effect
     * after the modules have started to be loaded. It is for internal use
     * only.</b>
     *
     * @param file
     * @throws IllegalStateException if called after modules have started to
     *          be loaded
     */
    public void addModuleDirectory(File file);

    /**
     * Gets all the directories that modules can be loaded from.
     *
     * @return a list of all the directories modules can be loaded from
     */
    public List<File> getModuleDirs();

    /**
     * Gets the current {@link LoadState} this module loader is in
     *
     * @return the current {@link LoadState} of the loader
     */
    public LoadState getLoadState();

    /**
     * Loads all modules from all directories specified in
     * {@link #getModuleDirs()}
     *
     * @throws LoadException if there is a severe unexpected error whilst
     *          loading the modules.
     */
    public void load() throws LoadException;

    /**
     * Loads a module from the file specified.
     *
     * @param file the jar file to load the module from
     */
    public void load(File file);

    /**
     * Unloads all modules safely from memory.
     */
    public void unload();

    /**
     * Unloads a specific module safely from memory.
     *
     * @param module the module to unload from memory
     */
    public void unload(Module module);

    /**
     * Get the specific module instance for a given module name.
     *
     * @param name the module name to lookup
     * @return the module instance requested
     */
    public Module getModule(String name);

    /**
     * Get a list of all currently loaded modules in this loader.
     *
     * @return a list of all currently loaded modules in this loader
     */
    public List<Module> getModules();

}
