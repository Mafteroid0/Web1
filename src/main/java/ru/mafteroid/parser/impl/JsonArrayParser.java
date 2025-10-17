package ru.mafteroid.parser.impl;

import ru.mafteroid.parser.ParameterParser;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class JsonArrayParser implements ParameterParser<Set<BigDecimal>> {
    @Override
    public Set<BigDecimal> parse(String jsonBody) {
        String cleanJson = jsonBody.trim()
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "");

        Set<BigDecimal> result = new HashSet<>();
        String[] values = cleanJson.split(",");
        for (String value : values) {
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                try {
                    BigDecimal num = new BigDecimal(trimmed);
                    result.add(num);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Неверный формат: " + trimmed);
                }
            }
        }
        return result;
    }
}