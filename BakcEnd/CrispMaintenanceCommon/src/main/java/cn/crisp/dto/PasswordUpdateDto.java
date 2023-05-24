package cn.crisp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordUpdateDto {
    private static final long serialVersionUID = 142353225231L;
    private Long id;
    private String oldPassword;
    private String newPassword;
}
