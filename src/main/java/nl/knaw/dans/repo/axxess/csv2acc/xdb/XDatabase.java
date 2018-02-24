package nl.knaw.dans.repo.axxess.csv2acc.xdb;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XDatabase extends XDatabaseObject {

    private List<XRelationship> relationships = new ArrayList<>();
    private List<XQuery> queries = new ArrayList<>();
    private List<XTable> tables = new ArrayList<>();

    public List<XRelationship> getRelationships() {
        return relationships;
    }

    public void addRelationship(XRelationship relationship) {
        relationships.add(relationship);
    }

    public List<XQuery> getQueries() {
        return queries;
    }

    public void addQuery(XQuery query) {
        queries.add(query);
    }

    public List<XTable> getTables() {
        return tables;
    }

    public void addTable(XTable table) {
        tables.add(table);
    }


}
