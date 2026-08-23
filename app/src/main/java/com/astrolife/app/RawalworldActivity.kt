package com.astrolife.app

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val RW_URL = "https://hcpvuripnlhofxfczyyb.supabase.co"
private const val RW_KEY = "sb_publishable_J8YoD4yenQO-nlEMoC1kvA_3_vJgGjg"
private const val RW_PREFS = "rawalworld_prefs_v2"

data class RWService(val key:String,val icon:String,val title:String,val subtitle:String,val options:List<String>)
data class RWProduct(val name:String,val description:String,val price:String,val imageUrl:String?,val externalUrl:String?)
data class RWBooking(val id:String,val service:String,val name:String,val mobile:String,val city:String,val status:String)
data class RWAstrology(val id:String,val type:String,val name:String,val mobile:String,val place:String,val status:String)
data class RWOrder(val id:String,val name:String,val mobile:String,val amount:String,val status:String)
data class RWAdminData(val bookings:List<RWBooking>,val astrology:List<RWAstrology>,val products:Int,val orders:List<RWOrder>)
data class RWContact(val name:String,val phone:String,val email:String,val whatsapp:String)

private val rwServices = listOf(
    RWService("astrology","🔮","Astrology","Horoscope, Kundli & consultation",listOf("Daily Horoscope","Kundli / Birth Chart","Marriage Matching","Ask an Astrologer","Muhurat & Puja")),
    RWService("events","🎉","Events","Weddings, birthdays & corporate events",listOf("Wedding","Birthday","Engagement","Anniversary","Corporate Event","Religious Event")),
    RWService("decoration","🌸","Decoration","Themes, flowers, stage & lighting",listOf("Wedding Decoration","Stage Decoration","Birthday Theme","Flower Decoration","Mandap","Lighting")),
    RWService("catering","🍽️","Catering","Menus and packages for every occasion",listOf("Gujarati","Punjabi","South Indian","Jain","Continental","Custom Package")),
    RWService("consultancy","💼","Consultancy","Business and professional services",listOf("Accounts & Finance","HR","Business Setup","French Support","Real Estate","Documentation")),
    RWService("travel","✈️","Tours & Travel","Trips, hotels, visa & transport",listOf("Holiday Packages","Hotels","Flight Enquiry","Visa Assistance","Cab / Vehicle Rental","Group Tours")),
    RWService("shopping","🛍️","Online Shopping","Products, gifts and essentials",listOf("Puja Products","Astrology Products","Gifts","Decoration Items","Travel Accessories","Local Products"))
)

private val rwDetails = mapOf(
    "Daily Horoscope" to "Daily horoscope overview for career, money, relationships, health and general outlook.",
    "Kundli / Birth Chart" to "A Vedic birth chart prepared from birth date, exact time and place.",
    "Marriage Matching" to "Traditional compatibility guidance using two birth charts.",
    "Ask an Astrologer" to "Send your birth details and question for a personal consultation.",
    "Muhurat & Puja" to "Guidance for traditionally favorable timing for important events.",
    "Wedding" to "Venue, decoration, catering, photography, transport and coordination.",
    "Birthday" to "Themes, decoration, cake, catering and entertainment.",
    "Accounts & Finance" to "Bookkeeping, MIS, budgeting and finance support.",
    "HR" to "Recruitment, HR documentation and process support.",
    "French Support" to "French communication and translation support.",
    "Real Estate" to "Property search and documentation coordination.",
    "Visa Assistance" to "Visa checklist and application support.",
    "Cab / Vehicle Rental" to "Cab, car, pickup and group transport rental.",
    "Astrology Products" to "Astrology products and digital services.",
    "Puja Products" to "Puja essentials and religious-use products."
)

class RawalworldActivity: ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContent{ MaterialTheme{ RWApp() } }
    }
}

private fun mainPost(table:String,payload:JSONObject,done:(Boolean)->Unit){
    Thread{
        var ok=false
        try{
            val c=URL("$RW_URL/rest/v1/$table").openConnection() as HttpURLConnection
            c.requestMethod="POST"; c.setRequestProperty("Content-Type","application/json"); c.setRequestProperty("apikey",RW_KEY); c.setRequestProperty("Prefer","return=minimal"); c.doOutput=true
            c.outputStream.use{it.write(payload.toString().toByteArray())}; ok=c.responseCode in 200..299; c.disconnect()
        }catch(_:Exception){}
        Handler(Looper.getMainLooper()).post{done(ok)}
    }.start()
}

