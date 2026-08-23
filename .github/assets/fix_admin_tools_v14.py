from pathlib import Path
p=Path('app/src/main/java/com/astrolife/app/MainActivityV2.kt')
s=p.read_text(encoding='utf-8')

# Normalize any older dynamic payment placeholders to V14.
s=s.replace('${RWUpiV13}','${RWUpiV14}').replace('${RWPayeeV13}','${RWPayeeV14}')
s=s.replace('harshrawal1929-1@okicici','${RWUpiV14}')
s=s.replace('Haresh Rawal','${RWPayeeV14}')

anchor='@Composable fun RawalworldV2()'
helper=r'''private var RWPayeeV14="Haresh Rawal"
private var RWUpiV14="harshrawal1929-1@okicici"

private fun loadPaymentV14(onDone:()->Unit={}){
    Thread{
        try{
            val a=getArray("app_settings?select=setting_key,setting_value&setting_key=in.(payment_payee_name,payment_upi_id)")
            for(i in 0 until a.length()){
                val x=a.getJSONObject(i)
                when(x.optString("setting_key")){
                    "payment_payee_name"->RWPayeeV14=x.optString("setting_value",RWPayeeV14)
                    "payment_upi_id"->RWUpiV14=x.optString("setting_value",RWUpiV14)
                }
            }
        }catch(_:Exception){}
        ui{onDone()}
    }.start()
}

private fun saveSettingV14(key:String,value:String,token:String,onDone:(Boolean)->Unit){
    Thread{
        val ok=try{writeJson("app_settings?setting_key=eq.${URLEncoder.encode(key,"UTF-8")}","PATCH",JSONObject().put("setting_value",value).put("updated_at",java.time.Instant.now().toString()),token)}catch(_:Exception){false}
        ui{onDone(ok)}
    }.start()
}

private fun deleteV14(path:String,token:String,onDone:(Boolean)->Unit){
    Thread{
        val ok=try{val c=conn("$SB_URL/rest/v1/$path",token);c.requestMethod="DELETE";val r=c.responseCode in 200..299;c.disconnect();r}catch(_:Exception){false}
        ui{onDone(ok)}
    }.start()
}

'''
if 'private var RWPayeeV14=' not in s and anchor in s:
    s=s.replace(anchor,helper+anchor,1)

if '@Composable fun RawalworldV2(){' in s and 'loadPaymentV14{}' not in s:
    s=s.replace('@Composable fun RawalworldV2(){','@Composable fun RawalworldV2(){LaunchedEffect(Unit){loadPaymentV14{}};',1)

