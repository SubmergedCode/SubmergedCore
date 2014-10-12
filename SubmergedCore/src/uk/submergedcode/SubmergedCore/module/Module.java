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
package uk.submergedcode.SubmergedCore.module;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import uk.submergedcode.SubmergedCore.SubmergedCore;
import uk.submergedcode.SubmergedCore.commands.ModuleCommand;
import uk.submergedcode.SubmergedCore.commands.ModuleCommandHandler;
import uk.submergedcode.SubmergedCore.config.ConfigFactory;
import uk.submergedcode.SubmergedCore.config.ConfigFile;
import uk.submergedcode.SubmergedCore.module.loader.LoadState;
import uk.submergedcode.SubmergedCore.module.loader.Loadable;
import uk.submergedcode.SubmergedCore.player.PlayerData;
import uk.submergedcode.SubmergedCore.update.UpdateThread;
import uk.submergedcode.SubmergedCore.update.Updater;
import uk.thecodingbadgers.bDatabaseManager.Database.BukkitDatabase;

/**
 * The base Module class any module should extend this, it also provides
 * helper methods for the module.
 */
public abstract class Module extends Loadable implements Listener {

    protected static BukkitDatabase m_database = null;
    private static Permission m_permissions = null;

    protected final SubmergedCore m_plugin;
    protected File m_configFile = null;
    protected FileConfiguration m_config;

    private boolean m_debug = false;
    private boolean loadedLanguageFile;
    private boolean m_enabled;

    private List<Class<? extends ConfigFile>> m_configFiles;
    private List<Listener> m_listeners = new ArrayList<Listener>();
    private ModuleLogger m_log;
    private Map<String, String> m_languageMap = new HashMap<String, String>();
    private UpdateThread m_updater;

    /**
     * Instantiates a new module with default settings.
     */
    public Module() {
        super();
        m_plugin = SubmergedCore.getInstance();
        m_database = SubmergedCore.getBukkitDatabase();
        m_debug = SubmergedCore.getConfigurationManager().isDebugEnabled();
        m_permissions = SubmergedCore.getPermissions();
    }

    public JavaPlugin getPlugin() {
        return m_plugin; 
    }
    
    public final void init() {
        Preconditions.checkState(m_log == null, "Modules already initialized, cannot reinitialize.");
        m_log = new ModuleLogger(this);

        this.onLoad();
    }

    protected void setUpdater(Updater updater) {
        m_updater = new UpdateThread(updater);
        log(Level.INFO, "Set new updater to " + m_updater.getUpdater().getUpdater());
    }

    public void update() {
        if (m_updater == null) {
            log(Level.INFO, "Updater is null, cannot check for updates");
            return;
        }

        m_updater.start();
    }

    /**
     * @return
     */
    public Class<? extends PlayerData> getPlayerDataClass() {
        return null;
    }

