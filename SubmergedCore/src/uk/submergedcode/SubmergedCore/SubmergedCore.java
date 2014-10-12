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
package uk.submergedcode.SubmergedCore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.Validate;
import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import uk.submergedcode.SubmergedCore.bungee.BungeeMessenger;
import uk.submergedcode.SubmergedCore.bungee.SimpleBungeeMessenger;
import uk.submergedcode.SubmergedCore.message.ClickEventType;
import uk.submergedcode.SubmergedCore.message.HoverEventType;
import uk.submergedcode.SubmergedCore.message.Message;
import uk.submergedcode.SubmergedCore.module.Module;
import uk.submergedcode.SubmergedCore.module.loader.BukkitModuleLoader;
import uk.submergedcode.SubmergedCore.module.loader.ModuleLoader;
import uk.submergedcode.SubmergedCore.module.loader.exception.LoadException;
import uk.submergedcode.SubmergedCore.player.FundamentalPlayer;
import uk.submergedcode.SubmergedCore.player.FundamentalPlayerArray;
import uk.submergedcode.SubmergedCore.player.PlayerData;
import uk.submergedcode.SubmergedCore.serialization.AchievementSerializer;
import uk.submergedcode.SubmergedCore.serialization.ItemStackSerializer;
import uk.thecodingbadgers.bDatabaseManager.Database.BukkitDatabase;
import uk.thecodingbadgers.bDatabaseManager.bDatabaseManager;
import uk.thecodingbadgers.bDatabaseManager.bDatabaseManager.DatabaseType;

public class SubmergedCore extends JavaPlugin implements Listener {

    protected static Gson m_gson = null;
    protected static Logger m_log = null;
    protected static SubmergedCore m_instance = null;

    protected static Permission m_permissions = null;
    protected static Chat m_chat = null;
    protected static Economy m_economy = null;

    protected static BukkitDatabase m_database = null;
    protected static ModuleLoader m_moduleLoader = null;
    protected static ConfigManager m_configuration = null;
    protected static BungeeMessenger m_messenger = null;

    public static FundamentalPlayerArray Players = new FundamentalPlayerArray();

    /**
     * Called on loading. This is called before onEnable.
     * Store the instance here, to do it as early as possible.
     */
    @Override
    public void onLoad() {
        setInstance(this);
        setupGson();

        m_log = getLogger();
        log(Level.INFO, "SubmergedCore Loading");
    }

