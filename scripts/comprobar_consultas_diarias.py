import urllib.request,json,concurrent.futures,datetime,pathlib
rows=[]
def fetch(item):
 sport,league,day=item
 url=f'https://site.api.espn.com/apis/site/v2/sports/{sport}/{league}/scoreboard?dates={day}&limit=100'
 try:
  with urllib.request.urlopen(urllib.request.Request(url,headers={'User-Agent':'PulseSport-Educational/1.0','Accept':'application/json'}),timeout=25) as r:
   data=json.load(r);row={'url':url,'http':r.status,'partidos':len(data.get('events',[]))}
   pass
 except Exception as e:row={'url':url,'error':str(e)}
 row['utc']=datetime.datetime.now(datetime.timezone.utc).isoformat();return row
items=[(s,l,d) for s,l in [('soccer','eng.1'),('soccer','esp.1'),('soccer','col.1'),('basketball','nba')] for d in ['20261120','20261121','20261122']]
with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
 for row in pool.map(fetch,items):rows.append(row);print(row,flush=True)
(pathlib.Path(__file__).resolve().parents[1]/'docs/evidencias/correccion_consultas_diarias.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2))
