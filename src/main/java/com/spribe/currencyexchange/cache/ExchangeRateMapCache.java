package com.spribe.currencyexchange.cache;

import static java.lang.Boolean.FALSE;

import com.google.common.annotations.VisibleForTesting;
import com.spribe.currencyexchange.client.ExchangeRatesApiClient;
import com.spribe.currencyexchange.dto.ExchangeRateResponse;
import com.spribe.currencyexchange.exception.ExchangeRateException;
import com.spribe.currencyexchange.model.Currency;
import com.spribe.currencyexchange.model.ExchangeRate;
import com.spribe.currencyexchange.repository.CurrencyRepository;
import com.spribe.currencyexchange.repository.ExchangeRateRepository;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Component
public class ExchangeRateMapCache implements ExchangeRateCache {

  private final CurrencyRepository currencyRepository;
  private final ExchangeRateRepository exchangeRateRepository;
  private final ExchangeRatesApiClient exchangeRatesApiClient;
  private final TransactionTemplate transactionTemplate;

  private final Lock reentrantLock;
  private final Map<String, ExchangeRateResponse> cache;
  private final Map<String, Currency> currencyMap;

  @Autowired
  public ExchangeRateMapCache(CurrencyRepository currencyRepository, ExchangeRateRepository exchangeRateRepository,
      ExchangeRatesApiClient exchangeRatesApiClient, TransactionTemplate transactionTemplate
  ) {
    this.currencyRepository = currencyRepository;
    this.exchangeRateRepository = exchangeRateRepository;
    this.exchangeRatesApiClient = exchangeRatesApiClient;
    this.transactionTemplate = transactionTemplate;
    this.reentrantLock = new ReentrantLock();
    this.cache = new ConcurrentHashMap<>();
    this.currencyMap = new ConcurrentHashMap<>();
  }

  @Override
  public Set<String> getCurrencies() {
    return cache.keySet();
  }

  @Override
  public void addCurrency(String code) {
    // It is not entirely clear from the condition whether it is necessary to add a new currency immediately to the cache,
    // I decided to add it. Since I only have the added currency in the output and not all available, I recalculate the whole one.
    log.info("New currency added, rates appear soon: {}.", code);
    refreshRates();
  }

  @Override
  public ExchangeRateResponse getRates(String currency) {
    return cache.get(currency);
  }

  @Override
  public void refreshRates() {
    log.info("Refreshing exchange rates...");

    // limit the cache update to one thread at a same time.
    reentrantLock.lock();
    try {
      var currencyList = currencyRepository.findAll();
      currencyMap.putAll(currencyList.stream().collect(Collectors.toMap(Currency::getCode, c -> c)));
      var currencies = currencyList.stream().map(Currency::getCode).collect(Collectors.toSet());
      Consumer<String> runInTrx = (String currency) -> transactionTemplate.execute(txStatus -> {
        try {
          refreshRatesForCurrency(currency, currencies);
        } catch (Exception e) {
          log.error("Error during scheduled exchange rates update for currency {}", currency, e);
          return false;
        }
        return true;
      });

      // You can run parallelStream if needed
      currencies.forEach(runInTrx);
    } finally {
      currencyMap.clear();
      reentrantLock.unlock();
      log.info("Refreshing exchange rates finished");
    }
  }

  private void refreshRatesForCurrency(String baseCurrency, Set<String> currencies) {
    var now = LocalDateTime.now();
    log.debug("Refreshing rates for currency {}, timestamp {}", baseCurrency, now);

    var exchangeRateResponse = getRatesForCurrency(baseCurrency, currencies);
    var rates = exchangeRateResponse.getRates().entrySet().stream()
        .filter(entry -> currencies.contains(entry.getKey()))
        .map(entry -> ExchangeRate.builder()
            .baseCurrency(currencyMap.get(baseCurrency))
            .currency(currencyMap.get(entry.getKey()))
            .rate(entry.getValue())
            .timestamp(now)
            .build())
        .toList();
    exchangeRateRepository.saveAll(rates);

    cache.put(baseCurrency, exchangeRateResponse);

    log.debug("Rates for currency {} successfully saved", baseCurrency);
  }

  private ExchangeRateResponse getRatesForCurrency(String baseCurrency, Set<String> currencies) {
    log.debug("Get exchange rate for currency {}...", baseCurrency);
    var response = exchangeRatesApiClient.fetchExchangeRates(baseCurrency, currencies);
    if (response == null || FALSE.equals(response.isSuccess())) {
      throw new ExchangeRateException("Could not fetch exchange rate for currency " + baseCurrency);
    }

    log.debug("Received rates for currency {}: {}...", baseCurrency, response.getRates());
    return response;
  }

  @VisibleForTesting
  public void clear() {
    cache.clear();
  }
}
