package EmpiresMod.API.Commands.Command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
	String name();

	String permission();

	String syntax();

	String parentName() default "ROOT";

	String[] alias() default {};

	boolean console() default true;

	String[] completionKeys() default {};
}