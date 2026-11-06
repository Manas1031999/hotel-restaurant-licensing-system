package in.wb.nhls.notification.aspect;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import in.wb.nhls.notification.annotation.Notify;
import in.wb.nhls.notification.service.NotificationService;

// LOWEST_PRECEDENCE: @Transactional-এর ভেতরে চলে (TransactionConfig দেখ)
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class NotifyAspect {

    private static final Logger log = LoggerFactory.getLogger(NotifyAspect.class);

    private final NotificationService notificationService;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNames = new DefaultParameterNameDiscoverer();
    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

    public NotifyAspect(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @AfterReturning(pointcut = "@annotation(in.wb.nhls.notification.annotation.Notify)", returning = "result")
    public void afterSuccess(JoinPoint joinPoint, Object result) {

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Notify notify = AnnotationUtils.findAnnotation(method, Notify.class);

        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                null, method, joinPoint.getArgs(), parameterNames);
        context.setVariable("result", result);

        String recipient;
        Map<String, ?> params;
        try {
            if (!notify.condition().isBlank()
                    && !Boolean.TRUE.equals(evaluate(notify.condition(), context, Boolean.class))) {
                return;
            }
            recipient = evaluate(notify.to(), context, String.class);
            params = notify.params().isBlank() ? Map.of() : toStringKeyMap(evaluate(notify.params(), context, Map.class));
        } catch (Exception ex) {
            // ভুল expression-এর জন্য business কাজ আটকাবে না
            log.warn("@Notify expression ভুল: template={} ({})", notify.template(), ex.getMessage());
            return;
        }

        notificationService.enqueue(notify.template(), recipient, params);
    }

    private <T> T evaluate(String expression, MethodBasedEvaluationContext context, Class<T> type) {
        return expressionCache.computeIfAbsent(expression, parser::parseExpression).getValue(context, type);
    }

    private static Map<String, Object> toStringKeyMap(Map<?, ?> raw) {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        raw.forEach((k, v) -> map.put(String.valueOf(k), v));
        return map;
    }
}
