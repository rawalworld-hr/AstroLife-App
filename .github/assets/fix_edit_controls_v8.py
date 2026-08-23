from pathlib import Path
p=Path('app/src/main/java/com/astrolife/app/MainActivityV2.kt')
s=p.read_text(encoding='utf-8')

start=s.find('@Composable fun ProductManagerV7(')
end=s.find('@Composable fun MasterManagerV7(',start)
if start>=0 and end>start:
    rep=r'''@Composable fun ProductManagerV7(session:RWSession,onMessage:(String)->Unit){
    data class AdminP(val id:String,val name:String,val category:String,val price:Double,val active:Boolean)
    var rows by remember{mutableStateOf<List<AdminP>>(emptyList())};var refresh by remember{mutableStateOf(0)}
    LaunchedEffect(refresh){Thread{val out=mutableListOf<AdminP>();try{val a=getArray("products?select=id,name,category,price,is_active&order=created_at.desc",session.access);for(i in 0 until a.length()){val x=a.getJSONObject(i);out+=AdminP(x.optString("id"),x.optString("name"),x.optString("category"),x.optDouble("price"),x.optBoolean("is_active",true))}}catch(_:Exception){};ui{rows=out}}.start()}
    Text("Existing Products",fontWeight=FontWeight.Bold)
    rows.forEach{p->
        var editing by remember(p.id){mutableStateOf(false)}
        var name by remember(p.id){mutableStateOf(p.name)}
        var category by remember(p.id){mutableStateOf(p.category)}
        var price by remember(p.id){mutableStateOf(String.format("%.2f",p.price))}
        Card(Modifier.fillMaxWidth().padding(vertical=5.dp)){Column(Modifier.padding(10.dp)){
            if(editing){
                OutlinedTextField(name,{name=it},label={Text("Product name")},modifier=Modifier.fillMaxWidth())
                OutlinedTextField(category,{category=it},label={Text("Category")},modifier=Modifier.fillMaxWidth())
                OutlinedTextField(price,{price=it},label={Text("Price INR")},modifier=Modifier.fillMaxWidth())
            }else{
                Text(p.name,fontWeight=FontWeight.Bold);Text("${p.category} • ₹ ${String.format("%.2f",p.price)} • ${if(p.active)"Active" else "Inactive"}")
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){
                OutlinedButton(onClick={
                    if(editing){val amount=price.toDoubleOrNull();if(name.isBlank()||category.isBlank()||amount==null)onMessage("Enter valid product details.") else patchV5("products?id=eq.${p.id}",JSONObject().put("name",name).put("category",category).put("price",amount),session.access){ok->onMessage(if(ok)"Product updated." else "Product edit failed.");if(ok){editing=false;refresh++}}}
                    else editing=true
                },modifier=Modifier.weight(1f)){Text(if(editing)"Save" else "Edit")}
                OutlinedButton(onClick={patchV5("products?id=eq.${p.id}",JSONObject().put("is_active",!p.active),session.access){ok->onMessage(if(ok)if(p.active)"Product marked inactive." else "Product activated." else "Status update failed.");if(ok)refresh++}},modifier=Modifier.weight(1f)){Text(if(p.active)"Inactive" else "Activate")}
                OutlinedButton(onClick={deleteV5("products?id=eq.${p.id}",session.access){ok->onMessage(if(ok)"Product deleted." else "Delete failed.");if(ok)refresh++}},modifier=Modifier.weight(1f)){Text("Delete")}
            }
        }}
    }
}

'''
    s=s[:start]+rep+s[end:]

start=s.find('@Composable fun MasterManagerV7(')
end=s.find('@Composable fun ClientRecordsV7(',start)
if start>=0 and end>start:
    rep=r'''@Composable fun MasterManagerV7(session:RWSession,onMessage:(String)->Unit){
    var rows by remember{mutableStateOf<List<RWMasterV5>>(emptyList())};var refresh by remember{mutableStateOf(0)}
    LaunchedEffect(refresh){fetchMastersV5{rows=it}}
    Text("Existing Masters",fontWeight=FontWeight.Bold)
    rows.forEach{m->
        var editing by remember(m.id){mutableStateOf(false)};var name by remember(m.id){mutableStateOf(m.name)}
        Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(10.dp)){
            if(editing)OutlinedTextField(name,{name=it},label={Text("Master name")},modifier=Modifier.fillMaxWidth()) else Text("${m.type}: ${m.name}",fontWeight=FontWeight.Bold)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                OutlinedButton(onClick={if(editing){if(name.isBlank())onMessage("Enter master name.") else patchV5("masters?id=eq.${m.id}",JSONObject().put("name",name),session.access){ok->onMessage(if(ok)"Master updated." else "Master edit failed.");if(ok){editing=false;refresh++}}}else editing=true},modifier=Modifier.weight(1f)){Text(if(editing)"Save" else "Edit")}
                OutlinedButton(onClick={deleteV5("masters?id=eq.${m.id}",session.access){ok->onMessage(if(ok)"Master deleted." else "Delete failed.");if(ok)refresh++}},modifier=Modifier.weight(1f)){Text("Delete")}
            }
        }}
    }
}

'''
    s=s[:start]+rep+s[end:]

p.write_text(s,encoding='utf-8')
