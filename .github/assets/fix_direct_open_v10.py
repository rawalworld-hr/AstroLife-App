from pathlib import Path
p=Path('app/src/main/java/com/astrolife/app/MainActivityV2.kt')
s=p.read_text(encoding='utf-8')

anchor='@Composable fun RawalworldV2()'
if 'private fun directUrlV10' not in s and anchor in s:
    helper=r'''private fun directUrlV10(option:String):String?=when(option){
    "Flight Search"->"https://www.google.com/travel/flights"
    "Flight Schedule"->"https://www.flightstats.com/v2/flight-tracker/search"
    "Train Search"->"https://www.irctc.co.in/nget/train-search"
    "Train Schedule"->"https://enquiry.indianrail.gov.in/mntes/"
    "Hotel Booking"->"https://www.google.com/travel/hotels"
    "Latest Gujarat News"->"https://news.google.com/search?q=Gujarat&hl=en-IN&gl=IN&ceid=IN:en"
    "Business News"->"https://news.google.com/search?q=Gujarat%20business&hl=en-IN&gl=IN&ceid=IN:en"
    "Local Updates"->"https://news.google.com/search?q=Gujarat%20local&hl=en-IN&gl=IN&ceid=IN:en"
    "Gujarat Weather"->"https://www.google.com/search?q=Gujarat+weather"
    "Ahmedabad Weather"->"https://www.google.com/search?q=Ahmedabad+weather"
    "Gandhinagar Weather"->"https://www.google.com/search?q=Gandhinagar+weather"
    "Ludo"->"https://www.crazygames.com/game/ludo-king"
    "Solitaire"->"https://www.solitr.com/"
    "Chess"->"https://www.chess.com/play/online"
    "English Songs"->"https://www.youtube.com/results?search_query=english+songs"
    "Hindi Songs"->"https://www.youtube.com/results?search_query=hindi+songs"
    "Gujarati Songs"->"https://www.youtube.com/results?search_query=gujarati+songs"
    "French Songs"->"https://www.youtube.com/results?search_query=french+songs"
    else->null
}

'''
    s=s.replace(anchor,helper+anchor,1)

old='val lang=RWLanguageV5.value;var contact by remember{mutableStateOf<RWContact?>(null)};var selected by remember{mutableStateOf<String?>(null)}'
new='val context=LocalContext.current;val lang=RWLanguageV5.value;var contact by remember{mutableStateOf<RWContact?>(null)};var selected by remember{mutableStateOf<String?>(null)}'
if old in s:
    s=s.replace(old,new,1)

old_button='Button(onClick={selected=o}){Text(trV5(lang,"Open","ખોલો","खोलें","Ouvrir"))}'
new_button='Button(onClick={val url=directUrlV10(o);if(url!=null){try{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))}catch(_:Exception){selected=o}}else selected=o}){Text(trV5(lang,"Open","ખોલો","खोलें","Ouvrir"))}'
if old_button in s:
    s=s.replace(old_button,new_button,1)

p.write_text(s,encoding='utf-8')
