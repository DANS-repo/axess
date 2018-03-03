package nl.knaw.dans.repo.axxess;


import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.TableBuilder;
import org.apache.commons.csv.CSVFormat;

import java.io.File;

public class Tester {

    public static void main(String[] args) throws Exception {
        for (Database.FileFormat format : Database.FileFormat.values()) {
            System.out.print(" | " + format);
        }

        System.out.println();
        System.out.println(Database.FileFormat.valueOf("V1997"));

    }
}
