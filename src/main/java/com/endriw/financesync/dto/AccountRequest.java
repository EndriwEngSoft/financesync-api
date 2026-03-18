package com.endriw.financesync.dto;

import com.endriw.financesync.model.enums.AccountType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountRequest {

    private String accountNumber;
    private String agency;
    private String bankName;

    private AccountType type;

}
