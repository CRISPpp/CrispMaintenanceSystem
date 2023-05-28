package cn.crisp.crispmaintenanceorder.config;


import cn.crisp.common.CODE;
import cn.crisp.common.R;
import cn.crisp.exception.BusinessException;
import cn.crisp.exception.NotLoginException;
import cn.crisp.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ProjectExceptionHandler {
    @ExceptionHandler(SystemException.class)
    public R<String> doSystemException(SystemException exception){
        //记录日志
        log.info("SystemError");
        //发消息给运维

        //发邮件给开发人员

        return R.errorWithCode(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public R<String> doBusinessException(BusinessException exception){
        //记录日志
        log.info("BusinessError");
        //发消息给运维,让他把ip封了


        return R.errorWithCode(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public R<String> doNotLoginException(NotLoginException exception){
        //记录日志
        log.info("NotLoginException");
        R r = new R();
        r.setCode(401);
        r.setMsg("没有权限，请登录");
        return r;
    }

    @ExceptionHandler(Exception.class)
    public R<String> OtherException(Exception exception){
        exception.printStackTrace();
        //记录日志
        log.info("gg");
        //发消息给运维

        //发邮件给开发人员

        return R.errorWithCode(CODE.UNKNOWN_ERROR, "让立勋儿子把日志找出来发给张博父亲");
    }
}
