from pricip.restlet import PricipServices
from dao.pricip.postgres import PGDaoFactory
from dao.pricip import Products2
from java.sql import Types
import sys
import os

ROOT_SRC="//williamsfork/apache/htdocs/pricip/resources/dyrplots"
ROOT_DEST="x"

daos = PGDaoFactory.createPGDaoFactory(PricipServices.createDataSource()).create()

rows = daos.get(Products2).read()

while rows.hasNext():
    row = rows.next()
    loc = row.string(Products2.LOCATION)
    if not os.path.exists( os.path.join(ROOT_SRC,loc) ):
        print loc
    



