package com.astrolife.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

private const val SB_URL = "https://hcpvuripnlhofxfczyyb.supabase.co"
private const val SB_KEY = "sb_publishable_J8YoD4yenQO-nlEMoC1kvA_3_vJgGjg"
private const val PREFS = "rawalworld_final"
private const val ADMIN_TOKEN = "admin_token"
private val Purple = Color(0xFF6C4DB4)
private val Bg = Color(0xFFFFF8FF)

data class ProductRow(val id:String,val name:String,val category:String,val description:String,val price:Double,val active:Boolean)
data class MasterRow(val id:String,val type:String,val name:String,val active:Boolean)
data class ClientRow(val name:String,val mobile:String,val email:String,val city:String,val address:String,val pincode:String,val source:String)
data class GalleryRow(val title:String,val type:String,val image:String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Purple, background = Bg, surface = Color.White)) {
                RawalworldApp()
            }
        }
    }
}

private fun ui(block:()->Unit)=Handler(Looper.getMainLooper()).post(block)
private fun prefs(c:Context)=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
private fun token(c:Context)=prefs(c).getString(ADMIN_TOKEN,"") ?: ""
private fun saveToken(c:Context,v:String)=prefs(c).edit().putString(ADMIN_TOKEN,v).apply()
private fun conn(url:String,auth:String?=null):HttpURLConnection{
    val c=URL(url).openConnection() as HttpURLConnection
    c.setRequestProperty("apikey",SB_KEY)
    if(!auth.isNullOrBlank()) c.setRequestProperty("Authorization","Bearer $auth")
    return c
}
private fun getArray(path:String,auth:String?=null):JSONArray{
    val c=conn("$SB_URL/rest/v1/$path",auth)
    val code=c.responseCode
    val body=if(code in 200..299)c.inputStream.bufferedReader().use{it.readText()} else c.errorStream?.bufferedReader()?.use{it.readText()}.orEmpty()
    c.disconnect()
    if(code !in 200..299) throw IllegalStateException(body)
    return JSONArray(body)
}
private fun write(path:String,method:String,payload:JSONObject?=null,auth:String?=null):Boolean{
    val c=conn("$SB_URL/rest/v1/$path",auth)
    c.requestMethod=method
    c.setRequestProperty("Content-Type","application/json")
    c.setRequestProperty("Prefer","return=minimal")
    if(payload!=null){c.doOutput=true;c.outputStream.use{it.write(payload.toString().toByteArray())}}
    val ok=c.responseCode in 200..299
    c.disconnect()
    return ok
}
private fun login(email:String,password:String,onDone:(String?,String)->Unit){
    Thread{
        try{
            val c=conn("$SB_URL/auth/v1/token?grant_type=password")
            c.requestMethod="POST";c.setRequestProperty("Content-Type","application/json");c.doOutput=true
            c.outputStream.use{it.write(JSONObject().put("email",email).put("password",password).toString().toByteArray())}
            val code=c.responseCode
            val body=if(code in 200..299)c.inputStream.bufferedReader().use{it.readText()} else c.errorStream?.bufferedReader()?.use{it.readText()}.orEmpty()
            c.disconnect()
            if(code in 200..299){val j=JSONObject(body);ui{onDone(j.optString("access_token"),"Login successful.")}} else ui{onDone(null,"Check email/password or admin access.")}
        }catch(_:Exception){ui{onDone(null,"Unable to connect.")}}
    }.start()
}
private fun forgot(email:String,onDone:(Boolean)->Unit){Thread{val ok=try{val c=conn("$SB_URL/auth/v1/recover");c.requestMethod="POST";c.setRequestProperty("Content-Type","application/json");c.doOutput=true;c.outputStream.use{it.write(JSONObject().put("email",email).toString().toByteArray())};val r=c.responseCode in 200..299;c.disconnect();r}catch(_:Exception){false};ui{onDone(ok)}}.start()}
private fun loadPayment(onDone:(String,String)->Unit){Thread{var payee="Haresh Rawal";var upi="harshrawal1929-1@okicici";try{val a=getArray("app_settings?select=setting_key,setting_value&setting_key=in.(payment_payee_name,payment_upi_id)");for(i in 0 until a.length()){val x=a.getJSONObject(i);when(x.optString("setting_key")){"payment_payee_name"->payee=x.optString("setting_value",payee);"payment_upi_id"->upi=x.optString("setting_value",upi)}}}catch(_:Exception){};ui{onDone(payee,upi)}}.start()}
private fun saveSetting(auth:String,key:String,value:String,onDone:(Boolean)->Unit){Thread{val ok=try{write("app_settings?setting_key=eq.${URLEncoder.encode(key,"UTF-8")}","PATCH",JSONObject().put("setting_value",value),auth)}catch(_:Exception){false};ui{onDone(ok)}}.start()}

