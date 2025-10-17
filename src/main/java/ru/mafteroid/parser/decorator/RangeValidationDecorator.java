package ru.mafteroid.parser.decorator;

import ru.mafteroid.parser.ParameterParser;
import java.math.BigDecimal;

public class RangeValidationDecorator extends ParserDecorator<BigDecimal[]> {
    public RangeValidationDecorator(ParameterParser<BigDecimal[]> wrappee) {
        super(wrappee);
    }

    @Override
    public BigDecimal[] parse(String input) {
        BigDecimal[] range = wrappee.parse(input);

        BigDecimal min = range[0];
        BigDecimal max = range[1];

        if (min.compareTo(max) >= 0) {
            throw new IllegalArgumentException("Минимальное значение не должно превышать максимального");
        }

        return range;
    }
}