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
package uk.submergedcode.SubmergedCore.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;

import uk.submergedcode.SubmergedCore.SubmergedCore;
import uk.submergedcode.SubmergedCore.commands.executors.ModuleCommandExecutor;
import uk.submergedcode.SubmergedCore.module.Module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * A minecraft command that is associated with a specific SubmergedCore
 * {@link Module}.
 *
 * @author James.
 */
public class ModuleCommand extends Command implements TabExecutor {

    protected String m_label = null;
    protected String m_usage = null;
    protected List<String> m_aliases = Lists.newArrayList();
    protected String m_description = null;
    protected ModuleCommandHelpTopic m_helpTopic = null;
    protected String m_permission = null;
    protected Map<String, ModuleChildCommand> m_children = Maps.newHashMap();
    protected Module m_module;

    protected CommandExecutor m_executor;
    protected TabCompleter m_completer;

    /**
     * Instantiates a new module command.
     *
     * @param label  the label
     * @param usage  the usage
     */
    public ModuleCommand(String label, String usage) {
        super(label);
        m_label = label;
        m_usage = usage == null ? "/" + m_label : usage;
        m_description = usage;
        m_helpTopic = new ModuleCommandHelpTopic(this);
        m_permission = "SubmergedCore.command." + m_label;

        setExecutor(new ModuleCommandExecutor(this));
    }

    /**
     * Register this command to a module, called in
     * {@link ModuleCommandHandler#registerCommand(Module, ModuleCommand)}.
     * <p/>
     * <b>For internal use only<b/>.
     *
     * @param module to register it to
     * @throws IllegalStateException if already registered to another module
     */
    public void register(Module module) {
        Preconditions.checkState(m_module == null, "Cannot register this command to another module");

        m_module = module;
    }

    /**
     * Gets the module this command is registered to.
     *
     * @return the module this command is registered to
     */
    public Module getModule() {
        return m_module;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    @Override
    public String getLabel() {
        return m_label;
    }

    /**
     * Sets the usage.
     *
     * @param usage the new usage
     * @return the module command
     */
    public ModuleCommand setUsage(String usage) {
        m_usage = usage;
        return this;
    }

    /**
     * Gets the usage.
     *
     * @return the usage
     */
    @Override
    public String getUsage() {
        return m_usage;
    }

    /**
     * Adds a alias.
     *
     * @param alias the alias
     * @return the module command
     * @deprecated {@link #addAlias(String)}
     */
    public ModuleCommand addAliase(String alias) {
        return addAlias(alias);
    }

    /**
     * Adds a alias.
     *
     * @param alias the alias
     * @return the module command
     */
    public ModuleCommand addAlias(String alias) {
        m_aliases.add(alias.toLowerCase());
        return this;
    }

    /**
     * Gets the aliases.
     *
     * @return the aliases
     */
    @Override
    public List<String> getAliases() {
        return m_aliases;
    }

    /**
     * Set the command executor and tab completer for this command. This will
     * handle how the command is executed if called at runtime by a player. and
     * how the tab completions are handled for the command.
     * <p />
     * This is equal to {@link #setExecutor(TabExecutor)} followed by
     * {@link #setCompleter(TabCompleter)}.
     *
     * @param executor the new executor for the command
     * @return the current module command
     */
    public ModuleCommand setExecutor(TabExecutor executor) {
        m_executor = executor;
        m_completer = executor;
        return this;
    }

    /**
     * Set the command executor for this command. This will handle how the
     * command is executed if called at runtime by a player.
     *
     * @param executor the new executor for the command
     * @return the current module command
     */
    public ModuleCommand setExecutor(CommandExecutor executor) {
        m_executor = executor;
        return this;
    }

    /**
     * Set the tab completer for this command. This will handle how the tab
     * completions are handled at runtime for the command.
     *
     * @param completer the new tab completer
     * @return the current module command
     */
    public ModuleCommand setCompleter(TabCompleter completer) {
        m_completer = completer;
        return this;
    }

    /**
     * Get the current executor for this command.
     *
     * @return the current command executor for this command
     */
    public CommandExecutor getExecutor() {
        return m_executor;
    }

    /**
     * Get the tab completer for this command.
     *
     * @return the current tab completer for this command
     */
    public TabCompleter getCompleter() {
        return m_completer;
    }

    /**
     * Gets the help text.
     *
     * @return the help
     */
    @Override
    public String getDescription() {
        return m_description;
    }

    /**
     * Sets the description of this command.
     *
     * @param description the new help
     * @return the module command
     */
    public ModuleCommand setDescription(String description) {
        m_description = description;
        return this;
    }

    /**
     * Sets the help text.
     *
     * @param help the new help text
     * @return the command instance
     * @deprecated {@link #setDescription(String)}
     */
    public ModuleCommand setHelp(String help) {
        return setDescription(help);
    }

    /**
     * Gets the help topic.
     *
     * @return the help topic
     */
    public ModuleCommandHelpTopic getHelpTopic() {
        return m_helpTopic;
    }

    /**
     * Gets the permission for this command.
     *
     * @return the permission
     */
    public String getPermission() {
        return m_permission;
    }

    /**
     * Sets the permission for this command.
     *
     * @param permission the new permission
     */
    public void setPermission(String permission) {
        this.m_permission = permission;
    }

    /**
     * Add a new child command to this command.
     *
     * @param command the child command
     */
    public void addChildCommand(ModuleChildCommand command) {
        this.m_children.put(command.getLabel(), command);
    }

    /**
     * Get a specified child command for this command
     *
     * @param label the command label
     * @return the specified child command object
     */
    public ModuleCommand getChild(String label) {
        return this.m_children.get(label);
    }

    /**
     * Internal command handling.
     *
     * @param sender the command sender
     * @param label  the command label
     * @param args   the command arguments
     */
    @Override
    public final boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length >= 1) {
            for (ModuleChildCommand child : m_children.values()) {
                if (child.getLabel().equalsIgnoreCase(args[0])) {
                    // cut first argument (sub command) out of command then handle as child command
                    args = Arrays.copyOfRange(args, 1, args.length);
                    return child.execute(sender, label, args);
                }
            }
        }

