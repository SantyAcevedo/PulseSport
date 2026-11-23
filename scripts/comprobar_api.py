import urllib.request,json,pathlib,datetime
out=pathlib.Path(__file__).resolve().parents[1]/'docs/evidencias';out.mkdir(exist_ok=True,parents=True)
rows=[]
for sport,league in [('soccer','eng.1'),('soccer','esp.1'),('soccer','col.1'),('basketball','nba')]:
 for kind in ['scoreboard','standings']:
  url=('https://site.web.api.espn.com/apis/v2/sports/' if kind=='standings' else 'https://site.api.espn.com/apis/site/v2/sports/')+sport+'/'+league+'/'+kind
  try:
   with urllib.request.urlopen(url,timeout=25) as r:data=json.load(r);code=r.status
   def count(n):return len(n.get('standings',{}).get('entries',[]))+sum(count(c) for c in n.get('children',[]))
   n=count(data) if kind=='standings' else len(data.get('events',[]))
   rows.append({'url':url,'http':code,'registros':n,'utc':datetime.datetime.now(datetime.timezone.utc).isoformat()})

  except Exception as e: rows.append({'url':url,'error':str(e)})
 print(league,rows[-2:],flush=True)
(out/'comprobacion_api.json').write_text(json.dumps(rows,indent=2,ensure_ascii=False))
