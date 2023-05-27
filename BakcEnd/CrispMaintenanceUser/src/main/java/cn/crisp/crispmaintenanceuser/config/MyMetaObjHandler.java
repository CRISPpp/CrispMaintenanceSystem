package cn.crisp.crispmaintenanceuser.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class MyMetaObjHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
        this.strictInsertFill(metaObject, "version", Long.class, 1L);
        this.strictInsertFill(metaObject, "role", Integer.class, 1);
        this.strictInsertFill(metaObject, "icon", String.class, "http://43.139.14.96:11000/crisp/sage.jpg");
        this.strictInsertFill(metaObject, "isDefault", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject,"updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "balance", BigDecimal.class, new BigDecimal("0.0"));
        this.strictUpdateFill(metaObject, "quality", BigDecimal.class, new BigDecimal("5.0"));

    }
}
