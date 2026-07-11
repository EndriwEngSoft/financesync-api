package com.endriw.financesync.client;

import com.endriw.financesync.dto.integration.CurrencyConversionResponse;
import com.endriw.financesync.exception.ExternalServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class FrankfurterClient {

    private final RestClient restClient;

    public FrankfurterClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public CurrencyConversionResponse getCurrencyConversion(String base, String quote) {
        try {
            return restClient.get()
                    .uri("/rate/{base}/{quote}", base, quote)
                    .retrieve()
                    .body(CurrencyConversionResponse.class);
        } catch (RestClientException e) {
            throw new ExternalServiceException(
                    "Failed to fetch currency conversion rate from Frankfurter API", e);
        }
    }
}

