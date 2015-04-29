package pgdiagram;

import java.util.ArrayList;
import java.util.List;

public class Column {
    public String name;
    public String colType;    
    public long colTypeLen;
    public int order;
    public boolean isNotNull;
    public List<String> distinctValues = new ArrayList();
    public Table table;
    public long actualDistinctCount;
    
    public Column(Table table) {
        this.table = table;
    }
}