private fun authLogin(email:String,password:String,done:(String?,String?,String)->Unit){
    Thread{
        var at:String?=null; var rt:String?=null; var msg="Login failed."
        try{
            val c=URL("$RW_URL/auth/v1/token?grant_type=password").openConnection() as HttpURLConnection
            c.requestMethod="POST"; c.setRequestProperty("Content-Type","application/json"); c.setRequestProperty("apikey",RW_KEY); c.doOutput=true
            c.outputStream.use{it.write(JSONObject().put("email",email).put("password",password).toString().toByteArray())}
            val code=c.responseCode; val body=if(code in 200..299)c.inputStream.bufferedReader().use{it.readText()} else c.errorStream?.bufferedReader()?.use{it.readText()}.orEmpty()
            if(code in 200..299){val j=JSONObject(body);at=j.optString("access_token").takeIf{it.isNotBlank()};rt=j.optString("refresh_token").takeIf{it.isNotBlank()};msg="Login successful."}else msg="Check email and password."
            c.disconnect()
        }catch(_:Exception){msg="Unable to connect."}
        Handler(Looper.getMainLooper()).post{done(at,rt,msg)}
    }.start()
}

private fun authRefresh(refresh:String,done:(String?,String?)->Unit){
    Thread{
        var at:String?=null; var rt:String?=null
        try{
            val c=URL("$RW_URL/auth/v1/token?grant_type=refresh_token").openConnection() as HttpURLConnection
            c.requestMethod="POST";c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("apikey",RW_KEY);c.doOutput=true
            c.outputStream.use{it.write(JSONObject().put("refresh_token",refresh).toString().toByteArray())}
            if(c.responseCode in 200..299){val j=JSONObject(c.inputStream.bufferedReader().use{it.readText()});at=j.optString("access_token");rt=j.optString("refresh_token")}
            c.disconnect()
        }catch(_:Exception){}
        Handler(Looper.getMainLooper()).post{done(at,rt)}
    }.start()
}

private fun forgotPassword(email:String,done:(Boolean)->Unit){
    Thread{
        var ok=false
        try{
            val c=URL("$RW_URL/auth/v1/recover").openConnection() as HttpURLConnection
            c.requestMethod="POST";c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("apikey",RW_KEY);c.doOutput=true
            c.outputStream.use{it.write(JSONObject().put("email",email).toString().toByteArray())};ok=c.responseCode in 200..299;c.disconnect()
        }catch(_:Exception){}
        Handler(Looper.getMainLooper()).post{done(ok)}
    }.start()
}

private fun adminWrite(token:String,path:String,method:String,payload:JSONObject,prefer:String="return=minimal",done:(Boolean)->Unit){
    Thread{
        var ok=false
        try{
            val c=URL("$RW_URL/rest/v1/$path").openConnection() as HttpURLConnection
            c.requestMethod=method;c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("apikey",RW_KEY);c.setRequestProperty("Authorization","Bearer $token");c.setRequestProperty("Prefer",prefer);c.doOutput=true
            c.outputStream.use{it.write(payload.toString().toByteArray())};ok=c.responseCode in 200..299;c.disconnect()
        }catch(_:Exception){}
        Handler(Looper.getMainLooper()).post{done(ok)}
    }.start()
}

private fun fetchProducts(done:(List<RWProduct>)->Unit){
    Thread{
        val out=mutableListOf<RWProduct>()
        try{
            val c=URL("$RW_URL/rest/v1/products?select=name,description,price,currency,image_url,external_url,is_free&is_active=eq.true&order=created_at.desc").openConnection() as HttpURLConnection
            c.setRequestProperty("apikey",RW_KEY);val a=JSONArray(c.inputStream.bufferedReader().use{it.readText()})
            for(i in 0 until a.length()){val x=a.getJSONObject(i);val free=x.optBoolean("is_free");out+=RWProduct(x.optString("name"),x.optString("description"),if(free)"FREE" else "₹${x.optString("price")}",x.optString("image_url").takeIf{it.isNotBlank()},x.optString("external_url").takeIf{it.isNotBlank()})};c.disconnect()
        }catch(_:Exception){}
        Handler(Looper.getMainLooper()).post{done(out)}
    }.start()
}

