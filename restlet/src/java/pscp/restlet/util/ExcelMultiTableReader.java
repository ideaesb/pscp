package pscp.restlet.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

/**
 *
 * @author iws
 */
public class ExcelMultiTableReader {

    private List<TableEntry> tables;
    private Map<String, String> metaData;
    private int dataRowStart;

    public class TableEntry {
        public final String name;
        public final List<String> columns;
        public final ArrayList<String[]> data;
        private final int startColumn;
        TableEntry(String name,int startColumn) {
            this.name = name;
            this.startColumn = startColumn;
            this.columns = new ArrayList<String>();
            this.data = new ArrayList<String[]>();
        }
        public String resolveExcelCellName(int row,int col) {
            StringBuilder cellName = new StringBuilder();
            int realcol = startColumn + col;
            if (realcol > 25) {
                cellName.append((char) (64 + (realcol / 25)));
            }
            cellName.append((char) (65 + (realcol % 26)));
            cellName.append(dataRowStart + row + 1);
            return cellName.toString();
        }
        public int resolveExcelRow(int row) {
            return dataRowStart + row + 1;
        }
    }

    public ExcelMultiTableReader() {
        metaData = new HashMap<String, String>();
        tables = new ArrayList<TableEntry>();
    }

    public TableEntry getTable(String name) {
        for (int i = 0; i < tables.size(); i++) {
            if (name.equalsIgnoreCase(tables.get(i).name)) {
                return tables.get(i);
            }
        }
        return null;
    }

    public void read(Sheet sheet,boolean singleTable) throws Exception {
        int rows = sheet.getRows();
        int row = 0;
        for (; row < rows; row++) {
            Cell metaName = sheet.getCell(0, row);
            if (metaName.getContents().trim().length() == 0) {
                break;
            }
            Cell metaValue = sheet.getCell(1, row);
            metaData.put(metaName.getContents().trim(), metaValue.getContents().trim());
        }

        Cell[] tableRow = sheet.getRow(++row);
        Cell[] columnRow = sheet.getRow(++row);
        TableEntry table = null;
        for (int i = 0; i < columnRow.length; i++) {
            Cell column = columnRow[i];
            if (!singleTable && column.getContents().trim().length() == 0) {
                table = null;
                continue;
            }
            if (table == null) {
                String tableName = tableRow[i].getContents().trim();
                table = new TableEntry(tableName, i);
                tables.add(table);
            }
            table.columns.add(column.getContents().toString());
        }
        row++;
        dataRowStart = row;
        for (; row < rows; row++) {
            for (int i = 0; i < tables.size(); i++) {
                TableEntry t = tables.get(i);
                String[] data = new String[t.columns.size()];
                boolean allMissing = true;
                for (int j = 0; j < data.length; j++) {
                    int col = t.startColumn + j;
                    Cell cell = sheet.getCell(col, row);
                    data[j] = cell.getContents();
                    allMissing &= data[j] == null || data[j].trim().length() == 0;
                }
                if (!allMissing) {
                    t.data.add(data);
                } else {
                    break;
                }
            }
        }
    }

    public void read(File f,boolean singleTable) throws Exception {
        Workbook workbook = Workbook.getWorkbook(f);
        for (Sheet s: workbook.getSheets()) {
            read(s,true);
        }
    }

    public List<TableEntry> getEntries() {
        return tables;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    static void test(String f) throws Exception {
        ExcelMultiTableReader r = new ExcelMultiTableReader();
        r.read(new File(f),true);
        for (int i = 0; i < r.tables.size(); i++) {
            TableEntry e = r.tables.get(i);
            System.out.println("table : " + e.name);
            System.out.println("columns : " + e.columns);
            for (String[] d: e.data) {
                System.out.println(Arrays.toString(d));
            }
            System.out.println(e.resolveExcelCellName(0, 0));
            System.out.println(e.resolveExcelCellName(0, 3));
            System.out.println(e.resolveExcelCellName(0, 4));
            System.out.println(e.resolveExcelCellName(0, 5));
            System.out.println("-------------");
            System.out.println("");
        }
    }

    public static void main(String[] args) throws Exception {
        test(args[0]);
    }
}
