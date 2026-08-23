from pathlib import Path
p=Path('app/src/main/java/com/astrolife/app/MainActivityV2.kt')
s=p.read_text(encoding='utf-8')
if 'import android.content.Intent' not in s:
    s=s.replace('import android.content.Context\n','import android.content.Context\nimport android.content.Intent\n')
if 'data class RWMaster' not in s:
    s=s.replace('data class RWBooking(val service:String,val name:String,val mobile:String,val city:String)\n','data class RWBooking(val service:String,val name:String,val mobile:String,val city:String)\ndata class RWMaster(val id:String,val type:String,val name:String)\n')

# Helpers
anchor='private fun fetchShopMasters(onDone:(List<String>)->Unit)'
idx=s.find(anchor)
if idx>=0 and 'private fun fetchMastersV3' not in s:
    end=s.find('\n\n',idx)
    helper='''\nprivate fun fetchMastersV3(onDone:(List<RWMaster>)->Unit){Thread{val out=mutableListOf<RWMaster>();try{val a=getArray("masters?select=id,master_type,name&order=master_type.asc,name.asc");for(i in 0 until a.length()){val x=a.getJSONObject(i);out+=RWMaster(x.optString("id"),x.optString("master_type"),x.optString("name"))}}catch(_:Exception){};ui{onDone(out)}}.start()}\nprivate fun deleteRowV3(path:String,token:String,onDone:(Boolean)->Unit){Thread{val ok=try{val c=conn("$SB_URL/rest/v1/$path",token);c.requestMethod="DELETE";val r=c.responseCode in 200..299;c.disconnect();r}catch(_:Exception){false};ui{onDone(ok)}}.start()}\nprivate fun patchRowV3(path:String,payload:JSONObject,token:String,onDone:(Boolean)->Unit){Thread{val ok=try{writeJson(path,"PATCH",payload,token)}catch(_:Exception){false};ui{onDone(ok)}}.start()}\n'''
    s=s[:end]+helper+s[end:]

