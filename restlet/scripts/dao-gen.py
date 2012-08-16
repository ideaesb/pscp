# Use the run jython script to run this.

from pscp.restlet import PSCPDataSource 
from java.sql import Types
import sys

DAODIR="src/java/dao/pscp"

args = sys.argv[1:]

table = args.pop()
dao = args and args.pop() or table[0].upper() + table[1:]

dataSource = PSCPDataSource.createDataSource()

conn = dataSource.getConnection()

md = conn.getMetaData()

rs = md.getColumns("pscp",None,table,None)

alltypes = dict( [(getattr(Types,f),f) for f in dir(Types) if f[0].upper() == f[0]] ) 

template = """
package dao.pscp;

import dao.DAO;
import dao.Columns;

public class %(dao)s extends DAO {

    private static final TableDefinition columns = new TableDefinition("%(table)s");
    %(columns)s
}
"""

STRING=("String","string")
UUID=("UUID","uuid")
BOOLEAN=("Boolean","bool")
INTEGER=("Integer","integer")
DATE=("Date","date")
NUMERIC=("BigDecimal","decimal")

types = {
    Types.VARCHAR: STRING,
    Types.BOOLEAN : BOOLEAN,
    Types.BIT : BOOLEAN,
    Types.INTEGER: INTEGER,
    Types.DATE: DATE,
    Types.NUMERIC: NUMERIC,
    Types.CHAR: STRING, 
    Types.SMALLINT: INTEGER 
}
other = {
    "uuid" : UUID 
}

columns = []
while rs.next():
    coldef = "public static final Column<%s> %s = columns.%s(\"%s\");"
    dataType = rs.getObject("DATA_TYPE")
    try:
        info = list(int(dataType) == Types.OTHER and other[rs.getString("TYPE_NAME")] or types[dataType])
    except KeyError:
        raise Exception( "%s (%s) is not defined yet" % (alltypes[dataType],rs.getString("TYPE_NAME") ))
    colName = rs.getString("COLUMN_NAME")
    info.insert(1,colName.upper())
    info.append(colName)
    columns.append(coldef % tuple(info))

print template % dict(dao=dao,table=table,columns="\n\t".join(columns))




