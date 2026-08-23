package com.astrolife.app

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private const val SB_URL="https://hcpvuripnlhofxfczyyb.supabase.co"
private const val SB_KEY="sb_publishable_J8YoD4yenQO-nlEMoC1kvA_3_vJgGjg"
private const val PREFS="rawalworld_final"
private const val ADMIN_TOKEN="admin_token"
private val Purple=Color(0xFF6C4DB4)
private val Bg=Color(0xFFFFF8FF)
private fun rwText(lang:String,en:String,gu:String,hi:String,fr:String)=when(lang){"Gujarati"->gu;"Hindi"->hi;"French"->fr;else->en}

data class ProductRow(val id:String,val name:String,val category:String,val description:String,val price:Double,val active:Boolean,val imageUrl:String="")
data class MasterRow(val id:String,val type:String,val name:String,val active:Boolean)
data class ClientRow(val name:String,val mobile:String,val email:String,val city:String,val address:String,val pincode:String,val source:String)
data class GalleryRow(val title:String,val type:String,val image:String)
data class ServiceItem(val icon:String,val title:String,val items:List<Pair<String,String?>>)

class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{MaterialTheme(colorScheme=lightColorScheme(primary=Purple,background=Bg,surface=Color.White)){RawalworldApp()}}}}

private fun ui(block:()->Unit)=Handler(Looper.getMainLooper()).post(block)
private fun prefs(c:Context)=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
private fun token(c:Context)=prefs(c).getString(ADMIN_TOKEN,"")?:""
private fun saveToken(c:Context,v:String)=prefs(c).edit().putString(ADMIN_TOKEN,v).apply()
private fun openUrl(c:Context,url:String){try{c.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))}catch(_:Exception){}}
private fun conn(url:String,auth:String?=null):HttpURLConnection{val c=URL(url).openConnection() as HttpURLConnection;c.setRequestProperty("apikey",SB_KEY);if(!auth.isNullOrBlank())c.setRequestProperty("Authorization","Bearer $auth");return c}
private fun getArray(path:String,auth:String?=null):JSONArray{val c=conn("$SB_URL/rest/v1/$path",auth);val code=c.responseCode;val body=if(code in 200..299)c.inputStream.bufferedReader().use{it.readText()} else c.errorStream?.bufferedReader()?.use{it.readText()}.orEmpty();c.disconnect();if(code !in 200..299)throw IllegalStateException(body);return JSONArray(body)}
private fun write(path:String,method:String,payload:JSONObject?=null,auth:String?=null):Boolean{val c=conn("$SB_URL/rest/v1/$path",auth);c.requestMethod=method;c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Prefer","return=minimal");if(payload!=null){c.doOutput=true;c.outputStream.use{it.write(payload.toString().toByteArray())}};val ok=c.responseCode in 200..299;c.disconnect();return ok}
private fun login(email:String,password:String,onDone:(String?,String)->Unit){Thread{try{val c=conn("$SB_URL/auth/v1/token?grant_type=password");c.requestMethod="POST";c.setRequestProperty("Content-Type","application/json");c.doOutput=true;c.outputStream.use{it.write(JSONObject().put("email",email).put("password",password).toString().toByteArray())};val code=c.responseCode;val body=if(code in 200..299)c.inputStream.bufferedReader().use{it.readText()} else c.errorStream?.bufferedReader()?.use{it.readText()}.orEmpty();c.disconnect();if(code in 200..299){val j=JSONObject(body);ui{onDone(j.optString("access_token"),"Login successful.")}}else ui{onDone(null,"Check email/password or admin access.")}}catch(_:Exception){ui{onDone(null,"Unable to connect.")}}}.start()}
private fun forgot(email:String,onDone:(Boolean)->Unit){Thread{val ok=try{val c=conn("$SB_URL/auth/v1/recover");c.requestMethod="POST";c.setRequestProperty("Content-Type","application/json");c.doOutput=true;c.outputStream.use{it.write(JSONObject().put("email",email).toString().toByteArray())};val r=c.responseCode in 200..299;c.disconnect();r}catch(_:Exception){false};ui{onDone(ok)}}.start()}
private fun loadPayment(onDone:(String,String)->Unit){Thread{var payee="Haresh Rawal";var upi="harshrawal1929-1@okicici";try{val a=getArray("app_settings?select=setting_key,setting_value&setting_key=in.(payment_payee_name,payment_upi_id)");for(i in 0 until a.length()){val x=a.getJSONObject(i);when(x.optString("setting_key")){"payment_payee_name"->payee=x.optString("setting_value",payee);"payment_upi_id"->upi=x.optString("setting_value",upi)}}}catch(_:Exception){};ui{onDone(payee,upi)}}.start()}
private fun saveSetting(auth:String,key:String,value:String,onDone:(Boolean)->Unit){Thread{val ok=try{write("app_settings?setting_key=eq.${URLEncoder.encode(key,"UTF-8")}","PATCH",JSONObject().put("setting_value",value),auth)}catch(_:Exception){false};ui{onDone(ok)}}.start()}

private val publicServices=listOf(
 ServiceItem("🔮","Astrology",listOf("Daily Rashi" to "https://www.google.com/search?q=today+daily+rashi+rashifal","Hindu Calendar" to "https://www.drikpanchang.com/","Gujarati Calendar" to "https://www.google.com/search?q=today+Gujarati+calendar+tithi+festival","Daily Horoscope" to "https://www.astrosage.com/horoscope/daily-horoscope.asp","Kundli / Birth Chart" to null,"Marriage Matching" to null,"Ask an Astrologer" to null,"Muhurat & Puja" to null)),
 ServiceItem("🎉","Events",listOf("Wedding" to null,"Birthday" to null,"Engagement" to null,"Anniversary" to null,"Corporate Event" to null,"Religious Event" to null)),
 ServiceItem("🌸","Decoration",listOf("Wedding Decoration" to null,"Stage Decoration" to null,"Birthday Theme" to null,"Flower Decoration" to null,"Mandap" to null,"Lighting" to null)),
 ServiceItem("🍽️","Catering",listOf("Gujarati" to null,"Punjabi" to null,"South Indian" to null,"Jain" to null,"Continental" to null,"Custom Package" to null)),
 ServiceItem("💼","Consultancy",listOf("Accounts & Finance" to null,"HR" to null,"Business Setup" to null,"French Support" to null,"Real Estate" to null,"Documentation" to null)),
 ServiceItem("✈️","Tours & Travel",listOf("Flight Search" to "https://www.google.com/travel/flights","Flight Schedule" to "https://www.flightstats.com/v2/flight-tracker/search","Train Search" to "https://www.irctc.co.in/nget/train-search","Train Schedule" to "https://enquiry.indianrail.gov.in/mntes/","Hotel Booking" to "https://www.google.com/travel/hotels","Holiday Packages" to null,"Visa Assistance" to null,"Cab / Vehicle Rental" to null,"Group Tours" to null)),
 ServiceItem("🗺️","Map",listOf("Open Map" to "https://maps.google.com/","Nearby Places" to "https://www.google.com/maps/search/nearby+places/","Directions" to "https://www.google.com/maps/dir/")),
 ServiceItem("📰","Gujarat News",listOf("Latest Gujarat News" to "https://news.google.com/search?q=Gujarat&hl=en-IN&gl=IN&ceid=IN:en","Business News" to "https://news.google.com/search?q=Gujarat%20business&hl=en-IN&gl=IN&ceid=IN:en","Local Updates" to "https://news.google.com/search?q=Gujarat%20local&hl=en-IN&gl=IN&ceid=IN:en")),
 ServiceItem("🌦️","Weather Update",listOf("Gujarat Weather" to "https://www.google.com/search?q=Gujarat+weather","Ahmedabad Weather" to "https://www.google.com/search?q=Ahmedabad+weather","Gandhinagar Weather" to "https://www.google.com/search?q=Gandhinagar+weather")),
 ServiceItem("🎮","Entertainment",listOf("Live Cricket India" to "https://www.google.com/search?q=India+live+cricket+match","Ludo" to "https://www.crazygames.com/game/ludo-king","Solitaire" to "https://www.solitr.com/","Chess" to "https://www.chess.com/play/online","English Songs" to "https://www.youtube.com/results?search_query=english+songs","Hindi Songs" to "https://www.youtube.com/results?search_query=hindi+songs","Gujarati Songs" to "https://www.youtube.com/results?search_query=gujarati+songs","French Songs" to "https://www.youtube.com/results?search_query=french+songs"))
)

@Composable fun RawalworldApp(){
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
}

@Composable fun HomeScreen(lang:String,onLanguage:(String)->Unit,open:(ServiceItem)->Unit,openGallery:()->Unit,openDonation:()->Unit){
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
}

@Composable fun ServiceScreen(s:ServiceItem,lang:String,back:()->Unit,book:()->Unit){
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
}

@Composable fun BookingScreen(service:String,lang:String,back:()->Unit){
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
}

@Composable fun BookingsScreen(lang:String){var rows by remember{mutableStateOf<List<String>>(emptyList())};LaunchedEffect(Unit){Thread{val out=mutableListOf<String>();try{val a=getArray("bookings?select=service,customer_name,mobile,city,status&order=created_at.desc&limit=30");for(i in 0 until a.length()){val x=a.getJSONObject(i);out+="${x.optString("service")} • ${x.optString("customer_name")} • ${x.optString("mobile")} • ${x.optString("status")}"}}catch(_:Exception){};ui{rows=out}}.start()};Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){Text(rwText(lang,"📅 Bookings","📅 બુકિંગ","📅 बुकिंग","📅 Réservations"),style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);if(rows.isEmpty())Text(rwText(lang,"No bookings found.","કોઈ બુકિંગ મળ્યું નથી.","कोई बुकिंग नहीं मिली।","Aucune réservation."))else rows.forEach{Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Text(it,Modifier.padding(12.dp))}}}}

