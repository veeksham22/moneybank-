package com.progressive.banking.moneytransfer.aspect;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Arrays;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private static final String CORRELATION_ID = "correlationId";
    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    private static final List<String> SENSITIVE_KEYS =
            List.of("password", "secret", "token", "authorization", "idempotencyKey", "otp", "pin");

    // ✅ Controller + Service pointcuts (adjust packages as needed)
    @Pointcut("within(com.progressive.banking.moneytransfer..controller..*)")
    public void controllerLayer() {}

    @Pointcut("within(com.progressive.banking.moneytransfer..service..*)")
    public void serviceLayer() {}

    @Around("controllerLayer() || serviceLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        ensureCorrelationId();

        String cid = MDC.get(CORRELATION_ID);
        String signature = joinPoint.getSignature().toShortString();
        String request = requestSummary(); // null if no web request (service-to-service, tests, etc.)

        long startNs = System.nanoTime();

        try {
            // ENTRY
            if (request != null) log.info("[{}] --> {} | {}", cid, signature, request);
            else log.info("[{}] --> {}", cid, signature);

            // Args at DEBUG
            if (log.isDebugEnabled()) {
                String args = formatArgs(joinPoint.getArgs());
                if (!args.isBlank()) log.debug("[{}]     args={}", cid, args);
            }

            Object result = joinPoint.proceed();

            // EXIT
            long timeMs = (System.nanoTime() - startNs) / 1_000_000;
            log.info("[{}] <-- {} | timeMs={}", cid, signature, timeMs);

            // Response at DEBUG
            if (log.isDebugEnabled()) {
                String res = summarizeResponse(result);
                if (!res.isBlank()) log.debug("[{}]     response={}", cid, res);
            }

            return result;

        } catch (Throwable ex) {
            long timeMs = (System.nanoTime() - startNs) / 1_000_000;
            log.error("[{}] xx  {} | timeMs={} | ex={} : {}",
                    cid, signature, timeMs,
                    ex.getClass().getSimpleName(), ex.getMessage(), ex);
            throw ex;

        } finally {
            // ✅ Prevent correlationId leaking to the next request on same thread
            MDC.remove(CORRELATION_ID);
        }
    }

    private void ensureCorrelationId() {
        if (MDC.get(CORRELATION_ID) != null) return;

        HttpServletRequest req = currentRequest();
        String incoming = (req != null) ? req.getHeader(CORRELATION_HEADER) : null;

        String cid = (incoming != null && !incoming.isBlank())
                ? incoming
                : UUID.randomUUID().toString();

        MDC.put(CORRELATION_ID, cid);
    }

    private String requestSummary() {
        HttpServletRequest req = currentRequest();
        if (req == null) return null;

        String query = req.getQueryString();
        return req.getMethod() + " " + req.getRequestURI() + (query == null ? "" : "?" + query);
    }

    private HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getRequest();
        }
        return null;
    }

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) return "";

        return Arrays.stream(args)
                .filter(a -> a != null)
                .filter(a -> !isInfra(a))
                .map(this::safeMaskedToString)
                .collect(Collectors.joining(", "));
    }

    private boolean isInfra(Object arg) {
        // ✅ Skip servlet request/response & typical web infrastructure objects
        if (arg instanceof ServletRequest || arg instanceof ServletResponse) return true;

        String name = arg.getClass().getName();
        return name.startsWith("org.springframework.web.")
                || name.startsWith("org.springframework.validation.")
                || name.startsWith("jakarta.servlet.")
                || name.startsWith("javax.servlet.");
    }

    private String safeMaskedToString(Object obj) {
        try {
            return maskSensitive(String.valueOf(obj));
        } catch (Exception e) {
            return "<unprintable>";
        }
    }

    private String summarizeResponse(Object result) {
        if (result == null) return "";

        try {
            if (result instanceof ResponseEntity<?> re) {
                String body = re.getBody() == null ? "null" : re.getBody().toString();
                return "status=" + re.getStatusCode().value()
                        + ", body=" + trim(maskSensitive(body), 700);
            }
            return trim(maskSensitive(result.toString()), 700);

        } catch (Exception e) {
            return "<unprintable-response>";
        }
    }

    private String trim(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, max) + "...(truncated)";
    }

    private String maskSensitive(String input) {
        if (input == null || input.isBlank()) return input;

        String masked = input;
        for (String key : SENSITIVE_KEYS) {
            // Matches: key=VALUE or key: VALUE (case-insensitive)
            String pattern = "(?i)(" + Pattern.quote(key) + "\\s*[=:]\\s*)([^,}\\]]+)";
            masked = masked.replaceAll(pattern, "$1****");
        }
        return masked;
    }
}