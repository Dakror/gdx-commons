package de.dakror.common.libgdx.lml;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(value = { ElementType.TYPE })
public @interface LmlTag {
    String tagName();
}
