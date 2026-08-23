package com.astrolife.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class Service(val icon:String,val title:String,val subtitle:String,val options:List<String>)
data class Booking(val service:String,val name:String,val mobile:String,val city:String,val date:String,val note:String)
data class Product(val name:String,val description:String,val price:String,val url:String?,val isFree:Boolean)

private const val SUPABASE_URL="https://hcpvuripnlhofxfczyyb.supabase.co"
private const val SUPABASE_KEY="sb_publishable_J8YoD4yenQO-nlEMoC1kvA_3_vJgGjg"
private const val PREFS="rawalworld_prefs"
private val Brand=Color(0xFF8F3D2B)
private val Hero=Color(0xFF7D2D1F)
private val WarmBg=Color(0xFFFFF9F5)

private val services=listOf(
    Service("🔮","Astrology","Horoscope, Kundli & consultation",listOf("Daily Horoscope","Kundli / Birth Chart","Marriage Matching","Ask an Astrologer","Muhurat & Puja")),
    Service("🎉","Events","Weddings, birthdays & corporate events",listOf("Wedding","Birthday","Engagement","Anniversary","Corporate Event","Religious Event")),
    Service("🌸","Decoration","Themes, flowers, stage & lighting",listOf("Wedding Decoration","Stage Decoration","Birthday Theme","Flower Decoration","Mandap","Lighting")),
    Service("🍽️","Catering","Menus and packages for every occasion",listOf("Gujarati","Punjabi","South Indian","Jain","Continental","Custom Package")),
    Service("💼","Consultancy","Business and professional services",listOf("Accounts & Finance","HR","Business Setup","French Support","Real Estate","Documentation")),
    Service("✈️","Tours & Travel","Trips, hotels, visa & transport",listOf("Holiday Packages","Hotels","Flight Enquiry","Visa Assistance","Cab / Vehicle Rental","Group Tours")),
    Service("🛍️","Online Shopping","Products, gifts and essentials",listOf("Puja Products","Astrology Products","Gifts","Decoration Items","Travel Accessories","Local Products"))
)

private val details=mapOf(
    "Daily Horoscope" to "Daily horoscope gives a simple overview for career, money, relationships, health and general outlook.",
    "Kundli / Birth Chart" to "A Kundli is a Vedic birth chart prepared from your birth date, exact time and place. It can show Lagna, Moon sign, planets and houses.",
    "Marriage Matching" to "Marriage matching compares two birth charts for traditional compatibility and relationship guidance.",
    "Ask an Astrologer" to "Send your birth details and question for a personal consultation on career, finance, marriage, business, property or travel.",
    "Muhurat & Puja" to "Muhurat helps identify traditionally favorable timing for important events.",
    "Wedding" to "Plan venue, decoration, catering, photography, transport and coordination.",
    "Birthday" to "Birthday themes, decoration, cake, catering and entertainment.",
    "Engagement" to "Stage, decoration, catering and guest arrangements.",
    "Anniversary" to "Decoration, dining, gifts and celebration packages.",
    "Corporate Event" to "Meetings, launches, conferences and staff events.",
    "Religious Event" to "Decoration, catering and support for puja and religious functions.",
    "Wedding Decoration" to "Mandap, stage, floral, lighting and entrance decoration packages.",
    "Stage Decoration" to "Customized stage decoration for all event types.",
    "Birthday Theme" to "Birthday themes with balloons, backdrops and customized decor.",
    "Flower Decoration" to "Fresh and artificial flower decoration.",
    "Mandap" to "Traditional and modern mandap decoration.",
    "Lighting" to "Decorative and ambient event lighting.",
    "Gujarati" to "Gujarati catering menus for functions and weddings.",
    "Punjabi" to "Punjabi menu packages with starters, mains and desserts.",
    "South Indian" to "South Indian meal and live-counter options.",
    "Jain" to "Jain-friendly menu options.",
    "Continental" to "Continental snacks and buffet options.",
    "Custom Package" to "Custom catering based on guest count and budget.",
    "Accounts & Finance" to "Bookkeeping, MIS, budgeting and finance support.",
    "HR" to "Recruitment support, documentation and HR processes.",
    "Business Setup" to "Business planning and setup support.",
    "French Support" to "French language communication and translation support.",
    "Real Estate" to "Property search and documentation coordination.",
    "Documentation" to "General business documentation support.",
    "Holiday Packages" to "Domestic and international holiday planning.",
    "Hotels" to "Hotel enquiry and accommodation planning.",
    "Flight Enquiry" to "Flight route and fare enquiry.",
    "Visa Assistance" to "Visa checklist and application-support guidance.",
    "Cab / Vehicle Rental" to "Cab, car, pickup and group transport rental.",
    "Group Tours" to "Customized group tour planning.",
    "Puja Products" to "Browse puja essentials.",
    "Astrology Products" to "Browse astrology products and digital services.",
    "Gifts" to "Browse gifting options.",
    "Decoration Items" to "Browse event and home decoration items.",
    "Travel Accessories" to "Browse useful travel accessories.",
    "Local Products" to "Discover selected local and regional products."
)

