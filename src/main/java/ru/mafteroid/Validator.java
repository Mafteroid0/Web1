package ru.mafteroid;

import java.util.LinkedList;
import java.util.List;

public class Validator {

    private final List<Float> xStorage = new LinkedList<Float>();
    private final List<Float> rStorage = new LinkedList<Float>();

    public Validator() {
        xStorage.add(-2F);
        xStorage.add(-1.5F);
        xStorage.add(-1F);
        xStorage.add(-0.5F);
        xStorage.add(0F);
        xStorage.add(0.5F);
        xStorage.add(1F);
        xStorage.add(1.5F);
        xStorage.add(2F);
        rStorage.add(1F);
        rStorage.add(1.5F);
        rStorage.add(2F);
        rStorage.add(2.5F);
        rStorage.add(3F);
    }

    public boolean validation(float x,float y,float r) {
        return (checkX(x))&&(checkY(y))&&(checkR(r));
    }

    public boolean checkX(float x) {
        return xStorage.contains(x);
    }

    public boolean checkR(float r) {
        return rStorage.contains(r);
    }

    public boolean checkY(float y) {
        return (-5F <= y) && (y <= 5F);
    }
}