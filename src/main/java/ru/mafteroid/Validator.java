package ru.mafteroid;

import java.util.LinkedList;
import java.util.List;

public class Validator {

    private final List<Float> xStorage = new LinkedList<Float>();

    public Validator() {
        xStorage.add(-4F);
        xStorage.add(-3F);
        xStorage.add(-2F);
        xStorage.add(-1F);
        xStorage.add(0F);
        xStorage.add(1F);
        xStorage.add(2F);
        xStorage.add(3F);
        xStorage.add(4F);
    }

    public boolean validation(float x,float y,float r) {
        return (checkX(x))&&(checkY(y))&&(checkR(r));
    }

    public boolean checkX(float x) {
        return xStorage.contains(x);
    }

    public boolean checkY(float y) {

        return (-5F <= y) && (y <= 3F);
    }

    public boolean checkR(float r) {
        return (1F <= r) && (r <= 4F);
    }


}