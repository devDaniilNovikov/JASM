package dn.jasm.configuration.aop;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Before("@annotation(dn.jasm.configuration.aop.Loggable)")
    public void logBefore(JoinPoint joinPoint){
        log.info("Method: {} with args {} start process...",
                joinPoint.getSignature().getName(),
                joinPoint.getArgs());
    }

}


