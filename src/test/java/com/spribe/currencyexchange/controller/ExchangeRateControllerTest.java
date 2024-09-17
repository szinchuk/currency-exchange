package com.spribe.currencyexchange.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.spribe.currencyexchange.dto.ExchangeRateResponse;
import com.spribe.currencyexchange.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExchangeRateController.class)
class ExchangeRateControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ExchangeRateService exchangeRateService;

  @Test
  void getExchangeRates_shouldReturnExchangeRates() throws Exception {
    String currencyCode = "USD";
    ExchangeRateResponse response = ExchangeRateResponse.builder().build();
    when(exchangeRateService.getExchangeRates(currencyCode)).thenReturn(response);

    mockMvc.perform(get("/v1/api/exchange-rate/{code}", currencyCode))
        .andExpect(status().isOk());

    verify(exchangeRateService).getExchangeRates(currencyCode);
  }
}
