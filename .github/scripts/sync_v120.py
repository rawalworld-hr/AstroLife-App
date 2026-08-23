from pathlib import Path

path = Path('app/src/main/java/com/astrolife/app/MainActivity.kt')
s = path.read_text()


def replace_block(src: str, start: str, end: str, replacement: str) -> str:
    a = src.find(start)
    if a < 0:
        raise SystemExit(f'missing start marker: {start}')
    b = src.find(end, a)
    if b < 0:
        raise SystemExit(f'missing end marker: {end}')
    return src[:a] + replacement.rstrip() + '\n\n' + src[b:]

# Shared multilingual helper.
helper = '''private fun rwText(lang:String,en:String,gu:String,hi:String,fr:String)=when(lang){"Gujarati"->gu;"Hindi"->hi;"French"->fr;else->en}\n'''
if 'private fun rwText(' not in s:
    marker = 'private val Bg=Color(0xFFFFF8FF)\n'
    if marker not in s:
        raise SystemExit('color marker missing')
    s = s.replace(marker, marker + helper, 1)

app = r'''@Composable fun RawalworldApp(){
    val context=LocalContext.current
    var tab by remember{mutableStateOf("home")}
    var service by remember{mutableStateOf<ServiceItem?>(null)}
    var lang by remember{mutableStateOf(prefs(context).getString("app_language","English")?:"English")}
    fun changeLanguage(v:String){lang=v;prefs(context).edit().putString("app_language",v).apply()}
    Scaffold(bottomBar={NavigationBar{
        NavigationBarItem(tab=="home",{tab="home";service=null},{Icon(Icons.Default.Home,null)},label={Text(rwText(lang,"Home","હોમ","होम","Accueil"))})
        NavigationBarItem(tab=="bookings",{tab="bookings"},{Icon(Icons.Default.DateRange,null)},label={Text(rwText(lang,"Bookings","બુકિંગ","बुकिंग","Réservations"))})
        NavigationBarItem(tab=="shop",{tab="shop"},{Icon(Icons.Default.ShoppingCart,null)},label={Text(rwText(lang,"Shop","દુકાન","शॉप","Boutique"))})
        NavigationBarItem(tab=="profile",{tab="profile"},{Icon(Icons.Default.Person,null)},label={Text(rwText(lang,"Profile","પ્રોફાઇલ","प्रोफ़ाइल","Profil"))})
        NavigationBarItem(tab=="admin",{tab="admin"},{Icon(Icons.Default.Lock,null)},label={Text(rwText(lang,"Admin","એડમિન","एडमिन","Admin"))})
    }}){p->Box(Modifier.fillMaxSize().padding(p)){when(tab){
        "service"->service?.let{ServiceScreen(it,lang,{tab="home"},{tab="booking"})}
        "booking"->BookingScreen(service?.title?:"Rawalworld Service",lang){tab="service"}
        "bookings"->BookingsScreen(lang)
        "shop"->ShopScreen(lang)
        "gallery"->GalleryScreen()
        "profile"->ProfileScreen(lang)
        "donation"->DonationScreen(lang){tab="home"}
        "admin"->AdminScreen()
        else->HomeScreen(lang,::changeLanguage,{service=it;tab="service"},{tab="gallery"},{tab="donation"})
    }}}
}'''
s = replace_block(s, '@Composable fun RawalworldApp()', '@Composable fun HomeScreen', app)

