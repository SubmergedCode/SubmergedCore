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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.PluginManager;

import com.google.common.collect.ImmutableList;

import uk.submergedcode.SubmergedCore.SubmergedCore;
import uk.submergedcode.SubmergedCore.commands.annotations.Command;
import uk.submergedcode.SubmergedCore.commands.annotations.Parent;
import uk.submergedcode.SubmergedCore.commands.annotations.Permission;
import uk.submergedcode.SubmergedCore.commands.annotations.TabCompletion;
import uk.submergedcode.SubmergedCore.commands.executors.AnnotationCommandExecutor;
import uk.submergedcode.SubmergedCore.commands.executors.AnnotationTabCompleter;
import uk.submergedcode.SubmergedCore.commands.executors.LegacyCommandExecutor;
import uk.submergedcode.SubmergedCore.commands.utils.AnnotatedCommandInfo;
import uk.submergedcode.SubmergedCore.commands.utils.CommandArguments;
import uk.submergedcode.SubmergedCore.commands.utils.Completions;
import uk.submergedcode.SubmergedCore.module.Module;

/**
 * The Module Command handler, handles internal command registering.
 * <p/>
 * This class is unstable so can break without notice and as such <b>should not
 * be used outside SubmergedCore</b>.
 *
 * @see Module#registerCommand(ModuleCommand)
 */
public class ModuleCommandHandler {

    private static final Class<?>[] commandMethodArguments = new Class<?>[] { CommandSender.class, CommandArguments.class };
    private static final Class<?>[] tabCompleterMethodArguments = new Class<?>[] { CommandSender.class, CommandArguments.class };
    private static final Pattern COMMAND_CHILD_SPLIT = Pattern.compile(".", Pattern.LITERAL);

    // TODO check each update to make sure they don't change the field name
    private static final String commandMapFieldName = "commandMap";
    private static final String commandsFieldName = "knownCommands";

    private static CommandMap commandMap;
    private static Field knownCommandsField;

    private static Map<Module, Map<String, ModuleCommand>> commands = Maps.newHashMap();
    private static Map<String, LegacyCommandExecutor> legacyExecutorPool = Maps.newHashMap();


    static {
        setupCommandMap();
    }

    private static void setupCommandMap() {
        // reflection to make sure we don't have to use OBC code
        try {
            Class<? extends PluginManager> clazz = Bukkit.getServer().getPluginManager().getClass();
            Field field = clazz.getDeclaredField(commandMapFieldName);
            field.setAccessible(true);
            commandMap = (CommandMap) field.get(Bukkit.getServer().getPluginManager());
        } catch (Exception e) {
            SubmergedCore.log(Level.INFO, "Error setting up command handler", e);
            commandMap = null;
        }

        if (commandMap instanceof SimpleCommandMap) {
            try {
                knownCommandsField = SimpleCommandMap.class.getDeclaredField(commandsFieldName);
                knownCommandsField.setAccessible(true);
            } catch (Exception e) {
                SubmergedCore.log(Level.INFO, "Error setting up command handler", e);
            }
        } else {
            SubmergedCore.log(Level.SEVERE, "Unknown command map type, cannot deregister commands.");
        }

    }

    /**
     * Register command.
     *
     * @param module  the module
     * @param command the command
     */
    public static void registerCommand(Module module, ModuleCommand command) {
        command.register(module);
        commandMap.register(module.getName(), command);

        if (commands.containsKey(module)) {
            commands.get(module).put(command.getLabel(), command);
        } else {
            HashMap<String, ModuleCommand> map = new HashMap<String, ModuleCommand>();
            map.put(command.getLabel(), command);
            commands.put(module, map);
        }
    }

