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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import uk.submergedcode.SubmergedCore.SubmergedCore;
import uk.submergedcode.SubmergedCore.module.Module;
import uk.submergedcode.SubmergedCore.module.ModuleHelpTopic;
import uk.submergedcode.SubmergedCore.module.ModuleInfo;
import uk.submergedcode.SubmergedCore.module.loader.exception.LoadException;
import uk.submergedcode.SubmergedCore.player.FundamentalPlayer;
import uk.submergedcode.SubmergedCore.player.PlayerData;

public class BukkitModuleLoader implements ModuleLoader {

    private LoadState state = LoadState.PRE_SETUP;
    private Logger logger = SubmergedCore.getInstance().getLogger();

    private ModuleClassLoader classLoader;

    private DirectoryList directories = new DirectoryList();
    private Map<String, Module> modules = Maps.newLinkedHashMap();

    public BukkitModuleLoader() {}

    @Override
    public void addModuleDirectory(File file) { // TODO try and make it so modules can add directories at loadtime
        Preconditions.checkNotNull(file);
        Preconditions.checkState(state == LoadState.PRE_SETUP, "Cannot add module directory after modules have started to be loaded.");

        this.directories.add(file);

        if (file.exists() && !file.isDirectory()) {
            file.delete();
        }

        if (!file.exists()) {
            file.mkdir();
        }
    }

    @Override
    public List<File> getModuleDirs() {
        return ImmutableList.copyOf(directories);
    }

    @Override
    public LoadState getLoadState() {
        return state;
    }

    @Override
    public void load() throws LoadException {
        Preconditions.checkState(state == LoadState.PRE_SETUP, "Cannot load modules without unloading first.");
        updateState(LoadState.SETUP);

        List<File> potentialFiles = Lists.newArrayList();
        this.directories.setReadonly(true);

        for (File dir : this.directories) { // Collect all module files together
            Collections.addAll(potentialFiles, dir.listFiles(new FileExtensionFilter(".jar")));
        }

        classLoader = new ModuleClassLoader(getClass().getClassLoader());
        List<ModuleInfo> moduleDescs = Lists.newArrayList();
        JarFile jar = null;

        for (File file : potentialFiles) {
            try {
                classLoader.addURL(file.toURI().toURL()); // Add to current classpath
                jar = new JarFile(file);

                JarEntry yml = jar.getJarEntry("path.yml"); // Get the description file for the module

                if (yml == null) {
                    getLogger().log(Level.WARNING, "Description file is missing for file {0}", file.getName());
                    continue;
                }

                LoadableDescriptionFile desc = new LoadableDescriptionFile(jar.getInputStream(yml));
                getLogger().log(Level.INFO, "Found module {0}.", desc.getName());
                moduleDescs.add(new ModuleInfo(file, jar, desc)); // Create the module info file
            } catch (IOException e) {
                throw new LoadException(e);
            } finally {
                if (jar != null) {
                    try {
                        jar.close();
                    } catch (IOException e) {
                        throw new LoadException(e);
                    }
                }
            }
        }

        //Collections.sort(moduleDescs); // Sort the modules into dependency order
        getLogger().log(Level.INFO, "Found {0} modules.", moduleDescs.size());
        
        List<ModuleInfo> sortedModuleDescs = Lists.newArrayList();               
        for (Iterator<ModuleInfo> itr = moduleDescs.iterator(); itr.hasNext();) {

            ModuleInfo info = itr.next();
            Collection<String> depends = info.getDescription().getDependencies();
            if (depends.isEmpty()) {
                sortedModuleDescs.add(0, info);
                continue;
            }
            
            for (String depend : depends) {
                
                int dependIndex = 1;
                boolean addedDepend = false;
                
                for (ModuleInfo sortedInfo : sortedModuleDescs) {
                    if (sortedInfo.getName().equalsIgnoreCase(depend)) {
                        sortedModuleDescs.add(dependIndex, info);
                        addedDepend = true;
                        break;
                    }
                    dependIndex++;
                }
                
                if (!addedDepend) {
                    sortedModuleDescs.add(sortedModuleDescs.size(), info);
                }
                
            }
        }
        
        updateState(LoadState.LOAD);

        ModuleInfo info = null;

        for (Iterator<ModuleInfo> itr = sortedModuleDescs.iterator(); itr.hasNext();) {
            try {
                info = itr.next();
                boolean dependencies = true;

                for (String depend : info.getDescription().getDependencies()) {
                    if (!this.modules.containsKey(depend)) {
                        getLogger().log(Level.SEVERE, "{0} is missing dependency {1}", new Object[] { info.getName(), depend });
                        dependencies = false;
                    }
                }

                if (!dependencies) {
                    getLogger().log(Level.SEVERE, "{0} is missing dependencies and therefor cannot be loaded", new Object[] { info.getName() });
                    continue;
                }

                getLogger().log(Level.INFO, "Loading module {0} (main: {1}).", new Object[] { info.getDescription().getName(), info.getDescription().getMainClass() });
                Class<?> clazz = classLoader.loadClass(info.getDescription().getMainClass()); // Load the main class of the module

                Class<? extends Module> mainclass = clazz.asSubclass(Module.class);
                Constructor<? extends Module> ctor = mainclass.getConstructor();

                // Create a new instance of the module and load it
                Module module = ctor.newInstance();
                module.setInfo(info);
                module.init();
                this.modules.put(module.getName(), module);

                Bukkit.getHelpMap().addTopic(new ModuleHelpTopic(module)); // create a help entry for this module
            } catch (ClassNotFoundException e) {
                getLogger().log(Level.SEVERE, "Cannot find class {} for module {}, is the path.yml valid?", new Object[]{e.getMessage(), info.getName()});
            } catch (Throwable  e) {
                getLogger().log(Level.SEVERE, "A error has occurred whilst trying to load module {}.", info.getName());
                getLogger().log(Level.SEVERE, "Exception: ", e);
            }
        }

        getLogger().log(Level.INFO, "Loaded {0} modules.", this.modules.size());

        updateState(LoadState.ENABLE);
        call(enable());
        
        for (Module module : this.getModules()) {
            Class<? extends PlayerData> playerDataClass = module.getPlayerDataClass();
            if (playerDataClass != null) {
                try {                    
                    for (FundamentalPlayer player : SubmergedCore.Players) {
                        PlayerData data = (PlayerData) playerDataClass.newInstance();
                        player.addPlayerData(data.getGroup(), data.getName(), data);
                    }    
                } catch (IllegalAccessException ex) {
                    getLogger().log(Level.INFO, "Failed to add player data " + playerDataClass.getName(), ex);
                } catch (InstantiationException ex) {
                    getLogger().log(Level.INFO, "Failed to add player data " + playerDataClass.getName(), ex);
                }
            }
        }        
        
        updateState(LoadState.POST_ENABLE);
        call(postEnable());
        updateState(LoadState.LOADED);
    }