home = r'''@Composable fun HomeScreen(lang:String,onLanguage:(String)->Unit,open:(ServiceItem)->Unit,openGallery:()->Unit,openDonation:()->Unit){
    var menu by remember{mutableStateOf(false)}
    fun serviceName(t:String)=when(lang){
        "Gujarati"->mapOf("Astrology" to "જ્યોતિષ","Events" to "ઇવેન્ટ્સ","Decoration" to "ડેકોરેશન","Catering" to "કેટરિંગ","Consultancy" to "કન્સલ્ટન્સી","Tours & Travel" to "ટૂર્સ અને ટ્રાવેલ","Map" to "નકશો","Gujarat News" to "ગુજરાત સમાચાર","Weather Update" to "હવામાન","Entertainment" to "મનોરંજન")[t]?:t
        "Hindi"->mapOf("Astrology" to "ज्योतिष","Events" to "कार्यक्रम","Decoration" to "सजावट","Catering" to "कैटरिंग","Consultancy" to "परामर्श","Tours & Travel" to "टूर्स और ट्रैवल","Map" to "मैप","Gujarat News" to "गुजरात समाचार","Weather Update" to "मौसम","Entertainment" to "मनोरंजन")[t]?:t
        "French"->mapOf("Astrology" to "Astrologie","Events" to "Événements","Decoration" to "Décoration","Catering" to "Traiteur","Consultancy" to "Conseil","Tours & Travel" to "Tours & Voyages","Map" to "Carte","Gujarat News" to "Actualités du Gujarat","Weather Update" to "Météo","Entertainment" to "Divertissement")[t]?:t
        else->t
    }
    Column(Modifier.fillMaxSize().padding(14.dp).verticalScroll(rememberScrollState())){
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
            Column(Modifier.weight(1f)){Text("Rawalworld",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold);Text(rwText(lang,"Gujarat lifestyle & services","ગુજરાત લાઇફસ્ટાઇલ અને સેવાઓ","गुजरात लाइफस्टाइल और सेवाएँ","Lifestyle et services du Gujarat"),style=MaterialTheme.typography.bodySmall)}
            Box{OutlinedButton(onClick={menu=true}){Text("🌐 $lang")};DropdownMenu(menu,{menu=false}){listOf("English","Gujarati","Hindi","French").forEach{l->DropdownMenuItem(text={Text(l)},onClick={onLanguage(l);menu=false})}}}
        }
        Spacer(Modifier.height(10.dp))
        Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF312E81)),modifier=Modifier.fillMaxWidth()){
            Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){
                Column(Modifier.weight(1f)){Text(rwText(lang,"Everything you need, in one app.","તમારી દરેક જરૂરિયાત, એક જ એપમાં.","आपकी हर ज़रूरत, एक ही ऐप में।","Tout ce dont vous avez besoin, dans une seule application."),color=Color.White,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(rwText(lang,"Astrology • Travel • Map • News • Weather • Cricket • Songs • Shopping","જ્યોતિષ • ટ્રાવેલ • નકશો • સમાચાર • હવામાન • ક્રિકેટ • ગીતો • શોપિંગ","ज्योतिष • यात्रा • मैप • समाचार • मौसम • क्रिकेट • गाने • शॉपिंग","Astrologie • Voyage • Carte • Actualités • Météo • Cricket • Musique • Shopping"),color=Color.White,style=MaterialTheme.typography.bodySmall)}
                Spacer(Modifier.width(10.dp));Image(painterResource(R.drawable.rawalworld_ganeshji_final),"Ganeshji",Modifier.size(78.dp),contentScale=ContentScale.Fit)
            }
        }
        Text(rwText(lang,"Explore services","સેવાઓ જુઓ","सेवाएँ देखें","Découvrir les services"),fontWeight=FontWeight.ExtraBold,modifier=Modifier.padding(top=14.dp,bottom=6.dp))
        publicServices.chunked(3).forEach{items->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){items.forEach{x->Card(onClick={open(x)},modifier=Modifier.weight(1f).padding(vertical=4.dp)){Column(Modifier.fillMaxWidth().padding(vertical=12.dp,horizontal=6.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(x.icon,style=MaterialTheme.typography.headlineMedium);Text(serviceName(x.title),fontWeight=FontWeight.Bold,style=MaterialTheme.typography.bodySmall)}}};repeat(3-items.size){Spacer(Modifier.weight(1f))}}}
        Card(onClick={open(ServiceItem("🛍️","Online Shopping",emptyList()))},modifier=Modifier.fillMaxWidth().padding(top=8.dp)){Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text("🛍️",style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.width(10.dp));Text(rwText(lang,"Online Shopping","ઓનલાઇન શોપિંગ","ऑनलाइन शॉपिंग","Shopping en ligne"),Modifier.weight(1f),fontWeight=FontWeight.Bold);Text("›",color=Purple)}}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(onClick=openGallery,Modifier.weight(1f)){Text(rwText(lang,"📷 Gallery","📷 ગેલેરી","📷 गैलरी","📷 Galerie"))};OutlinedButton(onClick=openDonation,Modifier.weight(1f)){Text(rwText(lang,"❤️ Donate","❤️ દાન","❤️ दान","❤️ Don"))}}
        Text("📞 +91 77093 78969   ✉ rawalworld@gmail.com",Modifier.padding(top=16.dp),style=MaterialTheme.typography.bodySmall)
    }
}'''
s = replace_block(s, '@Composable fun HomeScreen', '@Composable fun ServiceScreen', home)

