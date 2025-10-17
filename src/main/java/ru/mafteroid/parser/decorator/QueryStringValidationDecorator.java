package ru.mafteroid.parser.decorator;

import ru.mafteroid.parser.ParameterParser;

import java.util.LinkedHashMap;

public class QueryStringValidationDecorator extends ParserDecorator<LinkedHashMap<String, String>> {
    public QueryStringValidationDecorator(ParameterParser<LinkedHashMap<String, String>> wrappee) {
        super(wrappee);
    }

    @Override
    public LinkedHashMap<String, String> parse(String input) {
        LinkedHashMap<String, String> params = wrappee.parse(input);

        if (params.size() != 3) {
            throw new IllegalArgumentException("Должно быть только 3 параметра (x, y, r)");
        }

        if (!params.containsKey("x") || !params.containsKey("y") || !params.containsKey("r")) {
            throw new IllegalArgumentException("Не хватает необходимых параметров: x, y, r");
        }

        return params;
    }
}