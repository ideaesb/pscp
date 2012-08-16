import urllib
import json
import zipfile
import os

def getjson(url):
    return json.loads(urllib.urlopen(url).read()[0:-1])

producttypes = [ (pt["id"],pt["name"].split(" ")[0]) for pt in getjson("http://williamsfork.riverside.com/pscp-api/producttypes/ac")]
productnames = [ (pt["typeid"],pt["name"]) for pt in getjson("http://williamsfork.riverside.com/pscp-api/productnames/ac")]

zf = zipfile.ZipFile("products-test.zip","w")

ds = ["551A","351A","029A"]

for t in producttypes:
    print t[1]
    pn = filter(lambda pn: t[0] == pn[0],productnames)
    for n in pn:
        print n[1]
        for d in ds:
            cmd = "gm.exe convert -fill blue -pointsize 20 label:'%s - %s\\n%s' label.png" % (d,t[1],n[1])
            os.system(cmd)
            zf.write("label.png","%s/%s/%s" % (t[1],n[1],d +".png"))
zf.close()
