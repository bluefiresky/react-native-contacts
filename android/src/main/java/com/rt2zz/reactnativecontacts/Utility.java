package com.rt2zz.reactnativecontacts;

public class Utility {
/**
 *     过滤字符串中的特殊字符
 * **/
    public static String filterStr(String text){
        if (text == null || "".equals(text))
            return text;

        return text.replaceAll("[\\[\\]\\{\\}]", "").replaceAll("[`~!@#$%^&*|':【】]", "").replaceAll("[\"\\n\\r\\f\\t]", "");
    }

    public static String filterSpace(String text){
        if (text == null || "".equals(text))
            return text;
        return text.replaceAll(" ", "");
    }
}
