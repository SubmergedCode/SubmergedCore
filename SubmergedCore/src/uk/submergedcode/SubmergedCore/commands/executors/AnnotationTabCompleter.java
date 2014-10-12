package uk.submergedcode.SubmergedCore.commands.executors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import uk.submergedcode.SubmergedCore.commands.utils.Completions;
import uk.submergedcode.SubmergedCore.commands.utils.CommandArguments;

import java.lang.reflect.Method;
import java.util.List;

public class AnnotationTabCompleter implements TabCompleter {

    private Object instance;
    private final Method method;

    public AnnotationTabCompleter(Method method) {
        this.method = method;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        try {
            Object result = this.method.invoke(instance(), sender, new CommandArguments(args));
            System.out.println(this.method.getName() + " being used for tab completion");

            if (!(result instanceof Completions)) {
                throw new CommandException("Tab completion method did not return a 'Completions' object.");
            }

            System.out.println("Completions " + ((Completions) result).getCompletions());
            return ((Completions) result).getCompletions();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace(); // TODO better logging
        }
        return null;
    }

    private Object instance() throws ReflectiveOperationException {
        if (instance == null) {
            instance = method.getDeclaringClass().newInstance();
        }

        return instance;
    }
}
