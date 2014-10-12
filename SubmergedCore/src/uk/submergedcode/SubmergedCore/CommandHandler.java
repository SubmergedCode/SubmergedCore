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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import uk.submergedcode.SubmergedCore.module.Module;
import uk.submergedcode.SubmergedCore.module.loader.exception.LoadException;

public class CommandHandler implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command commmand, String label, String[] args) {

        if (args.length < 1) {
            sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "SubmergedCore commands");
            sender.sendMessage(ChatColor.DARK_AQUA + "module " + ChatColor.WHITE + "- access module load/unload/reload commands");
            sender.sendMessage(ChatColor.DARK_AQUA + "debug " + ChatColor.WHITE + "- debug a given module");
            sender.sendMessage(ChatColor.DARK_AQUA + "reload " + ChatColor.WHITE + "- reload the plugin");
            return true;
        }

        if (!SubmergedCore.getPermissions().has(sender, "SubmergedCore.admin")) {
            sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "Sorry you do not have permission to do that");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            try {               
                SubmergedCore.getModuleLoader().unload();
                SubmergedCore.getModuleLoader().load();
                sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "Reloaded all modules");
            } catch (LoadException ex) {
                sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "Unhandled exception whilst loading modules");
                sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + ex.getMessage());
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("module")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "SubmergedCore module commands");
                sender.sendMessage(ChatColor.DARK_AQUA + "unload " + ChatColor.WHITE + "- unload a module");
                sender.sendMessage(ChatColor.DARK_AQUA + "load " + ChatColor.WHITE + "- load a module");
                sender.sendMessage(ChatColor.DARK_AQUA + "reload " + ChatColor.WHITE + "- reload a module");
                sender.sendMessage(ChatColor.DARK_AQUA + "debug " + ChatColor.WHITE + "- debug a module");
                return true;
            }

            if (args[1].equalsIgnoreCase("unload")) {
                if (args.length == 3) {
                    Module module = SubmergedCore.getModuleLoader().getModule(args[2]);

                    if (module == null) {
                        sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "Sorry that module isn't enabled on SubmergedCore.getInstance() server, do /modules for a list that are");
                        return true;
                    }
                    SubmergedCore.getModuleLoader().unload(module);
                    sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "Unloaded " + args[2]);
                    return true;
                }

                SubmergedCore.getModuleLoader().unload();
                sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "UnLoaded all modules");
                return true;
            }

            /*if (args[1].equalsIgnoreCase("load")) { TODO replace

                if (args.length == 3) {
                    SubmergedCore.getModuleLoader().load();
                    Module module = SubmergedCore.getModuleLoader().getModule(args[2]);

                    if (module == null) {
                        sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "Error loading module " + args[2]);
                        return true;
                    }

                    module.onEnable();
                    sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "Loaded " + args[2]);
                    return true;
                }

                try {
                    SubmergedCore.getModuleLoader().load();
                    sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "Loaded all modules");
                } catch (LoadException ex) {
                    sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "Unhandled exception whilst loading modules");
                    sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + ex.getMessage());
                }

                return true;
            }*/

            if (args[1].equalsIgnoreCase("reload")) {

                if (args.length == 3) {
                    Module module = SubmergedCore.getModuleLoader().getModule(args[2]);

                    if (module == null) {
                        sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "Sorry that module isn't enabled on this server, do /modules for a list that are");
                        return true;
                    }
                    SubmergedCore.getModuleLoader().unload(module);
                    SubmergedCore.getModuleLoader().load(module.getFile());
                    module = SubmergedCore.getModuleLoader().getModule(args[2]);

                    if (module == null) {
                        sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "Error loading module " + args[2]);
                        return true;
                    }

                    module.onEnable();
                    sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "ReLoaded " + args[2]);
                    return true;
                }

                try {
                    SubmergedCore.getModuleLoader().unload();
                    SubmergedCore.getModuleLoader().load();
                    sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "Reloaded all modules");
                } catch (LoadException ex) {
                    sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "Unhandled exception whilst loading modules");
                    sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + ex.getMessage());
                }

                return true;
            }

            if (args[1].equalsIgnoreCase("debug")) {

                if (args.length != 3) {
                    sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "/SubmergedCore module debug <module>");
                    return true;
                }

                Module module = SubmergedCore.getModuleLoader().getModule(args[2]);

                if (module == null) {
                    sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "Module " + args[2] + " isn't loaded");
                    return true;
                }

                module.setDebug(!module.isDebug());
                sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + module.getName() + ": debug " + (module.isDebug() ? "enabled" : "disabled"));
                return true;
            }

            sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "SubmergedCore module commands");
            sender.sendMessage(ChatColor.DARK_AQUA + "unload " + ChatColor.WHITE + "- unload a module");
            sender.sendMessage(ChatColor.DARK_AQUA + "load " + ChatColor.WHITE + "- load a module");
            sender.sendMessage(ChatColor.DARK_AQUA + "reload " + ChatColor.WHITE + "- reload a module");
            sender.sendMessage(ChatColor.DARK_AQUA + "debug " + ChatColor.WHITE + "- debug a module");
            return true;
        }

        sender.sendMessage(ChatColor.DARK_AQUA + "[SubmergedCore] " + ChatColor.WHITE + "SubmergedCore commands");
        sender.sendMessage(ChatColor.DARK_AQUA + "module " + ChatColor.WHITE + "- access module load/unload/reload commands");
        sender.sendMessage(ChatColor.DARK_AQUA + "debug " + ChatColor.WHITE + "- debug a given module");
        sender.sendMessage(ChatColor.DARK_AQUA + "reload " + ChatColor.WHITE + "- reload the plugin");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sebder, Command command, String label, String[] args) {
        if (args.length == 0) {
            return ImmutableList.of("module", "debug", "reload");
        }

        if (args.length == 1) {
            Builder<String> list = ImmutableList.builder();
            for (String string : Arrays.asList("module", "debug", "reload")) {
                if (string.startsWith(args[0])) {
                    list.add(string);
                }
            }
            return list.build();
        }

        if (args.length == 2) {
            Builder<String> list = ImmutableList.builder();
            for (String string : Arrays.asList("unload", "load", "reload", "debug")) {
                if (string.startsWith(args[0])) {
                    list.add(string);
                }
            }
            return list.build();
        }

        return ImmutableList.of();
    }

}
