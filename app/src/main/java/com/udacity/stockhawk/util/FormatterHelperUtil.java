package com.udacity.stockhawk.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Helper Utility to format currency and digits
 *
 * Created by stefanie on 19.04.17.
 */

public class FormatterHelperUtil {
    private static FormatterHelperUtil formatter;

    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat dollarFormat;
    private final DecimalFormat percentageFormat;
    private final DecimalFormat decimalFormat;

    private FormatterHelperUtil(){
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        dollarFormatWithPlus.setNegativePrefix("-$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
        decimalFormat = new DecimalFormat("##.00");
    }

    public static FormatterHelperUtil getInstance(){
        if(formatter == null){
            formatter = new FormatterHelperUtil();
        }
        return formatter;
    }

    /**
     * Formats as a dollar value
     *
     * @param value
     * @return
     */
    public String formatDollarValue(float value){
        return dollarFormat.format(value);
    }

    /**
     * Formats as a dollar value with leading + or leading -
     * @param value
     * @return
     */
    public String formatDollarValueWithPlus(float value){
        return dollarFormatWithPlus.format(value);
    }

    /**
     * Formats the value as percent, with leading prefix and exactly two decimal places after comma
     * @param value
     * @return
     */
    public String formatPercentageValue(float value){
        return percentageFormat.format(value);
    }

    /**
     * Formats a value as a decimal with exactly two decimal places after comma
     * @param value
     * @return
     */
    public String formatDecimalValue(float value){
        return decimalFormat.format(value);
    }

}
