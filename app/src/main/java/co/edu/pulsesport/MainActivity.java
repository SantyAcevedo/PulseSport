package co.edu.pulsesport;
import android.Manifest;import android.app.*;import android.content.*;import android.content.pm.PackageManager;import android.graphics.Typeface;import android.graphics.drawable.GradientDrawable;import android.os.*;import android.view.*;import android.widget.*;import org.json.*;import java.time.*;import java.time.format.*;import java.util.*;import java.util.concurrent.*;
public class MainActivity extends Activity {
 private static final int BG=0xff0b1220,CARD=0xff172536,FG=0xfff4f7fb,DIM=0xffaabbcb,GREEN=0xff55e6b1,BLUE=0xff54b8ff,LINE=0xff2a3b4d,GOLD=0xffffd166;
 private final ExecutorService io=Executors.newSingleThreadExecutor();private final Handler main=new Handler(Looper.getMainLooper());private Store store;private Repository api;private LinearLayout page,body;private String screen="home";private League league=League.PREMIER,selectedLeague=League.PREMIER;private Models.Team selectedTeam;private Models.Match selectedMatch;private int generation=0,week=0;private String feedFilter="Próximos",detailTab="Resumen";private League feedLeague=null;private String homeTab="Favoritos";
 @Override public void onCreate(Bundle b){super.onCreate(b);store=new Store(this);api=new Repository(this);TrackingService.channels(this);if(b!=null){screen=b.getString("screen","home");week=b.getInt("week",0);}if(!store.onboarded()&&screen.equals("home"))screen="onboard";draw();}
 @Override protected void onSaveInstanceState(Bundle b){super.onSaveInstanceState(b);b.putString("screen",screen.equals("match")||screen.equals("team")||screen.equals("league")?"home":screen);b.putInt("week",week);}
 @Override protected void onDestroy(){generation++;io.shutdownNow();super.onDestroy();}
 private int dp(int n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}private TextView label(String s,int size,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setPadding(dp(4),dp(9),dp(4),dp(9));return v;}
 private LinearLayout vertical(){LinearLayout v=new LinearLayout(this);v.setOrientation(1);return v;}private LinearLayout row(){LinearLayout v=new LinearLayout(this);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
 private GradientDrawable bg(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(14));d.setStroke(dp(1),LINE);return d;}
 private void add(LinearLayout parent,View v){parent.addView(v,new LinearLayout.LayoutParams(-1,-2));}
 private void flex(LinearLayout parent,View v){parent.addView(v,new LinearLayout.LayoutParams(0,-2,1));}
 private Button button(String name,Runnable action){Button b=new Button(this);b.setAllCaps(false);b.setText(name);b.setTextColor(FG);b.setTextSize(14);b.setMinHeight(dp(48));b.setMinWidth(0);b.setMinimumWidth(0);b.setBackground(bg(CARD));b.setOnClickListener(v->action.run());return b;}
 private LinearLayout card(){LinearLayout c=vertical();c.setPadding(dp(13),dp(11),dp(13),dp(11));c.setBackground(bg(CARD));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(7),0,dp(7));body.addView(c,p);return c;}
 private void title(String s){TextView t=label(s,22,FG);t.setTypeface(null,Typeface.BOLD);add(body,t);}
 private ImageButton searchControl(Runnable action){
  ImageButton control=new ImageButton(this);control.setImageResource(android.R.drawable.ic_menu_search);
  control.setColorFilter(BLUE);control.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
  control.setPadding(dp(11),dp(11),dp(11),dp(11));control.setMinimumWidth(dp(56));control.setMinimumHeight(dp(56));
  control.setBackground(bg(CARD));control.setContentDescription("Buscar equipos");control.setOnClickListener(v->action.run());return control;
 }
 private int contentWidth(){return Math.min(dp(720),getResources().getDisplayMetrics().widthPixels-dp(32));}
 private void draw(){
  generation++;page=vertical();page.setGravity(Gravity.CENTER_HORIZONTAL);page.setBackgroundColor(BG);
  page.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(dp(16),i.getSystemWindowInsetTop(),dp(16),i.getSystemWindowInsetBottom());return i;});
  setContentView(page);LinearLayout header=row();header.setMinimumHeight(dp(64));
  TextView brand=label("PulseSport",26,FG);brand.setTypeface(null,Typeface.BOLD);flex(header,brand);
  if(store.onboarded()){
   ImageButton search=searchControl(()->{screen="teams";draw();});
   LinearLayout.LayoutParams searchParams=new LinearLayout.LayoutParams(dp(56),dp(56));searchParams.leftMargin=dp(8);header.addView(search,searchParams);
  }
  page.addView(header,new LinearLayout.LayoutParams(contentWidth(),-2));
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);
  body=vertical();body.setPadding(0,dp(8),0,dp(24));scroll.addView(body,new android.widget.FrameLayout.LayoutParams(contentWidth(),-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL));
  page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  switch(screen){case "onboard":onboard();break;case "teams":teamPicker(false);break;case "leagues":leaguePicker(false);break;case "match":match();break;case "team":teamProfile();break;case "league":leagueProfile();break;case "settings":settings();break;case "favorites":favorites();break;case "live":feedFilter="En vivo";home();break;default:home();}
  LinearLayout nav=row();nav.setPadding(dp(2),dp(3),dp(2),dp(3));nav.setBackground(bg(CARD));
  String[] names={"Inicio","En vivo","Favoritos","Ligas","Ajustes"};String[] icons={"⌂","●","★","▤","⚙"};String[] targets={"home","live","favorites","leagues","settings"};
  for(int i=0;i<names.length;i++){
   final String target=targets[i];LinearLayout item=vertical();item.setGravity(Gravity.CENTER);item.setMinimumHeight(dp(64));
   boolean active=screen.equals(target);TextView icon=label(icons[i],21,active?BLUE:DIM);icon.setGravity(Gravity.CENTER);icon.setPadding(0,0,0,0);add(item,icon);
   TextView caption=label(names[i],11,active?FG:DIM);caption.setGravity(Gravity.CENTER);caption.setPadding(0,0,0,0);add(item,caption);
   item.setContentDescription(names[i]);item.setOnClickListener(v->{screen=!store.onboarded()&&!target.equals("settings")?"onboard":target;
    if(screen.equals("home")){feedFilter="Próximos";feedLeague=null;}draw();});nav.addView(item,new LinearLayout.LayoutParams(0,dp(64),1));
  }
  LinearLayout.LayoutParams navParams=new LinearLayout.LayoutParams(contentWidth(),-2);navParams.topMargin=dp(4);page.addView(nav,navParams);
 }
 private void async(Callable<?> work,java.util.function.Consumer<Object> success){int token=generation;io.execute(()->{try{Object result=work.call();main.post(()->{if(token==generation&&!isFinishing()){success.accept(result);}});}catch(Exception e){main.post(()->{if(token==generation){add(body,label(DataErrors.describe(e)+" "+e.getMessage(),14,DIM));add(body,button("Reintentar",this::draw));}});}});}
 private void onboard(){title("Haz tu deporte tuyo");add(body,label("1 de 2 · Elige equipos populares o busca el tuyo. Puedes cambiar tu selección después.",16,DIM));teamPicker(true);}
 private void teamPicker(boolean onboarding){
  if(!onboarding)title("Tus equipos favoritos");
  add(body,label(onboarding?"Elige tus equipos. Puedes buscarlos o continuar sin seleccionar.":"Toca la estrella o la tarjeta para seguir un equipo.",14,DIM));
  LinearLayout searchRow=row();searchRow.setPadding(dp(6),0,dp(6),0);searchRow.setBackground(bg(CARD));
  EditText search=new EditText(this);search.setSingleLine(true);search.setTextSize(17);search.setMinHeight(dp(56));search.setHint("Buscar equipo");
  search.setTextColor(FG);search.setHintTextColor(DIM);search.setBackgroundColor(android.graphics.Color.TRANSPARENT);
  search.setInputType(android.text.InputType.TYPE_CLASS_TEXT);flex(searchRow,search);
  ImageButton find=searchControl(()->{search.requestFocus();((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(search,android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);});
  find.setBackgroundColor(android.graphics.Color.TRANSPARENT);find.setContentDescription("Buscar equipo por nombre");searchRow.addView(find,new LinearLayout.LayoutParams(dp(56),dp(56)));add(body,searchRow);
  LinearLayout choices=vertical();add(body,choices);add(choices,label("Cargando equipos y escudos…",14,DIM));
  int token=generation;io.execute(()->{List<Object[]> entries=new ArrayList<>();List<String> errors=new ArrayList<>();
   ExecutorService workers=Executors.newFixedThreadPool(4);List<Future<List<Object[]>>> jobs=new ArrayList<>();
   for(League l:League.values())jobs.add(workers.submit(()->{
    List<Object[]> found=new ArrayList<>();JSONObject data=api.teams(l).json();
    JSONArray teams=data.getJSONArray("sports").getJSONObject(0).getJSONArray("leagues").getJSONObject(0).getJSONArray("teams");
    for(int i=0;i<teams.length();i++){JSONObject t=teams.getJSONObject(i).getJSONObject("team");
     found.add(new Object[]{l,new Models.Team(t.optString("id"),t.optString("displayName","Equipo"),"",Models.logo(t))});}
    return found;
   }));
   int index=0;for(League l:League.values())try{entries.addAll(jobs.get(index++).get());}catch(Exception e){errors.add(l.title);}
   workers.shutdownNow();main.post(()->{if(token!=generation)return;
    String[] popular={"Real Madrid","Barcelona","Manchester City","Arsenal","Liverpool","Los Angeles Lakers","Golden State Warriors","Millonarios","Atlético Nacional"};
    entries.sort((a,b)->{Models.Team x=(Models.Team)a[1],y=(Models.Team)b[1];int priority=Boolean.compare(isPopular(y.name(),popular),isPopular(x.name(),popular));return priority!=0?priority:x.name().compareToIgnoreCase(y.name());});
    Runnable render=()->renderTeams(choices,entries,search.getText().toString(),errors);
    search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence text,int start,int count,int after){}
     public void onTextChanged(CharSequence text,int start,int before,int count){render.run();}public void afterTextChanged(android.text.Editable edit){}
    });render.run();
   });
  });
  if(onboarding)add(page,button("Siguiente · ligas →",()->{screen="leagues";draw();}));
 }
 private void renderTeams(LinearLayout choices,List<Object[]> entries,String input,List<String> errors){
  choices.removeAllViews();String query=java.text.Normalizer.normalize(input,java.text.Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT).trim();
  if(!errors.isEmpty())add(choices,label("No se pudieron consultar: "+String.join(", ",errors)+". Puedes reintentar desde Actualizar.",12,DIM));
  int count=0;LinearLayout line=null;for(Object[] entry:entries){League l=(League)entry[0];Models.Team t=(Models.Team)entry[1];
   String normalized=java.text.Normalizer.normalize(t.name(),java.text.Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT);
   if(!normalized.contains(query))continue;if(query.isEmpty()&&count>=24)break;
   if(count%3==0){line=row();add(choices,line);}count++;
   LinearLayout tile=vertical();tile.setGravity(Gravity.CENTER);tile.setPadding(dp(5),dp(10),dp(5),dp(8));tile.setMinimumHeight(dp(160));tile.setBackground(bg(CARD));
   LinearLayout.LayoutParams tileParams=new LinearLayout.LayoutParams(0,-2,1);tileParams.setMargins(dp(3),dp(5),dp(3),dp(5));line.addView(tile,tileParams);
   ImageView icon=crest(t.logo());icon.setLayoutParams(new LinearLayout.LayoutParams(dp(54),dp(54)));icon.setContentDescription("Escudo de "+t.name());tile.addView(icon);
   TextView name=label(t.name(),12,FG);name.setGravity(Gravity.CENTER);name.setMaxLines(2);name.setMinHeight(dp(44));add(tile,name);
   TextView competition=label(l.title,10,DIM);competition.setGravity(Gravity.CENTER);competition.setSingleLine(true);competition.setEllipsize(android.text.TextUtils.TruncateAt.END);competition.setPadding(0,0,0,0);add(tile,competition);
   Button star=button(store.favorite(l,t.id())?"★":"☆",()->{store.toggle(l,t);renderTeams(choices,entries,input,errors);});
   star.setTextSize(25);star.setTextColor(store.favorite(l,t.id())?GOLD:FG);star.setMinWidth(dp(54));star.setContentDescription((store.favorite(l,t.id())?"Quitar de favoritos: ":"Agregar a favoritos: ")+t.name());
   tile.addView(star,new LinearLayout.LayoutParams(dp(54),dp(54)));
   tile.setOnClickListener(v->{store.toggle(l,t);renderTeams(choices,entries,input,errors);});tile.setContentDescription(t.name()+", "+l.title+(store.favorite(l,t.id())?", favorito":""));
  }
  if(count==0)add(choices,label(entries.isEmpty()?"No se pudieron cargar equipos. Comprueba la conexión y pulsa Actualizar.":"No encontramos ese equipo en estas ligas.",14,DIM));
  if(count>0&&count%3!=0){for(int missing=count%3;missing<3;missing++){View spacer=new View(this);line.addView(spacer,new LinearLayout.LayoutParams(0,dp(1),1));}}
  if(query.isEmpty()&&entries.size()>24)add(choices,label("Mostrando 24 equipos. Escribe el nombre para encontrar otros.",12,DIM));
 }
 private boolean isPopular(String s,String[] names){for(String x:names)if(s.equalsIgnoreCase(x))return true;return false;}
 private ImageView crest(String url){ImageView image=new ImageView(this);image.setLayoutParams(new LinearLayout.LayoutParams(dp(44),dp(44)));image.setScaleType(ImageView.ScaleType.FIT_CENTER);Crest.into(this,image,url);return image;}
 private void leaguePicker(boolean ignored){
  title(store.onboarded()?"Ligas favoritas":"2 de 2 · Elige tus ligas");
  add(body,label("Sigue tus competiciones para verlas en Inicio y recibir alertas durante el seguimiento.",14,DIM));
  String group="";for(League l:League.values()){
   if(!l.group.equals(group)){group=l.group;TextView heading=label(group,16,BLUE);heading.setTypeface(null,Typeface.BOLD);add(body,heading);}
   LinearLayout c=card();add(c,button((store.follows(l)?"★  ":"☆  ")+l.title,()->{store.toggleLeague(l);draw();}));
   add(c,button("Ver partidos y ficha →",()->{selectedLeague=l;screen="league";draw();}));
  }
  if(!store.onboarded())add(page,button("Terminar y ver partidos →",()->{store.completeOnboarding();screen="home";draw();}));
 }
 private String shortDate(String raw){try{return Instant.parse(raw).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("EEE d MMM · HH:mm",new Locale("es","CO")));}catch(Exception e){return raw;}}
 private void filters(){
  LinearLayout tabs=row();for(String name:new String[]{"Favoritos","Resultados"}){
   LinearLayout tab=vertical();tab.setGravity(Gravity.CENTER);TextView text=label(name.toUpperCase(Locale.ROOT),15,homeTab.equals(name)?FG:DIM);text.setTypeface(null,homeTab.equals(name)?Typeface.BOLD:Typeface.NORMAL);text.setGravity(Gravity.CENTER);add(tab,text);
   View line=new View(this);line.setBackgroundColor(homeTab.equals(name)?BLUE:LINE);tab.addView(line,new LinearLayout.LayoutParams(-1,dp(homeTab.equals(name)?3:1)));
   tab.setMinimumHeight(dp(56));tab.setOnClickListener(v->{homeTab=name;feedLeague=null;week=0;draw();});flex(tabs,tab);
  }add(body,tabs);
  LinearLayout chips=row();for(String filter:new String[]{"Próximos","En vivo","Mis ligas"}){
   Button chip=button(filter,()->{feedFilter=filter;if(!filter.equals("Mis ligas"))feedLeague=null;screen=filter.equals("En vivo")?"live":"home";week=0;draw();});
   chip.setTextSize(12);chip.setTextColor(feedFilter.equals(filter)?BLUE:DIM);flex(chips,chip);
  }add(body,chips);
 }
 private boolean followsMatch(League l,Models.Match m){return store.follows(l)||store.favorite(l,m.home().id())||store.favorite(l,m.away().id());}
 private void home(){
  filters();
  if(feedFilter.equals("Mis ligas")){LinearLayout leagueChips=row();int i=0;
   for(League l:League.values())if(store.follows(l)){if(i++==2){add(body,leagueChips);leagueChips=row();i=1;}
    Button chip=button(l.title,()->{feedLeague=l;draw();});chip.setTextSize(11);chip.setTextColor(feedLeague==l?BLUE:DIM);flex(leagueChips,chip);
   }if(i>0)add(body,leagueChips);
   if(feedLeague!=null)add(body,button("Todas mis ligas",()->{feedLeague=null;draw();}));
  }
  if(homeTab.equals("Favoritos")&&store.leagues().isEmpty()&&store.favorites().isEmpty()){
   add(body,label("Elige equipos o ligas para crear tu portada.",16,DIM));add(body,button("＋ Agregar favoritos",()->{screen="favorites";draw();}));return;
  }
  LinearLayout results=vertical();add(body,results);add(results,label("Consultando partidos…",14,DIM));
  final boolean allLeagues=homeTab.equals("Resultados");final int daysToShow=allLeagues?2:6;
  LocalDate start=LocalDate.now().plusDays(week*daysToShow);int token=generation;
  io.execute(()->{List<Object[]> all=new ArrayList<>();List<String> failures=new ArrayList<>();
   ExecutorService workers=Executors.newFixedThreadPool(4);List<League> requested=new ArrayList<>();List<Future<List<Object[]>>> jobs=new ArrayList<>();
   for(League l:League.values())if(allLeagues||store.follows(l)||hasFavoriteTeam(l)){
    requested.add(l);jobs.add(workers.submit(()->{
     List<Object[]> found=new ArrayList<>();
     for(int offset=0;offset<daysToShow;offset+=3){
      String from=start.plusDays(offset).format(DateTimeFormatter.BASIC_ISO_DATE);
      String to=start.plusDays(Math.min(offset+2,daysToShow-1)).format(DateTimeFormatter.BASIC_ISO_DATE);
      for(Models.Match m:Models.matches(api.scores(l,from+"-"+to,false).json()))
       if(allLeagues||followsMatch(l,m))found.add(new Object[]{l,m});
     }return found;
    }));
   }
   for(int i=0;i<jobs.size();i++)try{all.addAll(jobs.get(i).get());}
   catch(Exception e){failures.add(requested.get(i).title+": "+DataErrors.describe(e));}
   workers.shutdownNow();
   main.post(()->{if(token!=generation)return;results.removeAllViews();
    if(!failures.isEmpty())add(results,label(String.join(" · ",failures),13,DIM));
    all.sort((a,b)->{Models.Match x=(Models.Match)a[1],y=(Models.Match)b[1];int c=Boolean.compare(y.live(),x.live());if(c!=0)return c;
     boolean fx=store.favorite((League)a[0],x.home().id())||store.favorite((League)a[0],x.away().id());
     boolean fy=store.favorite((League)b[0],y.home().id())||store.favorite((League)b[0],y.away().id());
     c=Boolean.compare(fy,fx);return c!=0?c:x.date().compareTo(y.date());});
    List<Object[]> visible=new ArrayList<>();for(Object[] entry:all){Models.Match m=(Models.Match)entry[1];
     if(feedFilter.equals("En vivo")&&!m.live())continue;if(feedLeague!=null&&feedLeague!=(League)entry[0])continue;visible.add(entry);
    }
    String heading=feedFilter.equals("En vivo")?"EN VIVO":week==0?"HOY Y PRÓXIMOS":"SIGUIENTES PARTIDOS";
    TextView day=label(heading+"  ·  "+visible.size()+" partidos",15,FG);day.setTypeface(null,Typeface.BOLD);add(results,day);
    if(visible.isEmpty())add(results,label(feedFilter.equals("En vivo")?"No hay partidos en vivo ahora.":"No hay partidos disponibles con estos filtros.",15,DIM));
    League previous=null;for(Object[] entry:visible){League l=(League)entry[0];if(previous!=l){TextView leagueLabel=label(l.sport+"  ·  "+l.title,14,BLUE);leagueLabel.setTypeface(null,Typeface.BOLD);add(results,leagueLabel);previous=l;}matchCard(results,l,(Models.Match)entry[1]);}
    if(!feedFilter.equals("En vivo")){add(results,button("Ver siguientes partidos →",()->{week++;draw();}));
     if(homeTab.equals("Favoritos")){add(results,label("Llegaste al final de los partidos de tus favoritos.",13,DIM));add(results,button("＋ Agregar más ligas",()->{screen="leagues";draw();}));}}
   });
  });
 }
 private void favorites(){
  title("Siguiendo");add(body,label("Equipos y ligas que aparecen primero en tu portada.",14,DIM));
  add(body,button("＋ Buscar y seguir equipos",()->{screen="teams";draw();}));
  add(body,button("＋ Administrar ligas",()->{screen="leagues";draw();}));
  title("Mis ligas");boolean any=false;for(League l:League.values())if(store.follows(l)){any=true;add(body,button("★  "+l.title+"  →",()->{selectedLeague=l;screen="league";draw();}));}
  if(!any)add(body,label("Todavía no sigues ligas.",14,DIM));
  title("Mis equipos");if(store.favorites().isEmpty())add(body,label("Todavía no sigues equipos.",14,DIM));
  for(String key:store.favorites()){int split=key.indexOf(':');if(split<0)continue;try{League l=League.valueOf(key.substring(0,split));add(body,button("★  "+store.teamName(key)+" · "+l.title,()->{screen="teams";draw();}));}catch(Exception ignored){}}
 }
 private boolean hasFavoriteTeam(League l){for(String key:store.favorites())if(key.startsWith(l.name()+":"))return true;return false;}
 private void matchCard(League l,Models.Match m){matchCard(body,l,m);}
 private void matchCard(LinearLayout parent,League l,Models.Match m){
  LinearLayout c=vertical();c.setPadding(dp(14),dp(10),dp(14),dp(12));c.setBackground(bg(CARD));
  LinearLayout.LayoutParams outer=new LinearLayout.LayoutParams(-1,-2);outer.bottomMargin=dp(8);parent.addView(c,outer);
  String state=m.live()?"● EN VIVO  ·  "+m.status():m.state().equals("post")?"FINALIZADO":"PRÓXIMO";
  TextView status=label(state,12,m.live()?GREEN:DIM);status.setGravity(Gravity.CENTER);status.setTypeface(null,Typeface.BOLD);add(c,status);
  LinearLayout middle=row();middle.setGravity(Gravity.CENTER_VERTICAL);
  teamScoreColumn(middle,l,m.home());
  String score=m.state().equals("pre")?shortDate(m.date()).replaceAll(".* · ",""):m.score();
  LinearLayout center=vertical();center.setGravity(Gravity.CENTER);TextView main=label(score,m.state().equals("pre")?21:31,FG);main.setGravity(Gravity.CENTER);main.setTypeface(null,Typeface.BOLD);main.setPadding(0,0,0,0);add(center,main);
  TextView time=label(m.live()?m.status():m.state().equals("post")?"FINAL":shortDate(m.date()),11,m.live()?GREEN:DIM);time.setGravity(Gravity.CENTER);time.setMaxLines(2);add(center,time);
  middle.addView(center,new LinearLayout.LayoutParams(0,-2,1));teamScoreColumn(middle,l,m.away());add(c,middle);
  c.setContentDescription(l.title+", "+m.home().name()+" contra "+m.away().name()+", "+state+", "+score);
  c.setOnClickListener(v->{league=l;selectedMatch=m;detailTab="Resumen";screen="match";draw();});
 }
 private void teamScoreColumn(LinearLayout parent,League l,Models.Team team){
  LinearLayout col=vertical();col.setGravity(Gravity.CENTER);ImageView img=crest(team.logo());img.setLayoutParams(new LinearLayout.LayoutParams(dp(44),dp(44)));col.addView(img);
  TextView name=label((store.favorite(l,team.id())?"★ ":"")+team.name(),14,FG);name.setGravity(Gravity.CENTER);name.setMaxLines(2);name.setMinHeight(dp(46));add(col,name);
  parent.addView(col,new LinearLayout.LayoutParams(0,-2,1));
 }
 private void openTeam(League l,Models.Team t){league=l;selectedTeam=t;screen="team";draw();}
 private void match(){
  Models.Match m=selectedMatch;if(m==null){screen="home";draw();return;}
  add(body,button("← Partidos",()->{screen="home";draw();}));
  add(body,button(league.title+"  →",()->{selectedLeague=league;screen="league";draw();}));
  LinearLayout scoreboard=row();scoreboard.setPadding(dp(8),dp(16),dp(8),dp(16));scoreboard.setBackground(bg(CARD));
  teamColumn(scoreboard,league,m.home());
  TextView score=label(m.state().equals("pre")?shortDate(m.date()).replaceAll(".* · ",""):m.score(),m.state().equals("pre")?22:36,m.live()?GREEN:FG);
  score.setTypeface(null,Typeface.BOLD);score.setGravity(Gravity.CENTER);score.setMinWidth(dp(100));scoreboard.addView(score);
  teamColumn(scoreboard,league,m.away());add(body,scoreboard);
  TextView status=label((m.live()?"● EN VIVO · ":"")+m.status()+" · "+shortDate(m.date()),13,m.live()?GREEN:DIM);status.setGravity(Gravity.CENTER);add(body,status);
  LinearLayout tabs=row();for(String name:new String[]{"Resumen","Cronología","Tabla"}){Button tab=button(name,()->{detailTab=name;draw();});tab.setTextSize(12);tab.setTextColor(detailTab.equals(name)?GREEN:DIM);flex(tabs,tab);}add(body,tabs);
  if(detailTab.equals("Tabla")){add(body,label("Clasificación de "+league.title,14,DIM));async(()->api.standings(league,false),res->{try{standingsTable(Models.standings(((Repository.Result)res).json()));}catch(Exception e){add(body,label("Tabla no disponible.",14,DIM));}});return;}
  if(detailTab.equals("Resumen")){title("Partido");add(body,label("Toca un escudo para ver el equipo. La cronología se actualiza según la disponibilidad de la fuente.",14,DIM));}
  title(detailTab.equals("Resumen")?"Últimos eventos":"Minuto a minuto");
  if(m.timeline().isEmpty())add(body,label("Todavía no hay incidencias publicadas.",14,DIM));
  int from=detailTab.equals("Resumen")?Math.max(0,m.timeline().size()-4):0;
  for(int i=from;i<m.timeline().size();i++){String event=m.timeline().get(i);LinearLayout item=card();
   int color=event.toLowerCase(Locale.ROOT).contains("goal")||event.toLowerCase(Locale.ROOT).contains("gol")?GOLD:FG;
   add(item,label("●  "+event,14,color));}
  add(body,button("↻ Actualizar partido",()->{async(()->api.scores(league,LocalDate.now().minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE)+"-"+LocalDate.now().plusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE),true),res->{try{for(Models.Match x:Models.matches(((Repository.Result)res).json()))if(x.id().equals(m.id())){selectedMatch=x;break;}}catch(Exception ignored){}draw();});}));
 }
 private void teamColumn(LinearLayout parent,League l,Models.Team team){
  LinearLayout col=vertical();col.setGravity(Gravity.CENTER);ImageView icon=crest(team.logo());icon.setLayoutParams(new LinearLayout.LayoutParams(dp(48),dp(48)));icon.setContentDescription("Perfil de "+team.name());col.addView(icon);TextView name=label(team.name(),12,FG);name.setGravity(Gravity.CENTER);name.setMaxLines(2);add(col,name);
  parent.addView(col,new LinearLayout.LayoutParams(0,-2,1));col.setOnClickListener(v->openTeam(l,team));
 }
 private void standingsTable(List<Models.Standing> rows){
  if(rows.isEmpty()){add(body,label("Tabla no disponible.",14,DIM));return;}
  String group="";for(Models.Standing st:rows){if(!st.group().equals(group)){group=st.group();add(body,label(group,13,BLUE));}
   LinearLayout line=row();line.setPadding(dp(8),dp(3),dp(8),dp(3));line.setBackground(bg(CARD));
   TextView rank=label(st.rank(),13,GREEN);rank.setMinWidth(dp(28));line.addView(rank);
   TextView team=label(st.team(),13,FG);team.setSingleLine(true);team.setEllipsize(android.text.TextUtils.TruncateAt.END);flex(line,team);
   TextView played=label(st.played()+" PJ",12,DIM);line.addView(played);TextView pts=label(st.points(),14,FG);pts.setTypeface(null,Typeface.BOLD);pts.setGravity(Gravity.RIGHT);pts.setMinWidth(dp(36));line.addView(pts);
   LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(2);body.addView(line,lp);
  }
 }
 private void teamProfile(){Models.Team t=selectedTeam;if(t==null){screen="home";draw();return;}add(body,button("← Volver",()->{screen="match";draw();}));LinearLayout head=row();head.addView(crest(t.logo()));flex(head,label(t.name(),22,FG));add(body,head);add(body,button(store.favorite(league,t.id())?"★ Siguiendo":"☆ Seguir equipo",()->{store.toggle(league,t);draw();}));add(body,label("Datos de "+league.title+" · fuente ESPN",14,DIM));async(()->new Object[]{api.team(league,t.id()),api.schedule(league,t.id()),api.standings(league,false)},res->{Object[] data=(Object[])res;JSONObject team=((Repository.Result)data[0]).json().optJSONObject("team");if(team!=null){for(String key:new String[]{"location","abbreviation","venue"}){Object x=team.opt(key);if(x!=null)add(body,label(key+": "+(x instanceof JSONObject?((JSONObject)x).optString("fullName",""):x),14,DIM));}}
  title("Posición en "+league.title);try{for(Models.Standing s:Models.standings(((Repository.Result)data[2]).json()))if(s.team().equalsIgnoreCase(t.name()))add(body,label(s.group()+" · posición "+s.rank()+" · PJ "+s.played(),15,GREEN));}catch(Exception ignored){}title("Partidos anteriores y siguientes");try{List<Models.Match> games=Models.matches(((Repository.Result)data[1]).json());games.sort(Comparator.comparing(Models.Match::date));int count=0;for(Models.Match game:games)if(count++<30)matchCard(league,game);}catch(Exception e){add(body,label("Calendario no disponible.",14,DIM));}add(body,label("Títulos y goleadores: la fuente consultada no ofrece un historial completo y verificable para esta ficha.",14,DIM));});}
 private void leagueProfile(){
  League l=selectedLeague;add(body,button("← Volver",()->{screen="home";draw();}));
  title(l.title);add(body,label(l.group+" · "+l.sport+" · datos de ESPN",14,DIM));
  add(body,button(store.follows(l)?"★ Quitar de favoritas":"☆ Agregar a favoritas",()->{store.toggleLeague(l);draw();}));
  title("Partidos recientes y próximos");LinearLayout gamesBlock=vertical();add(body,gamesBlock);add(gamesBlock,label("Consultando calendario…",14,DIM));
  async(()->{List<Models.Match> games=new ArrayList<>();LocalDate day=LocalDate.now().minusDays(2);
   for(int offset=0;offset<6;offset+=3){String range=day.plusDays(offset).format(DateTimeFormatter.BASIC_ISO_DATE)+"-"+day.plusDays(offset+2).format(DateTimeFormatter.BASIC_ISO_DATE);
    games.addAll(Models.matches(api.scores(l,range,false).json()));}
   games.sort(Comparator.comparing(Models.Match::date));return games;
  },res->{gamesBlock.removeAllViews();List<?> games=(List<?>)res;
   if(games.isEmpty())add(gamesBlock,label("No hay partidos publicados en estos días. Algunas copas juegan solo en fechas concretas.",14,DIM));
   for(Object item:games)matchCard(gamesBlock,l,(Models.Match)item);
  });
  async(()->{try{return Models.standings(api.standings(l,false).json());}catch(Exception e){return Collections.<Models.Standing>emptyList();}},res->{
   title("Tabla actual");List<?> rows=(List<?>)res;
   if(rows.isEmpty())add(body,label("Esta competición no publica una tabla disponible en la fuente actual.",14,DIM));
   else {List<Models.Standing> standings=new ArrayList<>();for(Object row:rows)standings.add((Models.Standing)row);standingsTable(standings);}
   title("Historia y curiosidades");add(body,label("El historial completo de campeones y goleadores no está disponible de manera uniforme en esta fuente.",14,DIM));
  });
 }
 private void settings(){
  title("Configuración");add(body,label("Personaliza tus deportes y controla las notificaciones.",14,DIM));
  title("Tus favoritos");add(body,button("★  Editar equipos favoritos  →",()->{screen="teams";draw();}));
  add(body,button("▤  Editar ligas favoritas  →",()->{screen="leagues";draw();}));
  title("Notificaciones");boolean active=store.trackingUntil()>System.currentTimeMillis();
  add(body,button(active?"●  Detener seguimiento":"○  Activar seguimiento (2 horas)",this::alerts));
  add(body,label(active?"Sesión de seguimiento activa.":"Activa el seguimiento para recibir los eventos disponibles de tus favoritos.",14,active?GREEN:DIM));
  add(body,label("Las alertas dependen de la cobertura de cada partido, la conexión y las restricciones de Android.",13,DIM));
  title("Acerca de");add(body,label("PulseSport 1.5.0 · datos deportivos públicos de ESPN.",13,DIM));
 }
 private void alerts(){if(store.trackingUntil()>System.currentTimeMillis()){stopService(new Intent(this,TrackingService.class));store.trackingUntil(0);draw();return;}if(android.os.Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},101);return;}try{startForegroundService(new Intent(this,TrackingService.class));Toast.makeText(this,"Seguimiento iniciado",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"No se pudo iniciar el seguimiento",Toast.LENGTH_LONG).show();}}
 @Override public void onRequestPermissionsResult(int request,String[] p,int[] grants){super.onRequestPermissionsResult(request,p,grants);if(request==101&&grants.length>0&&grants[0]==PackageManager.PERMISSION_GRANTED)alerts();}
 @Override public void onBackPressed(){if(!store.onboarded()){screen="onboard";draw();return;}screen="home";draw();}
}
