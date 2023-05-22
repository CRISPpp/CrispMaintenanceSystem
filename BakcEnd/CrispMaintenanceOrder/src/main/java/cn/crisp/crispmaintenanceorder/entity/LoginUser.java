package cn.crisp.crispmaintenanceorder.entity;

import cn.crisp.entity.User;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.*;

import java.io.Serializable;
import java.util.Collection;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 442353225232L;
    @Setter
    @Getter private cn.crisp.entity.User User;
    @Setter @Getter private String token;
    @Setter @Getter private Long expireTime;
    @Setter @Getter private Long loginTime;

    public LoginUser(User sysUser){
        this.User = sysUser;
    }

}