@Composable fun RawalworldApp(){
    var tab by remember{mutableStateOf("home")}
    Scaffold(bottomBar={NavigationBar{
        NavigationBarItem(tab=="home",{tab="home"},{Icon(Icons.Default.Home,null)},label={Text("Home")})
        NavigationBarItem(tab=="shop",{tab="shop"},{Icon(Icons.Default.ShoppingCart,null)},label={Text("Shop")})
        NavigationBarItem(tab=="gallery",{tab="gallery"},{Icon(Icons.Default.Photo,null)},label={Text("Gallery")})
        NavigationBarItem(tab=="profile",{tab="profile"},{Icon(Icons.Default.Person,null)},label={Text("Profile")})
        NavigationBarItem(tab=="admin",{tab="admin"},{Icon(Icons.Default.Lock,null)},label={Text("Admin")})
    }}){p->Box(Modifier.fillMaxSize().padding(p)){when(tab){"shop"->ShopScreen();"gallery"->GalleryScreen();"profile"->ProfileScreen();"admin"->AdminScreen();else->HomeScreen{tab=it}}}}
}

@Composable fun HomeScreen(go:(String)->Unit){
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
        Row(verticalAlignment=Alignment.CenterVertically){Image(painterResource(R.drawable.rawalworld_ganeshji_final),"Ganeshji Rawalworld",Modifier.size(82.dp),contentScale=ContentScale.Fit);Spacer(Modifier.width(12.dp));Column{Text("Rawalworld",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold);Text("Gujarat lifestyle & services")}}
        Spacer(Modifier.height(14.dp))
        Card(colors=CardDefaults.cardColors(containerColor=Purple)){Column(Modifier.padding(18.dp)){Text("Everything you need, in one app.",color=Color.White,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("Astrology • Events • Travel • Shopping • Gallery",color=Color.White)}}
        Spacer(Modifier.height(14.dp))
        listOf("🔮 Astrology","🎉 Events","🌸 Decoration","🍽️ Catering","💼 Consultancy","✈️ Tours & Travel").forEach{Card(Modifier.fillMaxWidth().padding(vertical=5.dp)){Text(it,Modifier.padding(16.dp),fontWeight=FontWeight.Bold)}}
        Button(onClick={go("shop")},Modifier.fillMaxWidth().padding(top=12.dp)){Text("Open Online Shopping")}
        Text("📞 +91 77093 78969   ✉ rawalworld@gmail.com",Modifier.padding(top=18.dp),style=MaterialTheme.typography.bodySmall)
    }
}

