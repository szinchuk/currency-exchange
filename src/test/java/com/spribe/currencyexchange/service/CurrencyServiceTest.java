package com.spribe.currencyexchange.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spribe.currencyexchange.cache.ExchangeRateCache;
import com.spribe.currencyexchange.exception.ExchangeRateException;
import com.spribe.currencyexchange.model.Currency;
import com.spribe.currencyexchange.repository.CurrencyRepository;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

  @Mock
  private ExchangeRateCache exchangeRateCache;

  @Mock
  private CurrencyRepository currencyRepository;

  @InjectMocks
  private CurrencyService currencyService;

  @Test
  void getAllCurrencies_shouldReturnCurrenciesFromCache() {
    Set<String> expectedCurrencies = Set.of("USD", "EUR");
    when(exchangeRateCache.getCurrencies()).thenReturn(expectedCurrencies);

    Set<String> result = currencyService.getAllCurrencies();

    assertEquals(expectedCurrencies, result);
    verify(exchangeRateCache).getCurrencies();
  }

  @Test
  void addCurrency_shouldSaveCurrencyAndUpdateCache() {
    String currencyCode = "GBP";
    when(currencyRepository.existsByCode(currencyCode)).thenReturn(false);

    currencyService.addCurrency(currencyCode);

    verify(currencyRepository).save(any(Currency.class));
    verify(exchangeRateCache).addCurrency(currencyCode);
  }

  @Test
  void addCurrency_shouldThrowExceptionWhenCurrencyExists() {
    String currencyCode = "USD";
    when(currencyRepository.existsByCode(currencyCode)).thenReturn(true);

    assertThrows(ExchangeRateException.class, () -> currencyService.addCurrency(currencyCode));
  }
}
