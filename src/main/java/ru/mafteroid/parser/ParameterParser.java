package ru.mafteroid.parser;

public interface ParameterParser<T> {
    T parse(String queryString);
}
