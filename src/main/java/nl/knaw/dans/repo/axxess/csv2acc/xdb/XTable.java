package nl.knaw.dans.repo.axxess.csv2acc.xdb;

import nl.knaw.dans.repo.axxess.core.Axxess;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class XTable extends XDatabaseObject {

    private List<XColumn> columns = new ArrayList<>();
    private List<XIndex> indexes = new ArrayList<>();

    public List<XColumn> getColumns() {
        return columns;
    }

    public List<XIndex> getIndexes() {
        return indexes;
    }

    public void addColumn(XColumn column) {
        columns.add(column);
    }

    public void addIndex(XIndex index) {
        indexes.add(index);
    }

    public String getPrimaryKeyIndexName() {
        return getString(Axxess.TABLE_PRIMARY_KEY_INDEX);
    }

    public Optional<XIndex> getPrimaryKeyIndex() {
        return indexes
          .stream()
          .filter(xIndex -> xIndex.getBool(Axxess.I_IS_PRIMARY_KEY))
          .findFirst();
    }


}
