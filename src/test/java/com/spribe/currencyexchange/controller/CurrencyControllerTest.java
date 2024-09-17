package com.spribe.currencyexchange.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.spribe.currencyexchange.service.CurrencyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

@WebMvcTest(CurrencyController.class)
class CurrencyControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CurrencyService currencyService;

  @Test
  void getAllCurrencies_shouldReturnListOfCurrencies() throws Exception {
    Set<String> currencies = Set.of("USD", "EUR");
    when(currencyService.getAllCurrencies()).thenReturn(currencies);

    mockMvc.perform(get("/v1/api/currencies"))
        .andExpect(status().isOk())
        .andExpect(content().json("[\"USD\",\"EUR\"]"));
  }

  @Test
  void addCurrency_shouldAddNewCurrency() throws Exception {
    String currencyCode = "GBP";
    doNothing().when(currencyService).addCurrency(currencyCode);

    mockMvc.perform(put("/v1/api/currencies")
            .contentType(MediaType.APPLICATION_JSON)
            .content(currencyCode))
        .andExpect(status().isCreated());

    verify(currencyService).addCurrency(currencyCode);
  }
}
