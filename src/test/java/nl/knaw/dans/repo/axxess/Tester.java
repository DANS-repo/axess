package nl.knaw.dans.repo.axxess;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tester {

    public static void main(String[] args) {

        String value = "theegoed en \n\r43ander porselein";
        String nv  = value.replaceAll("[\r\n]", "\u0000");

        System.out.println("value: " + value);
        System.out.println("ralue: " + nv);
    }
}
