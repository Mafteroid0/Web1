package ru.mafteroid.parser.decorator;

import ru.mafteroid.parser.ParameterParser;

abstract class ParserDecorator<T> implements ParameterParser<T> {
    protected final ParameterParser<T> wrappee;

    protected ParserDecorator(ParameterParser<T> wrappee) {
        this.wrappee = wrappee;
    }
}