class MainActivity:ComponentActivity(){
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{MaterialTheme(colorScheme=lightColorScheme(primary=Brand,surface=Color.White,background=WarmBg)){RawalworldApp()}}}
}

private fun openWeb(context:Context,url:String){context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))}
private fun loadBookings(context:Context):List<Booking>{val raw=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getString("bookings","")?:"";if(raw.isBlank())return emptyList();return raw.split("\u001e").mapNotNull{r->val p=r.split("\u001f");if(p.size<6)null else Booking(p[0],p[1],p[2],p[3],p[4],p[5])}}
private fun saveBookings(context:Context,b:List<Booking>){val raw=b.joinToString("\u001e"){listOf(it.service,it.name,it.mobile,it.city,it.date,it.note).joinToString("\u001f")};context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putString("bookings",raw).apply()}

private fun postJson(table:String,payload:JSONObject,onDone:(Boolean)->Unit){Thread{var ok=false;try{val c=(URL("$SUPABASE_URL/rest/v1/$table").openConnection() as HttpURLConnection);c.requestMethod="POST";c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("apikey",SUPABASE_KEY);c.setRequestProperty("Prefer","return=minimal");c.doOutput=true;c.outputStream.use{it.write(payload.toString().toByteArray())};ok=c.responseCode in 200..299;c.disconnect()}catch(_:Exception){};Handler(Looper.getMainLooper()).post{onDone(ok)}}.start()}
private fun fetchProducts(onDone:(List<Product>)->Unit){Thread{val out=mutableListOf<Product>();try{val c=(URL("$SUPABASE_URL/rest/v1/products?select=name,description,price,currency,external_url,is_free&is_active=eq.true").openConnection() as HttpURLConnection);c.setRequestProperty("apikey",SUPABASE_KEY);val text=c.inputStream.bufferedReader().use{it.readText()};val arr=JSONArray(text);for(i in 0 until arr.length()){val o=arr.getJSONObject(i);out+=Product(o.optString("name"),o.optString("description"),if(o.optBoolean("is_free"))"FREE" else "${o.optString("currency")} ${o.optString("price")}",o.optString("external_url").takeIf{it.isNotBlank()},o.optBoolean("is_free"))};c.disconnect()}catch(_:Exception){};Handler(Looper.getMainLooper()).post{onDone(out)}}.start()}

@Composable fun RawalworldApp(){
    val context=LocalContext.current;var screen by remember{mutableStateOf("home")};var selected by remember{mutableStateOf<Service?>(null)};var bookings by remember{mutableStateOf(loadBookings(context))}
    Scaffold(bottomBar={NavigationBar{
        NavigationBarItem(screen=="home",{screen="home";selected=null},{Icon(Icons.Default.Home,null)},label={Text("Home")})
        NavigationBarItem(screen=="bookings",{screen="bookings";bookings=loadBookings(context)},{Icon(Icons.Default.DateRange,null)},label={Text("Bookings")})
        NavigationBarItem(screen=="service"&&selected?.title=="Online Shopping",{selected=services.last();screen="service"},{Icon(Icons.Default.ShoppingCart,null)},label={Text("Shop")})
        NavigationBarItem(screen=="profile",{screen="profile"},{Icon(Icons.Default.Person,null)},label={Text("Profile")})
    }}){pad->Box(Modifier.fillMaxSize().padding(pad)){when(screen){
        "service"->selected?.let{ServiceScreen(it,{screen="home"},{screen="booking"})}
        "booking"->selected?.let{s->BookingScreen(s,{screen="service"}){b->bookings=bookings+b;saveBookings(context,bookings)}}
        "bookings"->BookingsScreen(bookings)
        "profile"->ProfileScreen()
        else->HomeScreen{s->selected=s;screen="service"}
    }}}
}

