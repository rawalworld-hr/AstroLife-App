from pathlib import Path

p = Path('app/src/main/java/com/astrolife/app/MainActivityV2.kt')
s = p.read_text(encoding='utf-8')

if 'import android.content.Intent\n' not in s:
    s = s.replace('import android.content.Context\n', 'import android.content.Context\nimport android.content.Intent\n')

s = s.replace(
    'RWService("travel","✈️","Tours & Travel","Trips, hotels, visa & transport",listOf("Holiday Packages","Hotels","Flight Enquiry","Visa Assistance","Cab / Vehicle Rental","Group Tours"))',
    'RWService("travel","✈️","Tours & Travel","Flights, trains, hotels, visa & transport",listOf("Flight Search","Flight Schedule","Train Search","Train Schedule","Hotel Booking","Holiday Packages","Visa Assistance","Cab / Vehicle Rental","Group Tours"))'
)

start = s.index('@Composable fun ServiceScreenV2')
end = s.index('@Composable fun BookingFormV2', start)
new_func = r'''@Composable fun ServiceScreenV2(service:RWService,back:()->Unit){
 val context=LocalContext.current
 var contact by remember{mutableStateOf<RWContact?>(null)}
 var selected by remember{mutableStateOf<String?>(null)}
 val travelLinks=mapOf(
  "Flight Search" to "https://www.google.com/travel/flights",
  "Flight Schedule" to "https://www.flightstats.com/v2/flight-tracker/search",
  "Train Search" to "https://www.irctc.co.in/nget/train-search",
  "Train Schedule" to "https://enquiry.indianrail.gov.in/mntes/",
  "Hotel Booking" to "https://www.google.com/travel/hotels"
 )
 LaunchedEffect(service.id){fetchContact(service.id){contact=it}}
 Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())){
  TextButton(onClick=back){Text("← Back")}
  Text("${service.icon} ${service.title}",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
  Text(service.desc)
  contact?.let{c->
   if(listOf(c.contactName,c.phone,c.email,c.whatsapp).any{it.isNotBlank()}){
    Card(Modifier.fillMaxWidth().padding(vertical=10.dp)){
     Column(Modifier.padding(12.dp)){
      Text("Service Contact",fontWeight=FontWeight.Bold)
      if(c.contactName.isNotBlank())Text(c.contactName)
      if(c.phone.isNotBlank())Text("📞 ${c.phone}")
      if(c.email.isNotBlank())Text("✉ ${c.email}")
      if(c.whatsapp.isNotBlank())Text("WhatsApp ${c.whatsapp}")
     }
    }
   }
  }
  service.options.forEach{o->
   Card(Modifier.fillMaxWidth().padding(vertical=5.dp)){
    Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){
     Text(o,Modifier.weight(1f))
     Button(onClick={
      val url=if(service.id=="travel") travelLinks[o] else null
      if(url!=null){
       try{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))}catch(_:Exception){}
      }else selected=o
     }){Text(if(service.id=="travel" && travelLinks.containsKey(o)) "Search Online" else "Open")}
    }
   }
  }
  selected?.let{
   Card(Modifier.fillMaxWidth().padding(top=10.dp)){
    Column(Modifier.padding(14.dp)){
     Text(it,fontWeight=FontWeight.Bold)
     Text("Detailed information, booking and consultation support for $it.")
    }
   }
  }
  if(service.id=="travel"){
   Card(Modifier.fillMaxWidth().padding(top=10.dp)){
    Column(Modifier.padding(14.dp)){
     Text("Online Travel Search",fontWeight=FontWeight.Bold)
     Text("Search current domestic and international flights, Indian trains, schedules and hotels using the online buttons above.")
    }
   }
  }
  BookingFormV2(service.title)
 }
}

'''
s = s[:start] + new_func + s[end:]
p.write_text(s, encoding='utf-8')
