package com.example.demoreplicadatasource.datasource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Aspect
@Component
@Order(0)
public class ReadOnlyTransactionInterceptor
{
	@Pointcut(value = "execution(public * *(..))")
	public void anyPublicMethod() {}

	@Around("@annotation(readOnlyConnection)")
	public Object proceed(ProceedingJoinPoint pjp, ReadOnlyConnection readOnlyConnection) throws Throwable
	{
		try
		{
			RoutingDataSource.setReadonlyDataSource(true);
			Object result = pjp.proceed();
			return result;
		}
		finally
		{
			RoutingDataSource.cleanDataSourceType();
		}
	}
}
