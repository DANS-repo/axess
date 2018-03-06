package nl.knaw.dans.repo.axxess;


import java.io.File;

public class Tester {

    public static void main(String[] args) throws Exception {
        System.out.println(new File(".", "bla.txt").getCanonicalPath());
    }
}
