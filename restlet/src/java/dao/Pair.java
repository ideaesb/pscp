package dao;

/**
 *
 * @author iws
 */
public class Pair<T> {

    private static final Object USE_DEFAULT = new Object();
    private final Column<T> col;
    private final Object value;

    public static <X> Pair<X> useDefaultOf(Column<X> col) {
        return new Pair(col,USE_DEFAULT,true);
    }

    public static <X> Pair<X> of(Column<X> col, X value) {
        return new Pair(col, value);
    }

    private Pair(Column<T> col,Object value,boolean special) {
        this.col = col;
        this.value = value;
    }

    private Pair(Column<T> col, T value) {
        this.col = col;
        this.value = value;
    }

    public boolean useDefaultValue() {
        return value == USE_DEFAULT;
    }

    public Column column() {
        return col;
    }

    public Object value() {
        return value;
    }

    public String toString() {
        return col.columnName() + " : " + stringValue(value);
    }

    private String stringValue(Object value) {
        return value == USE_DEFAULT ? "<DEFAULT>" : String.valueOf(value);
    }
}
