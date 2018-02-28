package nl.knaw.dans.repo.axxess.core;

public enum ObjectType {

    EXTRACTION_METADATA("EM"),
    DATABASE("DB"),
    RELATIONSHIP("R"),
    QUERY("Q"),
    TABLE("T"),
    INDEX("X"),
    COLUMN("C"),
    TABLE_INDEX("TX"),
    TABLE_COLUMN("TC");

    public String abrreviation;

    ObjectType(String abbreviation) {
        this.abrreviation = abbreviation;
    }

    public String prefix(int... indexes) {
        if (this == EXTRACTION_METADATA) {
            return "[EM]";
        } else if (this == DATABASE) {
            return "[DB]";
        } else if (this == TABLE_INDEX) {
            return String.format("[T%d][X%d]", indexes[0], indexes[1]);
        } else if (this == TABLE_COLUMN) {
            return String.format("[T%d][C%d]", indexes[0], indexes[1]);
        } else {
            return String.format("[%s%d]", abrreviation, indexes[0]);
        }
    }

    public String pattern() {
        if (this == EXTRACTION_METADATA) {
            return "\\[EM\\]";
        } else if (this == DATABASE) {
            return "\\[DB\\]";
        } else if (this == TABLE_INDEX) {
            return "\\[T[0-9]*\\]\\[X[0-9]*\\]";
        } else if (this == TABLE_COLUMN) {
            return "\\[T[0-9]*\\]\\[C[0-9]*\\]";
        } else {
            return "\\[" + abrreviation + "[0-9]*\\]";
        }
    }


}