private fun fetchContact(key:String,done:(RWContact?)->Unit){
    Thread{
        var out:RWContact?=null
        try{
            val c=URL("$RW_URL/rest/v1/service_contacts?select=contact_name,phone,email,whatsapp&service_key=eq.$key&is_active=eq.true&limit=1").openConnection() as HttpURLConnection
            c.setRequestProperty("apikey",RW_KEY);val a=JSONArray(c.inputStream.bufferedReader().use{it.readText()});if(a.length()>0){val x=a.getJSONObject(0);out=RWContact(x.optString("contact_name"),x.optString("phone"),x.optString("email"),x.optString("whatsapp"))};c.disconnect()
        }catch(_:Exception){}
        Handler(Looper.getMainLooper()).post{done(out)}
    }.start()
}

private fun fetchAdmin(token:String,done:(RWAdminData?)->Unit){
    Thread{
        try{
            fun arr(path:String):JSONArray{val c=URL("$RW_URL/rest/v1/$path").openConnection() as HttpURLConnection;c.setRequestProperty("apikey",RW_KEY);c.setRequestProperty("Authorization","Bearer $token");if(c.responseCode !in 200..299)throw IllegalStateException();val a=JSONArray(c.inputStream.bufferedReader().use{it.readText()});c.disconnect();return a}
            val b=arr("bookings?select=id,service,customer_name,mobile,city,status&order=created_at.desc&limit=30");val a=arr("astrology_requests?select=id,request_type,customer_name,mobile,birth_place,status&order=created_at.desc&limit=30");val p=arr("products?select=id&is_active=eq.true");val o=arr("orders?select=id,customer_name,mobile,total_amount,currency,order_status&order=created_at.desc&limit=30")
            val bs=(0 until b.length()).map{val x=b.getJSONObject(it);RWBooking(x.optString("id"),x.optString("service"),x.optString("customer_name"),x.optString("mobile"),x.optString("city"),x.optString("status"))}
            val ast=(0 until a.length()).map{val x=a.getJSONObject(it);RWAstrology(x.optString("id"),x.optString("request_type"),x.optString("customer_name"),x.optString("mobile"),x.optString("birth_place"),x.optString("status"))}
            val os=(0 until o.length()).map{val x=o.getJSONObject(it);RWOrder(x.optString("id"),x.optString("customer_name"),x.optString("mobile"),"₹${x.optString("total_amount")}",x.optString("order_status"))}
            Handler(Looper.getMainLooper()).post{done(RWAdminData(bs,ast,p.length(),os))}
        }catch(_:Exception){Handler(Looper.getMainLooper()).post{done(null)}}
    }.start()
}

private fun uploadPhoto(context:Context,token:String,uri:Uri,productName:String,done:(String?)->Unit){
    Thread{
        var publicUrl:String?=null
        try{
            val bytes=context.contentResolver.openInputStream(uri)?.use{it.readBytes()}?:byteArrayOf();val type=context.contentResolver.getType(uri)?:"image/jpeg";val safe=productName.lowercase().replace(Regex("[^a-z0-9]+"),"-").trim('-');val file="${System.currentTimeMillis()}_${safe.ifBlank{"product"}}.jpg"
            val c=URL("$RW_URL/storage/v1/object/product-gallery/$file").openConnection() as HttpURLConnection
            c.requestMethod="POST";c.setRequestProperty("apikey",RW_KEY);c.setRequestProperty("Authorization","Bearer $token");c.setRequestProperty("Content-Type",type);c.setRequestProperty("x-upsert","false");c.doOutput=true;c.outputStream.use{it.write(bytes)}
            if(c.responseCode in 200..299)publicUrl="$RW_URL/storage/v1/object/public/product-gallery/$file";c.disconnect()
        }catch(_:Exception){}
        Handler(Looper.getMainLooper()).post{done(publicUrl)}
    }.start()
}

@Composable private fun RemoteImage(url:String?){
    var bitmap by remember(url){mutableStateOf<Bitmap?>(null)}
    LaunchedEffect(url){if(url!=null)Thread{try{val b=BitmapFactory.decodeStream(URL(url).openStream());Handler(Looper.getMainLooper()).post{bitmap=b}}catch(_:Exception){}}.start()}
    bitmap?.let{Image(it.asImageBitmap(),contentDescription=null,modifier=Modifier.fillMaxWidth().height(150.dp),contentScale=ContentScale.Crop)}
}

