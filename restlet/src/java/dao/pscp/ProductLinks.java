package dao.pscp;

import dao.Column;
import dao.DAO;
import dao.DAOException;
import dao.Pair;
import dao.RowIterator;
import dao.SQLWriter;
import dao.TableDefinition;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ProductLinks extends DAO {

    private static final TableDefinition columns = new TableDefinition("productlinks");
    public static final Column<Integer> STATIONID = columns.integer("stationid");
    public static final Column<Integer> NAMEID = columns.integer("nameid");
    public static final Column<Integer> TYPEID = columns.integer("typeid");
    public static final Column<UUID> CONTACTID = columns.uuid("contactid");
    public static final Column<String> LOCATION = columns.string("location");
    public static final Column<Date> CHANGED = columns.date("changed");
    public static final Column<String> PROCESS = columns.string("process");
    public static final Column<String> PERIOD = columns.string("period");
    public static final Column<String> SEASON = columns.string("season");
    public static final Column<String> QUADRANT = columns.string("quadrant");
    public static final Column<String> ATTRIBUTE = columns.string("attribute");

    public RowIterator readProducts() throws DAOException {
        return readRows(selectAllFrom(columns));
    }

    public RowIterator readProductsAtStation(int id) throws DAOException {
/*
        According to John Marra, in conversation of 10/18/2011, these are the 
        sort keys in their proper order:
        TYPEID,
        PROCESS,
        QUADRANT,
        PERIOD,
        SEASON,
        SUBJECT,
        ATTRIBUTE,
        TERM,
        STATISTIC,
        INDICE
 */

        String query = "SELECT p.* "+
                        " FROM productlinks p "+
                        " LEFT OUTER JOIN (SELECT distinct process, CASE process WHEN 'SW' THEN 1 WHEN 'HR' THEN 2 WHEN 'HS' THEN 3 END as processorder FROM productlinks) o ON p.process=o.process "+
                        " LEFT OUTER JOIN (SELECT distinct period, CASE period WHEN '1 day' THEN 1 WHEN '5 day' THEN 2 WHEN '30 day' THEN 3 ELSE 4 END AS periodorder FROM productlinks) o2 ON p.period=o2.period "+
                        " LEFT OUTER JOIN (SELECT distinct season, CASE season WHEN 'Annual' THEN 1 WHEN 'Seasonal' THEN 2 WHEN 'Winter' THEN 3 WHEN 'Summer' THEN 4 ELSE 5 END AS seasonorder FROM productlinks) o3 ON p.season=o3.season "+
                        " LEFT OUTER JOIN (SELECT distinct subject, term, statistic, indice, nameid FROM productname) n ON p.nameid=n.nameid "+
                        "WHERE p.stationid=? "+
                        "ORDER BY p.typeid, o.processorder, p.quadrant, o2.periodorder, o3.seasonorder, n.subject desc, p.attribute, n.term, n.statistic desc, n.indice";
        
        return readRowsHardCoded(query, STATIONID.pair(id));
    }

    public RowIterator search(List<Pair> pairs) throws DAOException {
        return selectAllFromWhere(columns.table(), pairs);
    }
}

