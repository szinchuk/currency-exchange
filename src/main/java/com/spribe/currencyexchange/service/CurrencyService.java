package com.spribe.currencyexchange.service;

import com.spribe.currencyexchange.cache.ExchangeRateCache;
import java.util.Set;
import com.spribe.currencyexchange.exception.ExchangeRateException;
import com.spribe.currencyexchange.model.Currency;
import com.spribe.currencyexchange.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrencyService {

  private final ExchangeRateCache exchangeRateCache;
  private final CurrencyRepository currencyRepository;

  public Set<String> getAllCurrencies() {
    return exchangeRateCache.getCurrencies();
  }

  @Transactional
  public void addCurrency(String code) {
    if (currencyRepository.existsByCode(code)) {
      throw new ExchangeRateException("Currency already exists");
    }

    currencyRepository.save(Currency.builder().code(code).build());
    exchangeRateCache.addCurrency(code);
  }
}
