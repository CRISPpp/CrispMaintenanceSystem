package cn.crisp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SuppressWarnings({"all"})
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Address {

    @TableId(value = "id")
    private Long id;

    private Long userId;

    private String name;

    private Integer sex;

    private String phone;

    private String detail;

    private Integer latitude;

    private Integer longitude;

    private Integer isDefault;

    @TableField(fill = FieldFill.INSERT)
    private Integer isDeleted;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    @Version
    private Long version;

    /**
     * 性别枚举
     */
    public static class Sex {
        private Sex() {}

        /**
         * 未知
         */
        public static final Integer UNKNOWN = 0;

        /**
         * 男
         */
        public static final Integer MALE = 1;

        /**
         * 女
         */
        public static final Integer FEMALE = 2;
    }

    /**
     * 是否默认地址
     */
    public static class IsDefault {
        private IsDefault() {}

        /**
         * 否
         */
        public static final Integer NO = 0;

        /**
         * 是
         */
        public static final Integer YES = 1;
    }
}