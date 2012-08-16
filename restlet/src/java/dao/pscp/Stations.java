package dao.pscp;

import dao.Column;
import dao.TableDefinition;
import dao.DAO;
import dao.DAOException;
import dao.Pair;
import dao.Row;
import dao.RowIterator;
import dao.SQLWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author iws
 */
public class Stations extends DAO {

    private static final String TABLE = "stations";
    private static final TableDefinition columns = new TableDefinition("stationcnt");
    public static final Column<Integer> STATIONID = columns.integer("stationid");
    public static final Column<String> STATIONNAME = columns.string("stationname", "Station Name");
    public static final Column<String> STATIONTYPE = columns.string("stationtype", "Station Type");
    public static final Column<BigDecimal> LATITUDE = columns.decimal("latitude");
    public static final Column<BigDecimal> LONGITUDE = columns.decimal("longitude");
    public static final Column<String> REGION = columns.string("region");
    public static final Column<String> NATION = columns.string("nation");
    public static final Column<String> STATUS = columns.string("status");
    public static final Column<String> METADATA = columns.string("metadata");
    public static final Column<String> LOCALID = columns.string("localid", "ID");
    public static final Column<Integer> ONUM = columns.integer("onum", "Operator");
    public static final Column<String> PROCESS = columns.string("process");
    public static final Column<Double> ELEVATION = columns.dbl("elevation");
    public static final Column<Integer> DATASETID = columns.integer("datasetid");
    public static final Column<String> STATIONCLASS = columns.string("stationclass", "Instrument");
    public static final Column<Integer> QUALITY = columns.integer("quality", "Data Quality");
    public static final Column<Integer> CNT = columns.integer("cnt", "Product Count");

    public RowIterator readUniqueStationTypes() throws DAOException {
        return readRows(selectDistintFrom(columns, STATIONTYPE));
    }

    public void updateStation(int id, Pair... values) throws DAOException {
        updateWhereEquals(TABLE, STATIONID.pair(id), values);
    }

    public int findStation(Pair... values) throws DAOException {
        RowIterator rows = selectAllFromWhere(columns.table(), values);
        int id = -1;
        if (rows.hasNext()) {
            Row next = rows.next();
            rows.close();
            id = next.value(STATIONID);
        }
        return id;
    }

    //@todo - need this anymore???
    public int findStation(String name, String process, String nation, int onum) throws DAOException {
        SQLWriter sql = selectAllFrom(columns);
        sql.whereColumnsEquals(STATIONNAME, PROCESS, NATION, ONUM);
        RowIterator rows = readRows(sql,
                STATIONNAME.pair(name),
                PROCESS.pair(process),
                NATION.pair(nation),
                ONUM.pair(onum));
        Row next = rows.next();
        rows.close();
        return next == null ? -1 : next.value(STATIONID);
    }

    public int insertStation(Pair... values) throws DAOException {
        Number id = (Number) insert(TABLE, STATIONID, values);
        return id.intValue();
    }

    public RowIterator readStations(String process, String region) throws DAOException {
        SQLWriter sql = selectAllFrom(columns);
        sql.whereColumnsEquals(
                process == null ? null : PROCESS,
                region == null ? null : REGION);
        sql.orderBy(STATIONNAME);
        return readRows(sql, PROCESS.pairNotNull(process), REGION.pairNotNull(region));
    }

    public RowIterator readStation(int id) throws DAOException {
        SQLWriter sql = selectAllFrom(columns);
        sql.whereColumnEquals(STATIONID);
        return readRows(sql, STATIONID.pair(id));
    }

    public RowIterator readStationLocations(String process, String region, Integer quality) throws DAOException {
        SQLWriter sql = stationLocationQuery();
        sql.whereColumnsEquals(
                process == null ? null : PROCESS,
                region == null ? null : REGION,
                quality == null ? null : QUALITY);
        sql.orderBy(STATIONNAME);
        return readRows(sql, PROCESS.pairNotNull(process), REGION.pairNotNull(region), QUALITY.pairNotNull(quality));
    }

