package com.spribe.currencyexchange.cache;

import com.spribe.currencyexchange.dto.ExchangeRateResponse;
import java.util.Set;

/**
 * For test task purposes used java.util.Map.
 * We can implement another solution(Redis?) and replace it in the service constructor.
 */
public interface ExchangeRateCache {

  Set<String> getCurrencies();

  void addCurrency(String currency);

  ExchangeRateResponse getRates(String currency);

  void refreshRates();
}
