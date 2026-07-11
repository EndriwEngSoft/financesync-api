package com.endriw.financesync.dto.integration;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CurrencyConversionResponse(

        LocalDate date,
        String base,
        String quote,
        BigDecimal rate

) {}