    /**
     * Deregister all commands from a module.
     *
     * @param module the module
     */
    @SuppressWarnings("unchecked")
    public static void deregisterCommand(Module module) {

        if (!commands.containsKey(module) || getCommands(module) == null) {
            return;
        }

        List<ModuleCommand> commands = new ArrayList<ModuleCommand>(getCommands(module));
        for (ModuleCommand command : commands) {
            ModuleCommandHandler.commands.get(module).remove(command);

            try {
                Map<String, org.bukkit.command.Command> knownCommands = (Map<String, org.bukkit.command.Command>) knownCommandsField.get(commandMap);
                org.bukkit.command.Command theCommand = commandMap.getCommand(command.getLabel());

                if (theCommand == null) {
                    throw new Exception("Cannot find command " + command.getLabel() + " in bukkit command map");
                }

                theCommand.unregister(commandMap);
                knownCommands.remove(theCommand.getLabel().toLowerCase());
                if (SubmergedCore.getConfigurationManager().isDebugEnabled()) {
                    SubmergedCore.log(Level.INFO, theCommand.getLabel().toLowerCase() + " for module " + module.getName() + " has been deregistering successfully");
                }
            } catch (Throwable e) {
                SubmergedCore.log(Level.INFO, "Error deregistering " + command.getName() + " for module " + module.getName(), e);
            }
        }
    }

    public static void findCommands(Module module, Class<?> clazz) {
        int commands = 0;
        int tabCompleters = 0;

        Map<CommandType, List<Method>> commandMethods = Maps.newLinkedHashMap();
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }

