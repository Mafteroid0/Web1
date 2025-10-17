package ru.mafteroid.parser.decorator;

import ru.mafteroid.parser.ParameterParser;

import java.util.LinkedHashMap;

public class InjectionCheckDecorator extends ParserDecorator<LinkedHashMap<String, String>> {
    public InjectionCheckDecorator(ParameterParser<LinkedHashMap<String, String>> wrappee) {
        super(wrappee);
    }

    @Override
    public LinkedHashMap<String, String> parse(String input) {
        if (input.contains(";") || input.contains("'") || input.contains("\"")) {
            throw new IllegalArgumentException("Пожалуйста не ломайте");
        }
        return wrappee.parse(input);
    }
}