@Composable fun HomeScreen(onOpen:(Service)->Unit){var q by remember{mutableStateOf("")};val filtered=services.filter{q.isBlank()||it.title.contains(q,true)||it.subtitle.contains(q,true)||it.options.any{o->o.contains(q,true)}};Column(Modifier.fillMaxSize().padding(horizontal=16.dp)){Spacer(Modifier.height(14.dp));Text("Rawalworld",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold);Text("Gujarat lifestyle & services super app",style=MaterialTheme.typography.bodySmall);Spacer(Modifier.height(14.dp));Card(colors=CardDefaults.cardColors(containerColor=Hero),shape=RoundedCornerShape(24.dp),modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(22.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Everything you need,\nin one app.",color=Color.White,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.ExtraBold);Text("Astrology, events, consultancy, travel and shopping.",color=Color.White.copy(alpha=.86f))};Text("RW",color=Color.White,style=MaterialTheme.typography.displaySmall,fontWeight=FontWeight.Black)}};Spacer(Modifier.height(12.dp));OutlinedTextField(q,{q=it},leadingIcon={Icon(Icons.Default.Search,null)},placeholder={Text("Search services...")},modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(8.dp));Text("📞 +91 77093 78969  •  ✉ rawalworld@gmail.com",style=MaterialTheme.typography.bodySmall);Text("📍 Gujarat, India  •  💳 Google Pay",style=MaterialTheme.typography.bodySmall);Spacer(Modifier.height(14.dp));LazyVerticalGrid(columns=GridCells.Fixed(2),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)){items(filtered){s->Card(onClick={onOpen(s)},shape=RoundedCornerShape(18.dp),modifier=Modifier.height(150.dp)){Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.SpaceBetween){Text(s.icon,style=MaterialTheme.typography.headlineMedium);Column{Text(s.title,fontWeight=FontWeight.Bold);Text(s.subtitle,style=MaterialTheme.typography.bodySmall)}}}}}}

@Composable fun ServiceScreen(service:Service,onBack:()->Unit,onBook:()->Unit){
    val context=LocalContext.current;var selectedInfo by remember{mutableStateOf<Pair<String,String>?>(null)};var dob by remember{mutableStateOf("")};var bt by remember{mutableStateOf("")};var place by remember{mutableStateOf("")};var astroMsg by remember{mutableStateOf("")};var products by remember{mutableStateOf<List<Product>>(emptyList())};var loading by remember{mutableStateOf(false)}
    LaunchedEffect(service.title){if(service.title=="Online Shopping"){loading=true;fetchProducts{products=it;loading=false}}}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){TextButton(onClick=onBack){Icon(Icons.Default.ArrowBack,null);Text(" Back to home")};Text("${service.icon} ${service.title}",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold);Text(service.subtitle);Spacer(Modifier.height(14.dp));service.options.forEach{o->Card(shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth().padding(bottom=8.dp)){Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){Text(o,Modifier.weight(1f));FilledTonalButton(onClick={selectedInfo=o to (details[o]?:"More information coming soon.")}){Text("Open")}}}};selectedInfo?.let{Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)){Column(Modifier.padding(14.dp)){Text(it.first,fontWeight=FontWeight.Bold);Text(it.second)}}};
        if(service.title=="Astrology"){Spacer(Modifier.height(12.dp));Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(14.dp)){Text("Personal Astrology Details",fontWeight=FontWeight.Bold);OutlinedTextField(dob,{dob=it},label={Text("Date of birth YYYY-MM-DD")},modifier=Modifier.fillMaxWidth());OutlinedTextField(bt,{bt=it},label={Text("Birth time HH:MM")},modifier=Modifier.fillMaxWidth());OutlinedTextField(place,{place=it},label={Text("Birth place")},modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(8.dp));Button(onClick={if(dob.isBlank()||bt.isBlank()||place.isBlank())astroMsg="Please fill all birth details." else {astroMsg="Submitting online...";val j=JSONObject().put("date_of_birth",dob).put("birth_time",bt).put("birth_place",place).put("request_type","kundli");postJson("astrology_requests",j){astroMsg=if(it)"Astrology request submitted online successfully." else "Online submission failed. Please check internet and try again."}}}){Text("Submit Astrology Request")};if(astroMsg.isNotBlank())Text(astroMsg,style=MaterialTheme.typography.bodySmall)}}}
        if(service.title=="Online Shopping"){Spacer(Modifier.height(12.dp));Text("Online Products",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);if(loading)CircularProgressIndicator();if(!loading&&products.isEmpty())Text("Could not load online products.");products.forEach{p->Card(Modifier.fillMaxWidth().padding(vertical=6.dp),shape=RoundedCornerShape(14.dp)){Column(Modifier.padding(14.dp)){Text(p.name,fontWeight=FontWeight.Bold);Text(p.description,style=MaterialTheme.typography.bodySmall);Text(p.price,fontWeight=FontWeight.Bold);p.url?.let{u->Button(onClick={openWeb(context,u)}){Text("Open")}}}}}
        Spacer(Modifier.height(14.dp));Button(onClick=onBook,modifier=Modifier.fillMaxWidth()){Text("Request Booking / Quotation")}
    }
}

