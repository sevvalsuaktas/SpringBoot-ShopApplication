package com.example.shop.shop.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("@annotation(com.example.shop.shop.logging.Loggable)")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String className  = sig.getDeclaringType().getSimpleName();
        String methodName = sig.getName();

        // Metot öncesi log
        log.info(">> Entering {}.{}({})",
                className, methodName, argsToString(pjp.getArgs()));

        try {
            Object result = pjp.proceed();
            // Metot sonrası log (dönen değeri de yazdırmak istersen)
            log.info("<< Exiting  {}.{} – returned={}",
                    className, methodName, result);
            return result;
        } catch (Throwable ex) {
            log.error("!! Exception in {}.{} – message={}",
                    className, methodName, ex.getMessage(), ex);
            throw ex;
        }
    }

    private String argsToString(Object[] args) {
        if (args == null || args.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (Object a : args) {
            sb.append(a).append(", ");
        }
        return sb.substring(0, sb.length() - 2);
    }
}


