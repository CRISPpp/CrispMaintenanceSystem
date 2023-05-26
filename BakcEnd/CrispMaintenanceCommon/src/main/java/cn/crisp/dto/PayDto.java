package cn.crisp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayDto implements Serializable {
    private static final long serialVersionUID = 142353225232L;
    private Long userId;
    private Long engineerId;
    private BigDecimal money;
}
