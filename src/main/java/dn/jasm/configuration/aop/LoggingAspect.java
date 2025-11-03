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
                joinPoint.getSignature().getName(), joinPoint.getArgs());
    }

//    @AfterReturning(value = "@annotation(dn.jasm.configuration.aop.Loggable)",returning = "result")
//    public void logAfterReturning(JoinPoint joinPoint,Object result){
//        log.info("Method: {} with args {} return value: {}...",
//                joinPoint.getSignature().getName().toUpperCase(),
//                joinPoint.getArgs(),
//                result);
//    }
//
//    @Around("@annotation(dn.jasm.configuration.aop.Loggable)")
//    public Object logMethodTime(ProceedingJoinPoint proceedingJoinPoint){
//        long startTime = System.currentTimeMillis();
//        Object result = proceedingJoinPoint.getArgs();
//        long endTime = System.currentTimeMillis()-startTime;
//        Object[] args = proceedingJoinPoint.getArgs();
//        String signature = proceedingJoinPoint.getSignature().getName();
//        log.info("Method: {} with args: {} ,proceeding uptime is: {}",signature,args,endTime);
//        return result;
//
//    }

}


