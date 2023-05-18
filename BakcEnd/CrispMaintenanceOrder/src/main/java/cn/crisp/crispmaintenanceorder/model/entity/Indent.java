package cn.crisp.crispmaintenanceorder.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class Indent {

    @TableId(value = "id")
    private Long id;

    private Long userId;

    private Long engineerId;

    private String name;

    private Integer sex;

    private String phone;

    private String addressDetail;

    private Integer latitude;

    private Integer longitude;

    private String problem;

    private String remark;

    private BigDecimal cost;

    private BigDecimal quality;

    private Integer status;

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
     * 订单状态
     */
    public static class Status {
        private Status() {}

        /**
         * 待处理
         */
        public static final Integer UNPROCESSED = 1;

        /**
         * 处理中
         */
        public static final Integer PROCESSING = 2;

        /**
         * 待支付
         */
        public static final Integer UNPAID = 3;

        /**
         * 待评价
         */
        public static final Integer UNEVALUATED = 4;

        /**
         * 已完成
         */
        public static final Integer COMPLETED = 5;

    }

}