service = r'''@Composable fun ServiceScreen(s:ServiceItem,lang:String,back:()->Unit,book:()->Unit){
    val c=LocalContext.current
    if(s.title=="Online Shopping"){ShopScreen(lang);return}
    fun optionName(v:String)=when(lang){
        "Gujarati"->mapOf("Flight Search" to "ફ્લાઇટ શોધ","Flight Schedule" to "ફ્લાઇટ સમયપત્રક","Train Search" to "ટ્રેન શોધ","Train Schedule" to "ટ્રેન સમયપત્રક","Hotel Booking" to "હોટેલ બુકિંગ","Live Cricket India" to "લાઇવ ક્રિકેટ ઇન્ડિયા","English Songs" to "અંગ્રેજી ગીતો","Hindi Songs" to "હિન્દી ગીતો","Gujarati Songs" to "ગુજરાતી ગીતો","French Songs" to "ફ્રેન્ચ ગીતો")[v]?:v
        "Hindi"->mapOf("Flight Search" to "फ्लाइट खोज","Flight Schedule" to "फ्लाइट समय-सारणी","Train Search" to "ट्रेन खोज","Train Schedule" to "ट्रेन समय-सारणी","Hotel Booking" to "होटल बुकिंग","Live Cricket India" to "लाइव क्रिकेट इंडिया","English Songs" to "अंग्रेज़ी गाने","Hindi Songs" to "हिंदी गाने","Gujarati Songs" to "गुजराती गाने","French Songs" to "फ्रेंच गाने")[v]?:v
        "French"->mapOf("Flight Search" to "Recherche de vols","Flight Schedule" to "Horaires des vols","Train Search" to "Recherche de trains","Train Schedule" to "Horaires des trains","Hotel Booking" to "Réservation d'hôtel","Live Cricket India" to "Cricket Inde en direct","English Songs" to "Chansons anglaises","Hindi Songs" to "Chansons hindi","Gujarati Songs" to "Chansons gujarati","French Songs" to "Chansons françaises")[v]?:v
        else->v
    }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
        TextButton(onClick=back){Text(rwText(lang,"← Back","← પાછા","← वापस","← Retour"))}
        Text("${s.icon} ${s.title}",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        s.items.forEach{(label,url)->Card(Modifier.fillMaxWidth().padding(vertical=5.dp)){Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text(optionName(label),Modifier.weight(1f));Button(onClick={if(url!=null)openUrl(c,url)}){Text(rwText(lang,"Open","ખોલો","खोलें","Ouvrir"))}}}}
        Button(onClick=book,Modifier.fillMaxWidth().padding(top=12.dp)){Text(rwText(lang,"Request Booking / Quotation","બુકિંગ / ક્વોટેશન માગો","बुकिंग / कोटेशन माँगें","Demander réservation / devis"))}
    }
}'''
s = replace_block(s, '@Composable fun ServiceScreen', '@Composable fun BookingScreen', service)

