package in.wb.nhls.notification.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import in.wb.nhls.notification.template.NotificationTemplate;

// সফল হলে outbox-এ notification লেখে; to, params, condition SpEL (#result, parameter নাম)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Notify {

    NotificationTemplate template();

    String to();

    // যেমন "{appNo: #result.applicationNumber}"
    String params() default "";

    // খালি হলে সবসময় পাঠাবে
    String condition() default "";
}
