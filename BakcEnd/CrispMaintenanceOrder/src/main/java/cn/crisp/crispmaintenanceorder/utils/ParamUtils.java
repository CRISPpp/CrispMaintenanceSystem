package cn.crisp.crispmaintenanceorder.utils;

import cn.crisp.exception.BusinessException;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

@SuppressWarnings({"all"})
public class ParamUtils {
    /**
     * 自定义函数式接口
     */
    @FunctionalInterface
    public static interface FieldFunction<T> extends Serializable {
        Object apply(T t);

        /**
         * 获取 SerializedLambda 对象
         */
        default SerializedLambda getSerializedLambda() {
            try {
                Method method = this.getClass().getDeclaredMethod("writeReplace");
                method.setAccessible(true);
                return (SerializedLambda) method.invoke(this);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * 获取 Lambda 表达式的实现方法，例如 User::getId 的 getId
         */
        default String getImplMethodName() {
            SerializedLambda serializedLambda = getSerializedLambda();
            return serializedLambda == null ? null : serializedLambda.getImplMethodName();
        }

        /**
         * 获取Lambda表达式的字段，例如 User::getId 的 id
         */
        default String getField() {
            String implMethodName = getImplMethodName();
            return implMethodName == null ? null : StringUtils.fieldOf(implMethodName);
        }
    }

    /**
     * 字段为null或者空串，抛出异常ParamBlankException
     */
    public static<T> void checkFieldNotBlank(T item, FieldFunction<T>... functions) {
        for (FieldFunction<T> function : functions) {
            if (StringUtils.isBlank(function.apply(item))) {
                String field = function.getField();
                throw new BusinessException(
                        0,
                        "必传字段" + (field == null ? "" : field) + "为null或空串"
                );
            }
        }
    }

    /**
     * 字段为null或者空串，抛出异常ParamBlankException
     */
    public static<T> void checkFieldNotNull(T item, FieldFunction<T>... functions) {
        for (FieldFunction<T> function : functions) {
            if (function.apply(item) == null) {
                String field = function.getField();
                throw new BusinessException(0, "必传字段" + (field == null ? "" : field) + "为null");
            }
        }
    }

}