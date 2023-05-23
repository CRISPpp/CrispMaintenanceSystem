package cn.crisp.oss.crispmaintenanceoss.entity;

import cn.crisp.entity.User;
import lombok.*;

import java.io.Serializable;

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