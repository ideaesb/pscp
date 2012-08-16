from cStringIO import StringIO

cols = [
"typeid",
"groupid",
"process",
"quadrant",
"season",
"period",
"term",
"subject",
"attribute",
"indice",
"statistic"
]

typetable = "producttype"
subtypetable = "productname"

typebuf = StringIO()
typecnt = 1
currtype = 1
subtypecnt = 1
subtypebuf = StringIO()
maxlens = [0] * len(cols)

def permute(kw):
    blanks = ["NULL"]
    colsrc = [blanks] * len(cols)
    cnt = 0
    for k in kw:
        src = kw[k]
        if type(src) == str:
            src = [src]
            if cnt == 0: cnt = 1
        else:
            cnt *= len(src)
        colsrc[ cols.index(k) ] = src
    return product(colsrc)

def product(args):
    pools = tuple(args) 
    result = [[]]
    for pool in pools:
        result = [x+[y] for x in result for y in pool]
    for prod in result:
        yield tuple(prod)

def permutations(iterable):
    pool = tuple(iterable)
    n = len(pool)
    for indices in product(range(n)):
        if len(set(indices)) == n:
            yield tuple(pool[i] for i in indices)


def quote(v):
    return (v == "NULL" and "NULL" or 
        v == "DEFAULT" and "DEFAULT" or
        type(v) == int and str(v) or 
        "'%s'" % v)
        

def gentype(numnames,*permutations):
    global typecnt
    global maxlens
    for typenum,name in numnames:
        cnt = 0
        num = int(typenum.split(".")[0])
        clazz = typeclasses[num]
        typebuf.write("INSERT INTO %s VALUES (%i,'%s','%s','%s');\n" % (typetable,typecnt,name,clazz,typenum))
        print typenum,len(permutations)
        for p in permutations:
            for vals in permute(p):
                vals = list(vals)
                maxlens = map(max,zip(maxlens,[ (i and i != "NULL") and len(i) or 0 for i in vals]))
                vals[0] = "DEFAULT"
                vals[1] = typecnt 
                vals = ",".join(map(quote,vals))
                subtypebuf.write("INSERT INTO %s VALUES (%s);\n" % (subtypetable,vals))
                cnt+=1
        #print typenum,cnt
        typecnt += 1 

def gen(**kw):
    return kw 

typeclasses=["Foundational"] * 2 +\
    ["Inter-Annual"] * 3 +\
    ["Annual"] * 3 +\
    ["Climate Indices"] * 2
typeclasses = dict( [ (e[0]+1,e[1]) for e in enumerate(typeclasses) ] )
    

ALL_SEASONS=["Annual","Winter","Summer"]
ANNUAL_SEASONAL=["Annual","Seasonal"]
SW_STAT=["maxima","extreme event","mean"]
HR_PERIODS=["1 day","5 day","30 day"]
TREND_TERMS=["25 year","50 year","100 year"]
P_75_90_95=["75p","90p","95p"]
CONTRASTING_STATS=["monthly max","extreme event","average"]
INDICES=["MEI","PDO","NPI","PNA","MJO"]
CARDINAL="NSEW"

gentype([("1.1","Time Series")],
    gen(process="SW",quadrant="All",season=ALL_SEASONS),
    gen(process="SW",quadrant="Each",season=ALL_SEASONS,statistic=SW_STAT),
    gen(process="HR",season=ALL_SEASONS,period=HR_PERIODS),
    gen(process="HS",season=ALL_SEASONS,subject="waves",attribute=["height","power"]),
    gen(process="HS",season=ALL_SEASONS,subject="water level",attribute=["observed","non-tidal residual"])
)

gentype([("2.1","Cumulative Distribution Function")],
    gen(process="SW",quadrant="All"),
    gen(process="SW",quadrant=["Each %s" % c for c in CARDINAL]),
    gen(process="HR",period=HR_PERIODS),
    gen(process="HS",subject="waves",attribute=["height","power"]),
    gen(process="HS",subject="water level",attribute=["observed","non-tidal residual"])
)

gentype([
    ("3.1.1","Frequency Counts"), # - relative - number of days"),
    ("3.1.2","Frequency Counts - relative - number of events"),
    ("3.2.1","Frequency Counts - absolute - number of days"),
    ("3.2.2","Frequency Counts - absolute - number of events"),
    ],
    gen(process="SW",quadrant="All",season=ANNUAL_SEASONAL),
    gen(process="SW",quadrant="Each",season=ALL_SEASONS,statistic=P_75_90_95),
    gen(process="HR",season=ANNUAL_SEASONAL,period=HR_PERIODS),
    gen(process="HS",season=ANNUAL_SEASONAL,subject="waves",attribute=["height","power"]),
    gen(process="HS",season=ANNUAL_SEASONAL,subject="water level",attribute=["observed","non-tidal residual"])
)

gentype([
    ("4.1","Exceedance Probabilities"), # - cumulative curves"),
    ("4.2","Exceedance Probabilities - stick plots")
    ],
    gen(process="SW",quadrant="All",season=ALL_SEASONS),
    gen(process="SW",quadrant=["Each %s" % c for c in CARDINAL],season=ALL_SEASONS),
    gen(process="HR",season=ALL_SEASONS,period=HR_PERIODS),
    gen(process="HS",season=ALL_SEASONS,subject="waves",attribute=["height","power"]),
    gen(process="HS",season=ALL_SEASONS,subject="water level",attribute=["observed","non-tidal residual"])
)

