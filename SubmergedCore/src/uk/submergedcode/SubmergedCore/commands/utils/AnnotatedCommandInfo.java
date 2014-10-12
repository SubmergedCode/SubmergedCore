package uk.submergedcode.SubmergedCore.commands.utils;

import uk.submergedcode.SubmergedCore.commands.annotations.Arguments;
import uk.submergedcode.SubmergedCore.commands.annotations.Permission;
import uk.submergedcode.SubmergedCore.commands.annotations.Sender;

import java.lang.reflect.Method;

public class AnnotatedCommandInfo {

    private final Arguments arguments;
    private final Sender sender;
    private final Permission permission;

    public AnnotatedCommandInfo(Arguments arguments, Sender sender, Permission permission) {
        this.arguments = arguments;
        this.sender = sender;
        this.permission = permission;
    }

    public AnnotatedCommandInfo(Method method) {
        this.arguments = method.getAnnotation(Arguments.class);
        this.sender = method.getAnnotation(Sender.class);
        this.permission = method.getAnnotation(Permission.class);
    }

    public boolean hasArguments() {
        return arguments != null;
    }

    public boolean hasSender() {
        return sender != null;
    }

    public boolean hasPermission() {
        return permission != null;
    }

    public Arguments getArguments() {
        return arguments;
    }

    public Sender getSender() {
        return sender;
    }

    public Permission getPermission() {
        return permission;
    }
}
