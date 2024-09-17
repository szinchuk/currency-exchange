package com.spribe.currencyexchange.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spribe.currencyexchange.cache.ExchangeRateMapCache;
import com.spribe.currencyexchange.client.ExchangeRatesApiClient;
import com.spribe.currencyexchange.dto.ExchangeRateResponse;
import com.spribe.currencyexchange.repository.CurrencyRepository;
import com.spribe.currencyexchange.repository.ExchangeRateRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

public class ControllerIntegrationTest extends BaseItEx {

  @Autowired
  private ExchangeRateRepository exchangeRateRepository;

  @Autowired
  private CurrencyRepository currencyRepository;

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ExchangeRatesApiClient exchangeRatesApiClient;

  @Autowired
  private ExchangeRateMapCache exchangeRateCache;

  @BeforeEach
  void setUp() {
    // Clearing the database and cache before each test
    exchangeRateRepository.deleteAll();
    currencyRepository.deleteAll();
    exchangeRateCache.clear();

    // Setting up a mock for ExchangeRatesApiClient
    ExchangeRateResponse usdResponse = new ExchangeRateResponse();
    usdResponse.setSuccess(true);
    Map<String, BigDecimal> usdRates = new HashMap<>();
    usdRates.put("EUR", BigDecimal.valueOf(0.85));
    usdRates.put("GBP", BigDecimal.valueOf(0.75));
    usdResponse.setRates(usdRates);

    when(exchangeRatesApiClient.fetchExchangeRates(eq("USD"), any())).thenReturn(usdResponse);
  }

  @Test
  public void testGetAllCurrencies_EmptyList() throws Exception {
    mockMvc.perform(get(BASE_URL_CURRENCIES))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));
  }

  @Test
  public void testAddCurrency_ThenGetAll() throws Exception {
    // Adding currency
    mockMvc.perform(put(BASE_URL_CURRENCIES)
            .contentType(MediaType.APPLICATION_JSON)
            .content("USD"))
        .andExpect(status().isCreated());

    // Check that the currency has been added
    mockMvc.perform(get(BASE_URL_CURRENCIES))
        .andExpect(status().isOk())
        .andExpect(content().json("[\"USD\"]"));
  }

  @Test
  public void testGetExchangeRates_Success() throws Exception {
    // First, add the currency
    mockMvc.perform(put(BASE_URL_CURRENCIES)
            .contentType(MediaType.APPLICATION_JSON)
            .content("USD"))
        .andExpect(status().isCreated());

    // Then we request the courses
    mockMvc.perform(get(BASE_URL_EXCHANGE_RATE + "/USD"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.quotes.EUR").value(0.85))
        .andExpect(jsonPath("$.quotes.GBP").value(0.75));
  }
}
