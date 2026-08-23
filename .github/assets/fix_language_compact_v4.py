from pathlib import Path
p=Path('app/src/main/java/com/astrolife/app/MainActivityV2.kt')
s=p.read_text(encoding='utf-8')

def replace_block(start_marker,end_marker,replacement):
    global s
    a=s.find(start_marker)
    if a<0: return False
    b=s.find(end_marker,a)
    if b<0: return False
    s=s[:a]+replacement+'\n\n'+s[b:]
    return True

# Shared localization helpers. Internal IDs/English names remain unchanged for database compatibility.
anchor='@Composable fun RawalworldV2()'
if 'private fun rwTrV4(' not in s and anchor in s:
    helpers=r'''private fun rwLangV4(c:Context)=prefs(c).getString("language","English")?:"English"
private fun rwCodeV4(lang:String)=when(lang){"Gujarati"->"GU";"Hindi"->"HI";"French"->"FR";else->"EN"}
private fun rwTrV4(lang:String,en:String,gu:String,hi:String,fr:String)=when(lang){"Gujarati"->gu;"Hindi"->hi;"French"->fr;else->en}
private fun rwServiceTitleV4(lang:String,id:String)=when(id){
 "astrology"->rwTrV4(lang,"Astrology","જ્યોતિષ","ज्योतिष","Astrologie")
 "events"->rwTrV4(lang,"Events","ઇવેન્ટ્સ","कार्यक्रम","Événements")
 "decoration"->rwTrV4(lang,"Decoration","ડેકોરેશન","सजावट","Décoration")
 "catering"->rwTrV4(lang,"Catering","કેટરિંગ","कैटरिंग","Traiteur")
 "consultancy"->rwTrV4(lang,"Consultancy","કન્સલ્ટન્સી","परामर्श","Conseil")
 "travel"->rwTrV4(lang,"Tours & Travel","ટૂર્સ અને ટ્રાવેલ","टूर्स और ट्रैवल","Tours & Voyages")
 "shopping"->rwTrV4(lang,"Online Shopping","ઓનલાઇન શોપિંગ","ऑनलाइन शॉपिंग","Shopping en ligne")
 else->id
}
private fun rwServiceDescV4(lang:String,id:String)=when(id){
 "astrology"->rwTrV4(lang,"Horoscope, Kundli & consultation","રાશિફળ, કુંડળી અને સલાહ","राशिफल, कुंडली और परामर्श","Horoscope, Kundli et consultation")
 "events"->rwTrV4(lang,"Weddings, birthdays & corporate events","લગ્ન, જન્મદિવસ અને કોર્પોરેટ ઇવેન્ટ્સ","शादी, जन्मदिन और कॉर्पोरेट कार्यक्रम","Mariages, anniversaires et événements d’entreprise")
 "decoration"->rwTrV4(lang,"Themes, flowers, stage & lighting","થીમ, ફૂલ, સ્ટેજ અને લાઇટિંગ","थीम, फूल, स्टेज और लाइटिंग","Thèmes, fleurs, scène et éclairage")
 "catering"->rwTrV4(lang,"Menus and packages for every occasion","દરેક પ્રસંગ માટે મેનૂ અને પેકેજ","हर अवसर के लिए मेनू और पैकेज","Menus et forfaits pour chaque occasion")
 "consultancy"->rwTrV4(lang,"Business and professional services","બિઝનેસ અને પ્રોફેશનલ સેવાઓ","व्यापार और पेशेवर सेवाएँ","Services professionnels et d’entreprise")
 "travel"->rwTrV4(lang,"Flights, trains, hotels, visa & transport","ફ્લાઇટ, ટ્રેન, હોટેલ, વિઝા અને ટ્રાન્સપોર્ટ","फ्लाइट, ट्रेन, होटल, वीज़ा और परिवहन","Vols, trains, hôtels, visa et transport")
 "shopping"->rwTrV4(lang,"Products, gifts and essentials","પ્રોડક્ટ, ગિફ્ટ અને જરૂરી વસ્તુઓ","उत्पाद, उपहार और ज़रूरी सामान","Produits, cadeaux et essentiels")
 else->""
}
private fun rwOptionV4(lang:String,o:String)=when(o){
 "Daily Horoscope"->rwTrV4(lang,"Daily Horoscope","દૈનિક રાશિફળ","दैनिक राशिफल","Horoscope du jour")
 "Kundli / Birth Chart"->rwTrV4(lang,"Kundli / Birth Chart","કુંડળી / જન્મકુંડળી","कुंडली / जन्मपत्री","Kundli / Thème natal")
 "Marriage Matching"->rwTrV4(lang,"Marriage Matching","લગ્ન મિલાન","विवाह मिलान","Compatibilité mariage")
 "Ask an Astrologer"->rwTrV4(lang,"Ask an Astrologer","જ્યોતિષીને પૂછો","ज्योतिषी से पूछें","Consulter un astrologue")
 "Muhurat & Puja"->rwTrV4(lang,"Muhurat & Puja","મુહૂર્ત અને પૂજા","मुहूर्त और पूजा","Muhurat et Puja")
 "Wedding"->rwTrV4(lang,"Wedding","લગ્ન","शादी","Mariage")
 "Birthday"->rwTrV4(lang,"Birthday","જન્મદિવસ","जन्मदिन","Anniversaire")
 "Engagement"->rwTrV4(lang,"Engagement","સગાઈ","सगाई","Fiançailles")
 "Anniversary"->rwTrV4(lang,"Anniversary","વર્ષગાંઠ","वर्षगाँठ","Anniversaire de mariage")
 "Corporate Event"->rwTrV4(lang,"Corporate Event","કોર્પોરેટ ઇવેન્ટ","कॉर्पोरेट कार्यक्रम","Événement d’entreprise")
 "Religious Event"->rwTrV4(lang,"Religious Event","ધાર્મિક ઇવેન્ટ","धार्मिक कार्यक्रम","Événement religieux")
 "Wedding Decoration"->rwTrV4(lang,"Wedding Decoration","લગ્ન ડેકોરેશન","शादी की सजावट","Décoration de mariage")
 "Stage Decoration"->rwTrV4(lang,"Stage Decoration","સ્ટેજ ડેકોરેશન","स्टेज सजावट","Décoration de scène")
 "Birthday Theme"->rwTrV4(lang,"Birthday Theme","જન્મદિવસ થીમ","जन्मदिन थीम","Thème anniversaire")
 "Flower Decoration"->rwTrV4(lang,"Flower Decoration","ફૂલ ડેકોરેશન","फूल सजावट","Décoration florale")
 "Mandap"->rwTrV4(lang,"Mandap","મંડપ","मंडप","Mandap")
 "Lighting"->rwTrV4(lang,"Lighting","લાઇટિંગ","लाइटिंग","Éclairage")
 "Gujarati"->rwTrV4(lang,"Gujarati","ગુજરાતી","गुजराती","Gujarati")
 "Punjabi"->rwTrV4(lang,"Punjabi","પંજાબી","पंजाबी","Punjabi")
 "South Indian"->rwTrV4(lang,"South Indian","સાઉથ ઇન્ડિયન","दक्षिण भारतीय","Sud-indien")
 "Jain"->rwTrV4(lang,"Jain","જૈન","जैन","Jain")
 "Continental"->rwTrV4(lang,"Continental","કોન્ટિનેન્ટલ","कॉन्टिनेंटल","Continental")
 "Custom Package"->rwTrV4(lang,"Custom Package","કસ્ટમ પેકેજ","कस्टम पैकेज","Forfait personnalisé")
 "Accounts & Finance"->rwTrV4(lang,"Accounts & Finance","એકાઉન્ટ્સ અને ફાઇનાન્સ","लेखा और वित्त","Comptabilité & Finance")
 "HR"->rwTrV4(lang,"HR","એચઆર","एचआर","RH")
 "Business Setup"->rwTrV4(lang,"Business Setup","બિઝનેસ સેટઅપ","बिज़नेस सेटअप","Création d’entreprise")
 "French Support"->rwTrV4(lang,"French Support","ફ્રેન્ચ સપોર્ટ","फ्रेंच सहायता","Support français")
 "Real Estate"->rwTrV4(lang,"Real Estate","રિયલ એસ્ટેટ","रियल एस्टेट","Immobilier")
 "Documentation"->rwTrV4(lang,"Documentation","ડોક્યુમેન્ટેશન","दस्तावेज़ीकरण","Documentation")
 "Flight Search"->rwTrV4(lang,"Flight Search","ફ્લાઇટ શોધ","फ्लाइट खोज","Recherche de vols")
 "Flight Schedule"->rwTrV4(lang,"Flight Schedule","ફ્લાઇટ સમયપત્રક","फ्लाइट समय-सारणी","Horaires des vols")
 "Train Search"->rwTrV4(lang,"Train Search","ટ્રેન શોધ","ट्रेन खोज","Recherche de trains")
 "Train Schedule"->rwTrV4(lang,"Train Schedule","ટ્રેન સમયપત્રક","ट्रेन समय-सारणी","Horaires des trains")
 "Hotel Booking"->rwTrV4(lang,"Hotel Booking","હોટેલ બુકિંગ","होटल बुकिंग","Réservation d’hôtel")
 "Holiday Packages"->rwTrV4(lang,"Holiday Packages","હોલિડે પેકેજ","हॉलिडे पैकेज","Forfaits vacances")
 "Visa Assistance"->rwTrV4(lang,"Visa Assistance","વિઝા સહાય","वीज़ा सहायता","Assistance visa")
 "Cab / Vehicle Rental"->rwTrV4(lang,"Cab / Vehicle Rental","કેબ / વાહન ભાડે","कैब / वाहन किराया","Location de véhicule")
 "Group Tours"->rwTrV4(lang,"Group Tours","ગ્રુપ ટૂર્સ","ग्रुप टूर","Voyages en groupe")
 "Puja Products"->rwTrV4(lang,"Puja Products","પૂજા પ્રોડક્ટ્સ","पूजा उत्पाद","Produits de Puja")
 "Astrology Products"->rwTrV4(lang,"Astrology Products","જ્યોતિષ પ્રોડક્ટ્સ","ज्योतिष उत्पाद","Produits d’astrologie")
 "Gifts"->rwTrV4(lang,"Gifts","ગિફ્ટ્સ","उपहार","Cadeaux")
 "Decoration Items"->rwTrV4(lang,"Decoration Items","ડેકોરેશન વસ્તુઓ","सजावट सामग्री","Articles de décoration")
 "Travel Accessories"->rwTrV4(lang,"Travel Accessories","ટ્રાવેલ એસેસરીઝ","यात्रा सहायक सामान","Accessoires de voyage")
 "Local Products"->rwTrV4(lang,"Local Products","સ્થાનિક પ્રોડક્ટ્સ","स्थानीय उत्पाद","Produits locaux")
 else->o
}
'''
    s=s.replace(anchor,helpers+'\n'+anchor,1)