    /**
     * Called when the plugin is being enabled
     * Load the configuration and all modules
     * Register the command listener
     */
    @Override
    public void onEnable() {

        // load the configuration into the configuration manager
        try {
            setConfigManager(new BukkitConfigurationManager());
            m_configuration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bukkit.getMessenger().registerOutgoingPluginChannel(SubmergedCore.getInstance(), "BungeeCord");
        setBungeeMessenger(new SimpleBungeeMessenger());

        // load the modules in
        try {
            m_moduleLoader = new BukkitModuleLoader();

            //m_moduleLoader.addModuleDirectory(new File(getDataFolder().getParent(), "SubmergedCore-Skills"));
            m_moduleLoader.addModuleDirectory(new File(getDataFolder().getParent(), "SubmergedCore-Modules"));

            m_moduleLoader.load();
        } catch (LoadException ex) {
            m_log.log(Level.WARNING, "Unhandled exception whilst loading modules", ex);
        }

        /*// check if any of the modules need updating
        if (m_configuration.isAutoUpdateEnabled()) {
            m_moduleLoader.update();
        }*/

        // Register this as a listener
        this.getServer().getPluginManager().registerEvents(this, this);

        getCommand("SubmergedCore").setExecutor(new CommandHandler());

        SubmergedCore.log(Level.INFO, "SubmergedCore Loaded.");
    }

    /**
     * Called when the plugin is being disabled
     * Here we disable the module and thus all modules
     */
    @Override
    public void onDisable() {
        SubmergedCore.log(Level.INFO, "SubmergedCore Disabled.");
        m_moduleLoader.unload();
        m_database.freeDatabase();

        // Clear instances
        m_instance = null;
        m_configuration = null;
        m_messenger = null;
        m_gson = null;
    }

    public static void setupGson() {

        if (m_gson != null) {
            throw new RuntimeException("Gson already setup, cannot resetup instance");
        }

        m_gson = new GsonBuilder()
                .registerTypeAdapter(Message.class, new Message.MessageSerializer())
                .registerTypeAdapter(ClickEventType.class, new ClickEventType.ClickEventSerializer())
                .registerTypeAdapter(HoverEventType.class, new HoverEventType.HoverEventSerializer())
                .registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
                .registerTypeAdapter(Achievement.class, new AchievementSerializer())
                .create();
    }

    public static void setInstance(SubmergedCore plugin) {
        if (m_instance != null) {
            throw new RuntimeException("Plugin instance already set, cannot redeclare");
        }
        m_instance = plugin;
    }

    public static void setConfigManager(ConfigManager manager) {
        if (m_configuration != null) {
            throw new RuntimeException("Configuration manager already set, cannot redeclare");
        }
        m_configuration = manager;
    }

    public static void setBungeeMessenger(BungeeMessenger manager) {
        if (m_messenger != null) {
            throw new RuntimeException("Bungee messenger already set, cannot redeclare");
        }
        m_messenger = manager;
    }

    /**
     * Get the SubmergedCore plugin instance.
     *
     * @return the plugin instance
     */
    public static SubmergedCore getInstance() {
        return m_instance;
    }

    /**
     * Get the configuration manager
     *
     * @return the configuration manager for SubmergedCore
     */
    public static ConfigManager getConfigurationManager() {
        return m_configuration;
    }

    /**
     * Get the Bungee messenger
     *
     * @return the bungee messeneger instance
     */
    public static BungeeMessenger getBungeeMessenger() {
        return m_messenger;
    }

    /**
     * Get the SubmergedCore gson instance, has custom serializers for bukkit
     * and minecraft classes
     *
     * @return the gson instance
     */
    public static Gson getGsonInstance() {
        return m_gson;
    }

    /**
     * Access to the bukkit database
     *
     * @return the bukkit database for SubmergedCore
     */
    public static BukkitDatabase getBukkitDatabase() {
        if (m_database == null) {
            DatabaseSettings settings = m_configuration.getDatabaseSettings();
            m_database = bDatabaseManager.createDatabase(settings.name, m_instance, settings.type);
            if (settings.type == DatabaseType.SQL) {
                m_database.login(settings.host, settings.user, settings.password, settings.port);
            }
        }
        return m_database;
    }

    /**
     * Static access to vaults permission manager
     *
     * @return the vault permission manager
     * @see Permission
     */
    public static Permission getPermissions() {
        if (m_permissions == null) {
            RegisteredServiceProvider<Permission> permissionProvider = m_instance.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
            if (permissionProvider != null) {
                m_permissions = permissionProvider.getProvider();
            }
        }
        return m_permissions;
    }

    /**
     * Static access to vaults chat manager
     *
     * @return the vault chat manager
     * @see Chat
     */
    public static Chat getChat() {
        if (m_chat == null) {
            RegisteredServiceProvider<Chat> chatProvider = m_instance.getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
            if (chatProvider != null) {
                m_chat = chatProvider.getProvider();
            }
        }
        return m_chat;
    }

    /**
     * Static access to vaults economy manager
     *
     * @return the vault economy manager
     * @see Economy
     */
    public static Economy getEconomy() {
        if (m_economy == null) {
            RegisteredServiceProvider<Economy> economyProvider = m_instance.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) {
                m_economy = economyProvider.getProvider();
            }
        }
        return m_economy;
    }

    /**
     * Gets the module loader
     *
     * @return the module loader for all SubmergedCore modules
     */
    public static ModuleLoader getModuleLoader() {
        return m_moduleLoader;
    }

    /**
     * Handle commands in the modules or plugin.
     *
     * @return True if the command was handled, False otherwise
     */
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (label.equalsIgnoreCase("modules")) {
            handleModulesCommand(sender);
            return true;
        }

