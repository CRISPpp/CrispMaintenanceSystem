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

import java.io.Serializable;
import java.time.LocalDateTime;

@SuppressWarnings({"all"})
@AllArgsConstructor
@NoArgsConstructor
@Data
public class IndentImage implements Serializable {
    private static final long serialVersionUID = 442353225234L;


    @TableId(value = "id")
    private Long id;

    private Long indentId;

    private String icon;

    private Integer type;

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
     * 维修图片类型枚举
     */
    public static class Type {
        private Type() {}

        /**
         * 维修前
         */
        public static final Integer PRE_PROCESS = 1;

        /**
         * 维修后
         */
        public static final Integer POST_PROCESS = 2;
    }

}