home=r'''@Composable fun HomeV2(open:(RWService)->Unit){
    val context=LocalContext.current
    var lang by remember{mutableStateOf(rwLangV4(context))}
    var languageOpen by remember{mutableStateOf(false)}
    val hero=rwTrV4(lang,"Everything you need, in one app.","તમારી દરેક જરૂરિયાત, એક જ એપમાં.","आपकी हर ज़रूरत, एक ही ऐप में।","Tout ce dont vous avez besoin, dans une seule application.")
    val sub=rwTrV4(lang,"Astrology, events, travel, shopping and more.","જ્યોતિષ, ઇવેન્ટ, પ્રવાસ, ખરીદી અને વધુ.","ज्योतिष, कार्यक्रम, यात्रा, खरीदारी और बहुत कुछ।","Astrologie, événements, voyages, shopping et plus encore.")
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
            Text("🕉️",style=MaterialTheme.typography.headlineLarge);Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)){Text("Rawalworld",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold);Text(rwTrV4(lang,"One app. Many services.","એક એપ. ઘણી સેવાઓ.","एक ऐप। कई सेवाएँ।","Une app. Plusieurs services."),style=MaterialTheme.typography.bodySmall)}
            Box{TextButton(onClick={languageOpen=true}){Text("🌐 ${rwCodeV4(lang)}")};DropdownMenu(expanded=languageOpen,onDismissRequest={languageOpen=false}){listOf("English","Gujarati","Hindi","French").forEach{l->DropdownMenuItem(text={Text(when(l){"Gujarati"->"ગુજરાતી";"Hindi"->"हिंदी";"French"->"Français";else->"English"})},onClick={lang=l;prefs(context).edit().putString("language",l).apply();languageOpen=false})}}}
        }
        Spacer(Modifier.height(8.dp))
        Card(colors=CardDefaults.cardColors(containerColor=Purple)){Column(Modifier.padding(18.dp)){Text(hero,color=Color.White,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(sub,color=Color.White)}}
        Spacer(Modifier.height(12.dp));Text(rwTrV4(lang,"Explore services","સેવાઓ જુઓ","सेवाएँ देखें","Découvrir les services"),fontWeight=FontWeight.Bold)
        baseServices.forEach{sv->Card(onClick={open(sv)},modifier=Modifier.fillMaxWidth().padding(vertical=5.dp),shape=RoundedCornerShape(18.dp)){Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){Text(sv.icon,style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(rwServiceTitleV4(lang,sv.id),fontWeight=FontWeight.Bold);Text(rwServiceDescV4(lang,sv.id),style=MaterialTheme.typography.bodySmall)};Text(rwTrV4(lang,"Open ›","ખોલો ›","खोलें ›","Ouvrir ›"),color=Purple,fontWeight=FontWeight.Bold)}}}
        Text("📞 +91 77093 78969   ✉ rawalworld@gmail.com",style=MaterialTheme.typography.bodySmall)
    }
}'''
replace_block('@Composable fun HomeV2(','@Composable fun ServiceScreenV2(',home)

