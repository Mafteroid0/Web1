package ru.mafteroid.parser.decorator;

import ru.mafteroid.parser.ParameterParser;
import java.math.BigDecimal;
import java.util.Set;

public class JsonArrayValidationDecorator extends ParserDecorator<Set<BigDecimal>> {
    public JsonArrayValidationDecorator(ParameterParser<Set<BigDecimal>> wrappee) {
        super(wrappee);
    }

    @Override
    public Set<BigDecimal> parse(String input) {
        Set<BigDecimal> values = wrappee.parse(input);

        if (values.isEmpty()) {
            throw new IllegalArgumentException("JSON не должен быть пуст");
        }

//        if (values.size() > 20) {
//            throw new IllegalArgumentException("Слишком много циферок");
//        }

        return values;
    }
}