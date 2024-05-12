package dec.core.model.utils.validater;

import java.io.Serializable;

public enum ErrorType implements Serializable{
	COMMON,//提示性错误，业务并未进行
	CRITICAL_ROLLBACK,//业务执行中，出现异常，且需事务回滚
	CRITICAL_NOROLLBACK,//业务执行中，出现异常，无需事务回滚
	SECURITY_DATA_AUTH_ERROR,//数据权限
	SECURITY_REQ_AUTH_ERROR,//请求认证失败
	SECURITY_BIZ_AUTH_ERROR//业务执行无权限(二级审核)
}