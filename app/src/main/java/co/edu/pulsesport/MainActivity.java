package co.edu.pulsesport;
import android.Manifest;import android.app.*;import android.content.*;import android.content.pm.PackageManager;import android.graphics.Typeface;import android.graphics.drawable.GradientDrawable;import android.os.*;import android.view.*;import android.widget.*;import org.json.*;import java.time.*;import java.time.format.*;import java.util.*;import java.util.concurrent.*;
public class MainActivity extends Activity {
 private static final int BG=0xff080c0f,CARD=0xff182125,FG=0xfff4f7fb,DIM=0xffaabbcb,GREEN=0xff55e6b1,BLUE=0xff54b8ff,LINE=0xff283337,GOLD=0xffffd166;
 private static final class CountdownView {
  final TextView text;final String kickoff;
  CountdownView(TextView text,String kickoff){this.text=text;this.kickoff=kickoff;}
 }
 private final List<CountdownView> countdowns=new ArrayList<>();private boolean resumed=false;
 private final Runnable countdownTick=new Runnable(){@Override public void run(){
  if(!resumed)return;long now=System.currentTimeMillis();
  for(CountdownView item:countdowns)if(item.text.getParent()!=null)item.text.setText(MatchTime.remaining(item.kickoff,now));
  main.postDelayed(this,1000);
 }};
 private void countdown(TextView text,String date){countdowns.add(new CountdownView(text,date));text.setText(MatchTime.remaining(date,System.currentTimeMillis()));}
 @Override protected void onResume(){super.onResume();resumed=true;main.removeCallbacks(countdownTick);main.post(countdownTick);}
 @Override protected void onPause(){resumed=false;main.removeCallbacks(countdownTick);super.onPause();}
 private final ExecutorService io=Executors.newSingleThreadExecutor();private final Handler main=new Handler(Looper.getMainLooper());private Store store;private Repository api;private LinearLayout page,body;private String screen="home";private League league=League.PREMIER,selectedLeague=League.PREMIER;private Models.Team selectedTeam;private Models.Match selectedMatch;private int generation=0,week=0;private String feedFilter="Próximos",detailTab="Resumen";private League feedLeague=null;private String homeTab="Favoritos",teamTab="Resumen";
 @Override public void onCreate(Bundle b){super.onCreate(b);store=new Store(this);api=new Repository(this);TrackingService.channels(this);if(b!=null){screen=b.getString("screen","home");week=b.getInt("week",0);}if(!store.onboarded()&&screen.equals("home"))screen="onboard";draw();}
 @Override protected void onSaveInstanceState(Bundle b){super.onSaveInstanceState(b);b.putString("screen",screen.equals("match")||screen.equals("team")||screen.equals("league")?"home":screen);b.putInt("week",week);}
 @Override protected void onDestroy(){generation++;if(historicalTask!=null)historicalTask.cancel(true);main.removeCallbacks(countdownTick);io.shutdownNow();super.onDestroy();}
 private int dp(int n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}private TextView label(String s,int size,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setPadding(dp(4),dp(9),dp(4),dp(9));return v;}
 private LinearLayout vertical(){LinearLayout v=new LinearLayout(this);v.setOrientation(1);return v;}private LinearLayout row(){LinearLayout v=new LinearLayout(this);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
 private GradientDrawable bg(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(12));return d;}
 private void add(LinearLayout parent,View v){parent.addView(v,new LinearLayout.LayoutParams(-1,-2));}
 private void flex(LinearLayout parent,View v){parent.addView(v,new LinearLayout.LayoutParams(0,-2,1));}
 private Button button(String name,Runnable action){Button b=new Button(this);b.setAllCaps(false);b.setText(name);b.setTextColor(FG);b.setTextSize(14);b.setMinHeight(dp(48));b.setMinWidth(0);b.setMinimumWidth(0);b.setPadding(dp(12),dp(8),dp(12),dp(8));b.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x3354b8ff),bg(CARD),null));b.setOnClickListener(v->action.run());return b;}
 private LinearLayout card(){LinearLayout c=vertical();c.setPadding(dp(13),dp(11),dp(13),dp(11));c.setBackground(bg(CARD));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(7),0,dp(7));body.addView(c,p);return c;}
 private void title(String s){TextView t=label(s,19,FG);t.setTypeface(null,Typeface.BOLD);add(body,t);}
 private ImageButton searchControl(Runnable action){
  ImageButton control=new ImageButton(this);control.setImageResource(android.R.drawable.ic_menu_search);
  control.setColorFilter(BLUE);control.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
  control.setPadding(dp(11),dp(11),dp(11),dp(11));control.setMinimumWidth(dp(56));control.setMinimumHeight(dp(56));
  control.setBackground(bg(CARD));control.setContentDescription("Buscar equipos");control.setOnClickListener(v->action.run());return control;
 }
 private int contentWidth(){return Math.min(dp(720),getResources().getDisplayMetrics().widthPixels-dp(32));}
 private void draw(){
  generation++;if(historicalTask!=null)historicalTask.cancel(true);main.removeCallbacks(countdownTick);countdowns.clear();page=vertical();page.setGravity(Gravity.CENTER_HORIZONTAL);page.setBackgroundColor(BG);
  page.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(dp(16),i.getSystemWindowInsetTop(),dp(16),i.getSystemWindowInsetBottom());return i;});
  setContentView(page);LinearLayout header=row();header.setMinimumHeight(dp(64));
  TextView brand=label("PulseSport",24,FG);brand.setTypeface(null,Typeface.BOLD);flex(header,brand);
  if(store.onboarded()){
   ImageButton search=searchControl(()->{screen="teams";draw();});
   LinearLayout.LayoutParams searchParams=new LinearLayout.LayoutParams(dp(56),dp(56));searchParams.leftMargin=dp(8);header.addView(search,searchParams);
  }
  page.addView(header,new LinearLayout.LayoutParams(contentWidth(),-2));
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);
  body=vertical();body.setPadding(0,dp(8),0,dp(24));scroll.addView(body,new android.widget.FrameLayout.LayoutParams(contentWidth(),-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL));
  page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  switch(screen){case "onboard":onboard();break;case "teams":teamPicker(false);break;case "leagues":leaguePicker(false);break;case "match":match();break;case "team":teamProfile();break;case "league":leagueProfile();break;case "settings":settings();break;case "credits":credits();break;case "favorites":favorites();break;case "live":feedFilter="En vivo";home();break;default:home();}
  LinearLayout nav=row();nav.setPadding(dp(2),dp(3),dp(2),dp(3));nav.setBackground(bg(CARD));
  String[] names={"Inicio","En vivo","Favoritos","Ligas","Ajustes"};String[] icons={"⌂","●","★","▤","⚙"};String[] targets={"home","live","favorites","leagues","settings"};
  for(int i=0;i<names.length;i++){
   final String target=targets[i];LinearLayout item=vertical();item.setGravity(Gravity.CENTER);item.setMinimumHeight(dp(64));item.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x3354b8ff),null,null));
   boolean active=screen.equals(target);TextView icon=label(icons[i],21,active?BLUE:DIM);icon.setGravity(Gravity.CENTER);icon.setPadding(0,0,0,0);add(item,icon);
   TextView caption=label(names[i],11,active?FG:DIM);caption.setGravity(Gravity.CENTER);caption.setPadding(0,0,0,0);add(item,caption);
   item.setContentDescription(names[i]);item.setOnClickListener(v->{screen=!store.onboarded()&&!target.equals("settings")?"onboard":target;
    if(screen.equals("home")){feedFilter="Próximos";feedLeague=null;}draw();});nav.addView(item,new LinearLayout.LayoutParams(0,dp(64),1));
  }
  LinearLayout.LayoutParams navParams=new LinearLayout.LayoutParams(contentWidth(),-2);navParams.topMargin=dp(4);page.addView(nav,navParams);
  if(resumed)main.post(countdownTick);
 }
 private void async(Callable<?> work,java.util.function.Consumer<Object> success){int token=generation;io.execute(()->{try{Object result=work.call();main.post(()->{if(token==generation&&!isFinishing()){success.accept(result);}});}catch(Exception e){main.post(()->{if(token==generation){add(body,label(DataErrors.describe(e)+" "+e.getMessage(),14,DIM));add(body,button("Reintentar",this::draw));}});}});}
 private void onboard(){title("Haz tu deporte tuyo");add(body,label("1 de 2 · Elige equipos populares o busca el tuyo. Puedes cambiar tu selección después.",16,DIM));teamPicker(true);}
 private void teamPicker(boolean onboarding){
  if(!onboarding)title("Buscar equipos y ligas");
  add(body,label(onboarding?"Elige tus equipos. Puedes buscarlos o continuar sin seleccionar.":"Abre un perfil tocando su escudo. Usa la estrella para seguirlo.",14,DIM));
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
    java.util.Set<String> seen=new java.util.HashSet<>();entries.removeIf(entry->!seen.add(((League)entry[0]).apiSport+":"+((Models.Team)entry[1]).id()));
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
  if(!input.trim().isEmpty()){for(League competition:League.values())if(competition.title.toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT))){
   add(choices,leagueLink(competition));}}
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
   tile.setOnClickListener(v->openTeam(l,t));tile.setContentDescription(t.name()+", "+l.title+(store.favorite(l,t.id())?", favorito":""));
  }
  if(count==0)add(choices,label(entries.isEmpty()?"No se pudieron cargar equipos. Comprueba la conexión y pulsa Actualizar.":"No encontramos ese equipo en estas ligas.",14,DIM));
  if(count>0&&count%3!=0){for(int missing=count%3;missing<3;missing++){View spacer=new View(this);line.addView(spacer,new LinearLayout.LayoutParams(0,dp(1),1));}}
  if(query.isEmpty()&&entries.size()>24)add(choices,label("Mostrando 24 equipos. Escribe el nombre para encontrar otros.",12,DIM));
 }
 private boolean isPopular(String s,String[] names){for(String x:names)if(s.equalsIgnoreCase(x))return true;return false;}
 private ImageView crest(String url){ImageView image=new ImageView(this);image.setLayoutParams(new LinearLayout.LayoutParams(dp(44),dp(44)));image.setScaleType(ImageView.ScaleType.FIT_CENTER);Crest.into(this,image,url);return image;}
 private LinearLayout leagueLink(League l){
  LinearLayout row=row();
  row.addView(crest(l.logo));
  flex(row,button(l.title+" →",()->{selectedLeague=l;screen="league";draw();}));
  return row;
 }
 private void leaguePicker(boolean ignored){
  title(store.onboarded()?"Ligas favoritas":"2 de 2 · Elige tus ligas");
  add(body,label("Sigue tus competiciones para verlas en Inicio y recibir alertas durante el seguimiento.",14,DIM));
  String group="";for(League l:League.values()){
   if(!l.group.equals(group)){group=l.group;TextView heading=label(group,16,BLUE);heading.setTypeface(null,Typeface.BOLD);add(body,heading);}
   LinearLayout c=card();add(c,button((store.follows(l)?"★  ":"☆  ")+l.title,()->{store.toggleLeague(l);draw();}));
   add(c,leagueLink(l));
  }
  if(!store.onboarded())add(page,button("Terminar y ver partidos →",()->{store.completeOnboarding();screen="home";draw();}));
 }
 private String shortDate(String raw){return MatchTime.localDate(raw,ZoneId.systemDefault());}
 private void filters(){
  LinearLayout tabs=row();for(String name:new String[]{"Favoritos","Resultados"}){
   LinearLayout tab=vertical();tab.setGravity(Gravity.CENTER);TextView text=label(name.toUpperCase(Locale.ROOT),15,homeTab.equals(name)?FG:DIM);text.setTypeface(null,homeTab.equals(name)?Typeface.BOLD:Typeface.NORMAL);text.setGravity(Gravity.CENTER);add(tab,text);
   View line=new View(this);line.setBackgroundColor(homeTab.equals(name)?BLUE:LINE);tab.addView(line,new LinearLayout.LayoutParams(-1,dp(homeTab.equals(name)?3:1)));
   tab.setMinimumHeight(dp(56));tab.setOnClickListener(v->{homeTab=name;feedLeague=null;week=0;draw();});flex(tabs,tab);
  }add(body,tabs);
  HorizontalScrollView scroll=new HorizontalScrollView(this);scroll.setHorizontalScrollBarEnabled(false);LinearLayout chips=row();
  for(String filter:new String[]{"Próximos","En vivo","Mis ligas"}){
   Button chip=button(filter,()->{feedFilter=filter;if(!filter.equals("Mis ligas"))feedLeague=null;screen=filter.equals("En vivo")?"live":"home";week=0;draw();});
   chip.setTextSize(13);chip.setTextColor(feedFilter.equals(filter)?BLUE:DIM);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(112),dp(48));lp.setMargins(0,dp(8),dp(8),dp(8));chips.addView(chip,lp);
  }scroll.addView(chips);add(body,scroll);
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
  title("Tu deporte, a tu manera");add(body,label("Sigue equipos y competiciones para personalizar tus resultados.",14,DIM));
  LinearLayout actions=row();flex(actions,button("＋ Equipos",()->{screen="teams";draw();}));flex(actions,button("＋ Competiciones",()->{screen="leagues";draw();}));add(body,actions);
  title("Competiciones que sigues");boolean any=false;String group="";
  for(League l:League.values())if(store.follows(l)){any=true;if(!l.group.equals(group)){group=l.group;add(body,label(group,13,BLUE));}
   LinearLayout item=card();add(item,button("★  "+l.title+"  →",()->{selectedLeague=l;screen="league";draw();}));}
  if(!any)add(body,label("Sigue tu primera competición para ver sus partidos aquí.",14,DIM));
  title("Tus equipos");if(store.favorites().isEmpty())add(body,label("Busca un equipo y toca la estrella para seguirlo.",14,DIM));
  for(String key:store.favorites()){int split=key.indexOf(':');if(split<0)continue;try{League l=League.valueOf(key.substring(0,split));
   LinearLayout item=card();add(item,label(store.teamName(key),17,FG));add(item,label(l.title,12,DIM));add(item,button("Ver equipo →",()->{openTeam(l,new Models.Team(key.substring(split+1),store.teamName(key),"",""));}));
  }catch(Exception ignored){}}
 }
 private boolean hasFavoriteTeam(League l){for(String key:store.favorites())if(key.startsWith(l.name()+":"))return true;return false;}
 private void matchCard(League l,Models.Match m){matchCard(body,l,m);}
 private void matchCard(LinearLayout parent,League l,Models.Match m){
  LinearLayout c=vertical();c.setPadding(dp(12),dp(8),dp(12),dp(8));c.setBackground(bg(CARD));
  LinearLayout.LayoutParams outer=new LinearLayout.LayoutParams(-1,-2);outer.bottomMargin=dp(8);parent.addView(c,outer);
  String state=m.live()?"● EN VIVO  ·  "+m.status():m.state().equals("post")?"FINALIZADO":"PRÓXIMO";
  TextView status=label(state,12,m.live()?GREEN:DIM);status.setGravity(Gravity.CENTER);status.setTypeface(null,Typeface.BOLD);add(c,status);
  LinearLayout middle=row();middle.setGravity(Gravity.CENTER_VERTICAL);
  teamScoreColumn(middle,l,m.home());
  String score=m.state().equals("pre")?MatchTime.remaining(m.date(),System.currentTimeMillis()):m.score();
  LinearLayout center=vertical();center.setGravity(Gravity.CENTER);TextView main=label(score,m.state().equals("pre")?21:31,m.state().equals("pre")?BLUE:FG);main.setSingleLine(true);main.setGravity(Gravity.CENTER);main.setTypeface(null,Typeface.BOLD);main.setFontFeatureSettings("tnum");main.setPadding(0,0,0,0);add(center,main);if(m.state().equals("pre"))countdown(main,m.date());
  TextView time=label(m.live()?m.status():m.state().equals("post")?"FINAL":shortDate(m.date()),11,m.live()?GREEN:DIM);time.setGravity(Gravity.CENTER);time.setMaxLines(2);add(center,time);
  middle.addView(center,new LinearLayout.LayoutParams(0,-2,1.25f));teamScoreColumn(middle,l,m.away());add(c,middle);
  c.setContentDescription(l.title+", "+m.home().name()+" contra "+m.away().name()+", "+state+", "+score);
  c.setOnClickListener(v->{league=l;selectedMatch=m;detailTab="Resumen";screen="match";draw();});
 }
 private void teamScoreColumn(LinearLayout parent,League l,Models.Team team){
  LinearLayout col=vertical();col.setGravity(Gravity.CENTER);ImageView img=crest(team.logo());img.setLayoutParams(new LinearLayout.LayoutParams(dp(44),dp(44)));col.addView(img);
  TextView name=label((store.favorite(l,team.id())?"★ ":"")+team.name(),14,FG);name.setGravity(Gravity.CENTER);name.setMaxLines(2);name.setMinHeight(dp(46));add(col,name);
  parent.addView(col,new LinearLayout.LayoutParams(0,-2,1));
 }
 private void openTeam(League l,Models.Team t){league=l;selectedTeam=t;teamTab="Resumen";screen="team";draw();}
 private void match(){
  Models.Match m=selectedMatch;if(m==null){screen="home";draw();return;}
  add(body,button("← Partidos",()->{screen="home";draw();}));
  add(body,button(league.title+"  →",()->{selectedLeague=league;screen="league";draw();}));
  LinearLayout scoreboard=row();scoreboard.setPadding(dp(8),dp(16),dp(8),dp(16));scoreboard.setBackground(bg(CARD));
  teamColumn(scoreboard,league,m.home());
  TextView score=label(m.state().equals("pre")?MatchTime.remaining(m.date(),System.currentTimeMillis()):m.score(),m.state().equals("pre")?22:36,m.live()?GREEN:FG);if(m.state().equals("pre"))countdown(score,m.date());
  score.setTypeface(null,Typeface.BOLD);score.setGravity(Gravity.CENTER);score.setMinWidth(dp(100));scoreboard.addView(score);
  teamColumn(scoreboard,league,m.away());add(body,scoreboard);
  TextView status=label((m.live()?"● EN VIVO · ":"")+m.status()+" · "+shortDate(m.date()),13,m.live()?GREEN:DIM);status.setGravity(Gravity.CENTER);add(body,status);
  HorizontalScrollView tabScroll=new HorizontalScrollView(this);tabScroll.setHorizontalScrollBarEnabled(false);LinearLayout tabs=row();
  for(String name:new String[]{"Resumen","Alineaciones","Estadísticas","Posiciones","Minuto a minuto"}){
   Button tab=button(name,()->{detailTab=name;draw();});tab.setTextSize(13);tab.setTextColor(detailTab.equals(name)?BLUE:DIM);tabs.addView(tab,new LinearLayout.LayoutParams(-2,dp(48)));
  }tabScroll.addView(tabs);add(body,tabScroll);
  if(detailTab.equals("Posiciones")){async(()->api.standings(league,false),res->{try{standingsTable(body,league,Models.standings(((Repository.Result)res).json()));}catch(Exception e){add(body,label("Clasificación no disponible para esta fase.",14,DIM));}});return;}
  if(detailTab.equals("Alineaciones")||detailTab.equals("Estadísticas")){
   title(detailTab);add(body,label("Consultando información del encuentro…",13,DIM));
   async(()->api.summary(league,m.id()),res->{JSONObject data=((Repository.Result)res).json();
    if(detailTab.equals("Alineaciones"))renderLineups(data);else renderStatistics(data);
   });return;
  }
  title(detailTab.equals("Resumen")?"Momentos del partido":"Minuto a minuto");
  if(m.timeline().isEmpty())add(body,label("Todavía no hay incidencias publicadas.",14,DIM));
  int from=detailTab.equals("Resumen")?Math.max(0,m.timeline().size()-5):0;
  for(int i=m.timeline().size()-1;i>=from;i--){String event=m.timeline().get(i);LinearLayout item=card();
   String lower=event.toLowerCase(Locale.ROOT);int color=lower.contains("goal")||lower.contains("gol")?GOLD:lower.contains("red card")?0xffff6b70:FG;
   LinearLayout eventRow=row();TextView dot=label("●",17,color);eventRow.addView(dot);flex(eventRow,label(event,14,color));add(item,eventRow);
  }
  add(body,button("↻ Actualizar partido",()->{async(()->api.scores(league,LocalDate.now().minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE)+"-"+LocalDate.now().plusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE),true),res->{try{for(Models.Match x:Models.matches(((Repository.Result)res).json()))if(x.id().equals(m.id())){selectedMatch=x;break;}}catch(Exception ignored){}draw();});}));
 }
 private void renderLineups(JSONObject data){
  JSONArray rosters=data.optJSONArray("rosters");boolean shown=false;
  if(rosters!=null)for(int i=0;i<rosters.length();i++){JSONObject roster=rosters.optJSONObject(i);if(roster==null)continue;JSONObject team=roster.optJSONObject("team");title(team==null?"Equipo":team.optString("displayName","Equipo"));
   JSONArray players=roster.optJSONArray("roster");if(players==null)continue;
   for(int j=0;j<players.length();j++){JSONObject player=players.optJSONObject(j);if(player==null)continue;JSONObject athlete=player.optJSONObject("athlete");if(athlete==null)continue;shown=true;
    LinearLayout item=card();add(item,label(player.optString("jersey",athlete.optString("jersey",""))+"  "+athlete.optString("displayName"),15,FG));
    JSONObject position=player.optJSONObject("position");if(position!=null)add(item,label(position.optString("displayName",position.optString("abbreviation")),12,DIM));
   }
  }if(!shown)add(body,label("Las alineaciones todavía no han sido publicadas para este partido.",14,DIM));
 }
 private void renderStatistics(JSONObject data){
  JSONObject box=data.optJSONObject("boxscore");JSONArray teams=box==null?null:box.optJSONArray("players");
  if(teams==null&&box!=null)teams=box.optJSONArray("teams");boolean shown=false;
  if(teams!=null)for(int i=0;i<teams.length();i++){JSONObject entry=teams.optJSONObject(i);if(entry==null)continue;JSONObject team=entry.optJSONObject("team");JSONArray stats=entry.optJSONArray("statistics");if(stats==null)continue;
   boolean heading=false;for(int j=0;j<stats.length();j++){JSONObject stat=stats.optJSONObject(j);if(stat==null)continue;
    String name=stat.optString("label",stat.optString("displayName",stat.optString("name")));String lower=name.toLowerCase(Locale.ROOT);
    if(lower.contains("expected")||lower.contains("esperad")||lower.contains("odds")||lower.contains("predict")||lower.equals("xg"))continue;
    String value=stat.optString("displayValue","");if(value.isEmpty())continue;
    if(!heading){title(team==null?"Estadísticas":team.optString("displayName","Equipo"));heading=true;}
    shown=true;LinearLayout line=row();flex(line,label(name,14,DIM));line.addView(label(value,17,FG));add(body,line);
   }
  }if(!shown)add(body,label("No hay estadísticas publicadas para este encuentro.",14,DIM));
 }
 private void teamColumn(LinearLayout parent,League l,Models.Team team){
  LinearLayout col=vertical();col.setGravity(Gravity.CENTER);ImageView icon=crest(team.logo());icon.setLayoutParams(new LinearLayout.LayoutParams(dp(48),dp(48)));icon.setContentDescription("Perfil de "+team.name());col.addView(icon);TextView name=label(team.name(),12,FG);name.setGravity(Gravity.CENTER);name.setMaxLines(2);add(col,name);
  parent.addView(col,new LinearLayout.LayoutParams(0,-2,1));col.setOnClickListener(v->openTeam(l,team));
 }
 private void standingsTable(LinearLayout target,League competition,List<Models.Standing> rows){
  boolean basketball=competition==League.NBA;
  if(rows.isEmpty()){add(target,label("Tabla no disponible.",14,DIM));return;}
  HorizontalScrollView scroll=new HorizontalScrollView(this);LinearLayout table=vertical();scroll.addView(table);add(target,scroll);
  String group=null;for(Models.Standing st:rows){if(!st.group().equals(group)){group=st.group();add(table,label(group,14,BLUE));
   LinearLayout heading=row();tableCell(heading,"#",30);tableCell(heading,"Equipo",190);for(String h:basketball?new String[]{"% G","PJ","G","P","PF","PC","DIF"}:new String[]{"PTS","PJ","G","E","P","GF","GC","DG"})tableCell(heading,h,48);add(table,heading);}
   LinearLayout line=row();line.setBackground(bg(CARD));tableCell(line,st.rank(),30);
   ImageView crest=crest(st.logo());crest.setLayoutParams(new LinearLayout.LayoutParams(dp(28),dp(28)));line.addView(crest);tableCell(line,st.team(),162);
   for(String v:basketball?new String[]{st.points(),st.played(),st.wins(),st.losses(),st.goalsFor(),st.goalsAgainst(),st.difference()}:new String[]{st.points(),st.played(),st.wins(),st.draws(),st.losses(),st.goalsFor(),st.goalsAgainst(),st.difference()})tableCell(line,v,48);add(table,line);
  }
 }
 private void tableCell(LinearLayout row,String value,int width){TextView text=label(value,13,FG);text.setSingleLine(true);text.setEllipsize(android.text.TextUtils.TruncateAt.END);row.addView(text,new LinearLayout.LayoutParams(dp(width),dp(48)));}
 private void teamProfile(){
  Models.Team t=selectedTeam;if(t==null){screen="home";draw();return;}
  add(body,button("← Volver al partido",()->{screen=selectedMatch==null?"home":"match";draw();}));
  FrameLayout hero=new FrameLayout(this);hero.setBackground(bg(CARD));
  ImageView stadium=new ImageView(this);stadium.setScaleType(ImageView.ScaleType.CENTER_CROP);hero.addView(stadium,new FrameLayout.LayoutParams(-1,-1));
  View shade=new View(this);shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0x33101820,0xee080c0f}));hero.addView(shade,new FrameLayout.LayoutParams(-1,-1));
  LinearLayout identity=vertical();identity.setGravity(Gravity.CENTER);ImageView icon=crest(t.logo());icon.setLayoutParams(new LinearLayout.LayoutParams(dp(72),dp(72)));identity.addView(icon);
  TextView name=label(t.name(),24,FG);name.setTypeface(null,Typeface.BOLD);name.setGravity(Gravity.CENTER);add(identity,name);
  TextView subtitle=label(league.title,13,DIM);subtitle.setGravity(Gravity.CENTER);add(identity,subtitle);
  hero.addView(identity,new FrameLayout.LayoutParams(-1,-2,Gravity.CENTER));body.addView(hero,new LinearLayout.LayoutParams(-1,dp(230)));
  add(body,button(store.favorite(league,t.id())?"★ Siguiendo equipo":"☆ Seguir equipo",()->{store.toggle(league,t);draw();}));
  LinearLayout tabs=row();for(String tab:new String[]{"Resumen","Partidos","Posiciones","Club"}){Button choice=button(tab,()->{teamTab=tab;draw();});choice.setTextSize(11);choice.setTextColor(teamTab.equals(tab)?BLUE:DIM);flex(tabs,choice);}add(body,tabs);
  League profileLeague=league;
  ClubProfile profile=ClubProfile.find(profileLeague,t.id());
  if(profile!=null&&!profile.asset().isEmpty()){
   StadiumPhoto.fromAsset(this,stadium,profile);
  }else{
   TextView venue=label("Buscando estadio…",12,DIM);
   add(body,venue);
   StadiumPhoto.into(this,stadium,profileLeague,t,api,photo->{
    venue.setText(profile==null?photo.caption():profile.stadium());
    if(!photo.source().isEmpty())rememberCredit(t.name()+" · Foto",photo.source());
   });
  }
  if(teamTab.equals("Club")){
   if(profile==null)ClubFacts.load(this,body,profileLeague,t,
     source->rememberCredit(t.name()+" · Ficha",source));
   else{
    title(profile.name());
    add(body,label(profile.stadium()+"\n"+profile.facts(),15,FG));
    title("Palmarés");
    for(String trophy:ClubProfile.honors(t.id()))add(body,label(trophy,15,FG));
   }
  }
  if(teamTab.equals("Resumen")||teamTab.equals("Posiciones"))async(()->api.standings(league,false),res->{
   title("Posición · "+league.title);try{List<Models.Standing> rows=Models.standings(((Repository.Result)res).json());if(teamTab.equals("Posiciones"))standingsTable(body,league,rows);else{boolean found=false;for(Models.Standing st:rows)if(st.team().equalsIgnoreCase(t.name())){found=true;add(card(),label("#"+st.rank()+"  ·  "+st.played()+" PJ  ·  "+(league==League.NBA?st.points()+" de victorias":st.points()+" puntos"),18,FG));}if(!found)add(body,label("Sin posición publicada para este equipo.",14,DIM));}}catch(Exception e){add(body,label("Clasificación no disponible.",14,DIM));}
  });
  if(teamTab.equals("Resumen")||teamTab.equals("Partidos")){
   League competition=league;
   int limit=teamTab.equals("Resumen")?5:Integer.MAX_VALUE;
   LinearLayout history=vertical();
   add(body,history);
   async(()->Models.matches(api.schedule(competition,t.id()).json()),res->{
    List<Models.Match> played=new ArrayList<>(), upcoming=new ArrayList<>(), live=new ArrayList<>();
    for(Object item:(List<?>)res){
     Models.Match game=(Models.Match)item;
     switch(game.state()){
      case "post": played.add(game);break;
      case "pre": upcoming.add(game);break;
      case "in": live.add(game);break;
     }
    }
    Comparator<Models.Match> byDate=Comparator.comparing(
      game->MatchTime.parse(game.date()),Comparator.nullsLast(Comparator.naturalOrder()));
    played.sort(Comparator.comparing(
      game->MatchTime.parse(game.date()),Comparator.nullsLast(Comparator.reverseOrder())));
    upcoming.sort(byDate);
    live.sort(byDate);
    matchSection(history,competition,"Resultados",played,limit);
    matchSection(history,competition,"En vivo",live,limit);
    matchSection(history,competition,"Próximos partidos",upcoming,limit);
    if(played.isEmpty()&&live.isEmpty()&&upcoming.isEmpty())
     add(history,label("Sin calendario publicado.",14,DIM));
   });
  }
 }
 private void rememberCredit(String name,String url){
  getSharedPreferences("credits",MODE_PRIVATE).edit().putString(name+" · "+android.net.Uri.parse(url).getHost(),url).apply();
 }
 private void credits(){
  add(body,button("← Ajustes",()->{screen="settings";draw();}));
  title("Créditos");
  creditLink("ESPN · Resultados, clasificaciones, escudos y algunas fotografías","https://www.espn.com/");
  creditLink("Wikidata · Fichas y palmarés","https://www.wikidata.org/");
  creditLink("Wikimedia Commons · Fotografías","https://commons.wikimedia.org/");
  creditLink("NBA · Historial de campeones","https://www.nba.com/news/history-nba-champions");
  title("Fuentes de clubes y fotografías locales");
  for(String id:List.of("83","5484","6137","2919","2029")){
   ClubProfile profile=ClubProfile.find(League.PREMIER,id);
   for(String source:profile.sources()){
    String host=android.net.Uri.parse(source).getHost();
    creditLink(profile.name()+" · "+host,source);
   }
   if(!profile.photoSource().isEmpty())creditLink(profile.stadium()+" · "+profile.credit()+" (encuadre adaptado)",profile.photoSource());
   if(!profile.license().isEmpty())creditLink(profile.stadium()+" · Licencia de la foto",profile.license());
  }
  JSONObject photoCatalog=ClubFacts.catalog(this,"stadium_catalog.json");
  for(String id:List.of("soccer:363","soccer:86")){
   JSONObject entry=photoCatalog.optJSONObject(id);
   if(entry==null||!entry.has("asset"))continue;
   String stadium=entry.optString("stadium");
   creditLink(stadium+" · "+entry.optString("credit")+" (encuadre adaptado)",entry.optString("source"));
   creditLink(stadium+" · Licencia de la foto",entry.optString("license"));
  }
  Map<String,?> opened=getSharedPreferences("credits",MODE_PRIVATE).getAll();
  if(!opened.isEmpty())title("Fuentes de perfiles consultados");
  for(String name:new TreeSet<>(opened.keySet())){
   Object url=opened.get(name);
   if(url instanceof String)creditLink(name,(String)url);
  }
 }
 private void creditLink(String name,String url){
  add(body,button(name,()->openSource(url)));
 }
 private void openSource(String url){
  try{startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(url)));}
  catch(ActivityNotFoundException error){Toast.makeText(this,url,Toast.LENGTH_LONG).show();}
 }
 private void matchSection(LinearLayout target,League competition,String name,List<Models.Match> games,int limit){
  if(games.isEmpty())return;
  add(target,label(name,18,FG));
  for(int i=0;i<Math.min(limit,games.size());i++)matchCard(target,competition,games.get(i));
 }
 private long historicalRequest;
 private Future<?> historicalTask;
 private void historicalTable(League competition){
  title("Historial por temporada");
  LinearLayout results=vertical();
  add(body,button("Elegir temporada",()->async(()->api.seasons(competition),res->{
   String[] years=((List<?>)res).stream().map(Object::toString).toArray(String[]::new);
   if(years.length==0){
    results.removeAllViews();
    add(results,label("No hay temporadas disponibles para esta competición.",14,DIM));
    return;
   }
   new AlertDialog.Builder(this).setTitle("Temporadas disponibles")
    .setItems(years,(dialog,index)->showHistoricalTable(competition,Integer.parseInt(years[index]),results))
    .show();
  })));
  add(body,results);
 }
 private void showHistoricalTable(League competition,int season,LinearLayout results){
  results.removeAllViews();
  add(results,label("Temporada "+season,18,FG));
  LinearLayout phases=vertical();
  LinearLayout detail=vertical();
  add(results,phases);
  add(results,detail);
  add(phases,label("Consultando fases…",14,DIM));
  int screenToken=generation;
  long request=++historicalRequest;
  if(historicalTask!=null)historicalTask.cancel(true);
  historicalTask=io.submit(()->{
   try{
    List<Repository.Phase> options=api.phases(competition,season);
    main.post(()->{
     if(screenToken!=generation||request!=historicalRequest||isFinishing())return;
     phases.removeAllViews();
     if(options.isEmpty()){
      loadHistoricalPhase(competition,season,null,detail);
      return;
     }
     HorizontalScrollView phaseScroll=new HorizontalScrollView(this);
     LinearLayout choices=row();
     phaseScroll.addView(choices);
     add(phases,phaseScroll);
     for(Repository.Phase phase:options){
      choices.addView(button(phaseName(phase),()->loadHistoricalPhase(competition,season,phase,detail)));
     }
     Repository.Phase initial=options.stream().filter(Repository.Phase::standings).findFirst().orElse(null);
     if(initial==null&&options.size()==1)initial=options.get(0);
     if(initial!=null)loadHistoricalPhase(competition,season,initial,detail);
     else add(detail,label("Elige una ronda para ver los cruces y marcadores.",14,DIM));
    });
   }catch(Exception error){
    main.post(()->{
     if(screenToken!=generation||request!=historicalRequest||isFinishing())return;
     phases.removeAllViews();
     add(phases,label("Fases sin respuesta. "+DataErrors.describe(error),14,DIM));
     add(phases,button("Reintentar fases",()->showHistoricalTable(competition,season,results)));
     loadHistoricalPhase(competition,season,null,detail);
    });
   }
  });
 }
 private String phaseName(Repository.Phase phase){
  return switch(phase.slug()){
   case "round-of-16" -> "Octavos de final";
   case "quarterfinals" -> "Cuartos de final";
   case "semifinals" -> phase.standings()?phase.name():"Semifinal";
   case "final","finals" -> phase.standings()?phase.name():"Final";
   case "group-stage" -> "Fase de grupos";
   default -> phase.name();
  };
 }
 private void loadHistoricalPhase(League competition,int season,Repository.Phase phase,LinearLayout target){
  target.removeAllViews();
  String title=phase==null?"Clasificación":phaseName(phase);
  add(target,label(title+" · "+season,16,FG));
  LinearLayout detail=vertical();
  add(target,detail);
  add(detail,label("Consultando…",14,DIM));
  int screenToken=generation;
  long request=++historicalRequest;
  if(historicalTask!=null)historicalTask.cancel(true);
  historicalTask=io.submit(()->{
   try{
    if(phase!=null&&!phase.standings()){
     Repository.PhaseGames round=api.phaseGames(competition,season,phase);
     main.post(()->{
      if(screenToken!=generation||request!=historicalRequest||isFinishing())return;
      detail.removeAllViews();
      if(round.matches().isEmpty())add(detail,label("Sin partidos disponibles para esta fase.",14,DIM));
      if(round.unavailable()>0)add(detail,label(round.unavailable()+" partidos sin datos disponibles.",14,DIM));
      showTies(detail,competition,round.matches());
     });
     return;
    }
    Repository.Result result=api.standings(competition,season,phase==null?"":phase.id());
    JSONObject seasonData=result.json().getJSONObject("season");
    if(seasonData.optInt("year")!=season)throw new java.io.IOException("Se recibió una temporada diferente.");
    List<Models.Standing> rows=Models.standings(result.json());
    main.post(()->{
     if(screenToken!=generation||request!=historicalRequest||isFinishing())return;
     detail.removeAllViews();
     add(detail,label(seasonData.optString("displayName",String.valueOf(season)),14,DIM));
     if(!result.warning().isEmpty())add(detail,label(result.warning(),14,DIM));
     if(rows.isEmpty())add(detail,label("No hay una tabla disponible para esta fase.",14,DIM));
     else standingsTable(detail,competition,rows);
    });
   }catch(Exception error){
    main.post(()->{
     if(screenToken!=generation||request!=historicalRequest||isFinishing())return;
     detail.removeAllViews();
     add(detail,label(DataErrors.describe(error)+" "+error.getMessage(),14,DIM));
     add(detail,button("Reintentar",()->loadHistoricalPhase(competition,season,phase,target)));
    });
   }
  });
 }
 private void showTies(LinearLayout target,League competition,List<Models.Match> matches){
  Map<List<String>,List<Models.Match>> ties=new LinkedHashMap<>();
  for(Models.Match match:matches){
   List<String> opponents=new ArrayList<>(List.of(match.home().id(),match.away().id()));
   Collections.sort(opponents);
   ties.computeIfAbsent(opponents,key->new ArrayList<>()).add(match);
  }
  for(List<Models.Match> games:ties.values()){
   if(games.size()>1){
    Models.Match first=games.get(0);
    add(target,label(first.home().name()+" · "+first.away().name(),15,BLUE));
   }
   for(Models.Match game:games)matchCard(target,competition,game);
  }
 }
 private void leagueProfile(){
  League l=selectedLeague;add(body,button("← Volver",()->{screen="home";draw();}));
  title(l.title);add(body,crest(l.logo));historicalTable(l);add(body,label(l.group+" · "+l.sport,14,DIM));
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
   else {List<Models.Standing> standings=new ArrayList<>();for(Object row:rows)standings.add((Models.Standing)row);standingsTable(body,l,standings);}
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
  add(body,button("Créditos",()->{screen="credits";draw();}));
  title("Acerca de");add(body,label("PulseSport 1.8.3",13,DIM));
 }
 private void alerts(){if(store.trackingUntil()>System.currentTimeMillis()){stopService(new Intent(this,TrackingService.class));store.trackingUntil(0);draw();return;}if(android.os.Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},101);return;}try{startForegroundService(new Intent(this,TrackingService.class));Toast.makeText(this,"Seguimiento iniciado",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"No se pudo iniciar el seguimiento",Toast.LENGTH_LONG).show();}}
 @Override public void onRequestPermissionsResult(int request,String[] p,int[] grants){super.onRequestPermissionsResult(request,p,grants);if(request==101&&grants.length>0&&grants[0]==PackageManager.PERMISSION_GRANTED)alerts();}
 @Override public void onBackPressed(){if(!store.onboarded()){screen="onboard";draw();return;}screen="home";draw();}
}
