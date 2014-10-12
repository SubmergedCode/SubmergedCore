package uk.submergedcode.SubmergedCore.commands.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Parent {

    String value();

}
