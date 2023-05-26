package cn.crisp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto implements Serializable {
    private static final long serialVersionUID = 442353225239L;
    private String phone;
    private String password;
    private Integer role;
}
