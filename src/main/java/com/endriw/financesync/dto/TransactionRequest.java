package com.endriw.financesync.dto;

import com.endriw.financesync.model.enums.PaymentMethod;
import com.endriw.financesync.model.enums.TransactionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class TransactionRequest {

    private BigDecimal fee;
    private BigDecimal amount;
    private Long accountId;
    private Long categoryId;
    private TransactionType type;
    private PaymentMethod paymentMethod;
    private String description;

}
