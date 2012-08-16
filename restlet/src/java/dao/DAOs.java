package dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author iws
 */
class DAOs {
    private static final Map<Class<? extends DAO>, DAOHolder> instances = new WeakHashMap<Class<? extends DAO>, DAOHolder>();

    private static class DAOHolder {
        DAOHolder(DAO dao) {
            this.dao = dao;
            this.columns = cook(dao);
        }
        private final DAO dao;
        private final List<Column> columns;
    }

    static DAO instance(Class<? extends DAO> spec) {
        return holder(spec).dao;
    }

    static List<Column> cook(DAO dao) {
        Field field;
        Class spec = dao.getClass();
        while (spec != null) {
            if (spec.getSuperclass() == DAO.class) {
                break;
            }
            spec = spec.getSuperclass();
        }
        try {
            field = spec.getDeclaredField("columns");
            field.setAccessible(true);
            TableDefinition cols = (TableDefinition) field.get(null);
            dao.setTableDefinition(cols);
            return cols.cook();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Error("Invalid concrete DAO " + dao.getClass().getName(),ex);
        }
    }

    private static DAOHolder holder(Class<? extends DAO> spec) {
        DAOHolder holder = instances.get(spec);
        if (holder == null) {
            try {
                instances.put(spec, holder = new DAOHolder(spec.newInstance()));
            } catch (InstantiationException ex) {
                Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return holder;
    }

    static List<Column> columns(Class<? extends DAO> spec) {
        return holder(spec).columns;
    }

    private static List<Column<?>> computeColumns(Class<? extends DAO> spec) {
        Field[] fields = spec.getFields();
        ArrayList<Column<?>> cols = new ArrayList<Column<?>>();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (Column.class.isAssignableFrom(f.getType())) {
                try {
                    cols.add((Column) f.get(null));
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(DAOs.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(DAOs.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        cols.trimToSize();
        return Collections.unmodifiableList(cols);
    }
}
