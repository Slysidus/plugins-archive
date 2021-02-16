package net.lightning.api.util;

import lombok.experimental.UtilityClass;

import java.text.CharacterIterator;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.StringCharacterIterator;
import java.util.Locale;

@UtilityClass
public class StringUtil {

    private final DecimalFormat decimalFormat;

    static {
        decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        decimalFormat.applyPattern("0");
        decimalFormat.setMaximumFractionDigits(2);
    }

    public String decapitalize(String name) {
        if (name != null && name.length() != 0) {
            if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0))) {
                return name;
            }
            else {
                char[] chars = name.toCharArray();
                chars[0] = Character.toLowerCase(chars[0]);
                return new String(chars);
            }
        }
        else {
            return name;
        }
    }

    public String capitalize(String name) {
        if (name != null && name.length() != 0) {
            char[] chars = name.toCharArray();
            chars[0] = Character.toUpperCase(chars[0]);
            return new String(chars);
        }
        else {
            return name;
        }
    }

    public String humanReadableNumber(long number) {
        if (-1000 < number && number < 1000) {
            return String.valueOf(number);
        }

        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (number <= -999_950 || number >= 999_950) {
            number /= 1000;
            ci.next();
        }
        return decimalFormat.format(number / 1000.) + ci.current();
    }

    public String round(double number) {
        return decimalFormat.format(number);
    }

    public String numberToRoman(int number) {
        return String.valueOf(new char[number]).replace('\0', 'I')
                .replace("IIIII", "V")
                .replace("IIII", "IV")
                .replace("VV", "X")
                .replace("VIV", "IX")
                .replace("XXXXX", "L")
                .replace("XXXX", "XL")
                .replace("LL", "C")
                .replace("LXL", "XC")
                .replace("CCCCC", "D")
                .replace("CCCC", "CD")
                .replace("DD", "M")
                .replace("DCD", "CM");
    }

}
