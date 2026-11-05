package in.wb.nhls.audit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// entityId/detail SpEL; failure-এ #result null, তাই #result?.id লিখবি
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    String action();

    String entityType();

    String entityId() default "";

    String detail() default "";
}
