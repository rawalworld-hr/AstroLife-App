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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
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

data class Service(val icon: String, val title: String, val subtitle: String, val options: List<String>)
data class Booking(val service: String, val name: String, val mobile: String, val city: String, val date: String, val note: String)
data class Product(val name: String, val description: String, val price: String, val url: String?)
data class AdminBooking(val id: String, val service: String, val name: String, val mobile: String, val city: String, val status: String)
data class AdminAstrology(val id: String, val type: String, val name: String, val mobile: String, val place: String, val status: String)
data class AdminOrder(val id: String, val name: String, val mobile: String, val amount: String, val status: String)
data class AdminData(val bookings: List<AdminBooking>, val astrology: List<AdminAstrology>, val productCount: Int, val orders: List<AdminOrder>)

private const val SUPABASE_URL = "https://hcpvuripnlhofxfczyyb.supabase.co"
private const val SUPABASE_KEY = "sb_publishable_J8YoD4yenQO-nlEMoC1kvA_3_vJgGjg"
private const val PREFS = "rawalworld_prefs"
private val Brand = Color(0xFF8F3D2B)
private val Hero = Color(0xFF7D2D1F)
private val WarmBg = Color(0xFFFFF9F5)

private val services = listOf(
    Service("🔮", "Astrology", "Horoscope, Kundli & consultation", listOf("Daily Horoscope", "Kundli / Birth Chart", "Marriage Matching", "Ask an Astrologer", "Muhurat & Puja")),
    Service("🎉", "Events", "Weddings, birthdays & corporate events", listOf("Wedding", "Birthday", "Engagement", "Anniversary", "Corporate Event", "Religious Event")),
    Service("🌸", "Decoration", "Themes, flowers, stage & lighting", listOf("Wedding Decoration", "Stage Decoration", "Birthday Theme", "Flower Decoration", "Mandap", "Lighting")),
    Service("🍽️", "Catering", "Menus and packages for every occasion", listOf("Gujarati", "Punjabi", "South Indian", "Jain", "Continental", "Custom Package")),
    Service("💼", "Consultancy", "Business and professional services", listOf("Accounts & Finance", "HR", "Business Setup", "French Support", "Real Estate", "Documentation")),
    Service("✈️", "Tours & Travel", "Trips, hotels, visa & transport", listOf("Holiday Packages", "Hotels", "Flight Enquiry", "Visa Assistance", "Cab / Vehicle Rental", "Group Tours")),
    Service("🛍️", "Online Shopping", "Products, gifts and essentials", listOf("Puja Products", "Astrology Products", "Gifts", "Decoration Items", "Travel Accessories", "Local Products"))
)

private val details = mapOf(
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = lightColorScheme(primary = Brand, surface = Color.White, background = WarmBg)) { RawalworldApp() } }
    }
}

