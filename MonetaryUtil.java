package com.zokudo.sor.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class MonetaryUtil {

    public static double add(final double amountA, final double amountB) {
        final BigDecimal bigDecimalA = new BigDecimal(amountA, MathContext.DECIMAL64);
        final BigDecimal bigDecimalB = new BigDecimal(amountB, MathContext.DECIMAL64);
        return bigDecimalA.add(bigDecimalB).setScale(4, RoundingMode.HALF_EVEN).doubleValue();
    }

    public static double subtract(final double amountA, final double amountB) {
        final BigDecimal bigDecimalA = new BigDecimal(amountA, MathContext.DECIMAL64);
        final BigDecimal bigDecimalB = new BigDecimal(amountB, MathContext.DECIMAL64);
        return bigDecimalA.subtract(bigDecimalB).setScale(4, RoundingMode.HALF_EVEN).doubleValue();
    }

    public static double multiply(final double amountA, final double amountB) {
        final BigDecimal bigDecimalA = new BigDecimal(amountA, MathContext.DECIMAL64);
        final BigDecimal bigDecimalB = new BigDecimal(amountB, MathContext.DECIMAL64);
        return bigDecimalA.multiply(bigDecimalB).setScale(4, RoundingMode.HALF_EVEN).doubleValue();
    }

    public static double percentage(final double amountA, final double amountB) {
        final BigDecimal bigDecimal = new BigDecimal(amountA, MathContext.DECIMAL64);
        final BigDecimal bigDecimalB = new BigDecimal(amountB, MathContext.DECIMAL64);
        return bigDecimal.multiply(bigDecimalB).divide(new BigDecimal(100)).setScale(4, RoundingMode.HALF_EVEN).doubleValue();
    }
}
