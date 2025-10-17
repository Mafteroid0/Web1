package ru.mafteroid.parser.impl;

import ru.mafteroid.parser.ParameterParser;

import java.util.LinkedHashMap;

public class QueryStringParser implements ParameterParser<LinkedHashMap<String, String>> {
    @Override
    public LinkedHashMap<String, String> parse(String queryString) {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        String[] pairs = queryString.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }
        return params;
    }
}
