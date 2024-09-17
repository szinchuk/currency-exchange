package com.spribe.currencyexchange.cache;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spribe.currencyexchange.client.ExchangeRatesApiClient;
import com.spribe.currencyexchange.dto.ExchangeRateResponse;
import com.spribe.currencyexchange.model.Currency;
import com.spribe.currencyexchange.repository.CurrencyRepository;
import com.spribe.currencyexchange.repository.ExchangeRateRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class ExchangeRateMapCacheTest {

  @Mock
  private CurrencyRepository currencyRepository;

  @Mock
  private ExchangeRateRepository exchangeRateRepository;

  @Mock
  private ExchangeRatesApiClient exchangeRatesApiClient;

  @Mock
  private TransactionTemplate transactionTemplate;

  private ExchangeRateMapCache exchangeRateMapCache;

  @BeforeEach
  void setUp() {
    exchangeRateMapCache = new ExchangeRateMapCache(
        currencyRepository,
        exchangeRateRepository,
        exchangeRatesApiClient,
        transactionTemplate
    );

    // Setting up TransactionTemplate to execute the passed callback
    lenient().when(transactionTemplate.execute(any()))
        .thenAnswer(invocation -> invocation.getArgument(0, TransactionCallback.class).doInTransaction(null));
  }

  @Test
  void getCurrencies_shouldReturnEmptySet_whenCacheIsEmpty() {
    Set<String> currencies = exchangeRateMapCache.getCurrencies();
    assertTrue(currencies.isEmpty());
  }

  @Test
  void getCurrencies_shouldReturnCachedCurrencies() {
    Currency usd = new Currency("USD");
    when(currencyRepository.findAll()).thenReturn(Collections.singletonList(usd));

    ExchangeRateResponse response = new ExchangeRateResponse();
    response.setSuccess(true);
    response.setRates(Map.of("EUR", BigDecimal.ONE));
    when(exchangeRatesApiClient.fetchExchangeRates(eq("USD"), any())).thenReturn(response);

    exchangeRateMapCache.refreshRates();

    Set<String> currencies = exchangeRateMapCache.getCurrencies();
    assertEquals(1, currencies.size());
    assertTrue(currencies.contains("USD"));
  }

  @Test
  void addCurrency_shouldRefreshRates() {
    Currency eur = new Currency("EUR");
    when(currencyRepository.findAll()).thenReturn(Collections.singletonList(eur));

    ExchangeRateResponse response = new ExchangeRateResponse();
    response.setSuccess(true);
    response.setRates(Map.of("USD", BigDecimal.ONE));
    when(exchangeRatesApiClient.fetchExchangeRates(eq("EUR"), any())).thenReturn(response);

    exchangeRateMapCache.addCurrency("EUR");

    verify(currencyRepository).findAll();
    verify(exchangeRatesApiClient).fetchExchangeRates(eq("EUR"), any());
  }

  @Test
  void addCurrency_shouldLogInfo() {
    when(currencyRepository.findAll()).thenReturn(Collections.emptyList());

    assertDoesNotThrow(() -> exchangeRateMapCache.addCurrency("JPY"));
  }

  @Test
  void getRates_shouldReturnNull_whenCurrencyNotInCache() {
    ExchangeRateResponse result = exchangeRateMapCache.getRates("NON_EXISTENT");
    assertNull(result);
  }

  @Test
  void getRates_shouldReturnCachedResponse() {
    Currency usd = new Currency("USD");
    when(currencyRepository.findAll()).thenReturn(Collections.singletonList(usd));

    ExchangeRateResponse expectedResponse = new ExchangeRateResponse();
    expectedResponse.setSuccess(true);
    expectedResponse.setRates(Map.of("EUR", BigDecimal.valueOf(0.85)));
    when(exchangeRatesApiClient.fetchExchangeRates(eq("USD"), any())).thenReturn(expectedResponse);

    exchangeRateMapCache.refreshRates();
    ExchangeRateResponse result = exchangeRateMapCache.getRates("USD");

    assertEquals(expectedResponse, result);
  }

  @Test
  void refreshRates_shouldUpdateCache() {
    Currency usd = new Currency("USD");
    Currency eur = new Currency("EUR");
    when(currencyRepository.findAll()).thenReturn(Arrays.asList(usd, eur));

    ExchangeRateResponse usdResponse = new ExchangeRateResponse();
    usdResponse.setSuccess(true);
    usdResponse.setRates(Map.of("EUR", BigDecimal.valueOf(0.85)));

    ExchangeRateResponse eurResponse = new ExchangeRateResponse();
    eurResponse.setSuccess(true);
    eurResponse.setRates(Map.of("USD", BigDecimal.valueOf(1.18)));

    when(exchangeRatesApiClient.fetchExchangeRates(eq("USD"), any())).thenReturn(usdResponse);
    when(exchangeRatesApiClient.fetchExchangeRates(eq("EUR"), any())).thenReturn(eurResponse);

    exchangeRateMapCache.refreshRates();

    assertEquals(usdResponse, exchangeRateMapCache.getRates("USD"));
    assertEquals(eurResponse, exchangeRateMapCache.getRates("EUR"));
  }

  @Test
  void refreshRates_shouldHandleExceptionGracefully() {
    Currency usd = new Currency("USD");
    when(currencyRepository.findAll()).thenReturn(Collections.singletonList(usd));
    when(exchangeRatesApiClient.fetchExchangeRates(eq("USD"), any())).thenThrow(new RuntimeException("API Error"));

    assertDoesNotThrow(() -> exchangeRateMapCache.refreshRates());
  }

  @Test
  void complexTest_cacheOperations() {
    Currency usd = new Currency("USD");
    Currency eur = new Currency("EUR");
    when(currencyRepository.findAll()).thenReturn(Arrays.asList(usd, eur));

    ExchangeRateResponse usdResponse = new ExchangeRateResponse();
    usdResponse.setSuccess(true);
    usdResponse.setRates(Map.of("EUR", BigDecimal.valueOf(0.85)));

    ExchangeRateResponse eurResponse = new ExchangeRateResponse();
    eurResponse.setSuccess(true);
    eurResponse.setRates(Map.of("USD", BigDecimal.valueOf(1.18)));

    when(exchangeRatesApiClient.fetchExchangeRates(eq("USD"), any())).thenReturn(usdResponse);
    when(exchangeRatesApiClient.fetchExchangeRates(eq("EUR"), any())).thenReturn(eurResponse);

    assertTrue(exchangeRateMapCache.getCurrencies().isEmpty());

    exchangeRateMapCache.refreshRates();

    Set<String> currencies = exchangeRateMapCache.getCurrencies();
    assertEquals(2, currencies.size());
    assertTrue(currencies.containsAll(Arrays.asList("USD", "EUR")));

    assertEquals(usdResponse, exchangeRateMapCache.getRates("USD"));
    assertEquals(eurResponse, exchangeRateMapCache.getRates("EUR"));

    exchangeRateMapCache.addCurrency("JPY");
    verify(currencyRepository, atLeast(2)).findAll();
  }

  @Test
  void complexTest_concurrentOperations() throws InterruptedException {
    Currency usd = new Currency("USD");
    Currency eur = new Currency("EUR");
    when(currencyRepository.findAll()).thenReturn(Arrays.asList(usd, eur));

    ExchangeRateResponse usdResponse = new ExchangeRateResponse();
    usdResponse.setSuccess(true);
    usdResponse.setRates(Map.of("EUR", BigDecimal.valueOf(0.85)));

    ExchangeRateResponse eurResponse = new ExchangeRateResponse();
    eurResponse.setSuccess(true);
    eurResponse.setRates(Map.of("USD", BigDecimal.valueOf(1.18)));

    when(exchangeRatesApiClient.fetchExchangeRates(anyString(), any())).thenReturn(usdResponse, eurResponse);

    // Simulation of parallel operations
    int threadCount = 10;
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          exchangeRateMapCache.refreshRates();
          exchangeRateMapCache.getRates("USD");
          exchangeRateMapCache.getRates("EUR");
          exchangeRateMapCache.addCurrency("JPY");
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await(10, TimeUnit.SECONDS);

    verify(currencyRepository, atLeast(threadCount)).findAll();
    verify(exchangeRatesApiClient, atLeast(threadCount * 2)).fetchExchangeRates(anyString(), any());

    Set<String> currencies = exchangeRateMapCache.getCurrencies();
    assertTrue(currencies.containsAll(Arrays.asList("USD", "EUR")));

    executorService.shutdown();
  }
}
