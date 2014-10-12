package uk.submergedcode.SubmergedCore.commands.annotations;

import java.lang.annotation.*;

/**
 * Annotation for commands specifying the amount and format of any arguments
 * that should be passed to the command.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Arguments {

    /**
     * Minimum amount of arguments for this command. A command executed with
     * a number of arguments bellow this value will send a error to the user
     * with the command's  {@link Command#usage()}.
     */
    int min();

    /**
     * Maximum amount of arguments for this command. A command executed with
     * a number of arguments above this value this will send a error to the
     * user with the command's {@link Command#usage()}.
     */
    int max();

}
