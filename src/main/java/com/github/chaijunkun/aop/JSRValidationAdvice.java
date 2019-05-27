package com.github.chaijunkun.aop;

import javax.validation.constraints.NotNull;

import com.github.chaijunkun.entity.Resp;

import org.aspectj.lang.ProceedingJoinPoint;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 * JSR303验证框架统一处理
 * @author chaijunkun
 * @since 2015年4月1日
 */
public class JSRValidationAdvice {

	Logger logger = LoggerFactory.getLogger(JSRValidationAdvice.class);

	/**
	 * 判断验证错误代码是否属于字段为空的情况
	 * @param code 验证错误代码
	 */
	private boolean isMissingParamsError(String code){
		if (code.equals(NotNull.class.getSimpleName()) || code.equals(NotBlank.class.getSimpleName()) || code.equals(NotEmpty.class.getSimpleName())){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 切点处理
	 * @param joinPoint
	 * @return
	 * @throws Throwable
	 */
	public Object aroundMethod(ProceedingJoinPoint joinPoint) throws Throwable {
		BindingResult result = null;
		Object[] args = joinPoint.getArgs();
		if (args != null && args.length != 0){
			for (Object object : args) {
				if (object instanceof BindingResult){
					result = (BindingResult)object;
					break;
				}
			}
		}
		if (result != null && result.hasErrors()){
			FieldError fieldError = result.getFieldError();
			String targetName = joinPoint.getTarget().getClass().getSimpleName();
			String method = joinPoint.getSignature().getName();
			logger.info("验证失败.控制器:{}, 方法:{}, 参数:{}, 属性:{}, 错误:{}, 消息:{}", targetName, method, fieldError.getObjectName(), fieldError.getField(), fieldError.getCode(), fieldError.getDefaultMessage());
			String firstCode = fieldError.getCode();
			if (isMissingParamsError(firstCode)){
				return Resp.fail("必选参数丢失");
			}else{
				return Resp.fail("其他错误");
			}
		}
		return joinPoint.proceed();
	}

}