@Composable fun RemoteImage(url:String){var bmp by remember(url){mutableStateOf<android.graphics.Bitmap?>(null)};LaunchedEffect(url){Thread{try{val b=URL(url).openStream().use{BitmapFactory.decodeStream(it)};ui{bmp=b}}catch(_:Exception){}}.start()};bmp?.let{Image(it.asImageBitmap(),"Product image",Modifier.fillMaxWidth().height(180.dp),contentScale=ContentScale.Crop)}}

@Composable fun ShopScreen(lang:String){
    val context=LocalContext.current
    var products by remember{mutableStateOf<List<ProductRow>>(emptyList())}
    var masterCategories by remember{mutableStateOf(listOf("Puja Products","Astrology Products","Gifts","Decoration Items","Travel Accessories","Local Products"))}
    var loading by remember{mutableStateOf(true)}
    var selected by remember{mutableStateOf<ProductRow?>(null)}
    var category by remember{mutableStateOf("All")}

    LaunchedEffect(Unit){
        Thread{
            val out=mutableListOf<ProductRow>()
            val cats=mutableListOf<String>()
            try{
                val a=getArray("products?select=id,name,category,description,price,is_active,image_url&is_active=eq.true&order=created_at.desc")
                for(i in 0 until a.length()){
                    val x=a.getJSONObject(i)
                    out+=ProductRow(x.optString("id"),x.optString("name"),x.optString("category"),x.optString("description"),x.optDouble("price"),true,x.optString("image_url"))
                }
            }catch(_:Exception){}
            try{
                val a=getArray("masters?select=name&master_type=eq.shop&is_active=eq.true&order=name.asc")
                for(i in 0 until a.length()){
                    val n=a.getJSONObject(i).optString("name")
                    if(n.isNotBlank())cats+=n
                }
            }catch(_:Exception){}
            ui{
                products=out
                masterCategories=(masterCategories+cats+out.map{it.category}).filter{it.isNotBlank()}.distinct()
                loading=false
            }
        }.start()
    }

    val categories=listOf("All")+masterCategories.distinct()
    val visible=if(category=="All")products else products.filter{it.category==category}
    val shareLabel=rwText(lang,"Share Product","પ્રોડક્ટ શેર કરો","प्रोडक्ट शेयर करें","Partager le produit")

    Column(Modifier.fillMaxSize().padding(14.dp).verticalScroll(rememberScrollState())){
        Text(rwText(lang,"🛍️ Online Shopping","🛍️ ઑનલાઇન શોપિંગ","🛍️ ऑनलाइन शॉपिंग","🛍️ Shopping en ligne"),style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
        Text(rwText(lang,"Shop category wise","કેટેગરી પ્રમાણે ખરીદી","कैटेगरी अनुसार शॉपिंग","Shopping par catégorie"),color=Purple,fontWeight=FontWeight.Bold)

        categories.chunked(2).forEach{cats->
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                cats.forEach{cat->
                    OutlinedButton(onClick={category=cat},modifier=Modifier.weight(1f)){
                        Text((if(category==cat)"✓ " else "")+cat)
                    }
                }
                if(cats.size==1)Spacer(Modifier.weight(1f))
            }
        }

        if(loading)CircularProgressIndicator(Modifier.padding(16.dp))
        if(!loading&&visible.isEmpty())Text(rwText(lang,"No active products found in this category.","આ કેટેગરીમાં કોઈ સક્રિય પ્રોડક્ટ નથી.","इस कैटेगरी में कोई सक्रिय प्रोडक्ट नहीं है।","Aucun produit actif dans cette catégorie."))

        visible.forEach{r->
            Card(Modifier.fillMaxWidth().padding(vertical=6.dp)){
                Column(Modifier.padding(14.dp)){
                    if(r.imageUrl.isNotBlank())RemoteImage(r.imageUrl)
                    Text(r.name,fontWeight=FontWeight.Bold)
                    Text(r.category,style=MaterialTheme.typography.bodySmall)
                    if(r.description.isNotBlank())Text(r.description)
                    Text("₹ ${String.format("%.2f",r.price)}",fontWeight=FontWeight.Bold)
                    Button(onClick={selected=r},modifier=Modifier.fillMaxWidth().padding(top=8.dp)){
                        Text(rwText(lang,"Buy Now","હમણાં ખરીદો","अभी खरीदें","Acheter"))
                    }
                    OutlinedButton(onClick={
                        val text="Rawalworld: ${r.name} - ₹ ${String.format("%.2f",r.price)}"
                        val shareIntent=Intent(Intent.ACTION_SEND)
                        shareIntent.type="text/plain"
                        shareIntent.putExtra(Intent.EXTRA_TEXT,text)
                        try{context.startActivity(Intent.createChooser(shareIntent,shareLabel))}catch(_:Exception){}
                    },modifier=Modifier.fillMaxWidth().padding(top=6.dp)){
                        Text(shareLabel)
                    }
                }
            }
        }
    }

    if(selected!=null){
        CheckoutDialog(context,selected!!){selected=null}
    }
}

