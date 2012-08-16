
package dao;

/**
 *
 * @author iws
 */
public interface Row {
    String string(Column col);
    <T> T value(Column<T> col);

    public static final Row NULL = new Row() {


        public String string(Column col) {
            return "";
        }

        public <T> T value(Column<T> col) {
            return null;
        }

        @Override
        public String toString() {
            return "NULL Row";
        }

    };
    public static interface Formatter {
        public static final Formatter DEFAULT = new Formatter() {

            public String string(Row row,
                    Column<?> col) {
                return row.string(col);
            }

            public String display(Row row,
                    Column<?> col) {
                return col.displayName();
            }
        };
        String display(Row row,Column<?> col);
        String string(Row row,Column<?> col);
    }
}