    /**
     * Load language file.
     */
    protected void loadLanguageFile() {
        File languageFile = new File(getDataFolder() + File.separator + getName() + "_" + SubmergedCore.getConfigurationManager().getLanguage() + ".lang");

        if (!languageFile.exists()) {
            log(Level.SEVERE, "Missing language file '" + languageFile.getAbsolutePath() + "'!");

            boolean foundLangFile = false;
            InputStream stream = null;
            FileOutputStream fstream = null;

            try {
                stream = getClass().getResourceAsStream("/" + languageFile.getName());

                // if default file exists in jar, copy it out to the right
                // directory
                if (stream != null) {
                    fstream = new FileOutputStream(languageFile);

                    foundLangFile = true;
                    IOUtils.copy(stream, fstream);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }

                    if (fstream != null) {
                        fstream.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            if (foundLangFile) {
                log(Level.INFO, "Copied default language file from jar file");
            } else {
                return;
            }
        }

        log(Level.INFO, "Loading Language File: " + languageFile.getName());

        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;

        try {
            fstream = new FileInputStream(languageFile);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in));

            String line = null;
            String key = null;
            while ((line = br.readLine()) != null) {

                if (line.isEmpty() || line.startsWith("//"))
                    continue;

                if (line.startsWith("#")) {
                    key = line.substring(1);
                    continue;
                }

                if (key == null) {
                    log(Level.WARNING, "Trying to parse a language value, with no key set!");
                    continue;
                }

                m_languageMap.put(key.toLowerCase(), line);
            }

            loadedLanguageFile = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fstream != null) {
                    fstream.close();
                }
                if (in != null) {
                    in.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Log a message console via this modules logger.
     *
     * @param level  the Log level
     * @param string the message
     */
    public void log(Level level, String string) {
        m_log.log(Level.INFO, string);
    }

    /**
     * Get the logger associated with this module
     *
     * @return this modules logger
     */
    public Logger getLogger() {
        return m_log;
    }

    /**
     * Register a bukkit event listener.
     *
     * @param listener the bukkit event listener
     */
    public final void register(Listener listener) {
        m_plugin.getServer().getPluginManager().registerEvents(listener, m_plugin);
        m_listeners.add(listener);
    }

    /**
     * Gets the vault permissions instance.
     *
     * @return the vault permissions instance
     */
    public Permission getPermissions() {
        return m_permissions;
    }

    /**
     * The enable method for this module, called on enabling the module via
     * {@link #setEnabled(boolean)} in the {@link LoadState.ENABLE} phase
     * this is used to register commands, events and any other things that
     * should be registered on enabling the module.
     */
    public abstract void onEnable();

    /**
     * The disable method for this module, called on disabling the module via
     * {@link #setEnabled(boolean)}this is used to clean up after the module
     * when it is disabled.
     */
    public abstract void onDisable();


    /**
     * The post enable method for this module, called after all modules have
     * been enabled via the @link ModuleLoader}, this can be used to hook into
     * other modules that have to be loaded before your own and add custom
     * behaviour that does not warrant a dependency.
     */
    public void onPostEnable() {
    }

    /**
     * The load method for this module, called on loading the module via the
     * {@link ModuleLoader} in the {@link LoadState.LOAD} phase this is called
     * before any module in that load batch is enabled.
     */
    public void onLoad() {
    }

    /**
     * Sets the module enabled status, will call {@link #onEnable()} if the
     * module isn't already enabled and you want to enable it and will call
     * {@link #onDisable()} if the module isn't already disabled and you want
     * to disable it.
     *
     * @param enabled if you want to enable or disable the module
     */
    public void setEnabled(boolean enabled) {
        if (enabled) {
            if (m_enabled) {
                return;
            }

            onEnable();
            m_enabled = true;
        } else {
            if (!m_enabled) {
                return;
            }

            onDisable();
            ModuleCommandHandler.deregisterCommand(this);
            m_enabled = false;
        }
    }

    /**
     * Returns the current state of the module, if it is enabled or disabled.
     *
     * @return if the module is enabled
     */
    public boolean isEnabled() {
        return m_enabled;
    }

    /**
     * The command handing method for this module, this is only called if the
     * command handing for that {@link ModuleCommand} returns false,
     * preferably the
     * {@link ModuleCommand#onCommand(CommandSender, String, String[])} should
     * be used, this is just left for backwards comparability.
     *
     * @param sender the command sender
     * @param label  the command label used
     * @param args   the arguments for the command
     * @return true, if the command has been handled, false if it hasn't
     */
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        return false;
    }

    /**
     * Checks if a player has a specific permission.
     *
     * @param player the player to check
     * @param node   the permission node
     * @return true, if the player has the permission
     */
    public static boolean hasPermission(final Player player, final String node) {
        if (m_permissions.has(player, node)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a sender has a specific permission.
     *
     * @param sender the sender to check
     * @param node   the permission node
     * @return true, if the player has the permission
     */
    public static boolean hasPermission(final CommandSender sender, final String node) {
        if (m_permissions.has(sender, node)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a sender has a specific permission.
     *
     * @param sender the sender to check
     * @param node   the permission node
     * @return true, if the player has the permission
     */
    public static boolean hasPermission(final String sender, final String node) {
        if (m_permissions.has((String) null, sender, node)) {
            return true;
        }
        return false;
    }

    /**
     * Send message to a player formated in the default style.
     *
     * @param name    the name of the module
     * @param player  the player to send to
     * @param message the message
     */
    public static void sendMessage(String name, CommandSender player, String message) {
        player.sendMessage(ChatColor.DARK_PURPLE + "[" + name + "] " + ChatColor.RESET + message);
    }

    /**
     * Register a command to this module.
     *
     * @param command the command
     */
    protected void registerCommand(ModuleCommand command) {
        ModuleCommandHandler.registerCommand(this, command);
    }

    /**
     * Register a command to this module.
     *
     * @param clazz the command
     */
    protected void registerCommands(Class<?> clazz) {
        ModuleCommandHandler.findCommands(this, clazz);
    }

    /**
     * Get all commands registered to this module
     *
     * @return the commands
     * @Deprecated {@link ModuleCommandHandler#getCommands(Module)}
     */
    public List<ModuleCommand> getCommands() {
        return ModuleCommandHandler.getCommands(this);
    }

    /**
     * Gets the language value for the current loaded language, case
     * insensitive, all keys are forced to be in lower case.
     *
     * @param key the language key
     * @return the language value, if available, the key with hyphens removed
     * and in lower case otherwise
     */
    public String getLanguageValue(String key) {
        Validate.notNull(key, "Language key cannot be null");

        if (!loadedLanguageFile) {
            log(Level.SEVERE, "Cannot get language value before loading language file");
        }

        String value = m_languageMap.get(key.toLowerCase());

        if (value == null) {
            value = key.toLowerCase().replace("-", " ");
        }

        return value;
    }

    /**
     * Get all the listeners registered to this module, for cleaning up on
     * disable
     *
     * @return a list of all listeners
     */
    public List<Listener> getListeners() {
        return m_listeners;
    }

    /**
     * Is debug mode enabled on this module
     *
     * @return if debug is enabled
     */
    public boolean isDebug() {
        return m_debug;
    }

    /**
     * Set the debug mode for this module
     *
     * @param debug whether debug is on or not
     */
    public void setDebug(boolean debug) {
        m_debug = debug;
    }

    /**
     * Output a message to console if debug mode is on
     *
     * @param message the message to output
     */
    public void debugConsole(String message) {
        if (!m_debug) {
            return;
        }

        log(Level.INFO, "[Debug] " + message);
    }

    /**
     * Registers a config class as a config and loads it, class must extend
     * {@link ConfigFile} and each element that is going to be included in the
     * file should be {@code static} and have a {@link Element} annotation
     * associated with it.
     *
     * @param clazz the config class
     */
    public void registerConfig(Class<? extends ConfigFile> clazz) {
        if (m_configFiles == null) {
            m_configFiles = new ArrayList<Class<? extends ConfigFile>>();
        }

        log(Level.INFO, "Load config file for " + clazz.getName());

        try {
            ConfigFactory.load(clazz, getDataFolder());
        } catch (Exception e) {
            e.printStackTrace();
        }
        m_configFiles.add(clazz);
    }

    /**
     * Get a list of players whose name matches a given string
     *
     * @param match      The name to match
     * @param onlineOnly Only return players who are currently online
     * @return A list of offline players whose names match the entry string
     */
    public List<OfflinePlayer> matchPlayer(String match, boolean onlineOnly) {

        Server server = m_plugin.getServer();
        List<OfflinePlayer> matches = new ArrayList<OfflinePlayer>();

        OfflinePlayer[] offlinePlayers = server.getOfflinePlayers();
        for (OfflinePlayer player : offlinePlayers) {

            if (onlineOnly && !player.isOnline()) {
                continue;
            }

            final String playerName = player.getName();

            // exact name, just return this
            if (playerName.equalsIgnoreCase(match)) {
                matches.clear();
                matches.add(player);
                return matches;
            }

            // match is contained within this player add them to the list
            if (playerName.toLowerCase().startsWith(match.toLowerCase())) {
                matches.add(player);
            }
        }

        return matches;
    }

}