@Composable fun CheckoutDialog(context:Context,p:ProductRow,onClose:()->Unit){var qty by remember{mutableStateOf("1")};var name by remember{mutableStateOf("")};var mobile by remember{mutableStateOf("")};var address by remember{mutableStateOf("")};var pincode by remember{mutableStateOf("")};var msg by remember{mutableStateOf("")};var busy by remember{mutableStateOf(false)};AlertDialog(onDismissRequest=onClose,title={Text("Checkout — ${p.name}")},text={Column(Modifier.verticalScroll(rememberScrollState())){OutlinedTextField(qty,{qty=it.filter(Char::isDigit)},label={Text("Quantity")},modifier=Modifier.fillMaxWidth());OutlinedTextField(name,{name=it},label={Text("Purchaser name")},modifier=Modifier.fillMaxWidth());OutlinedTextField(mobile,{mobile=it},label={Text("Mobile")},modifier=Modifier.fillMaxWidth());OutlinedTextField(address,{address=it},label={Text("Delivery address")},modifier=Modifier.fillMaxWidth());OutlinedTextField(pincode,{pincode=it},label={Text("Pincode")},modifier=Modifier.fillMaxWidth());val q=qty.toIntOrNull()?:1;Text("Total: ₹ ${String.format("%.2f",p.price*q)}",fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=8.dp));if(msg.isNotBlank())Text(msg)}},confirmButton={Button(enabled=!busy,onClick={val q=(qty.toIntOrNull()?:0).coerceAtLeast(1);if(name.isBlank()||mobile.isBlank()||address.isBlank()||pincode.isBlank()){msg="Please complete all delivery details.";return@Button};busy=true;val total=p.price*q;Thread{val orderOk=try{write("orders","POST",JSONObject().put("product_id",p.id).put("product_name",p.name).put("quantity",q).put("unit_price",p.price).put("total_amount",total).put("currency","INR").put("customer_name",name).put("mobile",mobile).put("delivery_address",address).put("pincode",pincode).put("payment_method","UPI / GPay").put("payment_status","pending").put("order_status","submitted"))}catch(_:Exception){false};ui{if(!orderOk){busy=false;msg="Could not submit order."}else loadPayment{payee,upi->busy=false;try{val uri=Uri.parse("upi://pay?pa=${Uri.encode(upi)}&pn=${Uri.encode(payee)}&am=${String.format("%.2f",total)}&cu=INR&tn=${Uri.encode(p.name)}");context.startActivity(Intent(Intent.ACTION_VIEW,uri));msg="Order submitted. Opening UPI / GPay…"}catch(_:Exception){msg="Order submitted. UPI app could not open."}}}}.start()}){Text("Continue to Payment")}},dismissButton={TextButton(onClick=onClose){Text("Close")}})}

