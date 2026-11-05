package in.wb.nhls.audit.aspect;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import in.wb.nhls.audit.annotation.Auditable;
import in.wb.nhls.audit.entity.AuditOutcome;
import in.wb.nhls.audit.event.AuditEvent;
import jakarta.servlet.http.HttpServletRequest;

// ExposeInvocationInterceptor-এর পরে, @Transactional-এর বাইরে চলে
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final ApplicationEventPublisher eventPublisher;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNames = new DefaultParameterNameDiscoverer();
    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

    public AuditAspect(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Around("@annotation(in.wb.nhls.audit.annotation.Auditable)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Auditable auditable = AnnotationUtils.findAnnotation(method, Auditable.class);

        long start = System.nanoTime();
        Object result = null;
        Throwable failure = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            failure = ex;
            throw ex;
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            publishSafely(method, joinPoint.getArgs(), auditable, result, failure, durationMs);
        }
    }

    private void publishSafely(Method method, Object[] args, Auditable auditable,
            Object result, Throwable failure, long durationMs) {
        try {
            MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                    null, method, args, parameterNames);
            context.setVariable("result", result);

            HttpServletRequest request = currentRequest();

            eventPublisher.publishEvent(new AuditEvent(
                    auditable.action(),
                    auditable.entityType(),
                    evaluate(auditable.entityId(), context),
                    failure == null ? AuditOutcome.SUCCESS : AuditOutcome.FAILURE,
                    currentActor(request),
                    request != null ? request.getRemoteAddr() : null,
                    request != null ? request.getMethod() + " " + request.getRequestURI() : null,
                    evaluate(auditable.detail(), context),
                    failure != null ? errorMessage(failure) : null,
                    durationMs,
                    LocalDateTime.now()));
        } catch (Exception ex) {
            log.warn("Audit event তৈরি করা যায়নি: action={}", auditable.action(), ex);
        }
    }

    private String evaluate(String expression, MethodBasedEvaluationContext context) {
        if (expression == null || expression.isBlank()) {
            return null;
        }
        try {
            Object value = expressionCache
                    .computeIfAbsent(expression, parser::parseExpression)
                    .getValue(context);
            return value == null ? null : String.valueOf(value);
        } catch (Exception ex) {
            log.warn("Audit SpEL expression ভুল: '{}' ({})", expression, ex.getMessage());
            return null;
        }
    }

    private static HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest();
        }
        return null;
    }

    private static String currentActor(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return auth.getName();
        }
        return request == null ? "SYSTEM" : "ANONYMOUS";
    }

    private static String errorMessage(Throwable failure) {
        if (failure instanceof ResponseStatusException rse) {
            return rse.getStatusCode().value() + ": " + rse.getReason();
        }
        return failure.getClass().getSimpleName() + ": " + failure.getMessage();
    }
}
