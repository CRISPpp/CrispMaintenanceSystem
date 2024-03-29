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
     * 地址索引
     */
    public static String ADDRESS_ES_INDEX_NAME = "address";

    /**
     * 用户属性索引
     */
    public static String USER_ATTRIBUTE_ES_INDEX_NAME = "user_attribute";

    /**
     * 工程师属性索引
     */
    public static String ENGINEER_ATTRIBUTE_ES_INDEX_NAME = "engineer_attribute";

    /**
     * 用户锁前缀
     */
    public static String USER_LOCK_NAME = "user_lock:";

    /**
     * 用户属性锁前缀
     */
    public static String USER_ATTRIBUTE_LOCK_NAME = "user_attribute_lock:";

    /**
     * 工程师属性锁前缀
     */
    public static String ENGINEER_ATTRIBUTE_LOCK_NAME = "engineer_attribute_lock:";

    /**
     * 订单锁前缀
     */
    public static String INDENT_LOCK_NAME = "indent_lock:";

    /**
     * 地址锁前缀
     */
    public static String ADDRESS_LOCK_NAME = "address_lock:";

    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 邮箱验证码 redis key
     */
    public static final String VALIDATE_MAIL_KEY = "validate_mail:";

    /**
     * 地址信息
     */
    public static final String ADDRESS_KEY = "address:";

    /**
     * GEO 的 key 名
     */
    public static final String GEO_NAME = "geo";

    /**
     * GEO 中用户放入时的前缀
     */
    public static final String GEO_USER_FIELD_PREFIX = "user:";

    /**
     * GEO 中订单放入时的前缀
     */
    public static final String GEO_INDENT_FIELD_PREFIX = "indent:";

    /**
     * GEO 用于计算订单和维修工程师的距离
     */
    public static final String GEO_DIST_NAME = "geo_dist";

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
