package com.nowcoder.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;


/**
 * AOP 面向切面实例,
 * 注释掉注解即可不被使用
 */
//@Component
//@Aspect
public class AlphaAspect {

    /**
     * 注解@Pointcut中的表达式,描述哪些Bean,哪些方法是要处理的目标
     * 第一个*的位置代表着方法的返回值,*表示什么返回值都行
     * com.nowcoder.community.service 包名
     * .* 表示包下所有类
     * 再.* 表示所有方法
     * (..) 表示所有的参数
     */
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut() {

    }

    /**
     * 在连接点一开始记日志或其他
     * 用@Before注解
     * 针对pointcut()这些连接点有效、切点
     */
    @Before("pointcut()")
    public void before() {
        System.out.println("before");
    }

    /**
     * 在连接点之后记日志或其他
     * 用@After注解
     * 针对pointcut()这些连接点有效
     */
    @After("pointcut()")
    public void after() {
        System.out.println("after");
    }

    /**
     * 在有了返回值以后记日志或其他
     * 用@AfterReturning注解
     * 针对pointcut()这些连接点有效
     */
    @AfterReturning("pointcut()")
    public void afterReturning() {
        System.out.println("afterReturning");
    }

    /**
     * 在抛异常时记日志或其他
     * 用@AfterThrowing注解
     * 针对pointcut()这些连接点有效
     */
    @AfterThrowing("pointcut()")
    public void afterThrowing() {
        System.out.println("afterThrowing");
    }

    /**
     * 既在前面织入逻辑，又想在后面织入逻辑
     * 用@Around
     *
     * @param joinPoint 代码织入的部位
     * @return
     * @throws Throwable
     */
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("around before");
        // 调目标对象被处理的方法逻辑
        Object obj = joinPoint.proceed();
        System.out.println("around after");
        return obj;
    }

}
