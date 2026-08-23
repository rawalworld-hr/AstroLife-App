from pathlib import Path
p=Path('app/src/main/java/com/astrolife/app/MainActivityV2.kt')
s=p.read_text(encoding='utf-8')

# Add new public service cards.
old='RWService("shopping","🛍️","Online Shopping","Products, gifts and essentials",listOf("Puja Products","Astrology Products","Gifts","Decoration Items","Travel Accessories","Local Products"))\n)'
new='RWService("shopping","🛍️","Online Shopping","Products, gifts and essentials",listOf("Puja Products","Astrology Products","Gifts","Decoration Items","Travel Accessories","Local Products")),\n RWService("news","📰","Gujarat News","Latest Gujarat headlines and updates",listOf("Latest Gujarat News","Business News","Local Updates")),\n RWService("weather","🌦️","Weather Update","Current weather and forecast",listOf("Gujarat Weather","Ahmedabad Weather","Gandhinagar Weather")),\n RWService("entertainment","🎮","Entertainment","Games and fun",listOf("Ludo","Solitaire"))\n)'
if old in s and 'RWService("news"' not in s:
    s=s.replace(old,new,1)

# Make new service options open useful online destinations.
needle='selected?.let{Card(Modifier.fillMaxWidth().padding(top=10.dp)){Column(Modifier.padding(14.dp)){Text(optionV5(lang,it),fontWeight=FontWeight.Bold);Text(trV5(lang,"Information and booking support available.","માહિતી અને બુકિંગ સહાય ઉપલબ્ધ છે.","जानकारी और बुकिंग सहायता उपलब्ध है।","Informations et assistance de réservation disponibles."))}}}'
if needle in s and 'openExternalV7' not in s:
    replacement='''selected?.let{o->\n            val context=LocalContext.current\n            val url=when(o){\n                "Latest Gujarat News"->"https://news.google.com/search?q=Gujarat&hl=en-IN&gl=IN&ceid=IN:en"\n                "Business News"->"https://news.google.com/search?q=Gujarat%20business&hl=en-IN&gl=IN&ceid=IN:en"\n                "Local Updates"->"https://news.google.com/search?q=Gujarat%20local&hl=en-IN&gl=IN&ceid=IN:en"\n                "Gujarat Weather"->"https://www.google.com/search?q=Gujarat+weather"\n                "Ahmedabad Weather"->"https://www.google.com/search?q=Ahmedabad+weather"\n                "Gandhinagar Weather"->"https://www.google.com/search?q=Gandhinagar+weather"\n                "Ludo"->"https://www.crazygames.com/game/ludo-king"\n                "Solitaire"->"https://www.solitr.com/"\n                else->null\n            }\n            Card(Modifier.fillMaxWidth().padding(top=10.dp)){Column(Modifier.padding(14.dp)){Text(optionV5(lang,o),fontWeight=FontWeight.Bold);Text(trV5(lang,"Information and support available.","માહિતી અને સહાય ઉપલબ્ધ છે.","जानकारी और सहायता उपलब्ध है।","Informations et assistance disponibles."));if(url!=null)Button(onClick={try{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))}catch(_:Exception){}},modifier=Modifier.fillMaxWidth()){Text(trV5(lang,"Open Online","ઓનલાઇન ખોલો","ऑनलाइन खोलें","Ouvrir en ligne"))}}}\n        }'''
    s=s.replace(needle,replacement,1)