booking = r'''@Composable fun BookingScreen(service:String,lang:String,back:()->Unit){
    var name by remember{mutableStateOf("")};var mobile by remember{mutableStateOf("")};var city by remember{mutableStateOf("")};var date by remember{mutableStateOf("")};var note by remember{mutableStateOf("")};var msg by remember{mutableStateOf("")};var busy by remember{mutableStateOf(false)}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
        TextButton(onClick=back){Text(rwText(lang,"← Back","← પાછા","← वापस","← Retour"))}
        Text(rwText(lang,"Booking / Quotation","બુકિંગ / ક્વોટેશન","बुकिंग / कोटेशन","Réservation / Devis"),style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text(service)
        OutlinedTextField(name,{name=it},label={Text(rwText(lang,"Name","નામ","नाम","Nom"))},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(mobile,{mobile=it},label={Text(rwText(lang,"Mobile","મોબાઇલ","मोबाइल","Téléphone"))},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(city,{city=it},label={Text(rwText(lang,"City","શહેર","शहर","Ville"))},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(date,{date=it},label={Text(rwText(lang,"Preferred date YYYY-MM-DD","પસંદ તારીખ YYYY-MM-DD","पसंदीदा तारीख YYYY-MM-DD","Date souhaitée AAAA-MM-JJ"))},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(note,{note=it},label={Text(rwText(lang,"Requirement","જરૂરિયાત","आवश्यकता","Besoin"))},modifier=Modifier.fillMaxWidth())
        Button(enabled=!busy,onClick={
            val cleanMobile=mobile.trim()
            val today=java.text.SimpleDateFormat("yyyy-MM-dd",java.util.Locale.US).format(java.util.Date())
            if(name.isBlank()||cleanMobile.isBlank()||city.isBlank()){msg=rwText(lang,"Enter name, mobile and city.","નામ, મોબાઇલ અને શહેર દાખલ કરો.","नाम, मोबाइल और शहर दर्ज करें।","Saisissez nom, téléphone et ville.");return@Button}
            if(!cleanMobile.matches(Regex("^\\+?[0-9 \\-]{8,15}$"))){msg=rwText(lang,"Enter a valid mobile number.","માન્ય મોબાઇલ નંબર દાખલ કરો.","मान्य मोबाइल नंबर दर्ज करें।","Saisissez un numéro valide.");return@Button}
            if(date.isNotBlank()&&(!date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))||date<today)){msg=rwText(lang,"Enter a valid future date.","માન્ય ભવિષ્ય તારીખ દાખલ કરો.","मान्य भविष्य की तारीख दर्ज करें।","Saisissez une date future valide.");return@Button}
            busy=true;msg=rwText(lang,"Submitting...","મોકલી રહ્યા છીએ...","जमा किया जा रहा है...","Envoi...")
            Thread{val j=JSONObject().put("service",service).put("customer_name",name.trim()).put("mobile",cleanMobile).put("city",city.trim()).put("source","android");if(date.isNotBlank())j.put("preferred_date",date);if(note.isNotBlank())j.put("requirement",note.trim());val ok=try{write("bookings","POST",j)}catch(_:Exception){false};ui{busy=false;msg=if(ok)rwText(lang,"✅ Booking submitted successfully.","✅ બુકિંગ સફળતાપૂર્વક મોકલાયું.","✅ बुकिंग सफलतापूर्वक जमा हुई।","✅ Réservation envoyée.") else rwText(lang,"Could not submit booking.","બુકિંગ મોકલી શકાયું નહીં.","बुकिंग जमा नहीं हुई।","Impossible d'envoyer la réservation.")}}.start()
        },Modifier.fillMaxWidth().padding(top=10.dp)){Text(rwText(lang,"Submit Request","વિનંતી મોકલો","अनुरोध भेजें","Envoyer la demande"))}
        if(msg.isNotBlank())Text(msg)
    }
}'''
s = replace_block(s, '@Composable fun BookingScreen', '@Composable fun BookingsScreen', booking)

bookings = r'''@Composable fun BookingsScreen(lang:String){var rows by remember{mutableStateOf<List<String>>(emptyList())};LaunchedEffect(Unit){Thread{val out=mutableListOf<String>();try{val a=getArray("bookings?select=service,customer_name,mobile,city,status&order=created_at.desc&limit=30");for(i in 0 until a.length()){val x=a.getJSONObject(i);out+="${x.optString("service")} • ${x.optString("customer_name")} • ${x.optString("mobile")} • ${x.optString("status")}"}}catch(_:Exception){};ui{rows=out}}.start()};Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){Text(rwText(lang,"📅 Bookings","📅 બુકિંગ","📅 बुकिंग","📅 Réservations"),style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);if(rows.isEmpty())Text(rwText(lang,"No bookings found.","કોઈ બુકિંગ મળ્યું નથી.","कोई बुकिंग नहीं मिली।","Aucune réservation."))else rows.forEach{Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Text(it,Modifier.padding(12.dp))}}}}'''
s = replace_block(s, '@Composable fun BookingsScreen', '@Composable fun RemoteImage', bookings)