        try {
            if (m_executor.onCommand(sender, this, label, args)) {
                return true;
            }
        } catch (CommandException ex) {
            sendMessage(sender, ChatColor.RED + ex.getMessage());
            return true;
        }

        // call command method in module if still not handled
        if (ModuleCommandHandler.getLegacyExecutor(m_module).onCommand(sender, this, label, args)) {
            return true;
        }

        // if not handled for any reason, send usage
        Module.sendMessage(m_module.getName(), sender, getUsage());
        return false;
    }

    /**
     * Internal tab completion handling.
     *
     * @param sender the command sender
     * @param alias  the command label
     * @param args   the command arguments
     * @return the tab completed list
     */
    @Override
    public final List<String> tabComplete(CommandSender sender, String alias, String[] args) throws CommandException, IllegalArgumentException {
        Preconditions.checkArgument(sender != null, "Sender cannot be null");
        Preconditions.checkArgument(args != null, "Arguments cannot be null");
        Preconditions.checkArgument(alias != null, "Alias cannot be null");

        List<String> completions = null;

        try {
            if (args.length >= 1) {
                for (ModuleChildCommand child : m_children.values()) {
                    if (child.getLabel().equalsIgnoreCase(args[0])) {
                        // cut first argument (sub command) out of command then handle as child command
                        args = Arrays.copyOfRange(args, 1, args.length);
                        completions = child.tabComplete(sender, alias, args);
                    }
                }
            } else {
                m_module.getLogger().info("using tab completer " + m_completer.getClass());
                completions = m_completer.onTabComplete(sender, this, alias, args);
            }
        } catch (Throwable ex) {
            StringBuilder message = new StringBuilder();
            message.append("Unhandled exception during tab completion for command '/").append(alias).append(' ');
            for (String arg : args) {
                message.append(arg).append(' ');
            }
            message.deleteCharAt(message.length() - 1).append("' in plugin ").append(m_module.getName());
            throw new CommandException(message.toString(), ex);
        }

        if (completions == null) {
            return super.tabComplete(sender, alias, args);
        }

        List<String> sortable = new ArrayList<String>(completions);
        Collections.sort(sortable, String.CASE_INSENSITIVE_ORDER);
        return ImmutableList.copyOf(completions);
    }

    /**
     * Handle tab completion on the command.
     *
     * @param sender  the command sender
     * @param command the command instance
     * @param alias   the label of the command
     * @param args    the arguments added to the command
     * @return true, if successful, false falls through to default module
     * command handling
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Builder<String> builder = ImmutableList.builder();

        if (m_children.size() >= 1 && args.length == 1) {
            for (ModuleChildCommand child : m_children.values()) {
                if (child.getLabel().startsWith(args[0])) {
                    builder.add(child.getLabel());
                }
            }
        } else {
            if (args.length == 0) {
                return builder.build();
            }

            String name = args[args.length - 1];
            List<OfflinePlayer> players = m_module.matchPlayer(name, true);

            for (OfflinePlayer player : players) {
                builder.add(player.getName());
            }
        }

        return builder.build();
    }

    /**
     * Handle the command.
     *
     * @param sender  the command sender
     * @param command the command instance
     * @param label   the label of the command
     * @param args    the arguments added to the command
     * @return true, if successful, false falls through to default module
     * command handling
     * @see Module#onCommand(CommandSender, String, String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    /**
     * Send a message to a player.
     *
     * @param sender the command sender to send the message to
     * @param message the message to send
     */
    protected void sendMessage(CommandSender sender, String message) {
        Module.sendMessage(m_module == null ? SubmergedCore.getInstance().getName() : m_module.getName(), sender, message);
    }
}