	/*
	 * Given the input parameters, create an appropriate query string, query the database, extract the results and return them.
	 *
	 * To add new elements to the where clause (and yes, its tricky and not intuitive):
	 * 1. Add a new parameter to the function signature.
	 * 2. Add a new block of code like the one that starts by checking for region != null.
	 * 3. Modify the new block to employ the new parameter you added.
	 *
	 * Opportunities for performance improvement:
	 * 1. The in clause with a sub-query in the where clause is a notorious source of performance problems. The subquery
	 * should be moved to the from clause to avoid having to rerun the query for every row.
	 *
	 * Opportunities to improve readability:
	 * 1. rather than checking each argument for null in the java code, it can be done in the SQL query. This change would
	 *    eliminate 75% of the code in this method without changing performance.
	 *
	 * Opportunities to improve design:
	 * 1: methods that fetch an object of some kind are an ideal place for a factory. In this case, encapsulating SQL queries
	 *    in a factory delegates all SQL to the perview of a factory, rather than spreading it over multiple packages and
	 *    multiple inheritance layers.
	 * 2. A stations class such as this is not a business object in the real-world sense (neither is a RowIterator). A station
	 *    class (singular) would be.
	 * 3. The semantics of methods like readStation() and readStationLocations() ambiguous because they both return the same
	 *    type of object (RowIterator). readStation is likely to always return a rowIterator with a single row.
	 * 4. The public static Column elements expose implementation details of the database to any other class in the system.
	 */
    public RowIterator readStationLocations(String process, String region, int[] productTypes, String[] indicator)
            throws DAOException {
        // don't do this at home
        SQLWriter writer = stationLocationQuery();
        StringBuilder buf = new StringBuilder("where");
        List<Pair> pairs = new ArrayList<Pair>();
        if (region != null) {
            if (pairs.size() > 0) {
                buf.append(" and");
            }
            buf.append(" region=?");
            pairs.add(REGION.pair(region));
        }
		if (pairs.size() > 0) {
			buf.append(" and");
		}
        buf.append(" stationid in ( ");
        buf.append("select distinct stationid from products ");
        buf.append("left join productname on productname.nameid = products.nameid where");
        // first set of predicates are an OR group (search for productname parts)
        if (indicator.length > 0) {
            Column<String> indicatorColumn = new Column<String>("indicator", String.class);
            buf.append(" ( quadrant in ( ");
            for (int i = 0; i < indicator.length; i++) {
                if (i > 0) {
                    buf.append(',');
                }
                buf.append('?');
                pairs.add(indicatorColumn.pair(indicator[i]));
            }
            buf.append(") ");
            buf.append("or period=? or attribute like ?");
            buf.append(")");
            // used for period comparison (we only allow one)
            pairs.add(indicatorColumn.pair(indicator[0]));
            // used for attribute comparison (we only allow one)
            pairs.add(indicatorColumn.pair("%" + indicator[0] + "%"));
            // - end OR group
            buf.append(" and");
        }
        // typeid must be present
        buf.append(" typeid in ( ");
        for (int i = 0; i < productTypes.length; i++) {
            if (i > 0) {
                buf.append(',');
            }
            buf.append(productTypes[i]);
        }
        buf.append(" )"); // end typeid in predicate
        if (process != null) {
            buf.append(" and process=?");
            pairs.add(PROCESS.pair(process));
        }
        buf.append(")"); // end product subquery
        appendComplexQuery(writer, buf.toString());
        return readRows(writer, pairs.toArray(new Pair[pairs.size()]));
    }

    /*
     * This version of the method does not use a prepared statement.
     */
    public RowIterator readStationLocations(String process, String region, Integer quality, int[] productTypes, String[] indicator)
	            throws DAOException {
        String regionStr = (region == null ? "NULL" : "'"+region+"'");
        String periodStr = (indicator == null || indicator.length == 0 ? "NULL": "'"+indicator[0]+"'");
        String processStr = (process == null ? "NULL" : "'"+process+"'");
        String attributeStr = (indicator == null || indicator.length == 0 ? "N.ATTRIBUTE": "'%"+indicator[0]+"%'");
        StringBuilder query = new StringBuilder("");
        StringBuilder quadrantList = new StringBuilder();
        StringBuilder quadrantPhrase = new StringBuilder();
        StringBuilder typeidList = new StringBuilder();
        if (indicator != null) {
            for (int i = 0; i < indicator.length; i++) {
                if (i > 0) {
                    quadrantList.append(',');
                }
                quadrantList.append(indicator[i]);
            }
        }
        if (quadrantList.length() > 0) {
            quadrantPhrase.append("QUADRANT IN ('"+quadrantList.toString()+"') OR ");
        }
        for (int i = 0; i < productTypes.length; i++) {
            if (i > 0) {
                    typeidList.append(',');
            }
            typeidList.append(productTypes[i]);
        }
        if (typeidList.length() == 0) {
            typeidList.append("TYPEID");
        }

        query.append("SELECT S.STATIONID, S.STATIONNAME, S.STATIONCLASS, S.PROCESS, S.LATITUDE, S.LONGITUDE "+
                 "  FROM STATIONS S, "+
                 "        DATASETS D, "+
                 "        (SELECT DISTINCT STATIONID FROM PRODUCTS PR, PRODUCTNAME N "+
                 "          WHERE PR.NAMEID=N.NAMEID "+
                 "            AND ("+quadrantPhrase.toString()+"(N.PERIOD IS NULL OR N.PERIOD=COALESCE("+periodStr+",N.PERIOD)) OR (N.ATTRIBUTE IS NULL OR N.ATTRIBUTE LIKE "+ attributeStr +")) AND TYPEID IN("+typeidList.toString()+")) P "+
                 " WHERE S.DATASETID=D.DATASETID "+
                 "   AND P.STATIONID=S.STATIONID "+
                 "   AND S.REGION=COALESCE("+regionStr+",S.REGION) "+
                 "   AND QUALITY=COALESCE("+quality+",QUALITY) "+
                 "   AND PROCESS=COALESCE("+processStr+",PROCESS)");
// System.out.println("readStationLocations query: ["+query.toString()+"]");
        return readRows(query.toString());
    }

    private SQLWriter stationLocationQuery() {
        return selectColumnsFrom(TABLE,
                STATIONID, STATIONNAME, STATIONCLASS, PROCESS, LATITUDE, LONGITUDE);
    }

    public RowIterator readStationLocationsByGroup(int productGroup) throws DAOException {
        String sql = "select * from stations where stationid in (select distinct p.stationid from products p, productname pn where p.nameid=pn.nameid and pn.typeid = ?)  ORDER BY stationname";
//System.out.println("query: ["+sql.toString()+"]");
        Column<Integer> typeid = new Column("typeid", null, Integer.class);
        return readRowsHardCoded(sql, typeid.pairNotNull(productGroup));
    }

}