@Composable fun BookingScreen(service:Service,onBack:()->Unit,onSaved:(Booking)->Unit){val prefs=LocalContext.current.getSharedPreferences(PREFS,Context.MODE_PRIVATE);var name by remember{mutableStateOf(prefs.getString("name","")?:"")};var mobile by remember{mutableStateOf(prefs.getString("mobile","")?:"")};var city by remember{mutableStateOf(prefs.getString("city","")?:"")};var date by remember{mutableStateOf("")};var note by remember{mutableStateOf("")};var msg by remember{mutableStateOf("")};Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){TextButton(onClick=onBack){Text("← Back")};Text("Booking / Quotation",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text(service.title);Spacer(Modifier.height(12.dp));OutlinedTextField(name,{name=it},label={Text("Name")},modifier=Modifier.fillMaxWidth());OutlinedTextField(mobile,{mobile=it},label={Text("Mobile")},modifier=Modifier.fillMaxWidth());OutlinedTextField(city,{city=it},label={Text("City")},modifier=Modifier.fillMaxWidth());OutlinedTextField(date,{date=it},label={Text("Preferred date YYYY-MM-DD")},modifier=Modifier.fillMaxWidth());OutlinedTextField(note,{note=it},label={Text("Requirement")},minLines=3,modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(10.dp));Button(onClick={if(name.isBlank()||mobile.isBlank()||city.isBlank())msg="Please enter name, mobile and city." else {val b=Booking(service.title,name.trim(),mobile.trim(),city.trim(),date.trim(),note.trim());onSaved(b);msg="Submitting online...";val j=JSONObject().put("service",b.service).put("customer_name",b.name).put("mobile",b.mobile).put("city",b.city).put("source","android");if(b.date.isNotBlank())j.put("preferred_date",b.date);if(b.note.isNotBlank())j.put("requirement",b.note);postJson("bookings",j){msg=if(it)"Request submitted online successfully." else "Saved on phone, but online submission failed."}}},modifier=Modifier.fillMaxWidth()){Text("Submit Request")};if(msg.isNotBlank())Text(msg)}}

@Composable fun BookingsScreen(items:List<Booking>){Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){Text("My Bookings",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Spacer(Modifier.height(12.dp));if(items.isEmpty())Text("No booking requests yet.");items.asReversed().forEach{b->Card(Modifier.fillMaxWidth().padding(bottom=8.dp)){Column(Modifier.padding(12.dp)){Text(b.service,fontWeight=FontWeight.Bold);Text("${b.name} • ${b.mobile} • ${b.city}",style=MaterialTheme.typography.bodySmall);if(b.date.isNotBlank())Text("Date: ${b.date}",style=MaterialTheme.typography.bodySmall);if(b.note.isNotBlank())Text(b.note,style=MaterialTheme.typography.bodySmall)}}}}}

@Composable fun ProfileScreen(){val context=LocalContext.current;val p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);var name by remember{mutableStateOf(p.getString("name","")?:"")};var mobile by remember{mutableStateOf(p.getString("mobile","")?:"")};var email by remember{mutableStateOf(p.getString("email","")?:"")};var city by remember{mutableStateOf(p.getString("city","")?:"")};var msg by remember{mutableStateOf("")};Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){Text("Profile",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);OutlinedTextField(name,{name=it},label={Text("Name")},modifier=Modifier.fillMaxWidth());OutlinedTextField(mobile,{mobile=it},label={Text("Mobile")},modifier=Modifier.fillMaxWidth());OutlinedTextField(email,{email=it},label={Text("Email")},modifier=Modifier.fillMaxWidth());OutlinedTextField(city,{city=it},label={Text("City")},modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(10.dp));Button(onClick={p.edit().putString("name",name).putString("mobile",mobile).putString("email",email).putString("city",city).apply();msg="Profile saved."},modifier=Modifier.fillMaxWidth()){Text("Save Profile")};if(msg.isNotBlank())Text(msg)}}
