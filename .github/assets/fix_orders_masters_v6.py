from pathlib import Path
p=Path('app/src/main/java/com/astrolife/app/MainActivityV2.kt')
s=p.read_text(encoding='utf-8')

# Fix orders payload field name to match Supabase schema.
s=s.replace('.put("address",address)', '.put("delivery_address",address)')

# Add a dedicated authenticated master manager with reliable edit/delete actions.
anchor='@Composable fun SimpleDropdown('
if 'MasterManagerV6' not in s and anchor in s:
    comp=r'''@Composable fun MasterManagerV6(session:RWSession,onMessage:(String)->Unit){
    var rows by remember{mutableStateOf<List<RWMasterV5>>(emptyList())}
    var refresh by remember{mutableStateOf(0)}
    LaunchedEffect(refresh){
        Thread{
            val out=mutableListOf<RWMasterV5>()
            try{
                val a=getArray("masters?select=id,master_type,name&order=master_type.asc,name.asc",session.access)
                for(i in 0 until a.length()){
                    val x=a.getJSONObject(i)
                    out+=RWMasterV5(x.optString("id"),x.optString("master_type"),x.optString("name"))
                }
            }catch(_:Exception){}
            ui{rows=out}
        }.start()
    }
    Text("Manage Existing Masters",fontWeight=FontWeight.Bold)
    if(rows.isEmpty()) Text("No masters found.",style=MaterialTheme.typography.bodySmall)
    rows.forEach{m->
        var editing by remember(m.id){mutableStateOf(false)}
        var name by remember(m.id){mutableStateOf(m.name)}
        Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){
            Column(Modifier.padding(10.dp)){
                Text(m.type,style=MaterialTheme.typography.bodySmall,color=Purple)
                if(editing) OutlinedTextField(name,{name=it},label={Text("Master name")},modifier=Modifier.fillMaxWidth())
                else Text(m.name,fontWeight=FontWeight.Bold)
                Row{
                    Button(onClick={
                        if(editing){
                            Thread{
                                val ok=try{writeJson("masters?id=eq.${m.id}","PATCH",JSONObject().put("name",name),session.access)}catch(_:Exception){false}
                                ui{onMessage(if(ok)"Master updated." else "Master update failed.");if(ok){editing=false;refresh++}}
                            }.start()
                        }else editing=true
                    }){Text(if(editing)"Save" else "Edit")}
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick={
                        Thread{
                            val ok=try{
                                val c=conn("$SB_URL/rest/v1/masters?id=eq.${m.id}",session.access)
                                c.requestMethod="DELETE"
                                val r=c.responseCode in 200..299
                                c.disconnect();r
                            }catch(_:Exception){false}
                            ui{onMessage(if(ok)"Master deleted." else "Master delete failed.");if(ok)refresh++}
                        }.start()
                    }){Text("Delete")}
                }
            }
        }
    }
}

'''
    s=s.replace(anchor,comp+anchor)

# Replace any older manager call with V6; otherwise inject after the heading.
s=s.replace('MasterManagerV5(session){msg=it}','MasterManagerV6(session){msg=it}')
needle='Text("MASTER MANAGEMENT",color=Purple,fontWeight=FontWeight.Bold)'
if needle in s and 'MasterManagerV6(session){msg=it}' not in s:
    s=s.replace(needle,needle+';MasterManagerV6(session){msg=it}',1)

p.write_text(s,encoding='utf-8')
