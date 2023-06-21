package org.metamechanists.death_lasers.utils;

import lombok.Getter;
import org.metamechanists.metalib.utils.ColorUtils;

public enum Colors {
    COUNT("9494B8"),
    POWER("FF0000"),
    FREQUENCY("00ff00"),
    PHASE("FFCC00");

    @Getter
    private final String string;

    Colors(String rawHex) {
        // "#ffffff" -> "&x&f&f&f&f&f&f
        // "#123456" -> "&x&1&2&3&4&5&6
        final StringBuilder colorStringBuilder = new StringBuilder("&x");
        for (char character : rawHex.toCharArray()) {
            colorStringBuilder.append("&").append(character);
        }

        string =  ColorUtils.formatColors(colorStringBuilder.toString());
    }
}