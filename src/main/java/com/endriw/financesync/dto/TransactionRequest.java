package com.endriw.financesync.dto;

import com.endriw.financesync.model.enums.PaymentMethod;
import com.endriw.financesync.model.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class TransactionRequest {

    private BigDecimal fee;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private Long accountId;
    @NotNull
    private Long categoryId;

    @NotNull
    private TransactionType type;

    private PaymentMethod paymentMethod;
    private String description;

    @NotNull
    @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter ISO 4217 code (e.g. BRL, USD)")
    private String currency;

}
