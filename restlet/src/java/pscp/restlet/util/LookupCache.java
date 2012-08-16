package pscp.restlet.util;

import dao.DAO;
import dao.DAOCollection;
import dao.DAOException;
import dao.DAOFactory;
import dao.Row;
import dao.RowIterator;
import dao.Rows;
import dao.pscp.Contacts;
import dao.pscp.DataSets;
import dao.pscp.Nations;
import dao.pscp.Operators;
import dao.pscp.ProductTypes;
import dao.pscp.ProductNames;
import dao.pscp.Regions;
import dao.pscp.Stations;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author iws
 */
public class LookupCache {

    private final DAOFactory factory;
    private List<Item> items = new ArrayList<Item>();

    public LookupCache(DAOFactory factory) {
        this.factory = factory;
        STATION_IDS.dependencies.add(OPERATORS);
    }

    public static <V,K> Map<V,K> reverse(Map<K,V> m) {
        HashMap reversed = new HashMap<V,K>();
        for (K k : m.keySet()) {
            if (reversed.put(m.get(k),k) != null) {
                throw new RuntimeException("This cannot be reversed");
            }
        }
        return reversed;
    }

    public void refreshAll() {
        for (Item item: items) {
            item.reload();
        }
    }

    public void daoModified(Class<? extends DAO> dao) {
        for (Item i: items) {
            if (i.daoDeps.contains(dao)) {
                i.change();
            }
        }
    }

    public Item<Map<Integer,String>> STATION_IDS = new Item<Map<Integer,String>>(Stations.class) {

        @Override
        protected Map<Integer, String> load(DAOCollection daos) throws DAOException {
            RowIterator rows = daos.get(Stations.class).readStations(null, null);
            HashMap<Integer,String> lookup = new HashMap<Integer,String>();
            Map<Integer,String> operators = OPERATORS.getValue();
            while (rows.hasNext()) {
                StringBuilder sb = new StringBuilder();
                Row row = rows.next();
                sb.append(row.string(Stations.STATIONNAME)).append('-');
                sb.append(row.string(Stations.PROCESS)).append('-');
                sb.append(row.string(Stations.NATION)).append('-');
                sb.append(operators.get(row.value(Stations.ONUM)));
                lookup.put(row.value(Stations.STATIONID),sb.toString());
            }
            return lookup;
        }

    };

    public Item<Map<String,String>> REGION_DISPLAY = new Item<Map<String,String>>(Regions.class) {

        @Override
        protected Map<String, String> load(DAOCollection daos) throws DAOException {
            return Rows.rowLookup(daos.get(Regions.class).readRegions(), Regions.CODE, Regions.NAME);
        }
    };

    public Item<Map<String,String>> NATION_DISPLAY = new Item<Map<String,String>>(Nations.class) {

        @Override
        protected Map<String, String> load(DAOCollection daos) throws DAOException {
            return Rows.rowLookup(daos.get(Nations.class).read(), Nations.ISOCODE, Nations.COUNTRYNAME);
        }
    };

    public Item<Map<String, Row>> CONTACTS = new Item<Map<String, Row>>(Contacts.class) {

        @Override
        protected Map<String, Row> load(DAOCollection daos) throws DAOException {
            return Rows.rowLookupString(daos.get(Contacts.class).readContacts(),Contacts.CONTACTID);
        }
    };
    public Item<Map<Integer, String>> OPERATORS = new Item<Map<Integer, String>>(Operators.class) {

        @Override
        protected Map<Integer, String> load(DAOCollection daos) throws DAOException {
            return daos.get(Operators.class).readLookup();
        }
    };
    public Item<Map<Integer, String>> PRODUCT_TYPES = new Item<Map<Integer, String>>(ProductNames.class) {

        @Override
        protected Map<Integer, String> load(DAOCollection daos) throws DAOException {
            return daos.get(ProductNames.class).readTypeNameLookup();
        }
    };
    public Item<Map<Integer, String>> PRODUCT_GROUP_NAMES = new Item<Map<Integer, String>>(ProductTypes.class) {

        @Override
        protected Map<Integer, String> load(DAOCollection daos) throws DAOException {
            return daos.get(ProductTypes.class).readTypeNameLookup();
        }
    };
    public Item<Map<Integer, String>> PRODUCT_GROUP_IDS = new Item<Map<Integer, String>>(ProductTypes.class) {

        @Override
        protected Map<Integer, String> load(DAOCollection daos) throws DAOException {
            return daos.get(ProductTypes.class).readTypeIDLookup();
        }
    };
    public Item<Map<Integer, String>> DATA_SET_NAMES = new Item<Map<Integer, String>>(ProductTypes.class) {

        @Override
        protected Map<Integer, String> load(DAOCollection daos) throws DAOException {
            return daos.get(DataSets.class).readNameLookup();
        }
    };

    public abstract class Item<T> {

        private T value;
        List<Item> dependencies = new LinkedList<Item>();
        private Set<ChangeListener> listeners;
        private Set<Class<? extends DAO>> daoDeps;

        Item(Class<? extends DAO>... deps) {
            daoDeps = new HashSet<Class<? extends DAO>>();
            Collections.addAll(daoDeps, deps);
            items.add(this);
        }

        public void addListener(ChangeListener listener) {
            if (listeners == null) {
                listeners = new HashSet<ChangeListener>();
            }
            listeners.add(listener);
        }

        public T getValue() {
            if (value == null) {
                reload();
            }
            return value;
        }

        public T getValue(boolean force) {
            if (force) {
                reload();
            }
            return getValue();
        }

        void change() {
            value = null;
            fireChange();
        }

        protected abstract T load(DAOCollection daos) throws DAOException;

        void reload() {
            DAOCollection daos = null;
            try {
                daos = factory.create();
                for (Item dep: dependencies) {
                    dep.reload();
                }
                value = load(daos);
            } catch (DAOException daoe) {
                throw new RuntimeException(daoe);
            } finally {
                if (daos != null) {
                    daos.close();
                }
            }
        }

        private void fireChange() {
            if (listeners != null) {
                ChangeEvent ce = new ChangeEvent(this);
                for (ChangeListener cl: listeners) {
                    cl.stateChanged(ce);
                }
            }
        }
    }
}