insert=s.find('@Composable fun SimpleDropdown(')
if insert>=0 and '@Composable fun AdminToolsV14' not in s:
    comp=r'''@Composable fun AdminToolsV14(session:RWSession){
    data class P(val id:String,val name:String,val category:String,val description:String,val price:Double,val active:Boolean)
    data class M(val id:String,val type:String,val name:String,val active:Boolean)
    data class C(val name:String,val mobile:String,val email:String,val city:String,val address:String,val pincode:String,val source:String)
    var products by remember{mutableStateOf<List<P>>(emptyList())}
    var masters by remember{mutableStateOf<List<M>>(emptyList())}
    var clients by remember{mutableStateOf<List<C>>(emptyList())}
    var payee by remember{mutableStateOf(RWPayeeV14)}
    var upi by remember{mutableStateOf(RWUpiV14)}
    var msg by remember{mutableStateOf("")}
    var refresh by remember{mutableStateOf(0)}
    fun reload(){refresh++}
    LaunchedEffect(refresh){
        loadPaymentV14{payee=RWPayeeV14;upi=RWUpiV14}
        Thread{
            val ps=mutableListOf<P>();val ms=mutableListOf<M>();val cs=mutableListOf<C>()
            try{val a=getArray("products?select=id,name,category,description,price,is_active&order=created_at.desc",session.access);for(i in 0 until a.length()){val x=a.getJSONObject(i);ps+=P(x.optString("id"),x.optString("name"),x.optString("category"),x.optString("description"),x.optDouble("price"),x.optBoolean("is_active",true))}}catch(_:Exception){}
            try{val a=getArray("masters?select=id,master_type,name,is_active&order=master_type.asc,name.asc",session.access);for(i in 0 until a.length()){val x=a.getJSONObject(i);ms+=M(x.optString("id"),x.optString("master_type"),x.optString("name"),x.optBoolean("is_active",true))}}catch(_:Exception){}
            try{val a=getArray("clients?select=customer_name,mobile,email,city,delivery_address,pincode,source,is_active&order=updated_at.desc&limit=200",session.access);for(i in 0 until a.length()){val x=a.getJSONObject(i);cs+=C(x.optString("customer_name"),x.optString("mobile"),x.optString("email"),x.optString("city"),x.optString("delivery_address"),x.optString("pincode"),x.optString("source"))}}catch(_:Exception){}
            ui{products=ps;masters=ms;clients=cs}
        }.start()
    }
    Text("ADMIN MANAGEMENT",color=Purple,fontWeight=FontWeight.ExtraBold)
    Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){
        Text("PAYMENT MASTER",fontWeight=FontWeight.Bold)
        OutlinedTextField(payee,{payee=it},label={Text("Payee name")},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(upi,{upi=it},label={Text("UPI ID / GPay UPI")},modifier=Modifier.fillMaxWidth())
        Button(onClick={
            if(payee.isBlank()||upi.isBlank()||!upi.contains("@"))msg="Enter valid payee name and UPI ID."
            else saveSettingV14("payment_payee_name",payee.trim(),session.access){a->if(!a)msg="Could not save payee name." else saveSettingV14("payment_upi_id",upi.trim(),session.access){b->if(b){RWPayeeV14=payee.trim();RWUpiV14=upi.trim();msg="✅ Payment Master updated."}else msg="Could not save UPI ID."}}
        },modifier=Modifier.fillMaxWidth()){Text("Save Payment Master")}
        Text("Current: $RWUpiV14",style=MaterialTheme.typography.bodySmall)
    }}
    Spacer(Modifier.height(12.dp))
    Text("PRODUCT EDIT / ACTIVE / DELETE",color=Purple,fontWeight=FontWeight.Bold)
    if(products.isEmpty()) Text("No products found.")
    products.forEach{p->
        var edit by remember(p.id){mutableStateOf(false)}
        var name by remember(p.id){mutableStateOf(p.name)}
        var price by remember(p.id){mutableStateOf(p.price.toString())}
        var desc by remember(p.id){mutableStateOf(p.description)}
        Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(10.dp)){
            Text("${p.category} • ${if(p.active)"ACTIVE" else "INACTIVE"}",style=MaterialTheme.typography.bodySmall,color=Purple)
            if(edit){OutlinedTextField(name,{name=it},label={Text("Product name")},modifier=Modifier.fillMaxWidth());OutlinedTextField(price,{price=it},label={Text("Price INR")},modifier=Modifier.fillMaxWidth());OutlinedTextField(desc,{desc=it},label={Text("Description")},modifier=Modifier.fillMaxWidth())}else{Text(p.name,fontWeight=FontWeight.Bold);Text("₹ ${String.format("%.2f",p.price)}")}
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){
                OutlinedButton(onClick={if(edit){val amt=price.toDoubleOrNull();if(name.isBlank()||amt==null)msg="Enter valid product name and price." else Thread{val ok=try{writeJson("products?id=eq.${p.id}","PATCH",JSONObject().put("name",name.trim()).put("price",amt).put("description",desc.trim()),session.access)}catch(_:Exception){false};ui{msg=if(ok)"✅ Product updated." else "Product update failed.";if(ok){edit=false;reload()}}}.start()}else edit=true},modifier=Modifier.weight(1f)){Text(if(edit)"Save" else "Edit")}
                OutlinedButton(onClick={Thread{val ok=try{writeJson("products?id=eq.${p.id}","PATCH",JSONObject().put("is_active",!p.active),session.access)}catch(_:Exception){false};ui{msg=if(ok)if(p.active)"Product set inactive." else "Product activated." else "Status update failed.";if(ok)reload()}}.start()},modifier=Modifier.weight(1f)){Text(if(p.active)"Inactive" else "Activate")}
            }
            OutlinedButton(onClick={deleteV14("products?id=eq.${p.id}",session.access){ok->msg=if(ok)"Product deleted." else "Delete failed.";if(ok)reload()}},modifier=Modifier.fillMaxWidth()){Text("Delete Product")}
        }}
    }
    Spacer(Modifier.height(12.dp))
    Text("MASTER EDIT / ACTIVE / DELETE",color=Purple,fontWeight=FontWeight.Bold)
    if(masters.isEmpty()) Text("No masters found.")
    masters.forEach{m->
        var edit by remember(m.id){mutableStateOf(false)}
        var name by remember(m.id){mutableStateOf(m.name)}
        Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(10.dp)){
            Text("${m.type} • ${if(m.active)"ACTIVE" else "INACTIVE"}",style=MaterialTheme.typography.bodySmall,color=Purple)
            if(edit)OutlinedTextField(name,{name=it},label={Text("Master name")},modifier=Modifier.fillMaxWidth()) else Text(m.name,fontWeight=FontWeight.Bold)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){
                OutlinedButton(onClick={if(edit){if(name.isBlank())msg="Enter master name." else Thread{val ok=try{writeJson("masters?id=eq.${m.id}","PATCH",JSONObject().put("name",name.trim()),session.access)}catch(_:Exception){false};ui{msg=if(ok)"✅ Master updated." else "Master update failed.";if(ok){edit=false;reload()}}}.start()}else edit=true},modifier=Modifier.weight(1f)){Text(if(edit)"Save" else "Edit")}
                OutlinedButton(onClick={Thread{val ok=try{writeJson("masters?id=eq.${m.id}","PATCH",JSONObject().put("is_active",!m.active),session.access)}catch(_:Exception){false};ui{msg=if(ok)if(m.active)"Master set inactive." else "Master activated." else "Master status failed.";if(ok)reload()}}.start()},modifier=Modifier.weight(1f)){Text(if(m.active)"Inactive" else "Activate")}
            }
            OutlinedButton(onClick={deleteV14("masters?id=eq.${m.id}",session.access){ok->msg=if(ok)"Master deleted." else "Master delete failed.";if(ok)reload()}},modifier=Modifier.fillMaxWidth()){Text("Delete Master")}
        }}
    }
    Spacer(Modifier.height(12.dp))
    Text("CLIENT RECORDS",color=Purple,fontWeight=FontWeight.Bold)
    Text("${clients.size} customer record(s)",style=MaterialTheme.typography.bodySmall)
    if(clients.isEmpty()) Text("No client records yet. New bookings and orders are added automatically.")
    clients.forEach{c->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(10.dp)){Text(if(c.name.isBlank())"Client" else c.name,fontWeight=FontWeight.Bold);if(c.mobile.isNotBlank())Text("📞 ${c.mobile}");if(c.email.isNotBlank())Text("✉ ${c.email}");if(c.city.isNotBlank())Text("📍 ${c.city}");if(c.address.isNotBlank())Text(c.address);if(c.pincode.isNotBlank())Text("Pincode: ${c.pincode}");if(c.source.isNotBlank())Text("Source: ${c.source}",style=MaterialTheme.typography.bodySmall)}}}
    Button(onClick={reload()},modifier=Modifier.fillMaxWidth()){Text("Refresh Admin Management")}
    if(msg.isNotBlank())Text(msg)
}

'''
    s=s[:insert]+comp+s[insert:]

# Put the reliable V14 block at the very top of the authenticated Admin screen.
start=s.find('@Composable fun AdminDashboardV2(')
end=s.find('@Composable fun SimpleDropdown(',start)
if start>=0 and end>start:
    block=s[start:end]
    heading='Text("🔐 Rawalworld Admin",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold)'
    if heading in block and 'AdminToolsV14(session)' not in block:
        block=block.replace(heading,heading+';Spacer(Modifier.height(10.dp));AdminToolsV14(session);Spacer(Modifier.height(14.dp))',1)
        s=s[:start]+block+s[end:]

p.write_text(s,encoding='utf-8')
