from pathlib import Path
p=Path('app/src/main/java/com/astrolife/app/MainActivityV2.kt')
s=p.read_text(encoding='utf-8')
if 'import android.content.Intent' not in s:
    s=s.replace('import android.content.Context\n','import android.content.Context\nimport android.content.Intent\n')
if 'data class RWMaster' not in s:
    s=s.replace('data class RWBooking(val service:String,val name:String,val mobile:String,val city:String)\n','data class RWBooking(val service:String,val name:String,val mobile:String,val city:String)\ndata class RWMaster(val id:String,val type:String,val name:String)\n')

# Helpers
anchor='private fun fetchShopMasters(onDone:(List<String>)->Unit)'
idx=s.find(anchor)
if idx>=0 and 'private fun fetchMastersV3' not in s:
    end=s.find('\n\n',idx)
    helper='''\nprivate fun fetchMastersV3(onDone:(List<RWMaster>)->Unit){Thread{val out=mutableListOf<RWMaster>();try{val a=getArray("masters?select=id,master_type,name&order=master_type.asc,name.asc");for(i in 0 until a.length()){val x=a.getJSONObject(i);out+=RWMaster(x.optString("id"),x.optString("master_type"),x.optString("name"))}}catch(_:Exception){};ui{onDone(out)}}.start()}\nprivate fun deleteRowV3(path:String,token:String,onDone:(Boolean)->Unit){Thread{val ok=try{val c=conn("$SB_URL/rest/v1/$path",token);c.requestMethod="DELETE";val r=c.responseCode in 200..299;c.disconnect();r}catch(_:Exception){false};ui{onDone(ok)}}.start()}\nprivate fun patchRowV3(path:String,payload:JSONObject,token:String,onDone:(Boolean)->Unit){Thread{val ok=try{writeJson(path,"PATCH",payload,token)}catch(_:Exception){false};ui{onDone(ok)}}.start()}\n'''
    s=s[:end]+helper+s[end:]

# Keep the original Shop/Gallery/Profile composables intact. Earlier replacement removed
# GalleryV2/ProfileV2/RemoteImageV2 because the boundary started at ShopV2 and ended at
# GalleryV2, which caused the Android compiler failure.

# Replace HomeV2 only, with compact language selector.
start=s.find('@Composable fun HomeV2(')
end=s.find('@Composable fun ServiceScreenV2(',start)
if start>=0 and end>start:
    replacement=r'''@Composable fun HomeV2(open:(RWService)->Unit){
    val context=LocalContext.current
    val p=prefs(context)
    var lang by remember{mutableStateOf(p.getString("language","English")?:"English")}
    var langOpen by remember{mutableStateOf(false)}
    val hero=when(lang){"Gujarati"->"તમારી દરેક જરૂરિયાત, એક જ એપમાં.";"Hindi"->"आपकी हर ज़रूरत, एक ही ऐप में।";"French"->"Tout ce dont vous avez besoin, dans une seule application.";else->"Everything you need, in one app."}
    val sub=when(lang){"Gujarati"->"જ્યોતિષ, ઇવેન્ટ, પ્રવાસ, ખરીદી અને વધુ.";"Hindi"->"ज्योतिष, इवेंट, यात्रा, खरीदारी और बहुत कुछ।";"French"->"Astrologie, événements, voyages, shopping et plus encore.";else->"Astrology • Events • Travel • Shopping • Gallery"}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
        Row(verticalAlignment=Alignment.CenterVertically){Text("🕉️",style=MaterialTheme.typography.displayMedium);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text("Rawalworld",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold);Text("One app. Many services.")};Box{OutlinedButton(onClick={langOpen=true},contentPadding=PaddingValues(horizontal=10.dp,vertical=4.dp)){Text(when(lang){"Gujarati"->"🌐 GU";"Hindi"->"🌐 HI";"French"->"🌐 FR";else->"🌐 EN"})};DropdownMenu(langOpen,onDismissRequest={langOpen=false}){listOf("English","Gujarati","Hindi","French").forEach{v->DropdownMenuItem(text={Text(v)},onClick={lang=v;p.edit().putString("language",v).apply();langOpen=false})}}}}
        Spacer(Modifier.height(12.dp));Card(colors=CardDefaults.cardColors(containerColor=Purple)){Column(Modifier.padding(18.dp)){Text(hero,color=Color.White,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(sub,color=Color.White)}}
        Spacer(Modifier.height(14.dp));baseServices.forEach{svc->Card(onClick={open(svc)},modifier=Modifier.fillMaxWidth().padding(vertical=5.dp),shape=RoundedCornerShape(18.dp)){Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){Text(svc.icon,style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(svc.title,fontWeight=FontWeight.Bold);Text(svc.desc,style=MaterialTheme.typography.bodySmall)};Text("Open ›",color=Purple,fontWeight=FontWeight.Bold)}}};Text("📞 +91 77093 78969   ✉ rawalworld@gmail.com",style=MaterialTheme.typography.bodySmall)
    }
}

'''
    s=s[:start]+replacement+s[end:]

p.write_text(s,encoding='utf-8')
