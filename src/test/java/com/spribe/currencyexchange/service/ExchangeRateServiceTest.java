package com.spribe.currencyexchange.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spribe.currencyexchange.cache.ExchangeRateCache;
import com.spribe.currencyexchange.dto.ExchangeRateResponse;
import com.spribe.currencyexchange.exception.ExchangeRateException;
import com.spribe.currencyexchange.repository.CurrencyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

  @Mock
  private ExchangeRateCache exchangeRateCache;

  @Mock
  private CurrencyRepository currencyRepository;

  @InjectMocks
  private ExchangeRateService exchangeRateService;

  @Test
  void getExchangeRates_shouldReturnRatesFromCache() {
    String currencyCode = "USD";
    ExchangeRateResponse expectedResponse = ExchangeRateResponse.builder().build();
    when(currencyRepository.existsByCode(currencyCode)).thenReturn(true);
    when(exchangeRateCache.getRates(currencyCode)).thenReturn(expectedResponse);

    ExchangeRateResponse result = exchangeRateService.getExchangeRates(currencyCode);

    assertEquals(expectedResponse, result);
    verify(exchangeRateCache).getRates(currencyCode);
  }

  @Test
  void getExchangeRates_shouldThrowExceptionWhenCurrencyNotSupported() {
    String currencyCode = "USD";
    when(currencyRepository.existsByCode(currencyCode)).thenReturn(false);

    assertThrows(ExchangeRateException.class, () -> exchangeRateService.getExchangeRates(currencyCode));
  }
}
