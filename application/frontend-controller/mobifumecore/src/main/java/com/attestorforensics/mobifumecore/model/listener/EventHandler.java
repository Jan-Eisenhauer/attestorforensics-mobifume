package com.attestorforensics.mobifumecore.model.listener;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for listener methods.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(METHOD)
public @interface EventHandler {

}
