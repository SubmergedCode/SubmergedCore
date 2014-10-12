package uk.submergedcode.SubmergedCore.commands.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

    String value();

    String usage() default "/<command>";

    String description() default "";

    String[] aliases() default "";

}