@Composable fun GalleryScreen(){var rows by remember{mutableStateOf<List<GalleryRow>>(emptyList())};LaunchedEffect(Unit){Thread{val out=mutableListOf<GalleryRow>();try{val a=getArray("gallery?select=title,gallery_type,image_url&is_active=eq.true&order=created_at.desc");for(i in 0 until a.length()){val x=a.getJSONObject(i);out+=GalleryRow(x.optString("title"),x.optString("gallery_type"),x.optString("image_url"))}}catch(_:Exception){};ui{rows=out}}.start()};Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){Text("📷 Gallery",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);if(rows.isEmpty())Text("No gallery photos found.")else rows.forEach{r->Card(Modifier.fillMaxWidth().padding(vertical=5.dp)){Column(Modifier.padding(14.dp)){if(r.image.isNotBlank())RemoteImage(r.image);Text(r.title,fontWeight=FontWeight.Bold);Text(r.type,style=MaterialTheme.typography.bodySmall)}}}}}

@Composable fun ProfileScreen(lang:String){
    val c=LocalContext.current
    var n by remember{mutableStateOf(prefs(c).getString("name","")?:"")};var m by remember{mutableStateOf(prefs(c).getString("mobile","")?:"")};var email by remember{mutableStateOf(prefs(c).getString("email","")?:"")};var city by remember{mutableStateOf(prefs(c).getString("city","")?:"")};var msg by remember{mutableStateOf("")}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
        Text(rwText(lang,"👤 Profile / Customer Account","👤 પ્રોફાઇલ / ગ્રાહક એકાઉન્ટ","👤 प्रोफ़ाइल / ग्राहक अकाउंट","👤 Profil / Compte client"),style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
        OutlinedTextField(n,{n=it},label={Text(rwText(lang,"Name","નામ","नाम","Nom"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(m,{m=it},label={Text(rwText(lang,"Mobile","મોબાઇલ","मोबाइल","Téléphone"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(email,{email=it},label={Text(rwText(lang,"Email","ઇમેઇલ","ईमेल","E-mail"))},modifier=Modifier.fillMaxWidth());OutlinedTextField(city,{city=it},label={Text(rwText(lang,"City","શહેર","शहर","Ville"))},modifier=Modifier.fillMaxWidth())
        Button(onClick={prefs(c).edit().putString("name",n.trim()).putString("mobile",m.trim()).putString("email",email.trim()).putString("city",city.trim()).apply();msg=rwText(lang,"Profile saved on this device.","પ્રોફાઇલ આ ડિવાઇસ પર સાચવાઈ.","प्रोफ़ाइल इस डिवाइस पर सेव हुई।","Profil enregistré sur cet appareil.")},Modifier.fillMaxWidth().padding(top=10.dp)){Text(rwText(lang,"Save Profile","પ્રોફાઇલ સાચવો","प्रोफ़ाइल सेव करें","Enregistrer le profil"))}
        Button(onClick={if(n.isBlank()||m.isBlank()){msg=rwText(lang,"Enter name and mobile first.","પહેલા નામ અને મોબાઇલ દાખલ કરો.","पहले नाम और मोबाइल दर्ज करें।","Saisissez d'abord nom et téléphone.")}else Thread{val ok=try{write("customer_accounts","POST",JSONObject().put("customer_name",n.trim()).put("mobile",m.trim()).put("email",email.trim().ifBlank{JSONObject.NULL}).put("city",city.trim().ifBlank{JSONObject.NULL}))}catch(_:Exception){false};ui{msg=if(ok)rwText(lang,"✅ Customer account created online. OTP login will be added after SMS authentication is enabled.","✅ ગ્રાહક એકાઉન્ટ ઑનલાઇન બનાવાયું. SMS ઓથેન્ટિકેશન પછી OTP લૉગિન ઉમેરાશે.","✅ ग्राहक अकाउंट ऑनलाइन बन गया। SMS ऑथेंटिकेशन के बाद OTP लॉगिन जोड़ा जाएगा।","✅ Compte client créé en ligne. La connexion OTP sera ajoutée après activation SMS.") else rwText(lang,"Account is saved locally or this mobile may already be registered.","એકાઉન્ટ સ્થાનિક રીતે સાચવાયું છે અથવા આ મોબાઇલ પહેલેથી નોંધાયેલ હોઈ શકે છે.","अकाउंट स्थानीय रूप से सेव है या यह मोबाइल पहले से पंजीकृत हो सकता है।","Le compte est enregistré localement ou ce mobile est peut-être déjà inscrit.")}}.start()},Modifier.fillMaxWidth().padding(top=8.dp)){Text(rwText(lang,"Create / Update Customer Account","ગ્રાહક એકાઉન્ટ બનાવો / અપડેટ કરો","ग्राहक अकाउंट बनाएँ / अपडेट करें","Créer / mettre à jour le compte"))}
        if(msg.isNotBlank())Text(msg,Modifier.padding(top=8.dp))
    }
}

@Composable fun DonationScreen(lang:String,back:()->Unit){
    val context=LocalContext.current
    var name by remember{mutableStateOf(prefs(context).getString("name","")?:"")}
    var mobile by remember{mutableStateOf(prefs(context).getString("mobile","")?:"")}
    var amount by remember{mutableStateOf("")}
    var note by remember{mutableStateOf("")}
    var msg by remember{mutableStateOf("")}

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
        TextButton(onClick=back){Text(rwText(lang,"← Back to home","← હોમ પર પાછા","← होम पर वापस","← Retour à l'accueil"))}
        Text(rwText(lang,"❤️ Donation for Needy People","❤️ જરૂરિયાતમંદ લોકો માટે દાન","❤️ ज़रूरतमंद लोगों के लिए दान","❤️ Don pour les personnes dans le besoin"),style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
        Text(rwText(lang,"Please verify the beneficiary or cause before donating.","દાન કરતા પહેલાં લાભાર્થી અથવા હેતુ ચકાસો.","दान करने से पहले लाभार्थी या उद्देश्य की पुष्टि करें।","Vérifiez le bénéficiaire ou la cause avant de donner."),style=MaterialTheme.typography.bodySmall)

        OutlinedTextField(name,{name=it},label={Text(rwText(lang,"Donor name","દાતાનું નામ","दाता का नाम","Nom du donateur"))},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(mobile,{mobile=it},label={Text(rwText(lang,"Mobile","મોબાઇલ","मोबाइल","Téléphone"))},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(amount,{amount=it},label={Text(rwText(lang,"Amount INR","રકમ INR","राशि INR","Montant INR"))},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(note,{note=it},label={Text(rwText(lang,"Purpose / note","હેતુ / નોંધ","उद्देश्य / नोट","Objet / note"))},modifier=Modifier.fillMaxWidth())

        Button(
            onClick = {
                val donationAmount = amount.toDoubleOrNull()
                if(name.isBlank() || mobile.isBlank() || donationAmount == null || donationAmount <= 0.0){
                    msg = "Enter donor name, mobile and valid amount."
                }else{
                    val donorName = name.trim()
                    val donorMobile = mobile.trim()
                    val purpose = note.trim()
                    Thread(Runnable {
                        try{
                            write("donations","POST",JSONObject()
                                .put("donor_name",donorName)
                                .put("mobile",donorMobile)
                                .put("amount",donationAmount)
                                .put("currency","INR")
                                .put("purpose",if(purpose.isBlank())JSONObject.NULL else purpose)
                                .put("payment_method","UPI / GPay")
                                .put("payment_status","initiated"))
                        }catch(_:Exception){}
                    }).start()
                    loadPayment { payee, upi ->
                        try{
                            val paymentNote = if(purpose.isBlank()) "Donation for needy people" else "Donation for needy people - $purpose"
                            val uriText = "upi://pay?pa=${Uri.encode(upi)}&pn=${Uri.encode(payee)}&am=${String.format("%.2f",donationAmount)}&cu=INR&tn=${Uri.encode(paymentNote)}"
                            context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(uriText)))
                            msg = "Opening UPI / GPay..."
                        }catch(_:Exception){
                            msg = "UPI app could not open."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top=10.dp)
        ){
            Text("Donate with UPI / GPay")
        }
        if(msg.isNotBlank())Text(msg,Modifier.padding(top=8.dp))
    }
}

@Composable fun AdminScreen(){val context=LocalContext.current;var auth by remember{mutableStateOf("")};if(auth.isBlank())AdminLogin(context){auth=it}else AdminDashboard(context,auth){saveToken(context,"");auth=""}}
@Composable fun AdminLogin(context:Context,onLogin:(String)->Unit){var email by remember{mutableStateOf("")};var pass by remember{mutableStateOf("")};var msg by remember{mutableStateOf("")};Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){Text("🔐 Rawalworld Admin",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);OutlinedTextField(email,{email=it},label={Text("Email")},modifier=Modifier.fillMaxWidth());OutlinedTextField(pass,{pass=it},label={Text("Password")},modifier=Modifier.fillMaxWidth());Button(onClick={login(email.trim(),pass){t,m->msg=m;if(!t.isNullOrBlank()){saveToken(context,t);onLogin(t)}}},Modifier.fillMaxWidth().padding(top=10.dp)){Text("Login")};TextButton(onClick={if(email.isBlank())msg="Enter email first." else forgot(email.trim()){msg=if(it)"Password reset email sent." else "Could not send reset email."}},Modifier.fillMaxWidth()){Text("Forgot Password")};if(msg.isNotBlank())Text(msg)}}

@Composable fun AdminDashboard(context:Context,auth:String,logout:()->Unit){var products by remember{mutableStateOf<List<ProductRow>>(emptyList())};var masters by remember{mutableStateOf<List<MasterRow>>(emptyList())};var clients by remember{mutableStateOf<List<ClientRow>>(emptyList())};var payee by remember{mutableStateOf("Haresh Rawal")};var upi by remember{mutableStateOf("harshrawal1929-1@okicici")};var msg by remember{mutableStateOf("")};var refresh by remember{mutableStateOf(0)};var editProduct by remember{mutableStateOf<ProductRow?>(null)};var editMaster by remember{mutableStateOf<MasterRow?>(null)};var pn by remember{mutableStateOf("")};var pc by remember{mutableStateOf("Puja Products")};var pp by remember{mutableStateOf("")};var pd by remember{mutableStateOf("")};var masterType by remember{mutableStateOf("shop")};var masterName by remember{mutableStateOf("")};var photoUri by remember{mutableStateOf<Uri?>(null)};val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){photoUri=it};fun reload(){refresh++};LaunchedEffect(refresh){loadPayment{a,b->payee=a;upi=b};Thread{val ps=mutableListOf<ProductRow>();val ms=mutableListOf<MasterRow>();val cs=mutableListOf<ClientRow>();try{val a=getArray("products?select=id,name,category,description,price,is_active,image_url&order=created_at.desc",auth);for(i in 0 until a.length()){val x=a.getJSONObject(i);ps+=ProductRow(x.optString("id"),x.optString("name"),x.optString("category"),x.optString("description"),x.optDouble("price"),x.optBoolean("is_active",true),x.optString("image_url"))}}catch(_:Exception){};try{val a=getArray("masters?select=id,master_type,name,is_active&order=master_type.asc,name.asc",auth);for(i in 0 until a.length()){val x=a.getJSONObject(i);ms+=MasterRow(x.optString("id"),x.optString("master_type"),x.optString("name"),x.optBoolean("is_active",true))}}catch(_:Exception){};try{val a=getArray("clients?select=customer_name,mobile,email,city,delivery_address,pincode,source&order=updated_at.desc&limit=200",auth);for(i in 0 until a.length()){val x=a.getJSONObject(i);cs+=ClientRow(x.optString("customer_name"),x.optString("mobile"),x.optString("email"),x.optString("city"),x.optString("delivery_address"),x.optString("pincode"),x.optString("source"))}}catch(_:Exception){};ui{products=ps;masters=ms;clients=cs}}.start()};Column(Modifier.fillMaxSize().padding(14.dp).verticalScroll(rememberScrollState())){Text("🔐 ADMIN MANAGEMENT",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold);Section("PAYMENT MASTER"){OutlinedTextField(payee,{payee=it},label={Text("Payee name")},modifier=Modifier.fillMaxWidth());OutlinedTextField(upi,{upi=it},label={Text("UPI ID / GPay UPI")},modifier=Modifier.fillMaxWidth());Button(onClick={if(payee.isBlank()||upi.isBlank()||!upi.contains("@"))msg="Enter valid payment details." else saveSetting(auth,"payment_payee_name",payee.trim()){a->if(!a)msg="Could not save payee name." else saveSetting(auth,"payment_upi_id",upi.trim()){b->msg=if(b)"✅ Payment Master updated." else "Could not save UPI ID."}}},Modifier.fillMaxWidth()){Text("Save Payment Master")}};Section("ADD PRODUCT WITH PHOTO"){OutlinedTextField(pn,{pn=it},label={Text("Product name")},modifier=Modifier.fillMaxWidth());OutlinedTextField(pc,{pc=it},label={Text("Category")},modifier=Modifier.fillMaxWidth());OutlinedTextField(pp,{pp=it},label={Text("Price INR")},modifier=Modifier.fillMaxWidth());OutlinedTextField(pd,{pd=it},label={Text("Description")},modifier=Modifier.fillMaxWidth());OutlinedButton(onClick={picker.launch("image/*")},Modifier.fillMaxWidth()){Text(if(photoUri==null)"Choose Product Photo" else "Photo Selected")};Button(onClick={val amount=pp.toDoubleOrNull();val uri=photoUri;if(pn.isBlank()||amount==null||uri==null){msg="Enter product details and choose photo."}else Thread{var publicUrl="";try{val bytes=context.contentResolver.openInputStream(uri)?.use{it.readBytes()}?:byteArrayOf();val name="product_${System.currentTimeMillis()}.jpg";val c=conn("$SB_URL/storage/v1/object/product-gallery/$name",auth);c.requestMethod="POST";c.setRequestProperty("Content-Type","image/jpeg");c.setRequestProperty("x-upsert","true");c.doOutput=true;c.outputStream.use{it.write(bytes)};if(c.responseCode in 200..299)publicUrl="$SB_URL/storage/v1/object/public/product-gallery/$name";c.disconnect()}catch(_:Exception){};val ok=publicUrl.isNotBlank()&&try{write("products","POST",JSONObject().put("name",pn).put("category",pc).put("description",pd).put("price",amount).put("currency","INR").put("image_url",publicUrl).put("is_active",true),auth)}catch(_:Exception){false};ui{msg=if(ok)"✅ Product added with photo." else "Could not add product/photo.";if(ok){pn="";pp="";pd="";photoUri=null;reload()}}}.start()},Modifier.fillMaxWidth()){Text("Add Product")}};Section("PRODUCT EDIT / DELETE / ACTIVE-INACTIVE"){if(products.isEmpty())Text("No products found.");products.forEach{p->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(10.dp)){if(p.imageUrl.isNotBlank())RemoteImage(p.imageUrl);Text(p.name,fontWeight=FontWeight.Bold);Text("${p.category} • ₹ ${String.format("%.2f",p.price)} • ${if(p.active)"ACTIVE" else "INACTIVE"}");Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){OutlinedButton(onClick={editProduct=p},Modifier.weight(1f)){Text("Edit")};OutlinedButton(onClick={Thread{val ok=try{write("products?id=eq.${p.id}","PATCH",JSONObject().put("is_active",!p.active),auth)}catch(_:Exception){false};ui{msg=if(ok)"Product status updated." else "Status update failed.";if(ok)reload()}}.start()},Modifier.weight(1f)){Text(if(p.active)"Inactive" else "Activate")}};OutlinedButton(onClick={Thread{val ok=try{write("products?id=eq.${p.id}","DELETE",null,auth)}catch(_:Exception){false};ui{msg=if(ok)"Product deleted." else "Delete failed.";if(ok)reload()}}.start()},Modifier.fillMaxWidth()){Text("Delete Product")}}}}};Section("CREATE MASTER"){
  var typeMenu by remember{mutableStateOf(false)}
  Box{OutlinedButton(onClick={typeMenu=true},Modifier.fillMaxWidth()){Text("Master type: $masterType")};DropdownMenu(typeMenu,{typeMenu=false}){listOf("shop","service","entertainment","travel","astrology").forEach{t->DropdownMenuItem(text={Text(t)},onClick={masterType=t;typeMenu=false})}}}
  OutlinedTextField(masterName,{masterName=it},label={Text("Master name")},modifier=Modifier.fillMaxWidth())
  Button(onClick={if(masterName.isBlank()){msg="Enter master name."}else Thread{val ok=try{write("masters","POST",JSONObject().put("master_type",masterType).put("name",masterName.trim()).put("is_active",true),auth)}catch(_:Exception){false};ui{msg=if(ok)"✅ Master created." else "Master creation failed — please log out and login again.";if(ok){masterName="";reload()}}}.start()},Modifier.fillMaxWidth()){Text("Add Master")}
};Section("MASTER EDIT / DELETE"){if(masters.isEmpty())Text("No masters found.");masters.forEach{m->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(10.dp)){Text(m.name,fontWeight=FontWeight.Bold);Text("${m.type} • ${if(m.active)"ACTIVE" else "INACTIVE"}");Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){OutlinedButton(onClick={editMaster=m},Modifier.weight(1f)){Text("Edit")};OutlinedButton(onClick={Thread{val ok=try{write("masters?id=eq.${m.id}","DELETE",null,auth)}catch(_:Exception){false};ui{msg=if(ok)"Master deleted." else "Master delete failed.";if(ok)reload()}}.start()},Modifier.weight(1f)){Text("Delete")}}}}}};Section("CLIENT RECORDS"){Text("${clients.size} customer record(s)",fontWeight=FontWeight.Bold);if(clients.isEmpty())Text("No client records yet.");clients.forEach{c->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(10.dp)){Text(if(c.name.isBlank())"Client" else c.name,fontWeight=FontWeight.Bold);if(c.mobile.isNotBlank())Text("📞 ${c.mobile}");if(c.email.isNotBlank())Text("✉ ${c.email}");if(c.city.isNotBlank())Text("📍 ${c.city}");if(c.address.isNotBlank())Text(c.address);if(c.pincode.isNotBlank())Text("Pincode: ${c.pincode}");if(c.source.isNotBlank())Text("Source: ${c.source}",style=MaterialTheme.typography.bodySmall)}}}};Button(onClick={reload()},Modifier.fillMaxWidth()){Text("Refresh Admin")};OutlinedButton(onClick=logout,Modifier.fillMaxWidth().padding(top=8.dp)){Text("Logout")};if(msg.isNotBlank())Text(msg,Modifier.padding(top=8.dp))};editProduct?.let{p->ProductEditDialog(p,auth,{editProduct=null;reload()},{editProduct=null})};editMaster?.let{m->MasterEditDialog(m,auth,{editMaster=null;reload()},{editMaster=null})}}

@Composable fun Section(title:String,content:@Composable ColumnScope.()->Unit){Text(title,color=Purple,fontWeight=FontWeight.ExtraBold,modifier=Modifier.padding(top=14.dp,bottom=6.dp));Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp),content=content)}}
@Composable fun ProductEditDialog(p:ProductRow,auth:String,onSaved:()->Unit,onClose:()->Unit){var name by remember{mutableStateOf(p.name)};var price by remember{mutableStateOf(p.price.toString())};var desc by remember{mutableStateOf(p.description)};var msg by remember{mutableStateOf("")};AlertDialog(onDismissRequest=onClose,title={Text("Edit Product")},text={Column{OutlinedTextField(name,{name=it},label={Text("Product name")},modifier=Modifier.fillMaxWidth());OutlinedTextField(price,{price=it},label={Text("Price INR")},modifier=Modifier.fillMaxWidth());OutlinedTextField(desc,{desc=it},label={Text("Description")},modifier=Modifier.fillMaxWidth());if(msg.isNotBlank())Text(msg)}},confirmButton={Button(onClick={val amt=price.toDoubleOrNull();if(name.isBlank()||amt==null)msg="Enter valid name and price." else Thread{val ok=try{write("products?id=eq.${p.id}","PATCH",JSONObject().put("name",name.trim()).put("price",amt).put("description",desc.trim()),auth)}catch(_:Exception){false};ui{if(ok)onSaved()else msg="Product update failed."}}.start()}){Text("Save")}},dismissButton={TextButton(onClick=onClose){Text("Cancel")}})}
@Composable fun MasterEditDialog(m:MasterRow,auth:String,onSaved:()->Unit,onClose:()->Unit){var name by remember{mutableStateOf(m.name)};var msg by remember{mutableStateOf("")};AlertDialog(onDismissRequest=onClose,title={Text("Edit Master")},text={Column{OutlinedTextField(name,{name=it},label={Text("Master name")},modifier=Modifier.fillMaxWidth());if(msg.isNotBlank())Text(msg)}},confirmButton={Button(onClick={if(name.isBlank())msg="Enter master name." else Thread{val ok=try{write("masters?id=eq.${m.id}","PATCH",JSONObject().put("name",name.trim()),auth)}catch(_:Exception){false};ui{if(ok)onSaved()else msg="Master update failed."}}.start()}){Text("Save")}},dismissButton={TextButton(onClick=onClose){Text("Cancel")}})}