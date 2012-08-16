
package dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author iws
 */
public class TableDefinition {
    private final String table;
    private List<Column> columns = new ArrayList<Column>();

    public TableDefinition(String table) {
        this.table = table;
    }
    List<Column> cook() {
        if (columns instanceof ArrayList) {
            columns = Collections.unmodifiableList(columns);
        }
        return columns;
    }

    public List<Column> subset(Column... remove) {
        ArrayList<Column> cols = new ArrayList<Column>(columns);
        for (int i = 0; i < remove.length; i++) {
            cols.remove(remove[i]);
        }
        return cols;
    }

    public Column find(String name) {
        Column found = null;
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).columnName().equals(name)) {
                found = columns.get(i);
                break;
            }
        }
        return found;
    }

    public String table() {
        return table;
    }
    private <T> Column<T> column(String name,String display,Class<T> type) {
        Column<T> column = new Column<T>(name,display,type);
        columns.add(column);
        return column;
    }
    public <T> Column<T> spec(String name, String display, Class<T> type) {
        return column(name, display, type);
    }
    public Column<Integer> integer(String name) {
        return integer(name, computeDisplay(name));
    }
    public Column<Integer> integer(String name, String display) {
        return column(name, display, Integer.class);
    }
    public Column<Double> dbl(String name, String display) {
        return column(name, display, Double.class);
    }
    public Column<BigDecimal> decimal(String name, String display) {
        return column(name, display, BigDecimal.class);
    }
    public Column<BigDecimal> decimal(String name) {
        return decimal(name, computeDisplay(name));
    }
    public Column<String> string(String name, String display) {
        return column(name, display, String.class);
    }
    public Column<String> string(String name) {
        return column(name, computeDisplay(name), String.class);
    }
    public Column<Date> date(String name,String display) {
        return column(name,display,Date.class);
    }
    public Column<Date> date(String name) {
        return date(name,computeDisplay(name));
    }
    public Column<Boolean> bool(String name,String display) {
        return column(name,display,Boolean.class);
    }
    public Column<Boolean> bool(String name) {
        return bool(name,computeDisplay(name));
    }
    public Column<UUID> uuid(String name) {
        return uuid(name,computeDisplay(name));
    }
    public Column<UUID> uuid(String name, String display) {
        return column(name, display, UUID.class);
    }

    public Column<Double> dbl(String name) {
        return column(name,computeDisplay(name),Double.class);
    }

    static String computeDisplay(String colName) {
        StringBuilder sb = new StringBuilder(colName);
        if (colName.indexOf('_') > 0) {
            for (int i = 0; i < sb.length(); i++) {
                if (sb.charAt(i) == '_') {
                    sb.setCharAt(i, ' ');
                    sb.setCharAt(i + 1, Character.toUpperCase(sb.charAt(i + 1)));
                    i++;
                }
            }
        }
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    public static class ColumnNameComparator implements Comparator<Column<?>> {

        public int compare(Column<?> o1,
                Column<?> o2) {
            return o1.columnName().compareTo(o2.columnName());
        }

    }
}