shop = r'''@Composable fun ShopScreen(lang:String){
    val context=LocalContext.current
    var products by remember{mutableStateOf<List<ProductRow>>(emptyList())}
    var masterCategories by remember{mutableStateOf(listOf("Puja Products","Astrology Products","Gifts","Decoration Items","Travel Accessories","Local Products"))}
    var loading by remember{mutableStateOf(true)};var selected by remember{mutableStateOf<ProductRow?>(null)};var category by remember{mutableStateOf("All")}
    LaunchedEffect(Unit){Thread{val out=mutableListOf<ProductRow>();val cats=mutableListOf<String>();try{val a=getArray("products?select=id,name,category,description,price,is_active,image_url&is_active=eq.true&order=created_at.desc");for(i in 0 until a.length()){val x=a.getJSONObject(i);out+=ProductRow(x.optString("id"),x.optString("name"),x.optString("category"),x.optString("description"),x.optDouble("price"),true,x.optString("image_url"))}}catch(_:Exception){};try{val a=getArray("masters?select=name&master_type=eq.shop&is_active=eq.true&order=name.asc");for(i in 0 until a.length()){val n=a.getJSONObject(i).optString("name");if(n.isNotBlank())cats+=n}}catch(_:Exception){};ui{products=out;masterCategories=(masterCategories+cats+out.map{it.category}).filter{it.isNotBlank()}.distinct();loading=false}}.start()}
    val categories=listOf("All")+masterCategories.distinct();val visible=if(category=="All")products else products.filter{it.category==category}
    Column(Modifier.fillMaxSize().padding(14.dp).verticalScroll(rememberScrollState())){
        Text(rwText(lang,"🛍️ Online Shopping","🛍️ ઑનલાઇન શોપિંગ","🛍️ ऑनलाइन शॉपिंग","🛍️ Shopping en ligne"),style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
        Text(rwText(lang,"Shop category wise","કેટેગરી પ્રમાણે ખરીદી","कैटेगरी अनुसार शॉपिंग","Shopping par catégorie"),color=Purple,fontWeight=FontWeight.Bold)
        categories.chunked(2).forEach{cats->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){cats.forEach{cat->OutlinedButton(onClick={category=cat},Modifier.weight(1f)){Text((if(category==cat)"✓ " else "")+cat)}};if(cats.size==1)Spacer(Modifier.weight(1f))}}
        if(loading)CircularProgressIndicator(Modifier.padding(16.dp));if(!loading&&visible.isEmpty())Text(rwText(lang,"No active products found in this category.","આ કેટેગરીમાં કોઈ સક્રિય પ્રોડક્ટ નથી.","इस कैटेगरी में कोई सक्रिय प्रोडक्ट नहीं है।","Aucun produit actif dans cette catégorie."))
        visible.forEach{r->Card(Modifier.fillMaxWidth().padding(vertical=6.dp)){Column(Modifier.padding(14.dp)){if(r.imageUrl.isNotBlank())RemoteImage(r.imageUrl);Text(r.name,fontWeight=FontWeight.Bold);Text(r.category,style=MaterialTheme.typography.bodySmall);if(r.description.isNotBlank())Text(r.description);Text("₹ ${String.format("%.2f",r.price)}",fontWeight=FontWeight.Bold);Button(onClick={selected=r},Modifier.fillMaxWidth().padding(top=8.dp)){Text(rwText(lang,"Buy Now","હમણાં ખરીદો","अभी खरीदें","Acheter"))};OutlinedButton(onClick={val text="Rawalworld: ${r.name} - ₹ ${String.format("%.2f",r.price)}";val i=Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,text)};try{context.startActivity(Intent.createChooser(i,rwText(lang,"Share Product","પ્રોડક્ટ શેર કરો","प्रोडक्ट शेयर करें","Partager le produit")))}catch(_:Exception){}},Modifier.fillMaxWidth().padding(top=6.dp)){Text(rwText(lang,"Share Product","પ્રોડક્ટ શેર કરો","प्रोडक्ट शेयर करें","Partager le produit"))}}}}
    }
    selected?.let{CheckoutDialog(context,it){selected=null}}
}'''
s = replace_block(s, '@Composable fun ShopScreen', '@Composable fun CheckoutDialog', shop)

