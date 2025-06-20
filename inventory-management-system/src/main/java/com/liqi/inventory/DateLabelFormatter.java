package com.liqi.inventory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.*;

public class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_PATTERN);
    @Override
    //文字列をDateに変換
    public Object stringToValue(String text) throws ParseException {
        return dateFormatter.parse(text);
    }
    @Override
    //Dateを表示用の文字列に変換
    public String valueToString(Object value) {
        if (value != null) {
            Calendar cal = (Calendar) value;
            return dateFormatter.format(cal.getTime());
        }
        return "";//null空文字列
    }
}
