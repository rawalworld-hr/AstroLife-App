from pathlib import Path
p=Path('app/src/main/java/com/astrolife/app/MainActivityV2.kt')
s=p.read_text(encoding='utf-8')

# Add Live Cricket to Entertainment and a separate Map service.
s=s.replace(
    'RWService("entertainment","🎮","Entertainment","Online games and free music links",listOf("Ludo","Solitaire","Chess","English Songs","Hindi Songs","Gujarati Songs","French Songs"))',
    'RWService("entertainment","🎮","Entertainment","Online games, live cricket and free music links",listOf("Live Cricket","Ludo","Solitaire","Chess","English Songs","Hindi Songs","Gujarati Songs","French Songs")),\n RWService("map","🗺️","Map","Maps, nearby places and directions",listOf("Open Google Maps","Search Nearby Places","Directions"))'
)

# Extend direct-open URLs. Cricket defaults to an India-live-match search.
needle='    "Ludo"->"https://www.crazygames.com/game/ludo-king"\n'
if needle in s and '"Live Cricket"->' not in s:
    s=s.replace(needle,'    "Live Cricket"->"https://www.google.com/search?q=India+live+cricket+match"\n'+needle,1)
needle='    "French Songs"->"https://www.youtube.com/results?search_query=french+songs"\n'
if needle in s and '"Open Google Maps"->' not in s:
    s=s.replace(needle,needle+'    "Open Google Maps"->"https://maps.google.com/"\n    "Search Nearby Places"->"https://www.google.com/maps/search/nearby"\n    "Directions"->"https://www.google.com/maps/dir/"\n',1)

# Give new services readable titles/descriptions in all languages.
old='private fun serviceTitleV5(lang:String,id:String)=when(id){'
if old in s and '"map"->trV5(lang,"Map"' not in s:
    s=s.replace(old,old+'"news"->trV5(lang,"Gujarat News","ગુજરાત સમાચાર","गुजरात समाचार","Actualités du Gujarat");"weather"->trV5(lang,"Weather Update","હવામાન અપડેટ","मौसम अपडेट","Météo");"entertainment"->trV5(lang,"Entertainment","મનોરંજન","मनोरंजन","Divertissement");"map"->trV5(lang,"Map","નકશો","मानचित्र","Carte");',1)
old='private fun serviceDescV5(lang:String,id:String)=when(id){'
if old in s and '"map"->trV5(lang,"Maps, nearby places and directions"' not in s:
    s=s.replace(old,old+'"news"->trV5(lang,"Latest Gujarat headlines and updates","ગુજરાતના તાજા સમાચાર અને અપડેટ","गुजरात की ताज़ा खबरें और अपडेट","Dernières actualités du Gujarat");"weather"->trV5(lang,"Current weather and forecast","હાલનું હવામાન અને આગાહી","वर्तमान मौसम और पूर्वानुमान","Météo actuelle et prévisions");"entertainment"->trV5(lang,"Online games, live cricket and free music links","ઓનલાઇન ગેમ, લાઇવ ક્રિકેટ અને મફત સંગીત લિંક્સ","ऑनलाइन गेम, लाइव क्रिकेट और मुफ्त संगीत लिंक","Jeux en ligne, cricket en direct et musique gratuite");"map"->trV5(lang,"Maps, nearby places and directions","નકશા, નજીકના સ્થળો અને દિશાઓ","मानचित्र, आसपास की जगहें और दिशा-निर्देश","Cartes, lieux à proximité et itinéraires");',1)

# Add translations for the new options.
old='private fun optionV5(lang:String,o:String)=when(o){'
if old in s and '"Live Cricket"->trV5' not in s:
    s=s.replace(old,old+'"Live Cricket"->trV5(lang,"Live Cricket","લાઇવ ક્રિકેટ","लाइव क्रिकेट","Cricket en direct");"Open Google Maps"->trV5(lang,"Open Google Maps","Google Maps ખોલો","Google Maps खोलें","Ouvrir Google Maps");"Search Nearby Places"->trV5(lang,"Search Nearby Places","નજીકના સ્થળો શોધો","आसपास की जगहें खोजें","Rechercher à proximité");"Directions"->trV5(lang,"Directions","દિશાઓ","दिशा-निर्देश","Itinéraire");',1)

# Put product/master/client management at the top of Admin so the controls cannot be missed.
insert=s.find('@Composable fun SimpleDropdown(')
if insert>=0 and '@Composable fun AdminManagementHubV11' not in s:
    comp=r'''@Composable fun AdminManagementHubV11(session:RWSession,onMessage:(String)->Unit){
    Card(Modifier.fillMaxWidth().padding(vertical=8.dp)){Column(Modifier.padding(12.dp)){
        Text("ADMIN MANAGEMENT",color=Purple,fontWeight=FontWeight.ExtraBold)
        Text("Products — Edit / Active-Inactive / Delete",fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=8.dp))
        ProductManagerV7(session,onMessage)
        HorizontalDivider(Modifier.padding(vertical=10.dp))
        Text("Masters — Edit / Delete",fontWeight=FontWeight.Bold)
        MasterManagerV7(session,onMessage)
        HorizontalDivider(Modifier.padding(vertical=10.dp))
        ClientRecordsV7(session)
    }}
}

'''
    s=s[:insert]+comp+s[insert:]

admin_title='Text("🔐 Rawalworld Admin",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold)'
if admin_title in s and 'AdminManagementHubV11(session){msg=it}' not in s:
    s=s.replace(admin_title,admin_title+';AdminManagementHubV11(session){msg=it}',1)

p.write_text(s,encoding='utf-8')
