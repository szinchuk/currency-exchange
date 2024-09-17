package com.spribe.currencyexchange.client;

import com.spribe.currencyexchange.dto.ExchangeRateResponse;
import com.spribe.currencyexchange.exception.ExchangeRateException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRatesApiClient {

  private final RestTemplate restTemplate;

  @Value("${application.client.currency.api.key}")
  private String apiKey;

  @Value("${application.client.currency.api.url}")
  private String apiUrl;

  @Value("${application.client.currency.api.format}")
  private String apiFormat;

  public ExchangeRateResponse fetchExchangeRates(String baseCurrency, Set<String> currencies) {
    String url = String.format(apiFormat, apiUrl, apiKey, baseCurrency, String.join(",", currencies));
    log.debug(url);
    try {
      var response = restTemplate.getForObject(url, ExchangeRateResponse.class);
      if (response == null) {
        throw new ExchangeRateException("Failed to fetch exchange rates for " + baseCurrency);
      }
      return parserInThreeLetterFormat(response);
    } catch (Exception e) {
      throw new ExchangeRateException("Failed to fetch exchange rates for " + baseCurrency, e);
    }
  }

  private static ExchangeRateResponse parserInThreeLetterFormat(ExchangeRateResponse response) {
    // Change to a more comfortable format of 3 characters because the source is like this USDEUR, USDPLN
    var iter = response.getRates() != null ? response.getRates().keySet().iterator() : null;
    if (iter != null && iter.hasNext() && iter.next().length() > 3) {
      response.setRates(response.getRates().entrySet().stream()
          .collect(Collectors.toMap(s -> s.getKey().substring(3, 6), Entry::getValue)));
    }
    return response;
  }
}
