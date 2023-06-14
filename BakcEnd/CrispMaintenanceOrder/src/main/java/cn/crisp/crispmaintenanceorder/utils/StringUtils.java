package cn.crisp.crispmaintenanceorder.utils;

import org.springframework.util.DigestUtils;

import javax.mail.event.MailEvent;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"all"})
public class StringUtils {
    /**
     * @param obj
     * @return obj为null或者空串返回true，其他情况返回false
     */
    public static boolean isBlank(Object obj) {
        if (obj == null) {
            return true;
        } else if (obj instanceof String) {
            return "".equals(obj);
        } else {
            return false;
        }
    }

    /**
     * @param getter
     * @return 返回getter对应的字段名，例如getId返回id
     */
    public static String fieldOf(String getter) {
        if (getter.matches("get.+")) {
            return (getter.substring(3, 4).toLowerCase() + getter.substring(4)).intern();
        } else {
            throw new RuntimeException("该字符串不符合getter的命名规范");
        }
    }

    /**
     * 生成uuid
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 为了提高安全性，将initStr和salt拼接后再md5加密，salt是随机生成的字符串
     * @param initStr 要加密的字符串
     * @param salt 要拼接的字符串
     */
    public static String md5(String initStr, String salt) {
        //md5加密结果是长度固定的二进制流，md5DigestAsHex将结果以十六进制的方式读取，例如 5362a4011932ba01f345546485d767fd
        return DigestUtils.md5DigestAsHex((initStr + salt).getBytes());
    }

    /**
     * 获取文件的后缀名
     */
    public static String suffix(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * 获取url中的文件名，例如从
     * http://localhost:8080/common/download/5b252bdd-903f-42e3-92c4-c0acb65439e1.jpg
     * 截取出5b252bdd-903f-42e3-92c4-c0acb65439e1.jpg
     */
    public static String fileNameOfUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1).intern();
    }


}