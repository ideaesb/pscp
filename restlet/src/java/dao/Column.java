
package dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;

/**
 *
 * @author iws
 */
public final class Column<T> {

    private final String column;
    private final String display;
    private final Class<T> type;
    private final int sqlType;

    public Column(String column,String display,Class<T> type) {
        this.column = column;
        this.display = display;
        this.type = type;
        this.sqlType = resolveSQLType(type);
    }

    public Column(String column,Class<T> type) {
        this.column = column;
        this.display = column;
        this.type = type;
        this.sqlType = resolveSQLType(type);
    }

    static int resolveSQLType(Class type) {
        int sqlType = Types.OTHER;
        if (type == Integer.class) {
            sqlType = Types.INTEGER;
        } else if (type == String.class) {
            sqlType = Types.VARCHAR;
        } else if (type == Date.class) {
            sqlType = Types.TIMESTAMP;
        } else if (type == Boolean.class) {
            sqlType = Types.BOOLEAN;
        } else if (type == Double.class) {
            sqlType = Types.DOUBLE;
        }
        return sqlType;
    }

    public T parse(String val) {
        T parsed;
        if (type == Integer.class) {
            parsed = (T) new Integer(val);
        } else if (type == Double.class) {
            parsed = (T) new Double(val);
        } else if (type == Boolean.class) {
            val = val.toLowerCase();
            if ("y".equals(val)) {
                parsed = (T) Boolean.TRUE;
            } else if ("n".equals(val)) {
                parsed = (T) Boolean.FALSE;
            } else if ("true".equals(val)) {
                parsed = (T) Boolean.TRUE;
            } else if ("false".equals(val)) {
                parsed = (T) Boolean.FALSE;
            } else {
                throw new IllegalArgumentException("Cannot parse " + val);
            }
        } else if (type == String.class) {
            parsed = (T) val;
        } else if (type == BigDecimal.class) {
            parsed = (T) new BigDecimal(val);
        }
        else {
            throw new UnsupportedOperationException(type.getSimpleName() + " column cannot parse");
        }
        return parsed;
    }

    public Pair<T> parsePair(String val) {
        return pair(parse(val));
    }

    public Pair<T> pair(T val) {
        return Pair.of(this,val);
    }

    public Pair<T> pairNull() {
        return Pair.of(this, (T) null);
    }

    public Pair<T> pairNotNull(T val) {
        return val == null ? null : Pair.of(this,val);
    }

    public Pair<T> useDefault() {
        return Pair.useDefaultOf(this);
    }

    public String columnName() {
        return column;
    }

    public String displayName() {
        return display;
    }

    public Class<T> type() {
        return type;
    }

    public int sqlType() {
        return sqlType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Column<T> other = (Column<T>) obj;
        if ((this.column == null) ? (other.column != null) : !this.column.equals(other.column)) {
            return false;
        }
        if (this.type != other.type && (this.type == null || !this.type.equals(other.type))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.column != null ? this.column.hashCode() : 0);
        hash = 29 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return column + " " + type.getSimpleName();
    }

}
