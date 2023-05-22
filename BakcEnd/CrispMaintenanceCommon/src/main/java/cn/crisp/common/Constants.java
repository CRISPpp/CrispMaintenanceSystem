package cn.crisp.common;

public class Constants {
    /**
     * 用户表es对应index
     */
    public static String USER_ES_INDEX_NAME = "user";
    /**
     * 订单索引
     */
    public static String INDENT_ES_INDEX_NAME = "indent";

    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 删除
     */
    public static final Character DELETED = '1';

    public static final Character NOT_DELETED = '0';

    public static final String UTF8 = "UTF-8";

    public static final String GBK = "GBK";

    /**
     * 令牌
     */
    public static final String TOKEN = "token";

    /**
     * 令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 令牌前缀
     */
    public static final String LOGIN_USER_KEY = "login_user_key";

    /**
     * 用户操作前缀
     */
    public static final String USER_OPERATION_TIME_KEY = "user_operation_time_key:";

    /**
     * 验证码前缀
     */
    public static final String PHONE_LOGIN_VALIDATE_KEY = "phone_login_validate_key:";
    public static final String PHONE_REGISTER_VALIDATE_KEY = "phone_register_validate_key:";
    public static final String PHONE_PUT_VALIDATE_KEY = "phone_put_validate_key";

    /**
     * 注册前缀
     */
    public static final String PHONE_REGISTER_FRE_KEY = "phone_register_fre_key:";

    /**
     * 登录前缀
     */
    public static final String PHONE_LOGIN_FRE_KEY = "phone_login_fre_key:";

    /**
     * 一天限制三次匹配
     */
    public static final Integer LIMIT = 3;

    /**
     * VIP可以匹配的次数
     */
    public static final Integer VIP_LIMIT = 10;
}