# Replace ShopV2 and ProductCard block
start=s.find('@Composable fun ShopV2()')
end=s.find('@Composable fun GalleryV2()',start)
if start>=0 and end>start:
    replacement=r'''@Composable fun ShopV2(){
    val context=LocalContext.current
    var cats by remember{mutableStateOf(baseServices.last().options)}
    var selected by remember{mutableStateOf<String?>(null)}
    var products by remember{mutableStateOf<List<RWProduct>>(emptyList())}
    var loading by remember{mutableStateOf(false)}
    LaunchedEffect(Unit){fetchShopMasters{cats=it}}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
        Text("🛍️ Online Shopping",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold)
        Text("Choose a category, quantity and delivery details, then pay securely using UPI / GPay.")
        cats.forEach{cat->Button(onClick={selected=cat;loading=true;fetchProducts(cat){products=it;loading=false}},modifier=Modifier.fillMaxWidth().padding(vertical=5.dp)){Text(cat)}}
        selected?.let{Text(it,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);if(loading)CircularProgressIndicator() else if(products.isEmpty())Text("No products added in this category yet.") else products.forEach{p->ShopCheckoutCardV3(p,context)}}
    }
}

@Composable fun ShopCheckoutCardV3(p:RWProduct,context:Context){
    var qtyText by remember(p.id){mutableStateOf("1")}
    var buyer by remember(p.id){mutableStateOf("")}
    var mobile by remember(p.id){mutableStateOf("")}
    var address by remember(p.id){mutableStateOf("")}
    var pincode by remember(p.id){mutableStateOf("")}
    var msg by remember(p.id){mutableStateOf("")}
    val qty=(qtyText.toIntOrNull()?:1).coerceAtLeast(1)
    val total=qty*p.price
    Card(Modifier.fillMaxWidth().padding(vertical=6.dp)){
        Column(Modifier.padding(12.dp)){
            p.imageUrl?.let{RemoteImageV2(it)}
            Text(p.name,fontWeight=FontWeight.Bold)
            Text(p.description,style=MaterialTheme.typography.bodySmall)
            Text(if(p.price==0.0)"FREE" else "₹ ${String.format("%.2f",p.price)} each",fontWeight=FontWeight.Bold)
            OutlinedTextField(qtyText,{qtyText=it.filter(Char::isDigit).ifBlank{"1"}},label={Text("Quantity")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(buyer,{buyer=it},label={Text("Purchaser name")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(mobile,{mobile=it},label={Text("Mobile number")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(address,{address=it},label={Text("Delivery address")},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(pincode,{pincode=it.filter(Char::isDigit)},label={Text("Pincode")},modifier=Modifier.fillMaxWidth())
            Text("Total: ₹ ${String.format("%.2f",total)}",fontWeight=FontWeight.Bold)
            Button(onClick={
                if(buyer.isBlank()||mobile.isBlank()||address.isBlank()||pincode.isBlank()){msg="Enter purchaser name, mobile, address and pincode."}
                else{
                    msg="Submitting order..."
                    val payload=JSONObject().put("product_id",p.id).put("product_name",p.name).put("quantity",qty).put("unit_price",p.price).put("total_amount",total).put("currency","INR").put("customer_name",buyer).put("mobile",mobile).put("address",address).put("pincode",pincode).put("payment_method","UPI / GPay").put("payment_status","pending").put("order_status","submitted")
                    postPublic("orders",payload){ok->msg=if(ok)"✅ Order submitted. Tap Pay with UPI / GPay." else "Could not submit order."}
                }
            },modifier=Modifier.fillMaxWidth()){Text("Purchase")}
            OutlinedButton(onClick={
                val uri=Uri.parse("upi://pay?pa=harshrawal1929-1@okicici&pn=Haresh%20Rawal&am=${String.format("%.2f",total)}&cu=INR&tn=${Uri.encode(p.name)}")
                try{context.startActivity(Intent(Intent.ACTION_VIEW,uri))}catch(_:Exception){msg="No UPI app found on this phone."}
            },modifier=Modifier.fillMaxWidth()){Text("Pay with UPI / GPay")}
            if(msg.isNotBlank())Text(msg,style=MaterialTheme.typography.bodySmall)
            Text("OTP verification will be enabled later.",style=MaterialTheme.typography.bodySmall)
        }
    }
}

'''
    s=s[:start]+replacement+s[end:]

# Replace HomeV2 with language selector
start=s.find('@Composable fun HomeV2(')
end=s.find('@Composable fun ServiceScreenV2(',start)
if start>=0 and end>start:
    replacement=r'''@Composable fun HomeV2(open:(RWService)->Unit){
    val context=LocalContext.current
    val p=prefs(context)
    var lang by remember{mutableStateOf(p.getString("language","English")?:"English")}
    val hero=when(lang){"Gujarati"->"તમારી દરેક જરૂરિયાત, એક જ એપમાં.";"Hindi"->"आपकी हर ज़रूरत, एक ही ऐप में।";"French"->"Tout ce dont vous avez besoin, dans une seule application.";else->"Everything you need, in one app."}
    val sub=when(lang){"Gujarati"->"જ્યોતિષ, ઇવેન્ટ, પ્રવાસ, ખરીદી અને વધુ.";"Hindi"->"ज्योतिष, इवेंट, यात्रा, खरीदारी और बहुत कुछ।";"French"->"Astrologie, événements, voyages, shopping et plus encore.";else->"Astrology • Events • Travel • Shopping • Gallery"}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
        Row(verticalAlignment=Alignment.CenterVertically){Text("🕉️",style=MaterialTheme.typography.displayMedium);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text("Rawalworld",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold);Text("One app. Many services.")};SimpleDropdown("Language",lang,listOf("English","Gujarati","Hindi","French")){lang=it;p.edit().putString("language",it).apply()}}
        Spacer(Modifier.height(12.dp));Card(colors=CardDefaults.cardColors(containerColor=Purple)){Column(Modifier.padding(18.dp)){Text(hero,color=Color.White,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(sub,color=Color.White)}}
        Spacer(Modifier.height(14.dp));baseServices.forEach{s->Card(onClick={open(s)},modifier=Modifier.fillMaxWidth().padding(vertical=5.dp),shape=RoundedCornerShape(18.dp)){Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){Text(s.icon,style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(s.title,fontWeight=FontWeight.Bold);Text(s.desc,style=MaterialTheme.typography.bodySmall)};Text("Open ›",color=Purple,fontWeight=FontWeight.Bold)}}};Text("📞 +91 77093 78969   ✉ rawalworld@gmail.com",style=MaterialTheme.typography.bodySmall)
    }
}

'''
    s=s[:start]+replacement+s[end:]