profile = r'''@Composable fun ProfileScreen(lang:String){
    val c=LocalContext.current
    var n by remember{mutableStateOf(prefs(c).getString("name","")?:"")};var m by remember{mutableStateOf(prefs(c).getString("mobile","")?:"")};var email by remember{mutableStateOf(prefs(c).getString("email","")?:"")};var city by remember{mutableStateOf(prefs(c).getString("city","")?:"")};var msg by remember{mutableStateOf("")}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
        Text(rwText(lang,"👤 Profile / Customer Account","👤 પ્રોફાઇલ / ગ્રાહક એકાઉન્ટ","👤 प्रोफ़ाइल / ग्राहक अकाउंट","👤 Profil / Compte client"),style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
        OutlinedTextField(n,{n=it},label={Text(rwText(lang,"Name","નામ","नाम","Nom"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(m,{m=it},label={Text(rwText(lang,"Mobile","મોબાઇલ","मोबाइल","Téléphone"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(email,{email=it},label={Text(rwText(lang,"Email","ઇમેઇલ","ईमेल","E-mail"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(city,{city=it},label={Text(rwText(lang,"City","શહેર","शहर","Ville"))},modifier=Modifier.fillMaxWidth())
        Button(onClick={prefs(c).edit().putString("name",n.trim()).putString("mobile",m.trim()).putString("email",email.trim()).putString("city",city.trim()).apply();msg=rwText(lang,"Profile saved on this device.","પ્રોફાઇલ આ ડિવાઇસ પર સાચવાઈ.","प्रोफ़ाइल इस डिवाइस पर सेव हुई।","Profil enregistré sur cet appareil.")},Modifier.fillMaxWidth().padding(top=10.dp)){Text(rwText(lang,"Save Profile","પ્રોફાઇલ સાચવો","प्रोफ़ाइल सेव करें","Enregistrer le profil"))}
        Button(onClick={if(n.isBlank()||m.isBlank()){msg=rwText(lang,"Enter name and mobile first.","પહેલા નામ અને મોબાઇલ દાખલ કરો.","पहले नाम और मोबाइल दर्ज करें।","Saisissez d'abord nom et téléphone.")}else Thread{val ok=try{write("customer_accounts","POST",JSONObject().put("customer_name",n.trim()).put("mobile",m.trim()).put("email",email.trim().ifBlank{JSONObject.NULL}).put("city",city.trim().ifBlank{JSONObject.NULL}))}catch(_:Exception){false};ui{msg=if(ok)rwText(lang,"✅ Customer account created online. OTP login will be added after SMS authentication is enabled.","✅ ગ્રાહક એકાઉન્ટ ઑનલાઇન બનાવાયું. SMS ઓથેન્ટિકેશન પછી OTP લૉગિન ઉમેરાશે.","✅ ग्राहक अकाउंट ऑनलाइन बन गया। SMS ऑथेंटिकेशन के बाद OTP लॉगिन जोड़ा जाएगा।","✅ Compte client créé en ligne. La connexion OTP sera ajoutée après activation SMS.") else rwText(lang,"Account is saved locally or this mobile may already be registered.","એકાઉન્ટ સ્થાનિક રીતે સાચવાયું છે અથવા આ મોબાઇલ પહેલેથી નોંધાયેલ હોઈ શકે છે.","अकाउंट स्थानीय रूप से सेव है या यह मोबाइल पहले से पंजीकृत हो सकता है।","Le compte est enregistré localement ou ce mobile est peut-être déjà inscrit.")}}.start()},Modifier.fillMaxWidth().padding(top=8.dp)){Text(rwText(lang,"Create / Update Customer Account","ગ્રાહક એકાઉન્ટ બનાવો / અપડેટ કરો","ग्राहक अकाउंट बनाएँ / अपडेट करें","Créer / mettre à jour le compte"))}
        if(msg.isNotBlank())Text(msg,Modifier.padding(top=8.dp))
    }
}'''
s = replace_block(s, '@Composable fun ProfileScreen', '@Composable fun AdminScreen', profile)

