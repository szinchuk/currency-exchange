package com.spribe.currencyexchange.controller;

import com.spribe.currencyexchange.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/api/currencies")
@RequiredArgsConstructor
@Tag(name = "Currency", description = "Currency management API")
public class CurrencyController {

  private final CurrencyService currencyService;

  @GetMapping
  @Operation(summary = "Get all currencies", description = "Retrieves a list of all available currencies")
  @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of currencies")
  public Set<String> getAllCurrencies() {
    return currencyService.getAllCurrencies();
  }

  @PutMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Add a new currency", description = "Adds a new currency to the system")
  @ApiResponse(responseCode = "201", description = "Currency successfully added")
  public void addCurrency(
      @Parameter(description = "Currency code to add", required = true, example = "USD")
      @RequestBody String currencyCode) {
    currencyService.addCurrency(currencyCode.toUpperCase());
  }
}