private fun openWeb(context: Context, url: String) { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
private fun loadBookings(context: Context): List<Booking> {
    val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("bookings", "") ?: ""
    if (raw.isBlank()) return emptyList()
    return raw.split("\u001e").mapNotNull { row -> val p = row.split("\u001f"); if (p.size < 6) null else Booking(p[0], p[1], p[2], p[3], p[4], p[5]) }
}
private fun saveBookings(context: Context, bookings: List<Booking>) {
    val raw = bookings.joinToString("\u001e") { listOf(it.service, it.name, it.mobile, it.city, it.date, it.note).joinToString("\u001f") }
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("bookings", raw).apply()
}

private fun postJson(table: String, payload: JSONObject, onDone: (Boolean) -> Unit) {
    Thread {
        var ok = false
        try {
            val c = URL("$SUPABASE_URL/rest/v1/$table").openConnection() as HttpURLConnection
            c.requestMethod = "POST"; c.setRequestProperty("Content-Type", "application/json"); c.setRequestProperty("apikey", SUPABASE_KEY); c.setRequestProperty("Prefer", "return=minimal"); c.doOutput = true
            c.outputStream.use { it.write(payload.toString().toByteArray()) }; ok = c.responseCode in 200..299; c.disconnect()
        } catch (_: Exception) {}
        Handler(Looper.getMainLooper()).post { onDone(ok) }
    }.start()
}

private fun fetchProducts(onDone: (List<Product>) -> Unit) {
    Thread {
        val out = mutableListOf<Product>()
        try {
            val c = URL("$SUPABASE_URL/rest/v1/products?select=name,description,price,currency,external_url,is_free&is_active=eq.true").openConnection() as HttpURLConnection
            c.setRequestProperty("apikey", SUPABASE_KEY)
            val arr = JSONArray(c.inputStream.bufferedReader().use { it.readText() })
            for (i in 0 until arr.length()) { val x = arr.getJSONObject(i); val free = x.optBoolean("is_free"); out += Product(x.optString("name"), x.optString("description"), if (free) "FREE" else "${x.optString("currency")} ${x.optString("price")}", x.optString("external_url").takeIf { it.isNotBlank() }) }
            c.disconnect()
        } catch (_: Exception) {}
        Handler(Looper.getMainLooper()).post { onDone(out) }
    }.start()
}

private fun adminLogin(email: String, password: String, onDone: (String?, String) -> Unit) {
    Thread {
        var token: String? = null; var msg = "Login failed."
        try {
            val c = URL("$SUPABASE_URL/auth/v1/token?grant_type=password").openConnection() as HttpURLConnection
            c.requestMethod = "POST"; c.setRequestProperty("Content-Type", "application/json"); c.setRequestProperty("apikey", SUPABASE_KEY); c.doOutput = true
            c.outputStream.use { it.write(JSONObject().put("email", email).put("password", password).toString().toByteArray()) }
            val code = c.responseCode
            val body = if (code in 200..299) c.inputStream.bufferedReader().use { it.readText() } else c.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code in 200..299) { token = JSONObject(body).optString("access_token").takeIf { it.isNotBlank() }; msg = if (token != null) "Login successful." else "Login response incomplete." } else msg = "Login failed. Check email/password."
            c.disconnect()
        } catch (_: Exception) { msg = "Unable to connect to admin login." }
        Handler(Looper.getMainLooper()).post { onDone(token, msg) }
    }.start()
}

private fun adminWrite(token: String, path: String, method: String, payload: JSONObject, onDone: (Boolean) -> Unit) {
    Thread {
        var ok = false
        try {
            val c = URL("$SUPABASE_URL/rest/v1/$path").openConnection() as HttpURLConnection
            c.requestMethod = method; c.setRequestProperty("Content-Type", "application/json"); c.setRequestProperty("apikey", SUPABASE_KEY); c.setRequestProperty("Authorization", "Bearer $token"); c.setRequestProperty("Prefer", "return=minimal"); c.doOutput = true
            c.outputStream.use { it.write(payload.toString().toByteArray()) }; ok = c.responseCode in 200..299; c.disconnect()
        } catch (_: Exception) {}
        Handler(Looper.getMainLooper()).post { onDone(ok) }
    }.start()
}

private fun fetchAdminData(token: String, onDone: (AdminData?, String) -> Unit) {
    Thread {
        try {
            fun arr(path: String): JSONArray { val c = URL("$SUPABASE_URL/rest/v1/$path").openConnection() as HttpURLConnection; c.setRequestProperty("apikey", SUPABASE_KEY); c.setRequestProperty("Authorization", "Bearer $token"); if (c.responseCode !in 200..299) { c.disconnect(); throw IllegalStateException() }; val a = JSONArray(c.inputStream.bufferedReader().use { it.readText() }); c.disconnect(); return a }
            val b = arr("bookings?select=id,service,customer_name,mobile,city,status&order=created_at.desc&limit=20")
            val a = arr("astrology_requests?select=id,request_type,customer_name,mobile,birth_place,status&order=created_at.desc&limit=20")
            val p = arr("products?select=id&is_active=eq.true")
            val o = arr("orders?select=id,customer_name,mobile,total_amount,currency,order_status&order=created_at.desc&limit=20")
            val bookings = (0 until b.length()).map { i -> val x=b.getJSONObject(i); AdminBooking(x.optString("id"),x.optString("service"),x.optString("customer_name"),x.optString("mobile"),x.optString("city"),x.optString("status","submitted")) }
            val astrology = (0 until a.length()).map { i -> val x=a.getJSONObject(i); AdminAstrology(x.optString("id"),x.optString("request_type","kundli"),x.optString("customer_name"),x.optString("mobile"),x.optString("birth_place"),x.optString("status","submitted")) }
            val orders = (0 until o.length()).map { i -> val x=o.getJSONObject(i); AdminOrder(x.optString("id"),x.optString("customer_name"),x.optString("mobile"),"${x.optString("currency","INR")} ${x.optString("total_amount","0")}",x.optString("order_status","submitted")) }
            Handler(Looper.getMainLooper()).post { onDone(AdminData(bookings, astrology, p.length(), orders), "Dashboard updated.") }
        } catch (_: Exception) { Handler(Looper.getMainLooper()).post { onDone(null, "Unable to load admin data. This account may not have admin access.") } }
    }.start()
}

