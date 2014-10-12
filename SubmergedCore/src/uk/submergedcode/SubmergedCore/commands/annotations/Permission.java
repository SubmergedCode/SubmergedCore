package uk.submergedcode.SubmergedCore.commands.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Permission {

    String value();

    String message() default "You do not have permission to use this command.";

}