donation = r'''@Composable fun DonationScreen(lang:String,back:()->Unit){
    val context=LocalContext.current
    var name by remember{mutableStateOf(prefs(context).getString("name","")?:"")};var mobile by remember{mutableStateOf(prefs(context).getString("mobile","")?:"")};var amount by remember{mutableStateOf("")};var note by remember{mutableStateOf("")};var msg by remember{mutableStateOf("")}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
        TextButton(onClick=back){Text(rwText(lang,"← Back to home","← હોમ પર પાછા","← होम पर वापस","← Retour à l'accueil"))}
        Text(rwText(lang,"❤️ Donation for Needy People","❤️ જરૂરિયાતમંદ લોકો માટે દાન","❤️ ज़रूरतमंद लोगों के लिए दान","❤️ Don pour les personnes dans le besoin"),style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
        Text(rwText(lang,"Please verify the beneficiary or cause before donating.","દાન કરતા પહેલાં લાભાર્થી અથવા હેતુ ચકાસો.","दान करने से पहले लाभार्थी या उद्देश्य की पुष्टि करें।","Vérifiez le bénéficiaire ou la cause avant de donner."),style=MaterialTheme.typography.bodySmall)
        OutlinedTextField(name,{name=it},label={Text(rwText(lang,"Donor name","દાતાનું નામ","दाता का नाम","Nom du donateur"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(mobile,{mobile=it},label={Text(rwText(lang,"Mobile","મોબાઇલ","मोबाइल","Téléphone"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(amount,{amount=it},label={Text(rwText(lang,"Amount INR","રકમ INR","राशि INR","Montant INR"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(note,{note=it},label={Text(rwText(lang,"Purpose / note","હેતુ / નોંધ","उद्देश्य / नोट","Objet / note"))},modifier=Modifier.fillMaxWidth())
        Button(onClick={val a=amount.toDoubleOrNull();if(name.isBlank()||mobile.isBlank()||a==null||a<=0){msg=rwText(lang,"Enter donor name, mobile and valid amount.","દાતાનું નામ, મોબાઇલ અને માન્ય રકમ દાખલ કરો.","दाता का नाम, मोबाइल और मान्य राशि दर्ज करें।","Saisissez nom, téléphone et montant valide.")}else Thread{try{write("donations","POST",JSONObject().put("donor_name",name.trim()).put("mobile",mobile.trim()).put("amount",a).put("currency","INR").put("purpose",note.trim().ifBlank{JSONObject.NULL}).put("payment_method","UPI / GPay").put("payment_status","initiated"))}catch(_:Exception){};ui{loadPayment{payee,upi->try{val uri=Uri.parse("upi://pay?pa=${Uri.encode(upi)}&pn=${Uri.encode(payee)}&am=${String.format("%.2f",a)}&cu=INR&tn=${Uri.encode("Donation for needy people"+(if(note.isBlank())"" else " - $note"))}");context.startActivity(Intent(Intent.ACTION_VIEW,uri));msg=rwText(lang,"Opening UPI / GPay...","UPI / GPay ખોલી રહ્યા છીએ...","UPI / GPay खोला जा रहा है...","Ouverture UPI / GPay...")}catch(_:Exception){msg=rwText(lang,"UPI app could not open.","UPI એપ ખોલી શકાઈ નહીં.","UPI ऐप नहीं खुल सका।","Impossible d'ouvrir l'application UPI.")}}}}.start()},Modifier.fillMaxWidth().padding(top=10.dp)){Text(rwText(lang,"Donate with UPI / GPay","UPI / GPay થી દાન કરો","UPI / GPay से दान करें","Donner avec UPI / GPay"))}
        if(msg.isNotBlank())Text(msg,Modifier.padding(top=8.dp))
    }
}'''
insert_marker='@Composable fun AdminScreen'
idx=s.find(insert_marker)
if idx<0: raise SystemExit('AdminScreen marker missing for donation insert')
s=s[:idx]+donation+'\n\n'+s[idx:]

path.write_text(s)
