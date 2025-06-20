package com.liqi.inventory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.*;

public class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_PATTERN);
    @Override
    public Object stringToValue(String text) throws ParseException {
        return dateFormatter.parse(text);
    }
    @Override
    public String valueToString(Object value) {
        if (value != null) {
            Calendar cal = (Calendar) value;
            return dateFormatter.format(cal.getTime());
        }
        return "";
    }
}