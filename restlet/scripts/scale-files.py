from pricip.restlet import PricipServices
from dao.pricip.postgres import PGDaoFactory
from dao.pricip import ProductRevisions
from java.sql import Types
import sys
import os

ROOT_SRC="//williamsfork/apache/htdocs/pricip/resources/dyrplots"
ROOT_DEST="x"

daos = PGDaoFactory.createPGDaoFactory(PricipServices.createDataSource()).create()

rows = daos.get(ProductRevisions).readAll()

cmd = "gm convert -scale 256x256 %s %s"

while rows.hasNext():
    row = rows.next()
    loc = row.string(ProductRevisions.LOCATION)
    input = ROOT_SRC + "/" + loc
    if os.path.exists( input ):
        dot = input.rindex(".")
        output = input[0:dot] + "_scaled" + input[dot:]
        print "scaling %s to %s" % (input,output)
        os.system(cmd % (input,output))
    