service=r'''@Composable fun ServiceScreenV2(service:RWService,back:()->Unit){
    val context=LocalContext.current;val lang=rwLangV4(context)
    var contact by remember{mutableStateOf<RWContact?>(null)};var selected by remember{mutableStateOf<String?>(null)}
    LaunchedEffect(service.id){fetchContact(service.id){contact=it}}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
        TextButton(onClick=back){Text(rwTrV4(lang,"← Back","← પાછા","← वापस","← Retour"))}
        Text("${service.icon} ${rwServiceTitleV4(lang,service.id)}",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text(rwServiceDescV4(lang,service.id))
        contact?.let{c->if(listOf(c.contactName,c.phone,c.email,c.whatsapp).any{it.isNotBlank()})Card(Modifier.fillMaxWidth().padding(vertical=10.dp)){Column(Modifier.padding(12.dp)){Text(rwTrV4(lang,"Service Contact","સેવા સંપર્ક","सेवा संपर्क","Contact du service"),fontWeight=FontWeight.Bold);if(c.contactName.isNotBlank())Text(c.contactName);if(c.phone.isNotBlank())Text("📞 ${c.phone}");if(c.email.isNotBlank())Text("✉ ${c.email}");if(c.whatsapp.isNotBlank())Text("WhatsApp ${c.whatsapp}")}}}
        service.options.forEach{o->Card(Modifier.fillMaxWidth().padding(vertical=5.dp)){Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text(rwOptionV4(lang,o),Modifier.weight(1f));Button(onClick={selected=o}){Text(rwTrV4(lang,"Open","ખોલો","खोलें","Ouvrir"))}}}}
        selected?.let{Card(Modifier.fillMaxWidth().padding(top=10.dp)){Column(Modifier.padding(14.dp)){Text(rwOptionV4(lang,it),fontWeight=FontWeight.Bold);Text(rwTrV4(lang,"Detailed information and booking support is available for this service.","આ સેવા માટે વિગતવાર માહિતી અને બુકિંગ સહાય ઉપલબ્ધ છે.","इस सेवा के लिए विस्तृत जानकारी और बुकिंग सहायता उपलब्ध है।","Des informations détaillées et une assistance à la réservation sont disponibles."))}}}
        BookingFormV2(rwServiceTitleV4(lang,service.id))
    }
}'''
replace_block('@Composable fun ServiceScreenV2(','@Composable fun BookingFormV2(',service)

