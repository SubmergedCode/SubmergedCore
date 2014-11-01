/**
 * bFundamentalsBuild 1.2-SNAPSHOT
 * Copyright (C) 2013  CodingBadgers <plugins@mcbadgercraft.com>
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
package uk.submergedcode.plugincontrol.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import uk.submergedcode.SubmergedCore.SubmergedCore;
import uk.submergedcode.SubmergedCore.commands.ModuleCommand;
import uk.submergedcode.SubmergedCore.module.Module;

public class CommandPlugin extends ModuleCommand {

    /**
     * Plugin description cas, as short urls take time to generate.
     */
    List<String> m_pluginDetailsCache = new ArrayList<String>();
    
	/**
	 * Command constructor.
	 */
	public CommandPlugin() {
		super("plugin", "plugin reload <plugin> | plugin disable <plugin> | plugin enable <plugin> | plugin info <plugin> | plugin load <plugin>");
        cachePluginDetails();
    }
	
    /**
     * Called when the /plugin command is executed.
     * Handles both player and console command senders.
     * @param sender The sender of the command
     * @param cmd The command which was sent
     * @param label The label of the command
     * @param args The arguments of the command
     * @return 
     */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0) {
            outputAllPluginsOverview(sender);
            return true;
        }
        
		if (!Module.hasPermission(sender, this.getPermission())) {
			Module.sendMessage("Plugin", sender, "You do not have permission to access these commands.");
			return true;
		}
                        
		if (args.length != 2) {
			// invalid usage
			Module.sendMessage("Plugin", sender, "The following plugin command uses exist:");
			Module.sendMessage("Plugin", sender, " - plugin reload <plugin>");
			Module.sendMessage("Plugin", sender, " - plugin disable <plugin>");
			Module.sendMessage("Plugin", sender, " - plugin enable <plugin>");
			Module.sendMessage("Plugin", sender, " - plugin load <plugin path>");
			Module.sendMessage("Plugin", sender, " - plugin unload <plugin>");
			Module.sendMessage("Plugin", sender, " - plugin info <plugin>");
			return true;
		}
		
		final String command = args[0];
		final String pluginName = args[1];

		if (command.equalsIgnoreCase("load")) {
			return loadPlugin(sender, pluginName);
		}
		
		if (command.equalsIgnoreCase("unload")) {
			return unloadPlugin(sender, pluginName);
		}
				
		if (command.equalsIgnoreCase("reload")) {
			return reloadPlugin(sender, pluginName);		
		}
		
		if (command.equalsIgnoreCase("disable")) {
			return disablePlugin(sender, pluginName);	
		}
		
		if (command.equalsIgnoreCase("enable")) {
			return enablePlugin(sender, pluginName);
		}
		
		if (command.equalsIgnoreCase("info")) {
			return pluginInformation(sender, pluginName);
		}
				
		return true;
	}
	
    /**
     * Finds a plugin by name ignoring case.
     * @param pluginName
     * @return 
     */
    private Plugin getPlugin(String pluginName) {
        
        PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        for (Plugin plugin : pluginManager.getPlugins())
        {
            if (plugin.getName().toLowerCase().equals(pluginName.toLowerCase()))
            {
                return plugin;
            }
        }        
        return null;
    }

    /**
     * 
     * @param longUrl
     * @return 
     */
    private String shortenURL(String longUrl)
    {
        String googUrl = "https://www.googleapis.com/urlshortener/v1/url?shortUrl=http://goo.gl/fbsS&key=AIzaSyA1fQnseCFv6vWOefIcNe7XL8lVOV7YVbU";
        String shortUrl = "";

        try
        {
            URLConnection conn = new URL(googUrl).openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write("{\"longUrl\":\"" + longUrl + "\"}");
            wr.flush();

            // Get the response
            BufferedReader rd =
                         new BufferedReader(
                         new InputStreamReader(conn.getInputStream()));
            String line;

            while ((line = rd.readLine()) != null)
            {
                if (line.contains("id"))
                {
                    // I'm sure there's a more elegant way of parsing
                    // the JSON response, but this is quick/dirty =)
                    shortUrl = line.substring(8, line.length() - 2);
                    break;
                }
            }

            wr.close();
            rd.close();
        }
        catch (MalformedURLException ex)
        {
            SubmergedCore.log(Level.INFO, "MalformedURLException: " + longUrl, ex);
            return null;
        }
        catch (IOException ex)
        {
            SubmergedCore.log(Level.INFO, "IOException: " + longUrl, ex);
            return null; 
        }

        return shortUrl;
    }
    
    private void cachePluginDetails() {
        
        PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        Plugin[] plugins = pluginManager.getPlugins();
        
        for (Plugin plugin : plugins) {
            PluginDescriptionFile discription = plugin.getDescription();

            String url = null;
            String pluginWebsite = discription.getWebsite();
            if (pluginWebsite != null) {
                url = shortenURL(pluginWebsite);
            } 
            if (url == null) {
                url = shortenURL("http://lmgtfy.com/?q=Bukkit+Plugin+" + plugin.getName().replaceAll(" ", "+"));
            }

            m_pluginDetailsCache.add(" • " + ChatColor.GOLD + plugin.getName() + " " + ChatColor.DARK_GREEN + "version " + discription.getVersion() + ChatColor.BLUE + " " + url);            
        }

        Collections.sort(m_pluginDetailsCache, new Comparator<String>() {
            @Override
            public int compare(String t, String t1) {
                return t.compareToIgnoreCase(t1);
            }        
        });
    }
    
    /**
     * 
     * @param sender 
     */
    private void outputAllPluginsOverview(CommandSender sender) {
        
        Module.sendMessage("Plugin", sender, Bukkit.getServerName() + " runs the following plugins...");
       
        if (m_pluginDetailsCache.isEmpty()) {
            cachePluginDetails();
        }
        
        for (String message : m_pluginDetailsCache) {
            sender.sendMessage(message);
        }
    }
    
	/**
	 * Output information about a given plugin.
	 */
	private boolean pluginInformation(CommandSender player, String pluginName) {
        
		Plugin plugin = getPlugin(pluginName);
		if (plugin == null) {
			Module.sendMessage("Plugin", player, "A plugin with the name '" + pluginName + "' could not be found.");
			return true;
		}
		
		PluginDescriptionFile discription = plugin.getDescription();
		
		String authors = "";
		List<String> authorsList = discription.getAuthors();
		for (int authorIndex = 0; authorIndex < authorsList.size(); ++authorIndex) {
			String author = authorsList.get(authorIndex);
			authors += author + ", ";
		}
        
        if (authorsList.size() > 0) {
            authors = authors.substring(0, authors.length() - 2);
        } else {
            authors = "Unknown";
        }
		
		Module.sendMessage("Plugin", player, "||======================================||");
		Module.sendMessage("Plugin", player, "Name: " + plugin.getName());
		Module.sendMessage("Plugin", player, "Version: " + discription.getVersion());
		Module.sendMessage("Plugin", player, "Authors: " + authors);
		Module.sendMessage("Plugin", player, "Website: " + shortenURL(discription.getWebsite()));
		Module.sendMessage("Plugin", player, "Enabled: " + (plugin.isEnabled() ? "True" : "False"));
		Module.sendMessage("Plugin", player, "||======================================||");
		
		return true;
	}
    
	/**
	 * Disable a given plugin.
	 */
	private boolean disablePlugin(CommandSender player, String pluginName) {
		PluginManager pluginManager = Bukkit.getServer().getPluginManager();
		Plugin plugin = getPlugin(pluginName);
		if (plugin == null) {
			Module.sendMessage("Plugin", player, "A plugin with the name '" + pluginName + "' could not be found.");
			return true;
		}

		pluginManager.disablePlugin(plugin);	
		
		Module.sendMessage("Plugin", player, "The plugin '" + pluginName + "' was successfully disabled.");
        cachePluginDetails();
        
		return true;
	}
	
	/**
	 * Unload a given plugin.
	 */
	@SuppressWarnings("unchecked")
	private boolean unloadPlugin(CommandSender player, String pluginName) {
		
		PluginManager pluginManager = Bukkit.getServer().getPluginManager();
		Plugin plugin = getPlugin(pluginName);
		if (plugin == null) {
			Module.sendMessage("Plugin", player, "A plugin with the name '" + pluginName + "' could not be found.");
			return true;
		}

		SimplePluginManager simplePluginManager = (SimplePluginManager) pluginManager;
		try {
			Field pluginsField = simplePluginManager.getClass().getDeclaredField("plugins");
	        pluginsField.setAccessible(true);
	        List<Plugin> plugins = (List<Plugin>) pluginsField.get(simplePluginManager);
	        
	        Field lookupNamesField = simplePluginManager.getClass().getDeclaredField("lookupNames");
            lookupNamesField.setAccessible(true);
            Map<String, Plugin> lookupNames = (Map<String, Plugin>) lookupNamesField.get(simplePluginManager);
            
	        Field commandMapField = simplePluginManager.getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(simplePluginManager);

            Field knownCommandsField = null;
            Map<String, Command> knownCommands = null;

            if (commandMap != null) {
                knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
            }

            pluginManager.disablePlugin(plugin);

            if (plugins != null && plugins.contains(plugin)) {
                plugins.remove(plugin);
            }

            if (lookupNames != null && lookupNames.containsKey(pluginName)) {
                lookupNames.remove(pluginName);
            }

            if (commandMap != null) {
                for (Iterator<Map.Entry<String, Command>> it = knownCommands.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<String, Command> entry = it.next();

                    if (entry.getValue() instanceof PluginCommand) {
                        PluginCommand command = (PluginCommand) entry.getValue();

                        if (command.getPlugin() == plugin) {
                            command.unregister(commandMap);
                            it.remove();
                        }
                    }
                }
            }
		} catch (Exception ex) {
			Module.sendMessage("Plugin", player, "Failed to query plugin manager, could not unload plugin.");
			return true;
		}
        
		Module.sendMessage("Plugin", player, "The plugin '" + pluginName + "' was successfully unloaded.");
		cachePluginDetails();
        
		return true;
	}
	
	/**
	 * Enable a given plugin.
	 */
	private boolean enablePlugin(CommandSender player, String pluginName) {
		PluginManager pluginManager = Bukkit.getServer().getPluginManager();
		Plugin plugin = getPlugin(pluginName);
		if (plugin == null) {
			Module.sendMessage("Plugin", player, "A plugin with the name '" + pluginName + "' could not be found.");
			return true;
		}
		
		pluginManager.enablePlugin(plugin);	
		
		Module.sendMessage("Plugin", player, "The plugin '" + pluginName + "' was successfully enabled.");
        cachePluginDetails();
        
		return true;
	}
	
	/**
	 * Reload a given plugin.
	 */
	private boolean reloadPlugin(CommandSender player, String pluginName) {
		PluginManager pluginManager = Bukkit.getServer().getPluginManager();
		Plugin plugin = getPlugin(pluginName);
		if (plugin == null) {
			Module.sendMessage("Plugin", player, "A plugin with the name '" + pluginName + "' could not be found.");
			return true;
		}
		
		pluginManager.disablePlugin(plugin);
		pluginManager.enablePlugin(plugin);	
		
		Module.sendMessage("Plugin", player, "The plugin '" + pluginName + "' was successfully reloaded.");
        cachePluginDetails();
        
		return true;
	}

	/**
	 * Load a given plugin.
	 */
	private boolean loadPlugin(CommandSender player, String pluginName) {	
		PluginManager pluginManager = Bukkit.getServer().getPluginManager();
		
		// load and enable the given plugin	
		File pluginFolder = SubmergedCore.getInstance().getDataFolder().getParentFile();
		File pluginFile = new File(pluginFolder + File.separator + pluginName);
		if (!pluginFile.exists()) {
			// plugin does not exist
			Module.sendMessage("Plugin", player, "A plugin with the name '" + pluginName + "' could not be found at location:");
			Module.sendMessage("Plugin", player, pluginFile.getAbsolutePath());
			return true;
		}
		
		// Try and load the plugin
		Plugin plugin = null;
		try {
			plugin = pluginManager.loadPlugin(pluginFile);
		} catch (Exception e) {
			// Something went wrong so set the plugin to null
			e.printStackTrace();
			plugin = null;
		}	
		
		if (plugin == null) {
			// The plugin failed to load correctly
			Module.sendMessage("Plugin", player, "The plugin '" + pluginName + "' failed to load correctly.");
			return true;
		}
		
		// plugin loaded and enabled successfully
		pluginManager.enablePlugin(plugin);
		Module.sendMessage("Plugin", player, "The plugin '" + pluginName + "' has been succesfully loaded and enabled.");		
        cachePluginDetails();
        
		return true;
	}
    
    /**
     * 
     * @param alias
     * @return 
     */
    @Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {	
        
        List<String> result = new ArrayList<String>();
        
        // Commands
        if (args.length == 1) {
            compareAndAdd(result, "reload", args[0]);
            compareAndAdd(result, "disable", args[0]);
            compareAndAdd(result, "enable", args[0]);
            compareAndAdd(result, "load", args[0]);
            compareAndAdd(result, "unload", args[0]);
            compareAndAdd(result, "info", args[0]);
            return result;
        }
        
        // plugin name
        if (args.length == 2) {
            final boolean fileName = args[0].equalsIgnoreCase("load");
            if (fileName) {
                File pluginFolder = SubmergedCore.getInstance().getDataFolder().getParentFile();
                for (File plugin : pluginFolder.listFiles()) {
                    if (plugin.isDirectory()) {
                        continue;
                    }
                    if (!plugin.getName().endsWith(".jar")) {
                        continue;
                    }
                    compareAndAdd(result, plugin.getName(), args[1]);
                }
            } else {
                PluginManager pluginManager = Bukkit.getServer().getPluginManager();
                for (Plugin plugin : pluginManager.getPlugins()) {
                    compareAndAdd(result, plugin.getName(), args[1]);
                }
            }
            return result;
        }
        
        return result;
        
    }

    /**
     * 
     * @param result
     * @param name
     * @param string 
     */
    private void compareAndAdd(List<String> result, String name, String substring) {
        if (substring.isEmpty()) {
            result.add(name);
            return;
        }
        
        if (name.toLowerCase().startsWith(substring.toLowerCase())) {
            result.add(name);
        }        
    }
	
}
