package uk.submergedcode.SubmergedCore.commands.utils;

import org.bukkit.command.CommandException;

import java.util.Arrays;
import java.util.List;

public class CommandArguments {

    private List<String> arguments;

    public CommandArguments(String[] args) {
        this.arguments = Arrays.asList(args);
    }

    public String get(int i) {
        return arguments.get(i);
    }

    public int getInt(int i) {
        try {
            return Integer.valueOf(get(i));
        } catch (NumberFormatException ex) {
            throw new CommandException("Argument " + i + " (" + get(i) + ") should be a integer but isn't.");
        }
    }

    public float getFloat(int i) {
        try {
            return Float.valueOf(get(i));
        } catch (NumberFormatException ex) {
            throw new CommandException("Argument " + i + " (" + get(i) + ") should be a float but isn't.");
        }
    }

    public double getDouble(int i) {
        try {
            return Double.valueOf(get(i));
        } catch (NumberFormatException ex) {
            throw new CommandException("Argument " + i + " (" + get(i) + ") should be a double but isn't.");
        }
    }

    public long getLong(int i) {
        try {
            return Long.valueOf(get(i));
        } catch (NumberFormatException ex) {
            throw new CommandException("Argument " + i + " (" + get(i) + ") should be a long but isn't.");
        }
    }

    public short getShort(int i) {
        try {
            return Short.valueOf(get(i));
        } catch (NumberFormatException ex) {
            throw new CommandException("Argument " + i + " (" + get(i) + ") should be a short but isn't.");
        }
    }

    public byte getByte(int i) {
        try {
            return Byte.valueOf(get(i));
        } catch (NumberFormatException ex) {
            throw new CommandException("Argument " + i + " (" + get(i) + ") should be a byte but isn't.");
        }
    }

    public boolean getBoolean(int i) {
        return Boolean.valueOf(get(i));
    }

}