@Composable fun ShopScreen(){
    val context=LocalContext.current
    var products by remember{mutableStateOf<List<ProductRow>>(emptyList())}
    var loading by remember{mutableStateOf(true)}
    var selected by remember{mutableStateOf<ProductRow?>(null)}
    LaunchedEffect(Unit){Thread{val out=mutableListOf<ProductRow>();try{val a=getArray("products?select=id,name,category,description,price,is_active&is_active=eq.true&order=created_at.desc");for(i in 0 until a.length()){val x=a.getJSONObject(i);out+=ProductRow(x.optString("id"),x.optString("name"),x.optString("category"),x.optString("description"),x.optDouble("price"),true)}}catch(_:Exception){};ui{products=out;loading=false}}.start()}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
        Text("🛍️ Online Shopping",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
        Text("Products are shown from your Admin database.")
        if(loading) CircularProgressIndicator(Modifier.padding(16.dp))
        if(!loading&&products.isEmpty()) Text("No active products found.",Modifier.padding(top=14.dp))
        products.forEach{p->Card(Modifier.fillMaxWidth().padding(vertical=6.dp)){Column(Modifier.padding(14.dp)){Text(p.name,fontWeight=FontWeight.Bold);Text(p.category,style=MaterialTheme.typography.bodySmall);if(p.description.isNotBlank())Text(p.description);Text("₹ ${String.format("%.2f",p.price)}",fontWeight=FontWeight.Bold);Button(onClick={selected=p},Modifier.fillMaxWidth().padding(top=8.dp)){Text("Buy Now")}}}}
    }
    selected?.let{p->CheckoutDialog(context,p,{selected=null})}
}

