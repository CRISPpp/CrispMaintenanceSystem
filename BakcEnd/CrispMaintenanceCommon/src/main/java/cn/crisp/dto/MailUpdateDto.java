package cn.crisp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailUpdateDto implements Serializable {
    private static final long serialVersionUID = 142353225230L;
    private Long id;
    private String mail;
    private String code;
}
