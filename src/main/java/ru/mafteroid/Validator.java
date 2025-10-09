package ru.mafteroid;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;

public class Validator {

    private final List<Integer> xStorage = new LinkedList<>();

    public Validator() {
        xStorage.add(-4);
        xStorage.add(-3);
        xStorage.add(-2);
        xStorage.add(-1);
        xStorage.add(0);
        xStorage.add(1);
        xStorage.add(2);
        xStorage.add(3);
        xStorage.add(4);
    }

    public String validate(String xStr, String yStr, String rStr) {
        String xError = validateX(xStr);
        if (xError != null) return xError;

        String yError = validateY(yStr);
        if (yError != null) return yError;

        String rError = validateR(rStr);
        if (rError != null) return rError;

        return null;
    }

    public String validateX(String xStr) {
        try {
            if (xStorage.contains(Integer.parseInt(xStr))){
                return null;
            }
        } catch (NumberFormatException e) {
            return "Неверный формат X";
        } catch (Exception e) {
            return "Ошибка обработки X";
        }
        return "Что-то пошло не так";
    }

    public String validateY(String yStr) {
        try {
            BigDecimal y = new BigDecimal(yStr).setScale(10, RoundingMode.HALF_UP);
            BigDecimal minY = new BigDecimal("-5");
            BigDecimal maxY = new BigDecimal("3");
            if (y.compareTo(minY) < 0 || y.compareTo(maxY) > 0) {
                return "Y должен быть от -5 до 3";
            }
            return null;
        } catch (NumberFormatException e) {
            return "Неверный формат Y";
        } catch (Exception e) {
            return "Ошибка обработки Y";
        }
    }

    public String validateR(String rStr) {
        try {
            BigDecimal r = new BigDecimal(rStr).setScale(10, RoundingMode.HALF_UP);
            BigDecimal minR = new BigDecimal("1");
            BigDecimal maxR = new BigDecimal("4");
            if (r.compareTo(minR) < 0 || r.compareTo(maxR) > 0) {
                return "R должен быть от 1 до 4";
            }
            return null;
        } catch (NumberFormatException e) {
            return "Неверный формат R";
        } catch (Exception e) {
            return "Ошибка обработки R";
        }
    }
}