package com.spribe.currencyexchange.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class QuotesDeserializer extends JsonDeserializer<Map<String, BigDecimal>> {

  //When a Map is expected but "quotes": [] is received, it gives an error. This is a fix.
  @Override
  public Map<String, BigDecimal> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    JsonNode node = p.getCodec().readTree(p);
    Map<String, BigDecimal> result = new HashMap<>();

    if (node.isObject()) {
      node.fields().forEachRemaining(entry ->
          result.put(entry.getKey(), new BigDecimal(entry.getValue().asText())));
    } else if (node.isArray() && !node.isEmpty()) {
      node.elements().forEachRemaining(element ->
          element.fields().forEachRemaining(entry ->
              result.put(entry.getKey(), new BigDecimal(entry.getValue().asText()))));
    }
    return result;
  }
}
