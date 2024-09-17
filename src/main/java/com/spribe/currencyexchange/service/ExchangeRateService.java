package com.spribe.currencyexchange.service;

import com.spribe.currencyexchange.cache.ExchangeRateCache;
import com.spribe.currencyexchange.dto.ExchangeRateResponse;
import com.spribe.currencyexchange.exception.ExchangeRateException;
import com.spribe.currencyexchange.repository.CurrencyRepository;
import org.springframework.stereotype.Service;

@Service
public class ExchangeRateService {

  private final ExchangeRateCache exchangeRateCache;
  private final CurrencyRepository currencyRepository;

  public ExchangeRateService(ExchangeRateCache exchangeRateCache, CurrencyRepository currencyRepository) {
    this.exchangeRateCache = exchangeRateCache;
    this.currencyRepository = currencyRepository;
  }

  public ExchangeRateResponse getExchangeRates(String code) {
    if (!currencyRepository.existsByCode(code)) {
      throw new ExchangeRateException("Currency " + code + " not supported");
    }
    return exchangeRateCache.getRates(code);
  }
}