            if (method.isAnnotationPresent(Command.class)) {
                if (method.isAnnotationPresent(Parent.class)) {
                    if (commandMethods.containsKey(CommandType.CHILD_COMMAND)) {
                        commandMethods.get(CommandType.CHILD_COMMAND).add(method);
                    } else {
                        commandMethods.put(CommandType.CHILD_COMMAND, Lists.newArrayList(method));
                    }
                } else {
                    if (commandMethods.containsKey(CommandType.COMMAND)) {
                        commandMethods.get(CommandType.COMMAND).add(method);
                    } else {
                        commandMethods.put(CommandType.COMMAND, Lists.newArrayList(method));
                    }
                }
            } else if (method.isAnnotationPresent(TabCompletion.class)) {
                if (commandMethods.containsKey(CommandType.TAB_COMPLETER)) {
                    commandMethods.get(CommandType.TAB_COMPLETER).add(method);
                } else {
                    commandMethods.put(CommandType.TAB_COMPLETER, Lists.newArrayList(method));
                }
            }
        }

        for (Method method : commandMethods.get(CommandType.COMMAND)) {
            module.debugConsole("Found method " + method.getName() + " loading as command.");

            if (!Arrays.equals(method.getParameterTypes(), commandMethodArguments)) {
                module.getLogger().log(Level.WARNING, "Method {0} has @Command annotation but has parameters {1} (not {2})",  new Object[] { method.getName(), format(method.getParameterTypes()), format(commandMethodArguments) });
                continue;
            }

            Command cmd = method.getAnnotation(Command.class);

            ModuleCommand command = new ModuleCommand(cmd.value(), cmd.usage());

            if (method.isAnnotationPresent(Permission.class)) {
                command.setPermission(method.getAnnotation(Permission.class).value());
            }

            command.setDescription(cmd.description());
            command.setExecutor(new AnnotationCommandExecutor(command, method, new AnnotatedCommandInfo(method)));

            registerCommand(module, command);
            commands++;
        }

        for (Method method : commandMethods.get(CommandType.CHILD_COMMAND)) {
            module.debugConsole("Found method " + method.getName() + " loading as command.");

            if (!Arrays.equals(method.getParameterTypes(), commandMethodArguments)) {
                module.getLogger().log(Level.WARNING, "Method {0} has @Command annotation but has parameters {1} (not {2})",  new Object[] { method.getName(), format(method.getParameterTypes()), format(commandMethodArguments) });
                continue;
            }

            Command cmd = method.getAnnotation(Command.class);
            Parent parent = method.getAnnotation(Parent.class);

            ModuleCommand parentCommand = getCommand(parent.value());
            ModuleCommand command = new ModuleChildCommand(parentCommand, cmd.value());

            command.setUsage(cmd.usage());

            if (method.isAnnotationPresent(Permission.class)) {
                command.setPermission(method.getAnnotation(Permission.class).value());
            }

            command.setDescription(cmd.description());
            command.setExecutor(new AnnotationCommandExecutor(command, method, new AnnotatedCommandInfo(method)));

            parentCommand.addChildCommand((ModuleChildCommand) command);
            commands++;
        }

        for (Method method : commandMethods.get(CommandType.TAB_COMPLETER)) {
            module.debugConsole("Found method " + method.getName() + " loading as tab completer.");

            if (!method.getReturnType().equals(Completions.class)) {
                module.getLogger().log(Level.WARNING, "Method {0} has @TabCompletion annotation but has return type {1} (not {2})",  new Object[] { method.getName(), method.getReturnType().getSimpleName(), Completions.class.getSimpleName() });
                continue;
            }

            if (!Arrays.equals(method.getParameterTypes(), commandMethodArguments)) {
                module.getLogger().log(Level.WARNING, "Method {0} has @TabCompletion annotation but has parameters {1} (not {2})",  new Object[] { method.getName(), format(method.getParameterTypes()), format(commandMethodArguments) });
                continue;
            }

            TabCompletion annotation = method.getAnnotation(TabCompletion.class);
            ModuleCommand command = getCommand(annotation.value());

            AnnotationTabCompleter completer = new AnnotationTabCompleter(method);
            command.setCompleter(completer);
            tabCompleters++;
        }

        if (commands == 0) {
            module.getLogger().log(Level.WARNING, "Did not find any commands to register in class {0}.",  new Object[] { clazz.getName() });
        } else {
            module.getLogger().log(Level.INFO, "Found {0} command{2} to register in class {1}.", new Object[] { commands, clazz.getName(), commands == 1 ? "" : "s" });
        }

        if (commands == 0) {
            module.getLogger().log(Level.WARNING, "Did not find any tab completers to register in class {0}.",  new Object[] { clazz.getName() });
        } else {
            module.getLogger().log(Level.INFO, "Found {0} tab completer{2} to register in class {1}.", new Object[] { tabCompleters, clazz.getName(), commands == 1 ? "" : "s" });
        }
    }

    private static String format(Class<?>[] parameterTypes) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (Class<?> clazz : parameterTypes) {
            builder.append(clazz.getSimpleName()).append(',');
        }
        builder.append(']');
        return builder.toString();
    }

    /**
     * Gets the commands for a module.
     *
     * @param module the module
     * @return the commands for that module
     */
    public static List<ModuleCommand> getCommands(Module module) {
        if (!commands.containsKey(module)) {
            return new ImmutableList.Builder<ModuleCommand>().build();
        }
        return new ImmutableList.Builder<ModuleCommand>().addAll(commands.get(module).values()).build();
    }

    /**
     * Get a command by its label.
     *
     * @param command the command label
     * @return the module command related to that command label
     * @throws IllegalArgumentException if the specified command pattern could
     *          not be found
     */
    public static ModuleCommand getCommand(String command) {
        String[] parts = COMMAND_CHILD_SPLIT.split(command);
        ModuleCommand moduleCommand = null;

        for (Map<String, ModuleCommand> entry : commands.values()) {
            ModuleCommand cmd = entry.get(parts[0]);

            if (cmd == null) {
                continue;
            }

            moduleCommand = cmd;
        }

        if (moduleCommand != null && parts.length > 1) {
            for (int i = 1; i < parts.length; i++) {
                moduleCommand = moduleCommand.getChild(parts[i]);

                if (moduleCommand == null) {
                    StringBuilder commandTree = new StringBuilder();
                    commandTree.append(parts[0]);

                    for (int j = 1; j < i; j++) {
                        commandTree.append("=>").append(parts[j]);
                    }

                    throw new IllegalArgumentException("Sub command not found [" + commandTree.toString() + "]");
                }
            }
        }

        if (moduleCommand == null) {
            throw new IllegalArgumentException("Command not found " + command);
        }

        return moduleCommand;
    }

    /**
     * Get the cached instance of a legacy command executor for the specified
     * module. If none exists create a new instance and cache that instance.
     *
     * @param module the module for the legacy executor
     * @return the legacy command executor for that module
     */
    public static LegacyCommandExecutor getLegacyExecutor(Module module) {
        LegacyCommandExecutor commandExecutor = legacyExecutorPool.get(module.getName());

        if (commandExecutor == null) {
            commandExecutor = new LegacyCommandExecutor(module);
            legacyExecutorPool.put(module.getName(), new LegacyCommandExecutor(module));
        }


        return commandExecutor;
    }

    private static enum CommandType {
        COMMAND,
        CHILD_COMMAND,
        TAB_COMPLETER;
    }
}