    @Override
    public void load(File module) { // TODO do
    }

    @Override
    public void unload() {
        Preconditions.checkState(state == LoadState.LOADED, "Cannot unload modules before they are loaded");

        for (FundamentalPlayer player : SubmergedCore.Players) {
            player.destroyPlayerData();
        }
        
        call(disable());

        this.modules.clear();
        this.directories.setReadonly(false);
        updateState(LoadState.PRE_SETUP);
    }

    @Override
    public void unload(Module module) {
        Preconditions.checkNotNull(module);
        Preconditions.checkState(this.modules.containsValue(module), "Module is not loaded and therefor cannot be unloaded");
        Preconditions.checkState(state == LoadState.LOADED, "Cannot unload modules before they are loaded.");

        disable().apply(module);
        this.modules.remove(module.getName());
    }

    @Override
    public Module getModule(String name) {
        Preconditions.checkState(state.after(LoadState.LOAD), "Cannot lookup module before they are loaded.");
        return modules.get(name);
    }

    @Override
    public List<Module> getModules() {
        Preconditions.checkState(state.after(LoadState.LOAD), "Cannot lookup module before they are loaded.");
        return ImmutableList.copyOf(this.modules.values());
    }

    private void updateState(LoadState state) {
        Preconditions.checkNotNull(state);

        this.state = state;
    }

    private Logger getLogger() {
        return logger;
    }

    /** Load Calls */

    private void call(Function<Module, Void> function) {
        Preconditions.checkNotNull(function);

        for (Module module : modules.values()) {
            function.apply(module);
        }
    }

    private Function<Module, Void> postEnable() {
        return new Function<Module, Void>() {
            @Override
            public Void apply(@Nullable Module module) {
                try {
                    if (module != null) {
                        module.onPostEnable();
                    }
                } catch (Exception ex) {
                    getLogger().log(Level.SEVERE, "A unexpected error has occurred whilst trying to enable module {}", module.getName());
                    getLogger().log(Level.SEVERE, "Exception:", ex);
                }
                return null;
            }
        };
    }

    private Function<Module, Void> enable() {
        return new Function<Module, Void>() {
            @Override
            public Void apply(@Nullable Module module) {
                try {
                    if (module != null) {
                        module.setEnabled(true);
                        getLogger().log(Level.INFO, "{0} version {1} is enabled.", new Object[]{module.getDescription().getName(), module.getDescription().getVersion()});
                    }
                } catch (Exception ex) {
                    getLogger().log(Level.SEVERE, "A unexpected error has occurred whilst trying to enable module {}", module.getName());
                    getLogger().log(Level.SEVERE, "Exception:", ex);
                }
                return null;
            }
        };
    }

    private Function<Module, Void> disable() {
        return new Function<Module, Void>() {
            @Override
            public Void apply(@Nullable Module module) {
                try {
                    if (module != null) {
                        module.setEnabled(false);
                    }

                    for (Listener listener : module.getListeners()) {
                        HandlerList.unregisterAll(listener);
                    }
                } catch (Exception ex) {
                    getLogger().log(Level.SEVERE, "A unexpected error has occurred whilst trying to enable module {}", module.getName());
                    getLogger().log(Level.SEVERE, "Exception:", ex);
                }
                return null;
            }
        };
    }

}
