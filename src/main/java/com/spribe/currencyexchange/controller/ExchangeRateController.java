package com.spribe.currencyexchange.controller;

import com.spribe.currencyexchange.dto.ExchangeRateResponse;
import com.spribe.currencyexchange.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/api/exchange-rate")
@RequiredArgsConstructor
@Tag(name = "Exchange Rate", description = "Exchange rate operations API")
public class ExchangeRateController {

  private final ExchangeRateService exchangeRateService;

  @GetMapping("/{code}")
  @Operation(summary = "Get exchange rates", description = "Retrieves exchange rates for a specific currency")
  @ApiResponse(responseCode = "200", description = "Successfully retrieved exchange rates")
  public ExchangeRateResponse getExchangeRates(@Parameter(description = "Currency code", required = true, example = "USD")
  @PathVariable String code) {
    return exchangeRateService.getExchangeRates(code.toUpperCase());
  }

}
