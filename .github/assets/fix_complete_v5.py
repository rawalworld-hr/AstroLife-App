from pathlib import Path
p=Path('app/src/main/java/com/astrolife/app/MainActivityV2.kt')
s=p.read_text(encoding='utf-8')

if 'import android.content.Intent' not in s:
    s=s.replace('import android.content.Context\n','import android.content.Context\nimport android.content.Intent\n')
if 'data class RWMasterV5' not in s:
    s=s.replace('data class RWBooking(val service:String,val name:String,val mobile:String,val city:String)\n','data class RWBooking(val service:String,val name:String,val mobile:String,val city:String)\ndata class RWMasterV5(val id:String,val type:String,val name:String)\n')

anchor='@Composable fun RawalworldV2()'
if 'private val RWLanguageV5' not in s and anchor in s:
    helpers=r'''private val RWLanguageV5=mutableStateOf("English")
private fun trV5(lang:String,en:String,gu:String,hi:String,fr:String)=when(lang){"Gujarati"->gu;"Hindi"->hi;"French"->fr;else->en}
private fun serviceTitleV5(lang:String,id:String)=when(id){"astrology"->trV5(lang,"Astrology","જ્યોતિષ","ज्योतिष","Astrologie");"events"->trV5(lang,"Events","ઇવેન્ટ્સ","कार्यक्रम","Événements");"decoration"->trV5(lang,"Decoration","ડેકોરેશન","सजावट","Décoration");"catering"->trV5(lang,"Catering","કેટરિંગ","कैटरिंग","Traiteur");"consultancy"->trV5(lang,"Consultancy","કન્સલ્ટન્સી","परामर्श","Conseil");"travel"->trV5(lang,"Tours & Travel","ટૂર્સ અને ટ્રાવેલ","टूर्स और ट्रैवल","Tours & Voyages");"shopping"->trV5(lang,"Online Shopping","ઓનલાઇન શોપિંગ","ऑनलाइन शॉपिंग","Shopping en ligne");else->id}
private fun serviceDescV5(lang:String,id:String)=when(id){"astrology"->trV5(lang,"Horoscope, Kundli & consultation","રાશિફળ, કુંડળી અને સલાહ","राशिफल, कुंडली और परामर्श","Horoscope, Kundli et consultation");"events"->trV5(lang,"Weddings, birthdays & corporate events","લગ્ન, જન્મદિવસ અને કોર્પોરેટ ઇવેન્ટ્સ","शादी, जन्मदिन और कॉर्पोरेट कार्यक्रम","Mariages, anniversaires et événements d’entreprise");"decoration"->trV5(lang,"Themes, flowers, stage & lighting","થીમ, ફૂલ, સ્ટેજ અને લાઇટિંગ","थीम, फूल, स्टेज और लाइटिंग","Thèmes, fleurs, scène et éclairage");"catering"->trV5(lang,"Menus and packages","મેનૂ અને પેકેજ","मेनू और पैकेज","Menus et forfaits");"consultancy"->trV5(lang,"Finance, HR, French & real estate","ફાઇનાન્સ, HR, ફ્રેન્ચ અને રિયલ એસ્ટેટ","वित्त, HR, फ्रेंच और रियल एस्टेट","Finance, RH, français et immobilier");"travel"->trV5(lang,"Flights, trains, hotels, visa & transport","ફ્લાઇટ, ટ્રેન, હોટેલ, વિઝા અને ટ્રાન્સપોર્ટ","फ्लाइट, ट्रेन, होटल, वीज़ा और परिवहन","Vols, trains, hôtels, visa et transport");"shopping"->trV5(lang,"Products, gifts and essentials","પ્રોડક્ટ, ગિફ્ટ અને જરૂરી વસ્તુઓ","उत्पाद, उपहार और ज़रूरी सामान","Produits, cadeaux et essentiels");else->""}
private fun optionV5(lang:String,o:String)=when(o){"Daily Horoscope"->trV5(lang,o,"દૈનિક રાશિફળ","दैनिक राशिफल","Horoscope du jour");"Kundli / Birth Chart"->trV5(lang,o,"કુંડળી / જન્મકુંડળી","कुंडली / जन्मपत्री","Kundli / Thème natal");"Marriage Matching"->trV5(lang,o,"લગ્ન મિલાન","विवाह मिलान","Compatibilité mariage");"Ask an Astrologer"->trV5(lang,o,"જ્યોતિષીને પૂછો","ज्योतिषी से पूछें","Consulter un astrologue");"Muhurat & Puja"->trV5(lang,o,"મુહૂર્ત અને પૂજા","मुहूर्त और पूजा","Muhurat et Puja");"Flight Search"->trV5(lang,o,"ફ્લાઇટ શોધ","फ्लाइट खोज","Recherche de vols");"Flight Schedule"->trV5(lang,o,"ફ્લાઇટ સમયપત્રક","फ्लाइट समय-सारणी","Horaires des vols");"Train Search"->trV5(lang,o,"ટ્રેન શોધ","ट्रेन खोज","Recherche de trains");"Train Schedule"->trV5(lang,o,"ટ્રેન સમયપત્રક","ट्रेन समय-सारणी","Horaires des trains");"Hotel Booking"->trV5(lang,o,"હોટેલ બુકિંગ","होटल बुकिंग","Réservation d’hôtel");"Puja Products"->trV5(lang,o,"પૂજા પ્રોડક્ટ્સ","पूजा उत्पाद","Produits de Puja");"Astrology Products"->trV5(lang,o,"જ્યોતિષ પ્રોડક્ટ્સ","ज्योतिष उत्पाद","Produits d’astrologie");"Gifts"->trV5(lang,o,"ગિફ્ટ્સ","उपहार","Cadeaux");"Decoration Items"->trV5(lang,o,"ડેકોરેશન વસ્તુઓ","सजावट सामग्री","Articles de décoration");"Travel Accessories"->trV5(lang,o,"ટ્રાવેલ એસેસરીઝ","यात्रा सामान","Accessoires de voyage");"Local Products"->trV5(lang,o,"લોકલ પ્રોડક્ટ્સ","स्थानीय उत्पाद","Produits locaux");else->o}
private fun fetchMastersV5(onDone:(List<RWMasterV5>)->Unit){Thread{val out=mutableListOf<RWMasterV5>();try{val a=getArray("masters?select=id,master_type,name&order=master_type.asc,name.asc");for(i in 0 until a.length()){val x=a.getJSONObject(i);out+=RWMasterV5(x.optString("id"),x.optString("master_type"),x.optString("name"))}}catch(_:Exception){};ui{onDone(out)}}.start()}
private fun deleteV5(path:String,token:String,onDone:(Boolean)->Unit){Thread{val ok=try{val c=conn("$SB_URL/rest/v1/$path",token);c.requestMethod="DELETE";val r=c.responseCode in 200..299;c.disconnect();r}catch(_:Exception){false};ui{onDone(ok)}}.start()}
private fun patchV5(path:String,payload:JSONObject,token:String,onDone:(Boolean)->Unit){Thread{val ok=try{writeJson(path,"PATCH",payload,token)}catch(_:Exception){false};ui{onDone(ok)}}.start()}
'''
    s=s.replace(anchor,helpers+'\n'+anchor)