insert=s.find('@Composable fun SimpleDropdown(')
if insert>=0 and '@Composable fun ProductManagerV7' not in s:
    comp=r'''@Composable fun ProductManagerV7(session:RWSession,onMessage:(String)->Unit){
    data class AdminP(val id:String,val name:String,val category:String,val price:Double,val active:Boolean)
    var rows by remember{mutableStateOf<List<AdminP>>(emptyList())};var refresh by remember{mutableStateOf(0)}
    LaunchedEffect(refresh){Thread{val out=mutableListOf<AdminP>();try{val a=getArray("products?select=id,name,category,price,is_active&order=created_at.desc",session.access);for(i in 0 until a.length()){val x=a.getJSONObject(i);out+=AdminP(x.optString("id"),x.optString("name"),x.optString("category"),x.optDouble("price"),x.optBoolean("is_active",true))}}catch(_:Exception){};ui{rows=out}}.start()}
    Text("Existing Products",fontWeight=FontWeight.Bold)
    rows.forEach{p->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(10.dp)){Text(p.name,fontWeight=FontWeight.Bold);Text("${p.category} • ₹ ${String.format("%.2f",p.price)} • ${if(p.active)"Active" else "Inactive"}");Row{OutlinedButton(onClick={val name=p.name;val payload=JSONObject().put("name",name);patchV5("products?id=eq.${p.id}",payload,session.access){ok->onMessage(if(ok)"Product saved." else "Product edit failed.");if(ok)refresh++}}){Text("Edit")};Spacer(Modifier.width(6.dp));OutlinedButton(onClick={patchV5("products?id=eq.${p.id}",JSONObject().put("is_active",!p.active),session.access){ok->onMessage(if(ok)if(p.active)"Product inactive." else "Product active." else "Status update failed.");if(ok)refresh++}}){Text(if(p.active)"Inactive" else "Activate")};Spacer(Modifier.width(6.dp));OutlinedButton(onClick={deleteV5("products?id=eq.${p.id}",session.access){ok->onMessage(if(ok)"Product deleted." else "Delete failed.");if(ok)refresh++}}){Text("Delete")}}}}}
}

@Composable fun MasterManagerV7(session:RWSession,onMessage:(String)->Unit){
    var rows by remember{mutableStateOf<List<RWMasterV5>>(emptyList())};var refresh by remember{mutableStateOf(0)}
    LaunchedEffect(refresh){fetchMastersV5{rows=it}}
    Text("Existing Masters",fontWeight=FontWeight.Bold)
    rows.forEach{m->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(10.dp)){Text("${m.type}: ${m.name}",fontWeight=FontWeight.Bold);Row{OutlinedButton(onClick={patchV5("masters?id=eq.${m.id}",JSONObject().put("name",m.name),session.access){ok->onMessage(if(ok)"Master saved." else "Master edit failed.");if(ok)refresh++}}){Text("Edit")};Spacer(Modifier.width(8.dp));OutlinedButton(onClick={deleteV5("masters?id=eq.${m.id}",session.access){ok->onMessage(if(ok)"Master deleted." else "Delete failed.");if(ok)refresh++}}){Text("Delete")}}}}}
}

@Composable fun ClientRecordsV7(session:RWSession){
    var rows by remember{mutableStateOf<List<String>>(emptyList())}
    LaunchedEffect(Unit){Thread{val out=mutableListOf<String>();try{val a=getArray("clients?select=customer_name,mobile,city,delivery_address,pincode,is_active&order=updated_at.desc&limit=100",session.access);for(i in 0 until a.length()){val x=a.getJSONObject(i);out+="${x.optString("customer_name")} • ${x.optString("mobile")} • ${x.optString("city")} • ${x.optString("delivery_address")} ${x.optString("pincode")}"}}catch(_:Exception){};ui{rows=out}}.start()}
    Text("CLIENT RECORDS",color=Purple,fontWeight=FontWeight.Bold);if(rows.isEmpty())Text("No client records yet.") else rows.forEach{r->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Text(r,Modifier.padding(10.dp))}}
}

'''
    s=s[:insert]+comp+s[insert:]

# Force visible management sections in Admin.
master_heading='Text("MASTER MANAGEMENT",color=Purple,fontWeight=FontWeight.Bold)'
if master_heading in s and 'MasterManagerV7(session){msg=it}' not in s:
    s=s.replace(master_heading,master_heading+';MasterManagerV7(session){msg=it}',1)
product_heading='Text("PRODUCT MANAGEMENT",color=Purple,fontWeight=FontWeight.Bold)'
if product_heading in s and 'ProductManagerV7(session){msg=it}' not in s:
    s=s.replace(product_heading,product_heading+';ProductManagerV7(session){msg=it}',1)
logout='OutlinedButton(onClick=logout,modifier=Modifier.fillMaxWidth()){Text("Logout")}'
if logout in s and 'ClientRecordsV7(session)' not in s:
    s=s.replace(logout,'ClientRecordsV7(session);Spacer(Modifier.height(12.dp));'+logout,1)

p.write_text(s,encoding='utf-8')
