package cn.crisp.exception;

@SuppressWarnings({"all"})
/**
 * 未登录异常
 */
public class NotLoginException extends RuntimeException {
    private Integer code;

    public NotLoginException() {
        this.code = 401;
    }

    public NotLoginException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public NotLoginException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

}