# Rawalworld shell/nav translation
start=s.find('@Composable fun RawalworldV2()')
end=s.find('@Composable fun HomeV2(',start)
if start>=0 and end>start:
    rep=r'''@Composable fun RawalworldV2(){
    val context=LocalContext.current
    if(RWLanguageV5.value=="English") RWLanguageV5.value=prefs(context).getString("language","English")?:"English"
    val lang=RWLanguageV5.value
    var tab by remember{mutableStateOf("home")};var selected by remember{mutableStateOf<RWService?>(null)}
    Scaffold(bottomBar={NavigationBar{
        NavigationBarItem(tab=="home",{tab="home"},{Icon(Icons.Default.Home,null)},label={Text(trV5(lang,"Home","હોમ","होम","Accueil"))})
        NavigationBarItem(tab=="bookings",{tab="bookings"},{Icon(Icons.Default.DateRange,null)},label={Text(trV5(lang,"Bookings","બુકિંગ","बुकिंग","Réservations"))})
        NavigationBarItem(tab=="shop",{tab="shop"},{Icon(Icons.Default.ShoppingCart,null)},label={Text(trV5(lang,"Shop","દુકાન","शॉप","Boutique"))})
        NavigationBarItem(tab=="gallery",{tab="gallery"},{Icon(Icons.Default.Photo,null)},label={Text(trV5(lang,"Gallery","ગેલેરી","गैलरी","Galerie"))})
        NavigationBarItem(tab=="profile",{tab="profile"},{Icon(Icons.Default.Person,null)},label={Text(trV5(lang,"Profile","પ્રોફાઇલ","प्रोफ़ाइल","Profil"))})
        NavigationBarItem(tab=="admin",{tab="admin"},{Icon(Icons.Default.Lock,null)},label={Text(trV5(lang,"Admin","એડમિન","एडमिन","Admin"))})
    }}){pad->Box(Modifier.fillMaxSize().padding(pad)){when(tab){"service"->selected?.let{ServiceScreenV2(it,{tab="home"})};"bookings"->BookingsV2();"shop"->ShopV2();"gallery"->GalleryV2();"profile"->ProfileV2();"admin"->AdminV2();else->HomeV2{selected=it;tab="service"}}}}}

'''
    s=s[:start]+rep+s[end:]