@Composable fun RWApp(){
    var screen by remember{mutableStateOf("home")};var selected by remember{mutableStateOf<RWService?>(null)}
    Scaffold(bottomBar={NavigationBar{listOf("home" to "🏠 Home","bookings" to "📅 Bookings","shop" to "🛍️ Shop","profile" to "👤 Profile","admin" to "🔐 Admin").forEach{(key,label)->NavigationBarItem(selected=screen==key,onClick={if(key=="shop"){selected=rwServices.last();screen="service"}else screen=key},icon={Text(label.substringBefore(" "))},label={Text(label.substringAfter(" "))})}}}){pad->Box(Modifier.fillMaxSize().padding(pad)){when(screen){"service"->selected?.let{RWServiceScreen(it,{screen="home"},{screen="booking"})};"booking"->selected?.let{RWBookingScreen(it){screen="service"}};"bookings"->RWLocalBookings();"profile"->RWProfile();"admin"->RWAdmin();else->RWHome{selected=it;screen="service"}}}}
}

@Composable private fun RWHome(open:(RWService)->Unit){
    var q by remember{mutableStateOf("")};val list=rwServices.filter{q.isBlank()||it.title.contains(q,true)||it.subtitle.contains(q,true)}
    Column(Modifier.fillMaxSize().padding(16.dp)){Text("🕉️ Rawalworld",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold);Text("Adishakti · Mahadev",fontWeight=FontWeight.Bold);Text("Gujarat lifestyle & services super app",style=MaterialTheme.typography.bodySmall);Spacer(Modifier.height(12.dp));OutlinedTextField(q,{q=it},label={Text("Search services")},modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(12.dp));LazyVerticalGrid(columns=GridCells.Fixed(2),verticalArrangement=Arrangement.spacedBy(10.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){items(list){s->Card(onClick={open(s)},modifier=Modifier.height(145.dp)){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.SpaceBetween){Text(s.icon,style=MaterialTheme.typography.headlineMedium);Column{Text(s.title,fontWeight=FontWeight.Bold);Text(s.subtitle,style=MaterialTheme.typography.bodySmall)}}}}}}
}

@Composable private fun RWServiceScreen(service:RWService,back:()->Unit,book:()->Unit){
    val context=LocalContext.current;var info by remember{mutableStateOf("")};var products by remember{mutableStateOf<List<RWProduct>>(emptyList())};var contact by remember{mutableStateOf<RWContact?>(null)};var dob by remember{mutableStateOf("")};var bt by remember{mutableStateOf("")};var place by remember{mutableStateOf("")};var msg by remember{mutableStateOf("")}
    LaunchedEffect(service.key){fetchContact(service.key){contact=it};if(service.key=="shopping")fetchProducts{products=it}}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){TextButton(onClick=back){Text("← Back")};Text("${service.icon} ${service.title}",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text(service.subtitle);Spacer(Modifier.height(10.dp));service.options.forEach{o->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){Text(o,Modifier.weight(1f));Button(onClick={info=rwDetails[o]?:"More details coming soon."}){Text("Open")}}}};if(info.isNotBlank())Card(Modifier.fillMaxWidth()){Text(info,Modifier.padding(14.dp))}
        contact?.let{c->Spacer(Modifier.height(10.dp));Card(Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp)){Text("Service Contact",fontWeight=FontWeight.Bold);if(c.name.isNotBlank())Text(c.name);if(c.phone.isNotBlank())Text("📞 ${c.phone}");if(c.email.isNotBlank())Text("✉️ ${c.email}");if(c.whatsapp.isNotBlank())Text("WhatsApp: ${c.whatsapp}")}}}
        if(service.key=="astrology"){Spacer(Modifier.height(10.dp));Text("Personal Astrology Details",fontWeight=FontWeight.Bold);OutlinedTextField(dob,{dob=it},label={Text("Date of birth YYYY-MM-DD")},modifier=Modifier.fillMaxWidth());OutlinedTextField(bt,{bt=it},label={Text("Birth time HH:MM")},modifier=Modifier.fillMaxWidth());OutlinedTextField(place,{place=it},label={Text("Birth place")},modifier=Modifier.fillMaxWidth());Button(onClick={if(dob.isBlank()||bt.isBlank()||place.isBlank())msg="Fill all birth details." else mainPost("astrology_requests",JSONObject().put("date_of_birth",dob).put("birth_time",bt).put("birth_place",place).put("request_type","kundli")){msg=if(it)"Astrology request submitted." else "Submission failed."}},modifier=Modifier.fillMaxWidth()){Text("Submit Astrology Request")}}
        if(service.key=="shopping"){Spacer(Modifier.height(10.dp));Text("Online Products",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);products.forEach{p->Card(Modifier.fillMaxWidth().padding(vertical=6.dp)){Column{RemoteImage(p.imageUrl);Column(Modifier.padding(12.dp)){Text(p.name,fontWeight=FontWeight.Bold);Text(p.description,style=MaterialTheme.typography.bodySmall);Text(p.price,fontWeight=FontWeight.Bold);p.externalUrl?.let{u->Button(onClick={context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(u)))}){Text("Open")}}}}}}}
        if(msg.isNotBlank())Text(msg);Spacer(Modifier.height(12.dp));Button(onClick=book,modifier=Modifier.fillMaxWidth()){Text("Request Booking / Quotation")}
    }
}