@Composable fun RawalworldApp() {
    val context = LocalContext.current; var screen by remember { mutableStateOf("home") }; var selected by remember { mutableStateOf<Service?>(null) }; var bookings by remember { mutableStateOf(loadBookings(context)) }
    Scaffold(bottomBar = { NavigationBar {
        NavigationBarItem(screen=="home", {screen="home";selected=null}, {Icon(Icons.Default.Home,null)}, label={Text("Home")})
        NavigationBarItem(screen=="bookings", {screen="bookings";bookings=loadBookings(context)}, {Icon(Icons.Default.DateRange,null)}, label={Text("Bookings")})
        NavigationBarItem(screen=="service"&&selected?.title=="Online Shopping", {selected=services.last();screen="service"}, {Icon(Icons.Default.ShoppingCart,null)}, label={Text("Shop")})
        NavigationBarItem(screen=="profile", {screen="profile"}, {Icon(Icons.Default.Person,null)}, label={Text("Profile")})
        NavigationBarItem(screen=="admin", {screen="admin"}, {Text("🔐")}, label={Text("Admin")})
    }}) { pad -> Box(Modifier.fillMaxSize().padding(pad)) { when(screen) {
        "service" -> selected?.let { ServiceScreen(it,{screen="home"},{screen="booking"}) }
        "booking" -> selected?.let { s -> BookingScreen(s,{screen="service"}) { b -> bookings=bookings+b;saveBookings(context,bookings) } }
        "bookings" -> BookingsScreen(bookings); "profile" -> ProfileScreen(); "admin" -> AdminScreen(); else -> HomeScreen {selected=it;screen="service"}
    } } }
}

@Composable fun HomeScreen(onOpen:(Service)->Unit) {
    var q by remember { mutableStateOf("") }; val filtered=services.filter { q.isBlank()||it.title.contains(q,true)||it.subtitle.contains(q,true)||it.options.any { o->o.contains(q,true) } }
    Column(Modifier.fillMaxSize().padding(horizontal=16.dp)) { Spacer(Modifier.height(14.dp)); Text("Rawalworld",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold); Text("Gujarat lifestyle & services super app",style=MaterialTheme.typography.bodySmall); Spacer(Modifier.height(14.dp)); Card(colors=CardDefaults.cardColors(containerColor=Hero),shape=RoundedCornerShape(24.dp),modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(22.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Everything you need,\nin one app.",color=Color.White,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.ExtraBold);Text("Astrology, events, consultancy, travel and shopping.",color=Color.White.copy(alpha=.86f))};Text("RW",color=Color.White,style=MaterialTheme.typography.displaySmall,fontWeight=FontWeight.Black)}}; Spacer(Modifier.height(12.dp)); OutlinedTextField(q,{q=it},leadingIcon={Icon(Icons.Default.Search,null)},placeholder={Text("Search services...")},modifier=Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); Text("📞 +91 77093 78969  •  ✉ rawalworld@gmail.com",style=MaterialTheme.typography.bodySmall); Text("📍 Gujarat, India  •  💳 Google Pay",style=MaterialTheme.typography.bodySmall); Spacer(Modifier.height(14.dp)); LazyVerticalGrid(columns=GridCells.Fixed(2),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=20.dp)){items(filtered){s->Card(onClick={onOpen(s)},shape=RoundedCornerShape(18.dp),modifier=Modifier.height(150.dp)){Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.SpaceBetween){Text(s.icon,style=MaterialTheme.typography.headlineMedium);Column{Text(s.title,fontWeight=FontWeight.Bold);Text(s.subtitle,style=MaterialTheme.typography.bodySmall)}}}}}
    }
}

@Composable fun ServiceScreen(service:Service,onBack:()->Unit,onBook:()->Unit) {
    val context=LocalContext.current; var selectedInfo by remember{mutableStateOf<Pair<String,String>?>(null)}; var dob by remember{mutableStateOf("")}; var bt by remember{mutableStateOf("")}; var place by remember{mutableStateOf("")}; var astroMsg by remember{mutableStateOf("")}; var products by remember{mutableStateOf<List<Product>>(emptyList())}; var loading by remember{mutableStateOf(false)}
    LaunchedEffect(service.title){if(service.title=="Online Shopping"){loading=true;fetchProducts{products=it;loading=false}}}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){TextButton(onClick=onBack){Icon(Icons.Default.ArrowBack,null);Text(" Back to home")};Text("${service.icon} ${service.title}",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold);Text(service.subtitle);Spacer(Modifier.height(14.dp));service.options.forEach{o->Card(shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth().padding(bottom=8.dp)){Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){Text(o,Modifier.weight(1f));FilledTonalButton(onClick={selectedInfo=o to (details[o]?:"More information coming soon.")}){Text("Open")}}}};selectedInfo?.let{Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)){Column(Modifier.padding(14.dp)){Text(it.first,fontWeight=FontWeight.Bold);Text(it.second)}}}
        if(service.title=="Astrology"){Spacer(Modifier.height(12.dp));Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(14.dp)){Text("Personal Astrology Details",fontWeight=FontWeight.Bold);OutlinedTextField(dob,{dob=it},label={Text("Date of birth YYYY-MM-DD")},modifier=Modifier.fillMaxWidth());OutlinedTextField(bt,{bt=it},label={Text("Birth time HH:MM")},modifier=Modifier.fillMaxWidth());OutlinedTextField(place,{place=it},label={Text("Birth place")},modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(8.dp));Button(onClick={if(dob.isBlank()||bt.isBlank()||place.isBlank())astroMsg="Please fill all birth details." else {astroMsg="Submitting online...";postJson("astrology_requests",JSONObject().put("date_of_birth",dob).put("birth_time",bt).put("birth_place",place).put("request_type","kundli")){astroMsg=if(it)"Astrology request submitted online successfully." else "Online submission failed."}}},modifier=Modifier.fillMaxWidth()){Text("Submit Astrology Request")};if(astroMsg.isNotBlank())Text(astroMsg,style=MaterialTheme.typography.bodySmall)}}}
        if(service.title=="Online Shopping"){Spacer(Modifier.height(12.dp));Text("Online Products",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);if(loading)CircularProgressIndicator() else if(products.isEmpty())Text("Could not load online products.") else products.forEach{p->Card(Modifier.fillMaxWidth().padding(vertical=6.dp),shape=RoundedCornerShape(14.dp)){Column(Modifier.padding(14.dp)){Text(p.name,fontWeight=FontWeight.Bold);Text(p.description,style=MaterialTheme.typography.bodySmall);Text(p.price,fontWeight=FontWeight.Bold);p.url?.let{u->Button(onClick={openWeb(context,u)}){Text("Open")}}}}}}
        Spacer(Modifier.height(14.dp));Button(onClick=onBook,modifier=Modifier.fillMaxWidth()){Text("Request Booking / Quotation")}
    }
}