# Compact home language selector
start=s.find('@Composable fun HomeV2(')
end=s.find('@Composable fun ServiceScreenV2(',start)
if start>=0 and end>start:
    rep=r'''@Composable fun HomeV2(open:(RWService)->Unit){
    val context=LocalContext.current;val lang=RWLanguageV5.value;var menu by remember{mutableStateOf(false)}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
        Row(verticalAlignment=Alignment.CenterVertically){Text("🕉️",style=MaterialTheme.typography.displayMedium);Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text("Rawalworld",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold);Text(trV5(lang,"One app. Many services.","એક એપ. ઘણી સેવાઓ.","एक ऐप. कई सेवाएँ।","Une appli. Plusieurs services."))};Box{OutlinedButton(onClick={menu=true},contentPadding=PaddingValues(horizontal=9.dp,vertical=3.dp)){Text("🌐 "+when(lang){"Gujarati"->"GU";"Hindi"->"HI";"French"->"FR";else->"EN"})};DropdownMenu(menu,onDismissRequest={menu=false}){listOf("English","Gujarati","Hindi","French").forEach{v->DropdownMenuItem(text={Text(v)},onClick={RWLanguageV5.value=v;prefs(context).edit().putString("language",v).apply();menu=false})}}}}
        Spacer(Modifier.height(12.dp));Card(colors=CardDefaults.cardColors(containerColor=Purple)){Column(Modifier.padding(18.dp)){Text(trV5(lang,"Everything you need, in one app.","તમારી દરેક જરૂરિયાત, એક જ એપમાં.","आपकी हर ज़रूरत, एक ही ऐप में।","Tout ce dont vous avez besoin, dans une seule application."),color=Color.White,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)}}
        Spacer(Modifier.height(14.dp));baseServices.forEach{svc->Card(onClick={open(svc)},modifier=Modifier.fillMaxWidth().padding(vertical=5.dp),shape=RoundedCornerShape(18.dp)){Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){Text(svc.icon,style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(serviceTitleV5(lang,svc.id),fontWeight=FontWeight.Bold);Text(serviceDescV5(lang,svc.id),style=MaterialTheme.typography.bodySmall)};Text(trV5(lang,"Open ›","ખોલો ›","खोलें ›","Ouvrir ›"),color=Purple,fontWeight=FontWeight.Bold)}}}
    }
}

'''
    s=s[:start]+rep+s[end:]

