package nl.knaw.dans.repo.axxess;


import java.util.regex.Pattern;

public class Tester {

    public static void main(String[] args) throws Exception {
        Pattern digitPattern = Pattern.compile("d?\\d+");

        System.out.println(Pattern.matches("[0-9]*", "2312345"));

        System.out.println(new Long(123456L));
        System.out.println(Long.valueOf("123456"));
        long x = 12345L;
        Object y = x;
        System.out.println(y instanceof Long);
    }
}