@Composable fun CheckoutDialog(context:Context,p:ProductRow,onClose:()->Unit){
    var qty by remember{mutableStateOf("1")};var name by remember{mutableStateOf("")};var mobile by remember{mutableStateOf("")};var address by remember{mutableStateOf("")};var pincode by remember{mutableStateOf("")};var msg by remember{mutableStateOf("")};var busy by remember{mutableStateOf(false)}
    AlertDialog(onDismissRequest=onClose,title={Text("Checkout — ${p.name}")},text={Column(Modifier.verticalScroll(rememberScrollState())){
        OutlinedTextField(qty,{qty=it.filter(Char::isDigit)},label={Text("Quantity")},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(name,{name=it},label={Text("Purchaser name")},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(mobile,{mobile=it},label={Text("Mobile")},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(address,{address=it},label={Text("Delivery address")},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(pincode,{pincode=it},label={Text("Pincode")},modifier=Modifier.fillMaxWidth())
        val q=qty.toIntOrNull()?:1
        Text("Total: ₹ ${String.format("%.2f",p.price*q)}",fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=8.dp))
        if(msg.isNotBlank())Text(msg,Modifier.padding(top=6.dp))
    }},confirmButton={Button(enabled=!busy,onClick={
        val q=(qty.toIntOrNull()?:0).coerceAtLeast(1)
        if(name.isBlank()||mobile.isBlank()||address.isBlank()||pincode.isBlank()){msg="Please complete all delivery details.";return@Button}
        busy=true;val total=p.price*q
        Thread{
            val orderOk=try{write("orders","POST",JSONObject().put("product_id",p.id).put("product_name",p.name).put("quantity",q).put("unit_price",p.price).put("total_amount",total).put("currency","INR").put("customer_name",name).put("mobile",mobile).put("delivery_address",address).put("pincode",pincode).put("payment_method","UPI / GPay").put("payment_status","pending").put("order_status","submitted"))}catch(_:Exception){false}
            ui{
                if(!orderOk){busy=false;msg="Could not submit order."}
                else loadPayment{payee,upi->busy=false;try{val uri=Uri.parse("upi://pay?pa=${Uri.encode(upi)}&pn=${Uri.encode(payee)}&am=${String.format("%.2f",total)}&cu=INR&tn=${Uri.encode(p.name)}");context.startActivity(Intent(Intent.ACTION_VIEW,uri));msg="Order submitted. Opening UPI / GPay…"}catch(_:Exception){msg="Order submitted. UPI app could not open."}}
            }
        }.start()
    }){Text("Continue to Payment")}},dismissButton={TextButton(onClick=onClose){Text("Close")}})
}

@Composable fun GalleryScreen(){
    var rows by remember{mutableStateOf<List<GalleryRow>>(emptyList())}
    LaunchedEffect(Unit){Thread{val out=mutableListOf<GalleryRow>();try{val a=getArray("gallery?select=title,gallery_type,image_url&is_active=eq.true&order=created_at.desc");for(i in 0 until a.length()){val x=a.getJSONObject(i);out+=GalleryRow(x.optString("title"),x.optString("gallery_type"),x.optString("image_url"))}}catch(_:Exception){};ui{rows=out}}.start()}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){Text("📷 Gallery",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);if(rows.isEmpty())Text("No gallery photos found.") else rows.forEach{Card(Modifier.fillMaxWidth().padding(vertical=5.dp)){Column(Modifier.padding(14.dp)){Text(it.title,fontWeight=FontWeight.Bold);Text(it.type,style=MaterialTheme.typography.bodySmall)}}}}
}

@Composable fun ProfileScreen(){
    val c=LocalContext.current
    var n by remember{mutableStateOf(prefs(c).getString("name","")?:"")};var m by remember{mutableStateOf(prefs(c).getString("mobile","")?:"")};var msg by remember{mutableStateOf("")}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){Text("👤 Profile",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);OutlinedTextField(n,{n=it},label={Text("Name")},modifier=Modifier.fillMaxWidth());OutlinedTextField(m,{m=it},label={Text("Mobile")},modifier=Modifier.fillMaxWidth());Button(onClick={prefs(c).edit().putString("name",n).putString("mobile",m).apply();msg="Profile saved."},Modifier.fillMaxWidth().padding(top=10.dp)){Text("Save Profile")};if(msg.isNotBlank())Text(msg)}
}

@Composable fun AdminScreen(){
    val context=LocalContext.current
    var auth by remember{mutableStateOf(token(context))}
    if(auth.isBlank()) AdminLogin(context){auth=it} else AdminDashboard(context,auth){saveToken(context,"");auth=""}
}

@Composable fun AdminLogin(context:Context,onLogin:(String)->Unit){
    var email by remember{mutableStateOf("")};var pass by remember{mutableStateOf("")};var msg by remember{mutableStateOf("")}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
        Text("🔐 Rawalworld Admin",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
        OutlinedTextField(email,{email=it},label={Text("Email")},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(pass,{pass=it},label={Text("Password")},modifier=Modifier.fillMaxWidth())
        Button(onClick={login(email.trim(),pass){t,m->msg=m;if(!t.isNullOrBlank()){saveToken(context,t);onLogin(t)}}},Modifier.fillMaxWidth().padding(top=10.dp)){Text("Login")}
        TextButton(onClick={if(email.isBlank())msg="Enter email first." else forgot(email.trim()){msg=if(it)"Password reset email sent." else "Could not send reset email."}},Modifier.fillMaxWidth()){Text("Forgot Password")}
        if(msg.isNotBlank())Text(msg)
    }
}

@Composable fun AdminDashboard(context:Context,auth:String,logout:()->Unit){
    var products by remember{mutableStateOf<List<ProductRow>>(emptyList())}
    var masters by remember{mutableStateOf<List<MasterRow>>(emptyList())}
    var clients by remember{mutableStateOf<List<ClientRow>>(emptyList())}
    var payee by remember{mutableStateOf("Haresh Rawal")};var upi by remember{mutableStateOf("harshrawal1929-1@okicici")};var msg by remember{mutableStateOf("")};var refresh by remember{mutableStateOf(0)}
    var editProduct by remember{mutableStateOf<ProductRow?>(null)};var editMaster by remember{mutableStateOf<MasterRow?>(null)}
    fun reload(){refresh++}
    LaunchedEffect(refresh){
        loadPayment{a,b->payee=a;upi=b}
        Thread{
            val ps=mutableListOf<ProductRow>();val ms=mutableListOf<MasterRow>();val cs=mutableListOf<ClientRow>()
            try{val a=getArray("products?select=id,name,category,description,price,is_active&order=created_at.desc",auth);for(i in 0 until a.length()){val x=a.getJSONObject(i);ps+=ProductRow(x.optString("id"),x.optString("name"),x.optString("category"),x.optString("description"),x.optDouble("price"),x.optBoolean("is_active",true))}}catch(_:Exception){}
            try{val a=getArray("masters?select=id,master_type,name,is_active&order=master_type.asc,name.asc",auth);for(i in 0 until a.length()){val x=a.getJSONObject(i);ms+=MasterRow(x.optString("id"),x.optString("master_type"),x.optString("name"),x.optBoolean("is_active",true))}}catch(_:Exception){}
            try{val a=getArray("clients?select=customer_name,mobile,email,city,delivery_address,pincode,source&order=updated_at.desc&limit=200",auth);for(i in 0 until a.length()){val x=a.getJSONObject(i);cs+=ClientRow(x.optString("customer_name"),x.optString("mobile"),x.optString("email"),x.optString("city"),x.optString("delivery_address"),x.optString("pincode"),x.optString("source"))}}catch(_:Exception){}
            ui{products=ps;masters=ms;clients=cs}
        }.start()
    }
    Column(Modifier.fillMaxSize().padding(14.dp).verticalScroll(rememberScrollState())){
        Text("🔐 ADMIN MANAGEMENT",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold)
        Section("PAYMENT MASTER"){
            OutlinedTextField(payee,{payee=it},label={Text("Payee name")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(upi,{upi=it},label={Text("UPI ID / GPay UPI")},modifier=Modifier.fillMaxWidth())
            Button(onClick={if(payee.isBlank()||upi.isBlank()||!upi.contains("@")){msg="Enter valid payment details."}else saveSetting(auth,"payment_payee_name",payee.trim()){a->if(!a)msg="Could not save payee name." else saveSetting(auth,"payment_upi_id",upi.trim()){b->msg=if(b)"✅ Payment Master updated." else "Could not save UPI ID."}}},Modifier.fillMaxWidth()){Text("Save Payment Master")}
        }
        Section("PRODUCT EDIT / DELETE / ACTIVE-INACTIVE"){
            if(products.isEmpty())Text("No products found.")
            products.forEach{p->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(10.dp)){Text(p.name,fontWeight=FontWeight.Bold);Text("${p.category} • ₹ ${String.format("%.2f",p.price)} • ${if(p.active)"ACTIVE" else "INACTIVE"}");Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){OutlinedButton(onClick={editProduct=p},Modifier.weight(1f)){Text("Edit")};OutlinedButton(onClick={Thread{val ok=try{write("products?id=eq.${p.id}","PATCH",JSONObject().put("is_active",!p.active),auth)}catch(_:Exception){false};ui{msg=if(ok)"Product status updated." else "Status update failed.";if(ok)reload()}}.start()},Modifier.weight(1f)){Text(if(p.active)"Inactive" else "Activate")}};OutlinedButton(onClick={Thread{val ok=try{write("products?id=eq.${p.id}","DELETE",null,auth)}catch(_:Exception){false};ui{msg=if(ok)"Product deleted." else "Delete failed.";if(ok)reload()}}.start()},Modifier.fillMaxWidth()){Text("Delete Product")}}}}
        }
        Section("MASTER EDIT / DELETE"){
            if(masters.isEmpty())Text("No masters found.")
            masters.forEach{m->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(10.dp)){Text(m.name,fontWeight=FontWeight.Bold);Text("${m.type} • ${if(m.active)"ACTIVE" else "INACTIVE"}");Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){OutlinedButton(onClick={editMaster=m},Modifier.weight(1f)){Text("Edit")};OutlinedButton(onClick={Thread{val ok=try{write("masters?id=eq.${m.id}","DELETE",null,auth)}catch(_:Exception){false};ui{msg=if(ok)"Master deleted." else "Master delete failed.";if(ok)reload()}}.start()},Modifier.weight(1f)){Text("Delete")}}}}}
        }
        Section("CLIENT RECORDS"){
            Text("${clients.size} customer record(s)",fontWeight=FontWeight.Bold)
            if(clients.isEmpty())Text("No client records yet.")
            clients.forEach{c->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(10.dp)){Text(if(c.name.isBlank())"Client" else c.name,fontWeight=FontWeight.Bold);if(c.mobile.isNotBlank())Text("📞 ${c.mobile}");if(c.email.isNotBlank())Text("✉ ${c.email}");if(c.city.isNotBlank())Text("📍 ${c.city}");if(c.address.isNotBlank())Text(c.address);if(c.pincode.isNotBlank())Text("Pincode: ${c.pincode}");if(c.source.isNotBlank())Text("Source: ${c.source}",style=MaterialTheme.typography.bodySmall)}}}
        }
        Button(onClick={reload()},Modifier.fillMaxWidth()){Text("Refresh Admin")}
        OutlinedButton(onClick=logout,Modifier.fillMaxWidth().padding(top=8.dp)){Text("Logout")}
        if(msg.isNotBlank())Text(msg,Modifier.padding(top=8.dp))
    }
    editProduct?.let{p->ProductEditDialog(p,auth,{editProduct=null;reload()},{editProduct=null})}
    editMaster?.let{m->MasterEditDialog(m,auth,{editMaster=null;reload()},{editMaster=null})}
}

