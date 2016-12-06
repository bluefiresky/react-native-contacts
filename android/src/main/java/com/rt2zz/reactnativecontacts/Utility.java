package com.rt2zz.reactnativecontacts;

public class Utility {
/**
 *     过滤字符串中的特殊字符
 * **/
    public static String filterStr(String text){
        if (text == null || "".equals(text))
            return text;
        return text.replaceAll("[\\n\\r\\v\\f\\s `~!@#$%^&*|{}':\\[\\]【】]", "");
    }

    public static String filterSpace(String text){
        if (text == null || "".equals(text))
            return text;
        return text.replaceAll(" ", "");
    }
}