# Translate booking form labels.
booking=r'''@Composable fun BookingFormV2(service:String){
    val c=LocalContext.current;val lang=rwLangV4(c);val p=prefs(c)
    var n by remember{mutableStateOf(p.getString("name","")?:"")};var m by remember{mutableStateOf(p.getString("mobile","")?:"")};var city by remember{mutableStateOf(p.getString("city","")?:"")};var note by remember{mutableStateOf("")};var msg by remember{mutableStateOf("")}
    Card(Modifier.fillMaxWidth().padding(top=12.dp)){Column(Modifier.padding(12.dp)){
        Text(rwTrV4(lang,"Request Booking / Quotation","બુકિંગ / ક્વોટેશન માગો","बुकिंग / कोटेशन माँगें","Demander une réservation / un devis"),fontWeight=FontWeight.Bold)
        OutlinedTextField(n,{n=it},label={Text(rwTrV4(lang,"Name","નામ","नाम","Nom"))},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(m,{m=it},label={Text(rwTrV4(lang,"Mobile","મોબાઇલ","मोबाइल","Téléphone"))},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(city,{city=it},label={Text(rwTrV4(lang,"City","શહેર","शहर","Ville"))},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(note,{note=it},label={Text(rwTrV4(lang,"Requirement","જરૂરિયાત","आवश्यकता","Besoin"))},modifier=Modifier.fillMaxWidth())
        Button(onClick={if(n.isBlank()||m.isBlank()||city.isBlank())msg=rwTrV4(lang,"Enter name, mobile and city.","નામ, મોબાઇલ અને શહેર દાખલ કરો.","नाम, मोबाइल और शहर दर्ज करें।","Saisissez le nom, le téléphone et la ville.") else{p.edit().putString("name",n).putString("mobile",m).putString("city",city).apply();postPublic("bookings",JSONObject().put("service",service).put("customer_name",n).put("mobile",m).put("city",city).put("requirement",note).put("source","android")){ok->msg=if(ok)rwTrV4(lang,"✅ Request submitted.","✅ વિનંતી મોકલાઈ.","✅ अनुरोध भेजा गया।","✅ Demande envoyée.") else rwTrV4(lang,"Could not submit request.","વિનંતી મોકલી શકાઈ નથી.","अनुरोध भेजा नहीं जा सका।","Impossible d’envoyer la demande.")}}},modifier=Modifier.fillMaxWidth()){Text(rwTrV4(lang,"Submit Request","વિનંતી મોકલો","अनुरोध भेजें","Envoyer la demande"))};if(msg.isNotBlank())Text(msg)
    }}
}'''
replace_block('@Composable fun BookingFormV2(','@Composable fun BookingsV2(',booking)