@Composable fun Section(title:String,content:@Composable ColumnScope.()->Unit){
    Text(title,color=Purple,fontWeight=FontWeight.ExtraBold,modifier=Modifier.padding(top=14.dp,bottom=6.dp))
    Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp),content=content)}
}

@Composable fun ProductEditDialog(p:ProductRow,auth:String,onSaved:()->Unit,onClose:()->Unit){
    var name by remember{mutableStateOf(p.name)};var price by remember{mutableStateOf(p.price.toString())};var desc by remember{mutableStateOf(p.description)};var msg by remember{mutableStateOf("")}
    AlertDialog(onDismissRequest=onClose,title={Text("Edit Product")},text={Column{OutlinedTextField(name,{name=it},label={Text("Product name")},modifier=Modifier.fillMaxWidth());OutlinedTextField(price,{price=it},label={Text("Price INR")},modifier=Modifier.fillMaxWidth());OutlinedTextField(desc,{desc=it},label={Text("Description")},modifier=Modifier.fillMaxWidth());if(msg.isNotBlank())Text(msg)}},confirmButton={Button(onClick={val amt=price.toDoubleOrNull();if(name.isBlank()||amt==null){msg="Enter valid name and price."}else Thread{val ok=try{write("products?id=eq.${p.id}","PATCH",JSONObject().put("name",name.trim()).put("price",amt).put("description",desc.trim()),auth)}catch(_:Exception){false};ui{if(ok)onSaved() else msg="Product update failed."}}.start()}){Text("Save")}},dismissButton={TextButton(onClick=onClose){Text("Cancel")}})
}

@Composable fun MasterEditDialog(m:MasterRow,auth:String,onSaved:()->Unit,onClose:()->Unit){
    var name by remember{mutableStateOf(m.name)};var msg by remember{mutableStateOf("")}
    AlertDialog(onDismissRequest=onClose,title={Text("Edit Master")},text={Column{OutlinedTextField(name,{name=it},label={Text("Master name")},modifier=Modifier.fillMaxWidth());if(msg.isNotBlank())Text(msg)}},confirmButton={Button(onClick={if(name.isBlank()){msg="Enter master name."}else Thread{val ok=try{write("masters?id=eq.${m.id}","PATCH",JSONObject().put("name",name.trim()),auth)}catch(_:Exception){false};ui{if(ok)onSaved() else msg="Master update failed."}}.start()}){Text("Save")}},dismissButton={TextButton(onClick=onClose){Text("Cancel")}})
}