@Composable fun BookingScreen(service:Service,onBack:()->Unit,onSaved:(Booking)->Unit) {
    val prefs=LocalContext.current.getSharedPreferences(PREFS,Context.MODE_PRIVATE);var name by remember{mutableStateOf(prefs.getString("name","")?:"")};var mobile by remember{mutableStateOf(prefs.getString("mobile","")?:"")};var city by remember{mutableStateOf(prefs.getString("city","")?:"")};var date by remember{mutableStateOf("")};var note by remember{mutableStateOf("")};var msg by remember{mutableStateOf("")};var submitting by remember{mutableStateOf(false)}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){TextButton(onClick=onBack){Text("← Back")};Text("Booking / Quotation",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text(service.title);Spacer(Modifier.height(12.dp));OutlinedTextField(name,{name=it},label={Text("Name")},modifier=Modifier.fillMaxWidth());OutlinedTextField(mobile,{mobile=it},label={Text("Mobile")},modifier=Modifier.fillMaxWidth());OutlinedTextField(city,{city=it},label={Text("City")},modifier=Modifier.fillMaxWidth());OutlinedTextField(date,{date=it},label={Text("Preferred date YYYY-MM-DD (optional)")},modifier=Modifier.fillMaxWidth());OutlinedTextField(note,{note=it},label={Text("Requirement")},minLines=3,modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(10.dp));Button(enabled=!submitting,onClick={if(name.isBlank()||mobile.isBlank()||city.isBlank())msg="Please enter name, mobile and city." else{val b=Booking(service.title,name.trim(),mobile.trim(),city.trim(),date.trim(),note.trim());onSaved(b);submitting=true;msg="Submitting online...";val j=JSONObject().put("service",b.service).put("customer_name",b.name).put("mobile",b.mobile).put("city",b.city).put("source","android");if(Regex("\\d{4}-\\d{2}-\\d{2}").matches(b.date))j.put("preferred_date",b.date);if(b.note.isNotBlank())j.put("requirement",b.note);postJson("bookings",j){submitting=false;msg=if(it)"✅ Request submitted online successfully." else "⚠️ Saved on phone, but online submission failed."}}},modifier=Modifier.fillMaxWidth()){Text(if(submitting)"Submitting..." else "Submit Request")};if(msg.isNotBlank())Text(msg)}
}

