package nl.knaw.dans.repo.axxess;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tester {

    public static void main(String[] args) {

        String filename = "AccWebFAQ.MDB.___metadata.csv";
        String name = filename.replaceAll("\\.[_]*metadata.csv", ".accdb");

        System.out.println(name);
    }
}