@Composable private fun RWBookingScreen(service:RWService,back:()->Unit){
    val context=LocalContext.current;val p=context.getSharedPreferences(RW_PREFS,Context.MODE_PRIVATE);var name by remember{mutableStateOf(p.getString("name","")?:"")};var mobile by remember{mutableStateOf(p.getString("mobile","")?:"")};var city by remember{mutableStateOf(p.getString("city","")?:"")};var date by remember{mutableStateOf("")};var note by remember{mutableStateOf("")};var msg by remember{mutableStateOf("")}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){TextButton(onClick=back){Text("← Back")};Text("Booking / Quotation",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text(service.title);OutlinedTextField(name,{name=it},label={Text("Name")},modifier=Modifier.fillMaxWidth());OutlinedTextField(mobile,{mobile=it},label={Text("Mobile")},modifier=Modifier.fillMaxWidth());OutlinedTextField(city,{city=it},label={Text("City")},modifier=Modifier.fillMaxWidth());OutlinedTextField(date,{date=it},label={Text("Preferred date YYYY-MM-DD")},modifier=Modifier.fillMaxWidth());OutlinedTextField(note,{note=it},label={Text("Requirement")},modifier=Modifier.fillMaxWidth());Button(onClick={if(name.isBlank()||mobile.isBlank()||city.isBlank())msg="Enter name, mobile and city." else{val j=JSONObject().put("service",service.title).put("customer_name",name).put("mobile",mobile).put("city",city).put("source","android-v2");if(Regex("\\d{4}-\\d{2}-\\d{2}").matches(date))j.put("preferred_date",date);if(note.isNotBlank())j.put("requirement",note);mainPost("bookings",j){ok->msg=if(ok)"✅ Request submitted online." else "Submission failed."}}},modifier=Modifier.fillMaxWidth()){Text("Submit Request")};if(msg.isNotBlank())Text(msg)}
}

@Composable private fun RWLocalBookings(){Column(Modifier.fillMaxSize().padding(16.dp)){Text("My Bookings",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("Online requests are managed from the Admin dashboard.")}}

@Composable private fun RWProfile(){
    val c=LocalContext.current;val p=c.getSharedPreferences(RW_PREFS,Context.MODE_PRIVATE);var name by remember{mutableStateOf(p.getString("name","")?:"")};var mobile by remember{mutableStateOf(p.getString("mobile","")?:"")};var email by remember{mutableStateOf(p.getString("email","")?:"")};var city by remember{mutableStateOf(p.getString("city","")?:"")};var msg by remember{mutableStateOf("")}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){Text("Profile",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);OutlinedTextField(name,{name=it},label={Text("Name")},modifier=Modifier.fillMaxWidth());OutlinedTextField(mobile,{mobile=it},label={Text("Mobile")},modifier=Modifier.fillMaxWidth());OutlinedTextField(email,{email=it},label={Text("Email")},modifier=Modifier.fillMaxWidth());OutlinedTextField(city,{city=it},label={Text("City")},modifier=Modifier.fillMaxWidth());Button(onClick={p.edit().putString("name",name).putString("mobile",mobile).putString("email",email).putString("city",city).apply();msg="Profile saved."},modifier=Modifier.fillMaxWidth()){Text("Save Profile")};if(msg.isNotBlank())Text(msg)}
}