# Service screen translation
start=s.find('@Composable fun ServiceScreenV2(')
end=s.find('@Composable fun BookingFormV2(',start)
if start>=0 and end>start:
    rep=r'''@Composable fun ServiceScreenV2(service:RWService,back:()->Unit){
    val lang=RWLanguageV5.value;var contact by remember{mutableStateOf<RWContact?>(null)};var selected by remember{mutableStateOf<String?>(null)}
    LaunchedEffect(service.id){fetchContact(service.id){contact=it}}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
        TextButton(onClick=back){Text(trV5(lang,"← Back","← પાછા","← वापस","← Retour"))}
        Text("${service.icon} ${serviceTitleV5(lang,service.id)}",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text(serviceDescV5(lang,service.id))
        contact?.let{c->if(listOf(c.contactName,c.phone,c.email,c.whatsapp).any{it.isNotBlank()})Card(Modifier.fillMaxWidth().padding(vertical=10.dp)){Column(Modifier.padding(12.dp)){Text(trV5(lang,"Service Contact","સર્વિસ સંપર્ક","सेवा संपर्क","Contact du service"),fontWeight=FontWeight.Bold);if(c.contactName.isNotBlank())Text(c.contactName);if(c.phone.isNotBlank())Text("📞 ${c.phone}");if(c.email.isNotBlank())Text("✉ ${c.email}");if(c.whatsapp.isNotBlank())Text("WhatsApp ${c.whatsapp}")}}}
        service.options.forEach{o->Card(Modifier.fillMaxWidth().padding(vertical=5.dp)){Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text(optionV5(lang,o),Modifier.weight(1f));Button(onClick={selected=o}){Text(trV5(lang,"Open","ખોલો","खोलें","Ouvrir"))}}}}
        selected?.let{Card(Modifier.fillMaxWidth().padding(top=10.dp)){Column(Modifier.padding(14.dp)){Text(optionV5(lang,it),fontWeight=FontWeight.Bold);Text(trV5(lang,"Information and booking support available.","માહિતી અને બુકિંગ સહાય ઉપલબ્ધ છે.","जानकारी और बुकिंग सहायता उपलब्ध है।","Informations et assistance de réservation disponibles."))}}}
        BookingFormV2(service.title)
    }
}

'''
    s=s[:start]+rep+s[end:]

# Booking form translation
start=s.find('@Composable fun BookingFormV2(')
end=s.find('@Composable fun ShopV2(',start)
if start>=0 and end>start:
    rep=r'''@Composable fun BookingFormV2(service:String){
    val lang=RWLanguageV5.value;val c=LocalContext.current;val p=prefs(c);var n by remember{mutableStateOf(p.getString("name","")?:"")};var m by remember{mutableStateOf(p.getString("mobile","")?:"")};var city by remember{mutableStateOf(p.getString("city","")?:"")};var note by remember{mutableStateOf("")};var msg by remember{mutableStateOf("")}
    Card(Modifier.fillMaxWidth().padding(top=12.dp)){Column(Modifier.padding(12.dp)){Text(trV5(lang,"Request Booking / Quotation","બુકિંગ / ક્વોટેશન માગો","बुकिंग / कोटेशन माँगें","Demander une réservation / un devis"),fontWeight=FontWeight.Bold);OutlinedTextField(n,{n=it},label={Text(trV5(lang,"Name","નામ","नाम","Nom"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(m,{m=it},label={Text(trV5(lang,"Mobile","મોબાઇલ","मोबाइल","Téléphone"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(city,{city=it},label={Text(trV5(lang,"City","શહેર","शहर","Ville"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(note,{note=it},label={Text(trV5(lang,"Requirement","જરૂરિયાત","आवश्यकता","Besoin"))},modifier=Modifier.fillMaxWidth());Button(onClick={if(n.isBlank()||m.isBlank()||city.isBlank())msg=trV5(lang,"Enter name, mobile and city.","નામ, મોબાઇલ અને શહેર દાખલ કરો.","नाम, मोबाइल और शहर दर्ज करें।","Saisissez nom, téléphone et ville.") else postPublic("bookings",JSONObject().put("service",service).put("customer_name",n).put("mobile",m).put("city",city).put("requirement",note).put("source","android")){ok->msg=if(ok)trV5(lang,"✅ Request submitted.","✅ વિનંતી મોકલાઈ.","✅ अनुरोध भेजा गया।","✅ Demande envoyée.") else trV5(lang,"Submission failed.","મોકલવામાં નિષ્ફળ.","भेजना विफल रहा।","Échec de l’envoi.")}},modifier=Modifier.fillMaxWidth()){Text(trV5(lang,"Submit Request","વિનંતી મોકલો","अनुरोध भेजें","Envoyer"))};if(msg.isNotBlank())Text(msg)}}
}

'''
    s=s[:start]+rep+s[end:]