@Composable fun BookingsScreen(items:List<Booking>){Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){Text("My Bookings",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Spacer(Modifier.height(12.dp));if(items.isEmpty())Text("No booking requests yet.");items.asReversed().forEach{b->Card(Modifier.fillMaxWidth().padding(bottom=8.dp)){Column(Modifier.padding(12.dp)){Text(b.service,fontWeight=FontWeight.Bold);Text("${b.name} • ${b.mobile} • ${b.city}",style=MaterialTheme.typography.bodySmall);if(b.date.isNotBlank())Text("Date: ${b.date}",style=MaterialTheme.typography.bodySmall);if(b.note.isNotBlank())Text(b.note,style=MaterialTheme.typography.bodySmall)}}}}}

@Composable fun ProfileScreen(){val context=LocalContext.current;val p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);var name by remember{mutableStateOf(p.getString("name","")?:"")};var mobile by remember{mutableStateOf(p.getString("mobile","")?:"")};var email by remember{mutableStateOf(p.getString("email","")?:"")};var city by remember{mutableStateOf(p.getString("city","")?:"")};var msg by remember{mutableStateOf("")};Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){Text("Profile",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);OutlinedTextField(name,{name=it},label={Text("Name")},modifier=Modifier.fillMaxWidth());OutlinedTextField(mobile,{mobile=it},label={Text("Mobile")},modifier=Modifier.fillMaxWidth());OutlinedTextField(email,{email=it},label={Text("Email")},modifier=Modifier.fillMaxWidth());OutlinedTextField(city,{city=it},label={Text("City")},modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(10.dp));Button(onClick={p.edit().putString("name",name).putString("mobile",mobile).putString("email",email).putString("city",city).apply();msg="Profile saved."},modifier=Modifier.fillMaxWidth()){Text("Save Profile")};if(msg.isNotBlank())Text(msg)}}

