package nl.knaw.dans.repo.axxess;


public class Tester {

    public static void main(String[] args) throws Exception {
        String value = "Foofoo" + "\r\n" + "Bar" + "\r\n" + "Bas";
        System.out.println(value);

        String encoded = value.replaceAll("\r", "\u0000").replaceAll("\n", "\u0001");
        System.out.println(encoded);

        String decoded = encoded.replaceAll("\u0000", "\r").replaceAll("\u0001", "\n");
        System.out.println(decoded);
    }
}