# Replace Shop only; keep Gallery/Profile functions intact
start=s.find('@Composable fun ShopV2()')
end=s.find('@Composable fun ProductCard(',start)
if start>=0 and end>start:
    rep=r'''@Composable fun ShopV2(){
    val lang=RWLanguageV5.value;val context=LocalContext.current;var cats by remember{mutableStateOf(baseServices.last().options)};var selected by remember{mutableStateOf<String?>(null)};var products by remember{mutableStateOf<List<RWProduct>>(emptyList())};var loading by remember{mutableStateOf(false)}
    LaunchedEffect(Unit){fetchShopMasters{cats=it}}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){Text("🛍️ "+trV5(lang,"Online Shopping","ઓનલાઇન શોપિંગ","ऑनलाइन शॉपिंग","Shopping en ligne"),style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold);cats.forEach{cat->Button(onClick={selected=cat;loading=true;fetchProducts(cat){products=it;loading=false}},modifier=Modifier.fillMaxWidth().padding(vertical=5.dp)){Text(optionV5(lang,cat))}};selected?.let{Text(optionV5(lang,it),style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);if(loading)CircularProgressIndicator() else if(products.isEmpty())Text(trV5(lang,"No products in this category yet.","આ કેટેગરીમાં હજી પ્રોડક્ટ નથી.","इस कैटेगरी में अभी प्रोडक्ट नहीं है।","Aucun produit dans cette catégorie.")) else products.forEach{p->CheckoutCardV5(p,context)}}}
}

@Composable fun CheckoutCardV5(p:RWProduct,context:Context){
    val lang=RWLanguageV5.value;var expanded by remember(p.id){mutableStateOf(false)};var qtyText by remember(p.id){mutableStateOf("1")};var buyer by remember(p.id){mutableStateOf("")};var mobile by remember(p.id){mutableStateOf("")};var address by remember(p.id){mutableStateOf("")};var pincode by remember(p.id){mutableStateOf("")};var msg by remember(p.id){mutableStateOf("")};val qty=(qtyText.toIntOrNull()?:1).coerceAtLeast(1);val total=qty*p.price
    Card(Modifier.fillMaxWidth().padding(vertical=6.dp)){Column(Modifier.padding(12.dp)){p.imageUrl?.let{RemoteImageV2(it)};Text(p.name,fontWeight=FontWeight.Bold);Text(p.description,style=MaterialTheme.typography.bodySmall);Text(if(p.price==0.0)"FREE" else "₹ ${String.format("%.2f",p.price)}",fontWeight=FontWeight.Bold)
        if(!expanded){Button(onClick={expanded=true},modifier=Modifier.fillMaxWidth()){Text(trV5(lang,"Buy Now","હમણાં ખરીદો","अभी खरीदें","Acheter"))}}
        else{OutlinedTextField(qtyText,{qtyText=it.filter(Char::isDigit).ifBlank{"1"}},label={Text(trV5(lang,"Quantity","જથ્થો","मात्रा","Quantité"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(buyer,{buyer=it},label={Text(trV5(lang,"Purchaser name","ખરીદનારનું નામ","खरीदार का नाम","Nom de l’acheteur"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(mobile,{mobile=it},label={Text(trV5(lang,"Mobile","મોબાઇલ","मोबाइल","Téléphone"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(address,{address=it},label={Text(trV5(lang,"Delivery address","ડિલિવરી સરનામું","डिलीवरी पता","Adresse de livraison"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(pincode,{pincode=it.filter(Char::isDigit)},label={Text(trV5(lang,"Pincode","પિનકોડ","पिनकोड","Code postal"))},modifier=Modifier.fillMaxWidth());Text("₹ ${String.format("%.2f",total)}",fontWeight=FontWeight.Bold);Button(onClick={if(buyer.isBlank()||mobile.isBlank()||address.isBlank()||pincode.isBlank())msg=trV5(lang,"Complete all delivery details.","બધી ડિલિવરી વિગતો भरो.","सभी डिलीवरी विवरण भरें।","Complétez les informations de livraison.") else{val payload=JSONObject().put("product_id",p.id).put("product_name",p.name).put("quantity",qty).put("unit_price",p.price).put("total_amount",total).put("currency","INR").put("customer_name",buyer).put("mobile",mobile).put("address",address).put("pincode",pincode).put("payment_method","UPI / GPay").put("payment_status","pending").put("order_status","submitted");postPublic("orders",payload){ok->if(ok){msg=trV5(lang,"Order saved. Opening UPI payment...","ઓર્ડર સાચવાયો. UPI ચુકવણી ખૂલી રહી છે...","ऑर्डर सेव हुआ। UPI भुगतान खुल रहा है...","Commande enregistrée. Ouverture du paiement UPI...");val uri=Uri.parse("upi://pay?pa=harshrawal1929-1@okicici&pn=Haresh%20Rawal&am=${String.format("%.2f",total)}&cu=INR&tn=${Uri.encode(p.name)}");try{context.startActivity(Intent(Intent.ACTION_VIEW,uri))}catch(_:Exception){msg=trV5(lang,"No UPI app found.","UPI એપ મળી નથી.","UPI ऐप नहीं मिला।","Aucune application UPI trouvée.")}}else msg=trV5(lang,"Could not submit order.","ઓર્ડર મોકલી શકાયો નહીં.","ऑर्डर जमा नहीं हो सका।","Impossible d’envoyer la commande.")}}},modifier=Modifier.fillMaxWidth()){Text(trV5(lang,"Continue to Payment","ચુકવણી માટે આગળ વધો","भुगतान के लिए आगे बढ़ें","Continuer vers le paiement"))};if(msg.isNotBlank())Text(msg,style=MaterialTheme.typography.bodySmall)}
    }}
}

'''
    s=s[:start]+rep+s[end:]

