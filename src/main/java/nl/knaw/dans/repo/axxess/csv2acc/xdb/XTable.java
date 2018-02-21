package nl.knaw.dans.repo.axxess.csv2acc.xdb;

import java.util.ArrayList;
import java.util.List;

public class XTable extends XDatabaseObject {

    private List<XColumn> columns = new ArrayList<>();

    public List<XColumn> getColumns() {
        return columns;
    }

    public void addColumn(XColumn column) {
        columns.add(column);
    }
}
