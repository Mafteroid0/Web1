package ru.mafteroid.validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

public class Validator {
    private Set<BigDecimal> validX = Set.of(
            new BigDecimal("-4"), new BigDecimal("-3"), new BigDecimal("-2"),
            new BigDecimal("-1"), BigDecimal.ZERO, BigDecimal.ONE,
            new BigDecimal("2"), new BigDecimal("3"), new BigDecimal("4")
    );
    private BigDecimal minY = new BigDecimal("-5");
    private BigDecimal maxY = new BigDecimal("3");
    private BigDecimal minR = new BigDecimal("1");
    private BigDecimal maxR = new BigDecimal("4");

    public void setValidX(Set<BigDecimal> validX) {
        this.validX = new HashSet<>(validX);
    }

    public void setValidYRange(BigDecimal min, BigDecimal max) {
        this.minY = min;
        this.maxY = max;
    }

    public void setValidRRange(BigDecimal min, BigDecimal max) {
        this.minR = min;
        this.maxR = max;
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
            BigDecimal x = new BigDecimal(xStr);

            BigDecimal normalizedX = x.stripTrailingZeros();

            for (BigDecimal validValue : validX) {
                if (normalizedX.compareTo(validValue.stripTrailingZeros()) == 0) {
                    return null;
                }
            }

            return "Недопустимое значение X (" + xStr + "). Допустимые: " + validX;
        } catch (NumberFormatException e) {
            return "Неверный формат X";
        } catch (Exception e) {
            return "Ошибка обработки X";
        }
    }

    public String validateY(String yStr) {
        try {
            BigDecimal y = new BigDecimal(yStr).setScale(10, RoundingMode.HALF_UP);
            if (y.compareTo(minY) < 0 || y.compareTo(maxY) > 0) {
                return String.format("Y должен быть от %s до %s", minY, maxY);
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
            if (r.compareTo(minR) < 0 || r.compareTo(maxR) > 0) {
                return String.format("R должен быть от %s до %s", minR, maxR);
            }
            return null;
        } catch (NumberFormatException e) {
            return "Неверный формат R";
        } catch (Exception e) {
            return "Ошибка обработки R";
        }
    }

    public Set<BigDecimal> getValidX() {
        return new HashSet<>(validX);
    }

    public BigDecimal[] getValidYRange() {
        return new BigDecimal[]{minY, maxY};
    }

    public BigDecimal[] getValidRRange() {
        return new BigDecimal[]{minR, maxR};
    }


    public float[] getValidYRangeAsFloats() {
        return new float[]{minY.floatValue(), maxY.floatValue()};
    }

    public float[] getValidRRangeAsFloats() {
        return new float[]{minR.floatValue(), maxR.floatValue()};
    }
}