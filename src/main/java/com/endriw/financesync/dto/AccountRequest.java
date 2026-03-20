package com.endriw.financesync.dto;

import com.endriw.financesync.model.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountRequest {

    @NotBlank
    private String accountNumber;

    private String agency;

    @NotBlank
    private String bankName;

    @NotNull
    private AccountType type;

}
