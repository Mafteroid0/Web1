package ru.mafteroid.parser.impl;

import ru.mafteroid.parser.ParameterParser;
import java.math.BigDecimal;

public class JsonRangeParser implements ParameterParser<BigDecimal[]> {
    @Override
    public BigDecimal[] parse(String jsonBody) {
        String cleanJson = jsonBody.trim()
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "");

        String[] values = cleanJson.split(",");
        if (values.length != 2) {
            throw new IllegalArgumentException("Ожидается 2 параметра [min, max]");
        }

        try {
            BigDecimal min = new BigDecimal(values[0].trim());
            BigDecimal max = new BigDecimal(values[1].trim());
            return new BigDecimal[]{min, max};
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Неверный формат чисел");
        }
    }
}