gentype([
    ("5.1.1","Long-term Trends and Epochal Change - Regression - full POR"),
    ("5.1.2","Long-term Trends and Epochal Change - Regression - contrasting POR")
    ],
    gen(process="SW",quadrant="All",season=ALL_SEASONS),
    gen(process="SW",quadrant=["Each %s" % c for c in CARDINAL],season=ALL_SEASONS),
    gen(process="HR",season=ALL_SEASONS,period=HR_PERIODS),
    gen(process="HS",season=ALL_SEASONS,subject="waves",attribute=["height","power"]),
    gen(process="HS",season=ALL_SEASONS,subject="water level",attribute=["observed","non-tidal residual"])
)
gentype([("5.2.1","Long-term Trends and Epochal Change - Extreme Value-based")],
    gen(process="SW",quadrant="All",season=ALL_SEASONS),
    gen(process="HR",season=ALL_SEASONS,period=HR_PERIODS),
    gen(process="HS",season=ALL_SEASONS,subject="waves",attribute=["height","power"]),
    gen(process="HS",season=ALL_SEASONS,subject="water level",attribute=["observed","non-tidal residual"])
)
gentype([("5.2.2","Long-term Trends and Epochal Change - GEV versus POT")],
    gen(process="SW",quadrant="All",season=ALL_SEASONS,term=TREND_TERMS),
    gen(process="HR",season=ALL_SEASONS,period=HR_PERIODS,term=TREND_TERMS),
    gen(process="HS",season=ALL_SEASONS,subject="waves",attribute=["height","power"],term=TREND_TERMS),
    gen(process="HS",season=ALL_SEASONS,subject="water level",attribute=["observed","non-tidal residual"],term=TREND_TERMS)
)
gentype([
    ("5.3","Long-term Trends and Epochal Change - Trends in Frequency"),
    ("5.4","Long-term Trends and Epochal Change - Trends in Variability")
    ]
)
gentype([("5.5","Long-term Trends and Epochal Change - Epochs")],
    gen(process="SW",quadrant="All",season=ALL_SEASONS),
    gen(process="HR",season=ALL_SEASONS,period=HR_PERIODS),
    gen(process="HS",season=ALL_SEASONS,subject="waves",attribute=["height","power"]),
    gen(process="HS",season=ALL_SEASONS,subject="water level",attribute=["observed","non-tidal residual"])
)


gentype([("6.1","Daily Time Series")],
    gen(process="SW",quadrant="All"),
    gen(process="HR",period=HR_PERIODS),
    gen(process="HS",subject="waves",attribute=["height","power"]),
    gen(process="HS",subject="water level",attribute=["observed","non-tidal residual","mixed"])
)

gentype([("7.1","Monthly Polar Plots - full POR")],
    gen(process="SW",quadrant="All",statistic="combined",period="month"),
    gen(process="SW",quadrant="Each",statistic=["combined","max","extreme"],period="month"),
    gen(process="SW",quadrant=["Each %s" % c for c in CARDINAL],statistic="combined",period="month"),
    gen(process="HR",statistic="total",period="month"),
    gen(process="HS",subject="waves",attribute=["height","power"],statistic="combined",period="month"),
    gen(process="HS",subject="water level",attribute=["observed","non-tidal residual"],statistic="combined",period="month"),
)
gentype([("7.2","Monthly Polar Plots - contrasting POR")],
    gen(process="SW",quadrant="All",statistic=CONTRASTING_STATS),
    gen(process="HR",statistic="total",statistic=CONTRASTING_STATS),
    gen(process="HS",subject="waves",attribute=["height","power"],statistic=CONTRASTING_STATS),
    gen(process="HS",subject="water level",attribute=["observed","non-tidal residual"],statistic=CONTRASTING_STATS),
)

gentype([
    ("8.1","Monthly Frequency Counts"), # - relative"),
    ("8.2","Monthly Frequency Counts - absolute"),
    ],
    gen(process="SW",quadrant="All"),
    gen(process="SW",quadrant="Each",statistic=P_75_90_95),
    gen(process="HR",period=HR_PERIODS),
    gen(process="HS",subject="waves",attribute=["height","power"]),
    gen(process="HS",subject="water level",attribute=["observed","non-tidal residual"])
)

gentype([("9.1","Paired Time Series")],
    gen(process="SW",quadrant="All",indice=INDICES),
    gen(process="HR",period="month",statistic="total",indice=INDICES),
    gen(process="HS",period="month",subject="waves",attribute=["height","power"],indice=INDICES),
    gen(process="HS",period="month",subject="water level",attribute=["observed","non-tidal residual"],indice=INDICES)
)
gentype([("9.2","Paired Time Series of Extremes Indicators and Climate Indices - grouped")],
    gen(process="AP",period="month",statistic="max",indice=INDICES),
    gen(process="AP",period="month",statistic="extreme",indice=INDICES),
    gen(process="AP",period="month",statistic="mean",indice=INDICES),
)
gentype([("10.1","Correlation of Extremes Indicators and Climate Indices")])

print typebuf.getvalue()
print subtypebuf.getvalue()
print maxlens
