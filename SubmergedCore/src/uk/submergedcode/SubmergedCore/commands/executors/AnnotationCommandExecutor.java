package uk.submergedcode.SubmergedCore.commands.executors;

import org.bukkit.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import uk.submergedcode.SubmergedCore.commands.exception.CommandPermissionException;
import uk.submergedcode.SubmergedCore.commands.utils.AnnotatedCommandInfo;
import uk.submergedcode.SubmergedCore.commands.ModuleCommand;
import uk.submergedcode.SubmergedCore.commands.utils.CommandArguments;
import uk.submergedcode.SubmergedCore.commands.utils.SenderType;
import uk.submergedcode.SubmergedCore.commands.exception.CommandSenderException;
import uk.submergedcode.SubmergedCore.commands.exception.CommandUsageException;
import uk.submergedcode.SubmergedCore.module.Module;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class AnnotationCommandExecutor implements CommandExecutor {

    private Object instance;
    private Method method;

    private ModuleCommand command;
    private AnnotatedCommandInfo info;

    public AnnotationCommandExecutor(ModuleCommand command, Method method, AnnotatedCommandInfo info) {
        this.command = command;
        this.method = method;
        this.info = info;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            if (info.hasPermission() && !hasPermission(sender)) {
                throw new CommandPermissionException();
            }

            if (info.hasSender() && !isValidSender(sender)) {
                throw new CommandSenderException();
            }

            if (info.hasArguments() && !checkArguments(args)) {
                throw new CommandUsageException();
            }

            method.invoke(instance(), sender, new CommandArguments(args));
        } catch (CommandException ex) {
            handleCommandException(sender, ex);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();

            if (cause instanceof CommandException) {
                handleCommandException(sender, cause);
            } else {
                handleException(sender, ex);
            }
        } catch (ReflectiveOperationException ex) {
            handleException(sender, ex);
        }

        return true;
    }

    private boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(this.info.getPermission().value());
    }

    private boolean checkArguments(String[] args) {
        if (args.length > this.info.getArguments().max()
                || args.length < this.info.getArguments().min()) {
            return false;
        }

        return true;
    }

    private boolean isValidSender(CommandSender sender) {
        boolean valid = false;

        for (SenderType type : info.getSender().value()) {
            if (type.isOfType(sender)) {
                valid = true;
                break;
            }
        }

        return valid;
    }

    private void handleCommandException(CommandSender sender, Throwable cause) {
        String message = "";

        if (cause instanceof CommandUsageException) {
            message = "Invalid usage: " + command.getUsage();
        } else if (cause instanceof CommandSenderException) {
            message = info.getSender().message().replace("${type}", SenderType.getFromClass(sender).toString());
        } else if (cause instanceof CommandPermissionException) {
            message = info.getPermission().message();
        } else {
            message = cause.getMessage();
        }

        Module.sendMessage(command.getModule().getName(), sender, ChatColor.RED + message);
    }

    private void handleException(CommandSender sender, Throwable ex) {
        Module.sendMessage(command.getModule().getName(), sender, "A unknown error has occurred whilst executing the command");
        Module.sendMessage(command.getModule().getName(), sender, ChatColor.RED + ex.getMessage());
        command.getModule().getLogger().log(Level.WARNING, "Exception: ", ex);
    }

    private Object instance() throws ReflectiveOperationException {
        if (instance == null) {
           instance = method.getDeclaringClass().newInstance();
        }

        return instance;
    }
}