# Customer-facing shop and checkout translations.
shop=r'''@Composable fun ShopV2(){
    val context=LocalContext.current;val lang=rwLangV4(context)
    var cats by remember{mutableStateOf(baseServices.last().options)};var selected by remember{mutableStateOf<String?>(null)};var products by remember{mutableStateOf<List<RWProduct>>(emptyList())};var loading by remember{mutableStateOf(false)}
    LaunchedEffect(Unit){fetchShopMasters{cats=it}}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
        Text("🛍️ ${rwServiceTitleV4(lang,"shopping")}",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold)
        Text(rwTrV4(lang,"Choose a category, quantity and delivery details, then pay using UPI / GPay.","કેટેગરી, જથ્થો અને ડિલિવરી વિગતો પસંદ કરો, પછી UPI / GPay દ્વારા ચુકવણી કરો.","श्रेणी, मात्रा और डिलीवरी विवरण चुनें, फिर UPI / GPay से भुगतान करें।","Choisissez une catégorie, la quantité et les détails de livraison, puis payez par UPI / GPay."))
        cats.forEach{cat->Button(onClick={selected=cat;loading=true;fetchProducts(cat){products=it;loading=false}},modifier=Modifier.fillMaxWidth().padding(vertical=5.dp)){Text(rwOptionV4(lang,cat))}}
        selected?.let{Text(rwOptionV4(lang,it),style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);if(loading)CircularProgressIndicator() else if(products.isEmpty())Text(rwTrV4(lang,"No products added in this category yet.","આ કેટેગરીમાં હજુ કોઈ પ્રોડક્ટ નથી.","इस श्रेणी में अभी कोई उत्पाद नहीं है।","Aucun produit dans cette catégorie.")) else products.forEach{p->ShopCheckoutCardV3(p,context)}}
    }
}'''
replace_block('@Composable fun ShopV2()','@Composable fun ShopCheckoutCardV3(',shop)