@Composable fun AdminScreen() {
    var email by remember{mutableStateOf("")};var password by remember{mutableStateOf("")};var token by remember{mutableStateOf<String?>(null)};var msg by remember{mutableStateOf("")};var loading by remember{mutableStateOf(false)};var data by remember{mutableStateOf<AdminData?>(null)}
    var productName by remember{mutableStateOf("")};var category by remember{mutableStateOf("Astrology Products")};var price by remember{mutableStateOf("0")};var description by remember{mutableStateOf("")}
    fun refresh(){val t=token?:return;loading=true;fetchAdminData(t){d,m->loading=false;data=d;msg=m}}
    fun update(path:String,status:String){val t=token?:return;loading=true;adminWrite(t,path,"PATCH",JSONObject().put("status",status)){ok->loading=false;msg=if(ok)"Status updated." else "Update failed.";if(ok)refresh()}}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
        Text("🔐 Rawalworld Admin",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold);Text("Secure management dashboard",style=MaterialTheme.typography.bodySmall);Spacer(Modifier.height(14.dp))
        if(token==null){Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(16.dp)){Text("Admin Login",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);OutlinedTextField(email,{email=it},label={Text("Admin email")},modifier=Modifier.fillMaxWidth());OutlinedTextField(password,{password=it},label={Text("Password")},modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(10.dp));Button(enabled=!loading,onClick={if(email.isBlank()||password.isBlank())msg="Enter admin email and password." else{loading=true;adminLogin(email.trim(),password){t,m->loading=false;token=t;msg=m;if(t!=null){loading=true;fetchAdminData(t){d,dm->loading=false;data=d;msg=dm}}}}},modifier=Modifier.fillMaxWidth()){Text(if(loading)"Please wait..." else "Login")};if(msg.isNotBlank())Text(msg,style=MaterialTheme.typography.bodySmall)}}}
        else{val d=data;if(loading)CircularProgressIndicator();if(d!=null){LazyVerticalGrid(columns=GridCells.Fixed(2),modifier=Modifier.height(190.dp),verticalArrangement=Arrangement.spacedBy(8.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){item{AdminCountCard("Bookings",d.bookings.size,"📅")};item{AdminCountCard("Astrology",d.astrology.size,"🔮")};item{AdminCountCard("Products",d.productCount,"🛍️")};item{AdminCountCard("Orders",d.orders.size,"📦")}}
            Text("Booking Management",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);d.bookings.forEach{b->Card(Modifier.fillMaxWidth().padding(vertical=5.dp)){Column(Modifier.padding(12.dp)){Text(b.service,fontWeight=FontWeight.Bold);Text("${b.name} • ${b.mobile} • ${b.city}",style=MaterialTheme.typography.bodySmall);Text("Status: ${b.status}");Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){TextButton(onClick={update("bookings?id=eq.${b.id}","contacted")}){Text("Contacted")};TextButton(onClick={update("bookings?id=eq.${b.id}","confirmed")}){Text("Confirmed")};TextButton(onClick={update("bookings?id=eq.${b.id}","completed")}){Text("Complete")}}}}}
            Spacer(Modifier.height(8.dp));Text("Astrology Management",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);d.astrology.forEach{a->Card(Modifier.fillMaxWidth().padding(vertical=5.dp)){Column(Modifier.padding(12.dp)){Text(a.type,fontWeight=FontWeight.Bold);Text("${a.name} • ${a.mobile} • ${a.place}",style=MaterialTheme.typography.bodySmall);Text("Status: ${a.status}");Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){TextButton(onClick={update("astrology_requests?id=eq.${a.id}","reviewing")}){Text("Reviewing")};TextButton(onClick={update("astrology_requests?id=eq.${a.id}","completed")}){Text("Complete")}}}}}
            Spacer(Modifier.height(8.dp));Text("Product Management",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){OutlinedTextField(productName,{productName=it},label={Text("Product name")},modifier=Modifier.fillMaxWidth());OutlinedTextField(category,{category=it},label={Text("Category")},modifier=Modifier.fillMaxWidth());OutlinedTextField(price,{price=it},label={Text("Price")},modifier=Modifier.fillMaxWidth());OutlinedTextField(description,{description=it},label={Text("Description")},modifier=Modifier.fillMaxWidth());Button(onClick={val t=token?:return@Button;if(productName.isBlank()){msg="Enter product name."}else{loading=true;val amount=price.toDoubleOrNull()?:0.0;val j=JSONObject().put("name",productName.trim()).put("category",category.trim()).put("description",description.trim()).put("price",amount).put("currency","INR").put("is_free",amount==0.0).put("is_active",true);adminWrite(t,"products","POST",j){ok->loading=false;msg=if(ok)"Product added." else "Could not add product.";if(ok){productName="";description="";refresh()}}}},modifier=Modifier.fillMaxWidth()){Text("Add Product")}}}
            Spacer(Modifier.height(8.dp));Text("Order Management",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);if(d.orders.isEmpty())Text("No orders yet.") else d.orders.forEach{o->Card(Modifier.fillMaxWidth().padding(vertical=5.dp)){Column(Modifier.padding(12.dp)){Text(o.name.ifBlank{"Order"},fontWeight=FontWeight.Bold);Text("${o.mobile} • ${o.amount}",style=MaterialTheme.typography.bodySmall);Text("Status: ${o.status}");Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){TextButton(onClick={val t=token?:return@TextButton;loading=true;adminWrite(t,"orders?id=eq.${o.id}","PATCH",JSONObject().put("order_status","processing")){ok->loading=false;msg=if(ok)"Order updated." else "Update failed.";if(ok)refresh()}}){Text("Processing")};TextButton(onClick={val t=token?:return@TextButton;loading=true;adminWrite(t,"orders?id=eq.${o.id}","PATCH",JSONObject().put("order_status","delivered")){ok->loading=false;msg=if(ok)"Order delivered." else "Update failed.";if(ok)refresh()}}){Text("Delivered")}}}}}
        };Spacer(Modifier.height(10.dp));Button(onClick={refresh()},enabled=!loading,modifier=Modifier.fillMaxWidth()){Text("Refresh Dashboard")};OutlinedButton(onClick={token=null;data=null;password="";msg="Logged out."},modifier=Modifier.fillMaxWidth()){Text("Logout")};if(msg.isNotBlank())Text(msg,style=MaterialTheme.typography.bodySmall)}
    }
}

@Composable fun AdminCountCard(label:String,count:Int,icon:String){Card(Modifier.fillMaxWidth()){Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){Column{Text(count.toString(),style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(label,style=MaterialTheme.typography.bodySmall)};Text(icon,style=MaterialTheme.typography.headlineMedium)}}}
