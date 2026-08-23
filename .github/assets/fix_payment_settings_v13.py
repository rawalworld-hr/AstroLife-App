from pathlib import Path
p=Path('app/src/main/java/com/astrolife/app/MainActivityV2.kt')
s=p.read_text(encoding='utf-8')

# Make existing checkout strings use live payment values.
s=s.replace('harshrawal1929-1@okicici','${RWUpiV13}')
s=s.replace('Haresh Rawal','${RWPayeeV13}')

anchor='@Composable fun RawalworldV2()'
if 'RWPayeeV13' not in s and anchor in s:
    helper=r'''private var RWPayeeV13="Haresh Rawal"
private var RWUpiV13="harshrawal1929-1@okicici"

private fun loadPaymentSettingsV13(onDone:()->Unit={}){
    Thread{
        try{
            val a=getArray("app_settings?select=setting_key,setting_value&setting_key=in.(payment_payee_name,payment_upi_id)")
            for(i in 0 until a.length()){
                val x=a.getJSONObject(i)
                when(x.optString("setting_key")){
                    "payment_payee_name"->RWPayeeV13=x.optString("setting_value",RWPayeeV13)
                    "payment_upi_id"->RWUpiV13=x.optString("setting_value",RWUpiV13)
                }
            }
        }catch(_:Exception){}
        ui{onDone()}
    }.start()
}

private fun savePaymentSettingV13(key:String,value:String,token:String,onDone:(Boolean)->Unit){
    Thread{
        val ok=try{
            writeJson("app_settings?setting_key=eq.${URLEncoder.encode(key,"UTF-8")}","PATCH",JSONObject().put("setting_value",value).put("updated_at",java.time.Instant.now().toString()),token)
        }catch(_:Exception){false}
        ui{onDone(ok)}
    }.start()
}

'''
    s=s.replace(anchor,helper+anchor,1)

# Load payment settings for customer checkout whenever the app starts.
if '@Composable fun RawalworldV2(){' in s and 'loadPaymentSettingsV13{}' not in s:
    s=s.replace('@Composable fun RawalworldV2(){','@Composable fun RawalworldV2(){LaunchedEffect(Unit){loadPaymentSettingsV13{}};',1)

# Add an authenticated payment settings editor in Admin.
insert=s.find('@Composable fun SimpleDropdown(')
if insert>=0 and '@Composable fun PaymentSettingsV13' not in s:
    comp=r'''@Composable fun PaymentSettingsV13(session:RWSession,onMessage:(String)->Unit){
    var payee by remember{mutableStateOf(RWPayeeV13)}
    var upi by remember{mutableStateOf(RWUpiV13)}
    var loading by remember{mutableStateOf(true)}
    LaunchedEffect(Unit){loadPaymentSettingsV13{payee=RWPayeeV13;upi=RWUpiV13;loading=false}}
    Text("PAYMENT SETTINGS",color=Purple,fontWeight=FontWeight.Bold)
    Card(Modifier.fillMaxWidth()){
        Column(Modifier.padding(12.dp)){
            Text("Change the UPI / GPay account used by customer checkout.",style=MaterialTheme.typography.bodySmall)
            OutlinedTextField(payee,{payee=it},label={Text("Payee name")},modifier=Modifier.fillMaxWidth(),enabled=!loading)
            OutlinedTextField(upi,{upi=it},label={Text("UPI ID / GPay UPI")},modifier=Modifier.fillMaxWidth(),enabled=!loading)
            Button(onClick={
                if(payee.isBlank()||upi.isBlank()||!upi.contains("@")){
                    onMessage("Enter a valid payee name and UPI ID.")
                }else{
                    savePaymentSettingV13("payment_payee_name",payee.trim(),session.access){ok1->
                        if(!ok1){onMessage("Could not save payee name.")}
                        else savePaymentSettingV13("payment_upi_id",upi.trim(),session.access){ok2->
                            if(ok2){RWPayeeV13=payee.trim();RWUpiV13=upi.trim();onMessage("✅ Payment settings updated for customer checkout.")}
                            else onMessage("Could not save UPI ID.")
                        }
                    }
                }
            },modifier=Modifier.fillMaxWidth(),enabled=!loading){Text("Save Payment Settings")}
            Text("Current UPI: $RWUpiV13",style=MaterialTheme.typography.bodySmall)
        }
    }
}

'''
    s=s[:insert]+comp+s[insert:]

# Put the payment editor near the top of Admin dashboard.
needle='Text("🔐 Rawalworld Admin",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold)'
if needle in s and 'PaymentSettingsV13(session){msg=it}' not in s:
    s=s.replace(needle,needle+';Spacer(Modifier.height(10.dp));PaymentSettingsV13(session){msg=it};Spacer(Modifier.height(12.dp))',1)

p.write_text(s,encoding='utf-8')