# Add edit/delete controls to admin by replacing the generated dashboard after the existing fixer runs.
start=s.find('@Composable\nfun AdminDashboardV2(')
if start<0: start=s.find('@Composable fun AdminDashboardV2(')
end=s.find('@Composable fun SimpleDropdown(',start)
if start>=0 and end>start:
    replacement=r'''@Composable
fun AdminDashboardV2(session:RWSession,logout:()->Unit){
    val context=LocalContext.current
    var msg by remember{mutableStateOf("")}
    var productName by remember{mutableStateOf("")};var productCategory by remember{mutableStateOf("Puja Products")};var price by remember{mutableStateOf("")};var description by remember{mutableStateOf("")};var productPhoto by remember{mutableStateOf<Uri?>(null)}
    var categories by remember{mutableStateOf(baseServices.last().options)};var products by remember{mutableStateOf<List<RWProduct>>(emptyList())}
    var galleryTitle by remember{mutableStateOf("")};var galleryType by remember{mutableStateOf("Event")};var galleryCaption by remember{mutableStateOf("")};var galleryPhoto by remember{mutableStateOf<Uri?>(null)};var galleryRows by remember{mutableStateOf<List<RWGallery>>(emptyList())}
    var masterType by remember{mutableStateOf("shop")};var masterName by remember{mutableStateOf("")};var masters by remember{mutableStateOf<List<RWMaster>>(emptyList())}
    var service by remember{mutableStateOf(baseServices.first())};var cn by remember{mutableStateOf("")};var cp by remember{mutableStateOf("")};var ce by remember{mutableStateOf("")};var cw by remember{mutableStateOf("")}
    val pp=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){productPhoto=it};val gp=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){galleryPhoto=it}
    fun refresh(){fetchProducts(){products=it};fetchGallery{galleryRows=it};fetchMastersV3{masters=it};fetchShopMasters{categories=it}}
    fun loadContact(){fetchContact(service.id){c->cn=c?.contactName?:"";cp=c?.phone?:"";ce=c?.email?:"";cw=c?.whatsapp?:""}}
    LaunchedEffect(Unit){refresh();loadContact()};LaunchedEffect(service.id){loadContact()}
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
        Text("🔐 Rawalworld Admin",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold)
        Text("MASTER MANAGEMENT",color=Purple,fontWeight=FontWeight.Bold)
        SimpleDropdown("Master Type",masterType,listOf("shop","service","gallery")){masterType=it};OutlinedTextField(masterName,{masterName=it},label={Text("Master name")},modifier=Modifier.fillMaxWidth());Button(onClick={if(masterName.isNotBlank())Thread{val ok=try{writeJson("masters","POST",JSONObject().put("master_type",masterType).put("name",masterName).put("is_active",true),session.access)}catch(_:Exception){false};ui{msg=if(ok)"Master added." else "Could not add master.";if(ok){masterName="";refresh()}}}.start()},modifier=Modifier.fillMaxWidth()){Text("Add Master")}
        masters.forEach{m->var editing by remember(m.id){mutableStateOf(false)};var mn by remember(m.id){mutableStateOf(m.name)};Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(10.dp)){if(editing)OutlinedTextField(mn,{mn=it},label={Text("Master name")},modifier=Modifier.fillMaxWidth()) else Text("${m.type}: ${m.name}",fontWeight=FontWeight.Bold);Row{Button(onClick={if(editing)patchRowV3("masters?id=eq.${m.id}",JSONObject().put("name",mn),session.access){ok->msg=if(ok)"Master updated." else "Update failed.";if(ok){editing=false;refresh()}} else editing=true}){Text(if(editing)"Save" else "Edit")};Spacer(Modifier.width(8.dp));OutlinedButton(onClick={deleteRowV3("masters?id=eq.${m.id}",session.access){ok->msg=if(ok)"Master deleted." else "Delete failed.";if(ok)refresh()}}){Text("Delete")}}}}}
        Spacer(Modifier.height(12.dp));Text("PRODUCT MANAGEMENT",color=Purple,fontWeight=FontWeight.Bold)
        SimpleDropdown("Category",productCategory,categories){productCategory=it};OutlinedTextField(productName,{productName=it},label={Text("Product name")},modifier=Modifier.fillMaxWidth());OutlinedTextField(price,{price=it},label={Text("Price (INR)")},modifier=Modifier.fillMaxWidth());OutlinedTextField(description,{description=it},label={Text("Description")},modifier=Modifier.fillMaxWidth());OutlinedButton(onClick={pp.launch("image/*")},modifier=Modifier.fillMaxWidth()){Text(if(productPhoto==null)"Choose Product Photo" else "Photo Selected")};Button(onClick={val uri=productPhoto;val amount=price.toDoubleOrNull();if(productName.isBlank()||uri==null||amount==null)msg="Enter product, price and photo." else uploadImage(context,session.access,"product-gallery",uri){url,_ ->if(url!=null)Thread{val ok=try{writeJson("products","POST",JSONObject().put("name",productName).put("category",productCategory).put("description",description).put("price",amount).put("currency","INR").put("image_url",url).put("is_free",amount==0.0).put("is_active",true),session.access)}catch(_:Exception){false};ui{msg=if(ok)"Product added." else "Product save failed.";if(ok){productName="";price="";description="";productPhoto=null;refresh()}}}.start()}},modifier=Modifier.fillMaxWidth()){Text("Add Product")}
        products.forEach{p->var editing by remember(p.id){mutableStateOf(false)};var pn by remember(p.id){mutableStateOf(p.name)};var ppv by remember(p.id){mutableStateOf(p.price.toString())};var pd by remember(p.id){mutableStateOf(p.description)};Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(10.dp)){p.imageUrl?.let{RemoteImageV2(it)};if(editing){OutlinedTextField(pn,{pn=it},label={Text("Name")},modifier=Modifier.fillMaxWidth());OutlinedTextField(ppv,{ppv=it},label={Text("Price INR")},modifier=Modifier.fillMaxWidth());OutlinedTextField(pd,{pd=it},label={Text("Description")},modifier=Modifier.fillMaxWidth())}else{Text(p.name,fontWeight=FontWeight.Bold);Text("₹ ${p.price}")};Row{Button(onClick={if(editing)patchRowV3("products?id=eq.${p.id}",JSONObject().put("name",pn).put("price",ppv.toDoubleOrNull()?:p.price).put("description",pd),session.access){ok->msg=if(ok)"Product updated." else "Update failed.";if(ok){editing=false;refresh()}} else editing=true}){Text(if(editing)"Save" else "Edit")};Spacer(Modifier.width(8.dp));OutlinedButton(onClick={deleteRowV3("products?id=eq.${p.id}",session.access){ok->msg=if(ok)"Product deleted." else "Delete failed.";if(ok)refresh()}}){Text("Delete")}}}}}
        Spacer(Modifier.height(12.dp));Text("PHOTO GALLERY",color=Purple,fontWeight=FontWeight.Bold)
        OutlinedTextField(galleryTitle,{galleryTitle=it},label={Text("Title")},modifier=Modifier.fillMaxWidth());SimpleDropdown("Type",galleryType,listOf("Event","Activity","Achievement","Other")){galleryType=it};OutlinedTextField(galleryCaption,{galleryCaption=it},label={Text("Caption")},modifier=Modifier.fillMaxWidth());OutlinedButton(onClick={gp.launch("image/*")},modifier=Modifier.fillMaxWidth()){Text(if(galleryPhoto==null)"Choose Gallery Photo" else "Photo Selected")};Button(onClick={val uri=galleryPhoto;if(galleryTitle.isBlank()||uri==null)msg="Enter title and photo." else uploadImage(context,session.access,"gallery",uri){url,_ ->if(url!=null)Thread{val ok=try{writeJson("gallery","POST",JSONObject().put("title",galleryTitle).put("gallery_type",galleryType).put("caption",galleryCaption).put("image_url",url).put("is_active",true),session.access)}catch(_:Exception){false};ui{msg=if(ok)"Gallery photo added." else "Gallery save failed.";if(ok){galleryTitle="";galleryCaption="";galleryPhoto=null;refresh()}}}.start()}},modifier=Modifier.fillMaxWidth()){Text("Add Gallery Photo")}
        galleryRows.forEach{g->var editing by remember(g.id){mutableStateOf(false)};var gt by remember(g.id){mutableStateOf(g.title)};Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(10.dp)){RemoteImageV2(g.imageUrl);if(editing)OutlinedTextField(gt,{gt=it},label={Text("Title")},modifier=Modifier.fillMaxWidth()) else Text(g.title,fontWeight=FontWeight.Bold);Row{Button(onClick={if(editing)patchRowV3("gallery?id=eq.${g.id}",JSONObject().put("title",gt),session.access){ok->msg=if(ok)"Gallery updated." else "Update failed.";if(ok){editing=false;refresh()}} else editing=true}){Text(if(editing)"Save" else "Edit")};Spacer(Modifier.width(8.dp));OutlinedButton(onClick={deleteRowV3("gallery?id=eq.${g.id}",session.access){ok->msg=if(ok)"Gallery deleted." else "Delete failed.";if(ok)refresh()}}){Text("Delete")}}}}}
        Spacer(Modifier.height(12.dp));Text("SERVICE CONTACT MASTER",color=Purple,fontWeight=FontWeight.Bold);SimpleDropdown("Service",service.title,baseServices.map{it.title}){name->service=baseServices.first{it.title==name}};OutlinedTextField(cn,{cn=it},label={Text("Contact name")},modifier=Modifier.fillMaxWidth());OutlinedTextField(cp,{cp=it},label={Text("Phone")},modifier=Modifier.fillMaxWidth());OutlinedTextField(ce,{ce=it},label={Text("Email")},modifier=Modifier.fillMaxWidth());OutlinedTextField(cw,{cw=it},label={Text("WhatsApp")},modifier=Modifier.fillMaxWidth());Button(onClick={Thread{val payload=JSONObject().put("service_key",service.id).put("service_name",service.title).put("contact_name",cn).put("phone",cp).put("email",ce).put("whatsapp",cw).put("is_active",true);val ok=try{val c=conn("$SB_URL/rest/v1/service_contacts?on_conflict=service_key",session.access);c.requestMethod="POST";c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Prefer","resolution=merge-duplicates,return=minimal");c.doOutput=true;c.outputStream.use{it.write(payload.toString().toByteArray())};val o=c.responseCode in 200..299;c.disconnect();o}catch(_:Exception){false};ui{msg=if(ok)"Contact saved." else "Could not save contact."}}.start()},modifier=Modifier.fillMaxWidth()){Text("Save Service Contact")}
        Spacer(Modifier.height(8.dp));Text(msg);OutlinedButton(onClick=logout,modifier=Modifier.fillMaxWidth()){Text("Logout")};Spacer(Modifier.height(30.dp))
    }
}

'''
    s=s[:start]+replacement+s[end:]

p.write_text(s,encoding='utf-8')
