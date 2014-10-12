package uk.submergedcode.SubmergedCore.commands.executors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import uk.submergedcode.SubmergedCore.commands.ModuleCommand;

import java.util.List;

public class ModuleCommandExecutor implements TabExecutor {

    private final ModuleCommand command;

    public ModuleCommandExecutor(ModuleCommand command) {
        this.command = command;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return this.command.onCommand(sender, cmd, label, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return this.command.onTabComplete(sender, cmd, label, args);
    }

}