# Translate profile safely
start=s.find('@Composable fun ProfileV2()')
end=s.find('@Composable fun BookingsV2()',start)
if start>=0 and end>start:
    rep=r'''@Composable fun ProfileV2(){val lang=RWLanguageV5.value;val c=LocalContext.current;val p=prefs(c);var n by remember{mutableStateOf(p.getString("name","")?:"")};var m by remember{mutableStateOf(p.getString("mobile","")?:"")};var e by remember{mutableStateOf(p.getString("email","")?:"")};var city by remember{mutableStateOf(p.getString("city","")?:"")};var msg by remember{mutableStateOf("")};Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){Text(trV5(lang,"Profile","પ્રોફાઇલ","प्रोफ़ाइल","Profil"),style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);OutlinedTextField(n,{n=it},label={Text(trV5(lang,"Name","નામ","नाम","Nom"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(m,{m=it},label={Text(trV5(lang,"Mobile","મોબાઇલ","मोबाइल","Téléphone"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(e,{e=it},label={Text(trV5(lang,"Email","ઇમેઇલ","ईमेल","E-mail"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(city,{city=it},label={Text(trV5(lang,"City","શહેર","शहर","Ville"))},modifier=Modifier.fillMaxWidth());Button(onClick={p.edit().putString("name",n).putString("mobile",m).putString("email",e).putString("city",city).apply();msg=trV5(lang,"Profile saved.","પ્રોફાઇલ સાચવાઈ.","प्रोफ़ाइल सेव हुई।","Profil enregistré.")},modifier=Modifier.fillMaxWidth()){Text(trV5(lang,"Save Profile","પ્રોફાઇલ સાચવો","प्रोफ़ाइल सेव करें","Enregistrer le profil"))};Text(msg)}}

'''
    s=s[:start]+rep+s[end:]