checkout=r'''@Composable fun ShopCheckoutCardV3(p:RWProduct,context:Context){
    val lang=rwLangV4(context);var qtyText by remember(p.id){mutableStateOf("1")};var buyer by remember(p.id){mutableStateOf("")};var mobile by remember(p.id){mutableStateOf("")};var address by remember(p.id){mutableStateOf("")};var pincode by remember(p.id){mutableStateOf("")};var msg by remember(p.id){mutableStateOf("")};val qty=(qtyText.toIntOrNull()?:1).coerceAtLeast(1);val total=qty*p.price
    Card(Modifier.fillMaxWidth().padding(vertical=6.dp)){Column(Modifier.padding(12.dp)){p.imageUrl?.let{RemoteImageV2(it)};Text(p.name,fontWeight=FontWeight.Bold);Text(p.description,style=MaterialTheme.typography.bodySmall);Text(if(p.price==0.0)rwTrV4(lang,"FREE","મફત","मुफ़्त","GRATUIT") else "₹ ${String.format("%.2f",p.price)}",fontWeight=FontWeight.Bold)
        OutlinedTextField(qtyText,{qtyText=it.filter(Char::isDigit).ifBlank{"1"}},label={Text(rwTrV4(lang,"Quantity","જથ્થો","मात्रा","Quantité"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(buyer,{buyer=it},label={Text(rwTrV4(lang,"Purchaser name","ખરીદનારનું નામ","खरीदार का नाम","Nom de l’acheteur"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(mobile,{mobile=it},label={Text(rwTrV4(lang,"Mobile number","મોબાઇલ નંબર","मोबाइल नंबर","Téléphone"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(address,{address=it},label={Text(rwTrV4(lang,"Delivery address","ડિલિવરી સરનામું","डिलीवरी पता","Adresse de livraison"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(pincode,{pincode=it.filter(Char::isDigit)},label={Text(rwTrV4(lang,"Pincode","પિનકોડ","पिनकोड","Code postal"))},modifier=Modifier.fillMaxWidth());Text("${rwTrV4(lang,"Total","કુલ","कुल","Total")}: ₹ ${String.format("%.2f",total)}",fontWeight=FontWeight.Bold)
        Button(onClick={if(buyer.isBlank()||mobile.isBlank()||address.isBlank()||pincode.isBlank())msg=rwTrV4(lang,"Enter all delivery details.","બધી ડિલિવરી વિગતો દાખલ કરો.","सभी डिलीवरी विवरण दर्ज करें।","Saisissez tous les détails de livraison.") else{val payload=JSONObject().put("product_id",p.id).put("product_name",p.name).put("quantity",qty).put("unit_price",p.price).put("total_amount",total).put("currency","INR").put("customer_name",buyer).put("mobile",mobile).put("address",address).put("pincode",pincode).put("payment_method","UPI / GPay").put("payment_status","pending").put("order_status","submitted");postPublic("orders",payload){ok->msg=if(ok)rwTrV4(lang,"✅ Order submitted. Now pay with UPI / GPay.","✅ ઓર્ડર મોકલાયો. હવે UPI / GPayથી ચૂકવો.","✅ ऑर्डर भेजा गया। अब UPI / GPay से भुगतान करें।","✅ Commande envoyée. Payez maintenant par UPI / GPay.") else rwTrV4(lang,"Could not submit order.","ઓર્ડર મોકલી શકાયો નથી.","ऑर्डर भेजा नहीं जा सका।","Impossible d’envoyer la commande.")}}},modifier=Modifier.fillMaxWidth()){Text(rwTrV4(lang,"Purchase","ખરીદો","खरीदें","Acheter"))}
        OutlinedButton(onClick={val uri=Uri.parse("upi://pay?pa=harshrawal1929-1@okicici&pn=Haresh%20Rawal&am=${String.format("%.2f",total)}&cu=INR&tn=${Uri.encode(p.name)}");try{context.startActivity(Intent(Intent.ACTION_VIEW,uri))}catch(_:Exception){msg=rwTrV4(lang,"No UPI app found.","UPI એપ મળી નથી.","UPI ऐप नहीं मिला।","Aucune application UPI trouvée.")}},modifier=Modifier.fillMaxWidth()){Text(rwTrV4(lang,"Pay with UPI / GPay","UPI / GPayથી ચૂકવો","UPI / GPay से भुगतान करें","Payer par UPI / GPay"))};if(msg.isNotBlank())Text(msg,style=MaterialTheme.typography.bodySmall)
    }}
}'''
replace_block('@Composable fun ShopCheckoutCardV3(','@Composable fun GalleryV2()',checkout)

p.write_text(s,encoding='utf-8')