@Composable private fun RWAdmin(){
    val context=LocalContext.current;val prefs=context.getSharedPreferences(RW_PREFS,Context.MODE_PRIVATE);var email by remember{mutableStateOf(prefs.getString("admin_email","")?:"")};var password by remember{mutableStateOf("")};var token by remember{mutableStateOf(prefs.getString("admin_token","")?.takeIf{it.isNotBlank()})};var refresh by remember{mutableStateOf(prefs.getString("admin_refresh","")?.takeIf{it.isNotBlank()})};var data by remember{mutableStateOf<RWAdminData?>(null)};var msg by remember{mutableStateOf("")};var photoUri by remember{mutableStateOf<Uri?>(null)};var productName by remember{mutableStateOf("")};var category by remember{mutableStateOf("Astrology Products")};var price by remember{mutableStateOf("")};var description by remember{mutableStateOf("")};var externalUrl by remember{mutableStateOf("")};var serviceKey by remember{mutableStateOf("astrology")};var contactName by remember{mutableStateOf("")};var phone by remember{mutableStateOf("")};var contactEmail by remember{mutableStateOf("")};var whatsapp by remember{mutableStateOf("")}
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){photoUri=it}
    fun refreshDashboard(){val t=token?:return;fetchAdmin(t){d->data=d;msg=if(d!=null)"Dashboard updated." else "Session expired."}}
    LaunchedEffect(Unit){if(token!=null)refreshDashboard() else if(refresh!=null){authRefresh(refresh!!){a,r->if(a!=null){token=a;refresh=r?:refresh;prefs.edit().putString("admin_token",a).putString("admin_refresh",refresh).apply();refreshDashboard()}}}}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){Text("🔐 Rawalworld Admin",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Spacer(Modifier.height(10.dp));if(token==null){OutlinedTextField(email,{email=it},label={Text("Admin email")},modifier=Modifier.fillMaxWidth());OutlinedTextField(password,{password=it},label={Text("Password")},modifier=Modifier.fillMaxWidth());Button(onClick={authLogin(email.trim(),password){a,r,m->msg=m;if(a!=null){token=a;refresh=r;prefs.edit().putString("admin_email",email.trim()).putString("admin_token",a).putString("admin_refresh",r?:"").apply();refreshDashboard()}}},modifier=Modifier.fillMaxWidth()){Text("Login")};OutlinedButton(onClick={if(email.isBlank())msg="Enter admin email first." else forgotPassword(email.trim()){msg=if(it)"Password reset email sent." else "Could not send reset email."}},modifier=Modifier.fillMaxWidth()){Text("Forgot Password")}}
        else{data?.let{d->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Text("Bookings ${d.bookings.size}");Text("Astrology ${d.astrology.size}");Text("Products ${d.products}");Text("Orders ${d.orders.size}")};Spacer(Modifier.height(10.dp));Text("Booking Management",fontWeight=FontWeight.Bold);d.bookings.forEach{b->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(10.dp)){Text("${b.service} • ${b.name}");Text("${b.mobile} • ${b.city} • ${b.status}",style=MaterialTheme.typography.bodySmall);Row{TextButton(onClick={adminWrite(token!!,"bookings?id=eq.${b.id}","PATCH",JSONObject().put("status","contacted")){refreshDashboard()}}){Text("Contacted")};TextButton(onClick={adminWrite(token!!,"bookings?id=eq.${b.id}","PATCH",JSONObject().put("status","completed")){refreshDashboard()}}){Text("Complete")}}}}};Text("Astrology Management",fontWeight=FontWeight.Bold);d.astrology.forEach{a->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(10.dp)){Text("${a.type} • ${a.place}");Text(a.status);Row{TextButton(onClick={adminWrite(token!!,"astrology_requests?id=eq.${a.id}","PATCH",JSONObject().put("status","reviewing")){refreshDashboard()}}){Text("Review")};TextButton(onClick={adminWrite(token!!,"astrology_requests?id=eq.${a.id}","PATCH",JSONObject().put("status","completed")){refreshDashboard()}}){Text("Complete")}}}}}}
            Spacer(Modifier.height(14.dp));Text("Add Product",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);OutlinedTextField(productName,{productName=it},label={Text("Product name")},modifier=Modifier.fillMaxWidth());OutlinedTextField(category,{category=it},label={Text("Category")},modifier=Modifier.fillMaxWidth());OutlinedTextField(price,{price=it},label={Text("Price (INR)")},modifier=Modifier.fillMaxWidth());OutlinedTextField("INR",{},enabled=false,label={Text("Currency")},modifier=Modifier.fillMaxWidth());OutlinedTextField(description,{description=it},label={Text("Description")},modifier=Modifier.fillMaxWidth());OutlinedTextField(externalUrl,{externalUrl=it},label={Text("External link (optional)")},modifier=Modifier.fillMaxWidth());Button(onClick={picker.launch("image/*")},modifier=Modifier.fillMaxWidth()){Text(if(photoUri==null)"Choose Product Photo" else "Photo Selected ✓")};Button(onClick={val t=token;if(t==null)return@Button;if(productName.isBlank()){msg="Enter product name.";return@Button};val u=photoUri;if(u==null){msg="Product photo is required.";return@Button};msg="Uploading photo...";uploadPhoto(context,t,u,productName){imageUrl->if(imageUrl==null){msg="Photo upload failed."}else{val amount=price.toDoubleOrNull()?:0.0;val j=JSONObject().put("name",productName.trim()).put("category",category.trim()).put("description",description.trim()).put("price",amount).put("currency","INR").put("image_url",imageUrl).put("external_url",externalUrl.trim().takeIf{it.isNotBlank()}).put("is_free",amount==0.0).put("is_active",true);adminWrite(t,"products","POST",j){ok->msg=if(ok)"Product added with photo in INR." else "Could not add product.";if(ok){productName="";price="";description="";externalUrl="";photoUri=null;refreshDashboard()}}}}},modifier=Modifier.fillMaxWidth()){Text("Add Product")}
            Spacer(Modifier.height(14.dp));Text("Service Contact Details",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);var expanded by remember{mutableStateOf(false)};Box{Button(onClick={expanded=true}){Text(rwServices.first{it.key==serviceKey}.title)};DropdownMenu(expanded=expanded,onDismissRequest={expanded=false}){rwServices.forEach{s->DropdownMenuItem(text={Text(s.title)},onClick={serviceKey=s.key;expanded=false;fetchContact(s.key){c->contactName=c?.name?:"";phone=c?.phone?:"";contactEmail=c?.email?:"";whatsapp=c?.whatsapp?:""}})}}};OutlinedTextField(contactName,{contactName=it},label={Text("Contact name")},modifier=Modifier.fillMaxWidth());OutlinedTextField(phone,{phone=it},label={Text("Phone")},modifier=Modifier.fillMaxWidth());OutlinedTextField(contactEmail,{contactEmail=it},label={Text("Email")},modifier=Modifier.fillMaxWidth());OutlinedTextField(whatsapp,{whatsapp=it},label={Text("WhatsApp")},modifier=Modifier.fillMaxWidth());Button(onClick={val t=token?:return@Button;val service=rwServices.first{it.key==serviceKey};val j=JSONObject().put("service_name",service.title).put("contact_name",contactName).put("phone",phone).put("email",contactEmail).put("whatsapp",whatsapp).put("is_active",true);adminWrite(t,"service_contacts?service_key=eq.$serviceKey","PATCH",j){msg=if(it)"Service contact saved." else "Could not save contact."}},modifier=Modifier.fillMaxWidth()){Text("Save Service Contact")};Spacer(Modifier.height(10.dp));Button(onClick={refreshDashboard()},modifier=Modifier.fillMaxWidth()){Text("Refresh Dashboard")};OutlinedButton(onClick={prefs.edit().remove("admin_token").remove("admin_refresh").apply();token=null;refresh=null;data=null;msg="Logged out."},modifier=Modifier.fillMaxWidth()){Text("Logout")}}
        if(msg.isNotBlank()){Spacer(Modifier.height(8.dp));Text(msg)}
    }
}
