package com.spribe.currencyexchange.scheduled;

import com.spribe.currencyexchange.cache.ExchangeRateCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExchangeRateScheduler {

  private final ExchangeRateCache exchangeRateCache;

  @Scheduled(cron = "${application.fetch.rates.cron}")
  public void getRates() {
    log.info("Scheduled refresh exchange rates updating...");
    exchangeRateCache.refreshRates();
    log.info("Scheduled refresh exchange rates finished...");
  }
}
