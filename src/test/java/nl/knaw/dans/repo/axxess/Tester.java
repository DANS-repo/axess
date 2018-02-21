package nl.knaw.dans.repo.axxess;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tester {

    public static void main(String[] args) {

        String key = "(User defined) Replicate Project";
        key = key.replaceAll("\\(.*?\\) ", "");
        System.out.println(key);
    }
}
