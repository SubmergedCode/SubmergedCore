package uk.submergedcode.SubmergedCore.commands.executors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import uk.submergedcode.SubmergedCore.module.Module;

import java.util.List;

public class LegacyCommandExecutor implements CommandExecutor {

    private final Module module;

    public LegacyCommandExecutor(Module module) {
        this.module = module;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return this.module.onCommand(sender, label, args);
    }

}