# Add master edit/delete manager before SimpleDropdown, then inject into Admin dashboard after Add Master area.
insert_at=s.find('@Composable fun SimpleDropdown(')
if insert_at>=0 and '@Composable fun MasterManagerV5' not in s:
    comp=r'''@Composable fun MasterManagerV5(session:RWSession,onMessage:(String)->Unit){
    val lang=RWLanguageV5.value;var rows by remember{mutableStateOf<List<RWMasterV5>>(emptyList())};var refresh by remember{mutableStateOf(0)}
    LaunchedEffect(refresh){fetchMastersV5{rows=it}}
    Text(trV5(lang,"Existing Masters","હાલના માસ્ટર","मौजूदा मास्टर","Référentiels existants"),fontWeight=FontWeight.Bold)
    rows.forEach{m->var edit by remember(m.id){mutableStateOf(false)};var name by remember(m.id){mutableStateOf(m.name)};Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(10.dp)){if(edit)OutlinedTextField(name,{name=it},label={Text(trV5(lang,"Master name","માસ્ટર નામ","मास्टर नाम","Nom du référentiel"))},modifier=Modifier.fillMaxWidth()) else Text("${m.type}: ${m.name}");Row{Button(onClick={if(edit)patchV5("masters?id=eq.${m.id}",JSONObject().put("name",name),session.access){ok->onMessage(if(ok)trV5(lang,"Master updated.","માસ્ટર અપડેટ થયો.","मास्टर अपडेट हुआ।","Référentiel mis à jour.") else trV5(lang,"Update failed.","અપડેટ નિષ્ફળ.","अपडेट विफल।","Échec de la mise à jour."));if(ok){edit=false;refresh++}} else edit=true}){Text(if(edit)trV5(lang,"Save","સાચવો","सेव करें","Enregistrer") else trV5(lang,"Edit","ફેરફાર","संपादित करें","Modifier"))};Spacer(Modifier.width(8.dp));OutlinedButton(onClick={deleteV5("masters?id=eq.${m.id}",session.access){ok->onMessage(if(ok)trV5(lang,"Master deleted.","માસ્ટર ડિલીટ થયો.","मास्टर हटाया गया।","Référentiel supprimé.") else trV5(lang,"Delete failed.","ડિલીટ નિષ્ફળ.","हटाना विफल।","Échec de la suppression."));if(ok)refresh++}}){Text(trV5(lang,"Delete","ડિલીટ","हटाएँ","Supprimer"))}}}}}
}

'''
    s=s[:insert_at]+comp+s[insert_at:]

# Inject MasterManager call after MASTER MANAGEMENT heading so controls always show.
needle='Text("MASTER MANAGEMENT",color=Purple,fontWeight=FontWeight.Bold)'
if needle in s and 'MasterManagerV5(session){msg=it}' not in s:
    s=s.replace(needle,needle+';MasterManagerV5(session){msg=it}',1)

p.write_text(s,encoding='utf-8')
