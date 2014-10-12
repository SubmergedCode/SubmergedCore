package uk.submergedcode.SubmergedCore.commands.utils;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;

public enum SenderType {
    PLAYER(Player.class),
    CONSOLE(ConsoleCommandSender.class),
    REMOTE(RemoteConsoleCommandSender.class),
    BLOCK(BlockCommandSender.class);

    private Class<? extends CommandSender> clazz;

    SenderType(Class<? extends CommandSender> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends CommandSender> getSenderClass() {
        return this.clazz;
    }

    public boolean isOfType(CommandSender sender) {
        for (Class<?> clazz : sender.getClass().getInterfaces()) {
            if (clazz == getSenderClass()) {
                return true;
            }
        }

        return false;
    }

    public static SenderType getFromClass(CommandSender sender) {
        for (SenderType type : values()) {
            if (type.isOfType(sender)) {
                return type;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

}
