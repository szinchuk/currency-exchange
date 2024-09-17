package com.spribe.currencyexchange.repository;

import com.spribe.currencyexchange.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {

  boolean existsByCode(String code);

}
