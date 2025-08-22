package dn.jasm.configuration.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect(value = "time")
@Component
@Slf4j
public class TimeAspect {

    @Around("@annotation(dn.jasm.configuration.aop.TimeResulting)")
    public Object checkMethodTime(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = pjp.proceed();
        long executionTime = System.currentTimeMillis() - startTime;
        log.info("Method: {} with args: {} was make in time: {}",
                pjp.getSignature().getName(),
                pjp.getArgs(),
                executionTime);
        return result;
    }
}