        return false;
    }

    /**
     * Disable a specific module
     *
     * @param module the module to disable
     */
    public void disableModule(Module module) {
        Validate.notNull(module, "Moudule cannot be null");

        m_moduleLoader.unload(module);
    }

    /**
     * Reloads a specific module
     *
     * @param module the module to reload
     */
    public void reloadModule(Module module) {
        Validate.notNull(module, "Moudule cannot be null");

        m_moduleLoader.unload(module);
        m_moduleLoader.load(module.getFile());
        m_moduleLoader.getModule(module.getName()).onEnable();
    }

    /**
     * Static access to log as SubmergedCore
     *
     * @param level the log level
     * @param msg   the message to log
     */
    public static void log(Level level, String msg) {
        Validate.notNull(level, "Log level cannot be null");
        Validate.notNull(msg, "Message cannot be null");

        m_log.log(level, msg);
    }

    /**
     * Static access to log as SubmergedCore
     *
     * @param level the log level
     * @param msg   the message to log
     * @param e     the exception to log
     */
    public static void log(Level level, String msg, Throwable e) {
        Validate.notNull(level, "Log level cannot be null");
        Validate.notNull(msg, "Message cannot be null");
        Validate.notNull(e, "The exception to log cannot be null");

        m_log.log(level, msg, e);
    }

    private void handleModulesCommand(CommandSender sender) {
        List<Module> modules = m_moduleLoader.getModules();
        String moduleString = ChatColor.GREEN + "Modules(" + modules.size() + "): ";
        boolean first = true;

        for (Module module : modules) {
            moduleString += (first ? "" : ", ") + module.getName();
            first = false;
        }

        sender.sendMessage(moduleString);
    }

    /**
     * Handle a player join event
     *
     * @param event The player join event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        FundamentalPlayer newPlayer = new FundamentalPlayer(event.getPlayer());
        SubmergedCore.Players.add(newPlayer);

        List<Module> modules = m_moduleLoader.getModules();
        for (Module module : modules) {
            Class<? extends PlayerData> playerDataClass = module.getPlayerDataClass();
            if (playerDataClass != null) {
                try {
                    PlayerData data = (PlayerData) playerDataClass.newInstance();
                    newPlayer.addPlayerData(data.getGroup(), data.getName(), data);
                } catch (Exception ex) {
                    SubmergedCore.log(Level.WARNING, "Failed to create new player data for '" + event.getPlayer().getName() + "' for module '" + module.getName() + "'", ex);
                }
            }
        }
    }

    /**
     * Handle a player join event
     *
     * @param event The player join event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        SubmergedCore.Players.removePlayer(event.getPlayer());
    }

    /**
     * Get a list of online players for a given rank or group
     *
     * @param rank The rank to get the list of online players from
     * @return An array list of online players within a given rank or group
     */
    public ArrayList<Player> getPlayersOfRank(String rank) {

        PermissionManager pexmanager = null;

        try {
            pexmanager = PermissionsEx.getPermissionManager();
        } catch (Exception ex) {
            // If pex does not exist on the server, just return now, we don't want errors
            return null;
        }

        PermissionGroup group = pexmanager.getGroup(rank);

        // If the group doesn't exist just leave.
        if (group == null) {
            return null;
        }

        //
        ArrayList<Player> players = new ArrayList<Player>();
        for (PermissionUser user : group.getUsers()) {
            Player player = Bukkit.getPlayer(user.getName());
            if (player != null) {
                players.add(player);
            }
        }

        return players;
    }

    /**
     * @param time
     * @return
     */
    public static String formatTime(Long time) {

        Long days = TimeUnit.MILLISECONDS.toDays(time);
        time = time - TimeUnit.DAYS.toMillis(days);

        Long hours = TimeUnit.MILLISECONDS.toHours(time);
        time = time - TimeUnit.HOURS.toMillis(hours);

        Long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
        time = time - TimeUnit.MINUTES.toMillis(minutes);

        Long seconds = TimeUnit.MILLISECONDS.toSeconds(time);

        String formattedTime = "";
        if (days != 0) {
            formattedTime += (days == 1 ? "1 Day" : days + " Days");
        }

        if (hours != 0) {
            if (days != 0) {
                formattedTime += ", ";
            }
            formattedTime += (hours == 1 ? "1 Hour" : hours + " Hours");
        }

        if (minutes != 0) {
            if (days != 0 || hours != 0) {
                formattedTime += ", ";
            }
            formattedTime += (minutes == 1 ? "1 Minute" : minutes + " Minutes");
        }

        if (seconds != 0) {
            if (days != 0 || hours != 0 || minutes != 0) {
                formattedTime += ", ";
            }
            formattedTime += (seconds == 1 ? "1 Second" : seconds + " Seconds");
        }

        return formattedTime;
    }

    /**
     * @param minutes
     * @param seconds
     * @return
     */
    public static Long timeToTicks(int minutes, int seconds) {
        return seconds * 20L + (minutes * (20L * 60L));
    }

    /**
     * Find the player a specific instance of player data belongs to
     *
     * @param data THe data to test
     * @return The player the data belongs to or null
     */
    public static FundamentalPlayer getDataOwner(PlayerData data) {

        for (FundamentalPlayer player : SubmergedCore.Players) {
            if (player.isDataOwner(data)) {
                return player;
            }
        }
        return null;
    }

    /**
     * Get a module of a given class type
     *
     * @param moduleClass The class of the module to find
     * @return The module if found, or null
     */
    public Module getModuleInstance(Class moduleClass) {
        for (Module module : m_moduleLoader.getModules()) {
            if (moduleClass.isInstance(module)) {
                return module;
            }
        }
        return null;
    }

    /**
     * Get all modules of a given class type
     *
     * @param moduleClass The class of the module to find
     * @return The modules if found, or null
     */
    public <T extends PlayerData> List<T> getModuleInstances(Class<? extends T> moduleClass) {
        List<T> modules = new ArrayList<T>();
        for (Module module : m_moduleLoader.getModules()) {
            if (moduleClass.isInstance(module)) {
                modules.add((T) module);
            }
        }
        return modules;
    }

}
