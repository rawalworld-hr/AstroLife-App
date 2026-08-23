from pathlib import Path
p=Path('app/src/main/java/com/astrolife/app/MainActivityV2.kt')
s=p.read_text(encoding='utf-8')

# Expand entertainment options to include multilingual song lists.
s=s.replace('RWService("entertainment","🎮","Entertainment","Games and fun",listOf("Ludo","Solitaire"))','RWService("entertainment","🎮","Entertainment","Offline games and multilingual music",listOf("Ludo","Solitaire","English Songs","Hindi Songs","Gujarati Songs","French Songs"))')

# Add offline entertainment composable before admin dropdown helpers.
insert=s.find('@Composable fun SimpleDropdown(')
if insert>=0 and '@Composable fun EntertainmentV9' not in s:
    comp=r'''@Composable fun EntertainmentV9(option:String){
    val lang=RWLanguageV5.value
    when(option){
        "Ludo"->{
            var p1 by remember{mutableStateOf(0)};var p2 by remember{mutableStateOf(0)};var turn by remember{mutableStateOf(1)};var msg by remember{mutableStateOf(trV5(lang,"Roll the dice to start.","શરૂ કરવા ડાઇસ ફેંકો.","शुरू करने के लिए पासा फेंकें।","Lancez le dé pour commencer."))}
            Card(Modifier.fillMaxWidth().padding(top=10.dp)){Column(Modifier.padding(14.dp)){
                Text("🎲 "+trV5(lang,"Offline Ludo","ઓફલાઇન લુડો","ऑफलाइन लूडो","Ludo hors ligne"),fontWeight=FontWeight.Bold)
                Text(trV5(lang,"Two-player dice race. First to 30 wins.","બે ખેલાડીની ડાઇસ રેસ. 30 સુધી પહોંચનાર જીતે.","दो खिलाड़ियों की पासा दौड़। 30 तक पहुँचने वाला जीतेगा।","Course à deux joueurs. Le premier à 30 gagne."),style=MaterialTheme.typography.bodySmall)
                Text("Player 1: $p1   •   Player 2: $p2",fontWeight=FontWeight.Bold,modifier=Modifier.padding(vertical=10.dp))
                Text(msg)
                Button(onClick={if(p1<30&&p2<30){val d=(1..6).random();if(turn==1)p1=(p1+d).coerceAtMost(30) else p2=(p2+d).coerceAtMost(30);msg=if(p1>=30||p2>=30)"🏆 Player ${if(p1>=30)1 else 2} wins!" else "Player $turn rolled $d";if(p1<30&&p2<30)turn=if(turn==1)2 else 1}},modifier=Modifier.fillMaxWidth()){Text(if(p1>=30||p2>=30)trV5(lang,"Game Finished","રમત પૂરી","खेल समाप्त","Partie terminée") else trV5(lang,"Roll Dice — Player $turn","ડાઇસ ફેંકો — ખેલાડી $turn","पासा फेंकें — खिलाड़ी $turn","Lancer le dé — Joueur $turn"))}
                OutlinedButton(onClick={p1=0;p2=0;turn=1;msg=trV5(lang,"New game started.","નવી રમત શરૂ થઈ.","नया खेल शुरू हुआ।","Nouvelle partie commencée.")},modifier=Modifier.fillMaxWidth()){Text(trV5(lang,"Restart","ફરી શરૂ કરો","फिर शुरू करें","Recommencer"))}
            }}
        }
        "Solitaire"->{
            val suits=listOf("♠","♥","♦","♣");val ranks=listOf("A","2","3","4","5","6","7","8","9","10","J","Q","K")
            var deck by remember{mutableStateOf((suits.flatMap{suit->ranks.map{rank->"$rank$suit"}}).shuffled())};var index by remember{mutableStateOf(0)};var aces by remember{mutableStateOf(0)}
            Card(Modifier.fillMaxWidth().padding(top=10.dp)){Column(Modifier.padding(14.dp)){
                Text("🃏 "+trV5(lang,"Offline Solitaire","ઓફલાઇન સોલિટેર","ऑफलाइन सॉलिटेयर","Solitaire hors ligne"),fontWeight=FontWeight.Bold)
                Text(trV5(lang,"Draw the shuffled deck and find all four Aces.","શફલ કરેલી ગડીમાંથી ચારેય એસ શોધો.","फेंटी हुई गड्डी से चारों इक्के खोजें।","Piochez le paquet mélangé et trouvez les quatre As."),style=MaterialTheme.typography.bodySmall)
                Text(if(index==0)"🂠" else deck[index-1],style=MaterialTheme.typography.displayMedium,modifier=Modifier.padding(vertical=12.dp))
                Text("Aces: $aces / 4   •   ${index} / ${deck.size}")
                Button(onClick={if(index<deck.size){val c=deck[index];if(c.startsWith("A"))aces++;index++}},modifier=Modifier.fillMaxWidth()){Text(trV5(lang,"Draw Card","કાર્ડ લો","कार्ड निकालें","Piocher une carte"))}
                OutlinedButton(onClick={deck=(suits.flatMap{suit->ranks.map{rank->"$rank$suit"}}).shuffled();index=0;aces=0},modifier=Modifier.fillMaxWidth()){Text(trV5(lang,"Restart","ફરી શરૂ કરો","फिर शुरू करें","Recommencer"))}
            }}
        }
        else->{
            val songs=when(option){
                "English Songs"->listOf("Perfect — Ed Sheeran","Someone Like You — Adele","Shape of You — Ed Sheeran","Blinding Lights — The Weeknd","Counting Stars — OneRepublic")
                "Hindi Songs"->listOf("Kesariya — Arijit Singh","Apna Bana Le — Arijit Singh","Tum Hi Ho — Arijit Singh","Chaleya — Arijit Singh & Shilpa Rao","Heeriye — Jasleen Royal")
                "Gujarati Songs"->listOf("Vhalam Aavo Ne — Jigardan Gadhavi","Chaand Ne Kaho — Jigardan Gadhavi","Gori Radha Ne Kalo Kaan — Kirtidan Gadhvi","Moti Veraana — Osman Mir","Mara Ghat Ma Birajta Shrinathji — Traditional")
                "French Songs"->listOf("Dernière danse — Indila","Je te promets — Johnny Hallyday","Alors on danse — Stromae","La vie en rose — Édith Piaf","Papaoutai — Stromae")
                else->emptyList()
            }
            Card(Modifier.fillMaxWidth().padding(top=10.dp)){Column(Modifier.padding(14.dp)){
                Text("🎵 $option",fontWeight=FontWeight.Bold)
                Text(trV5(lang,"Song list. Full audio playback requires licensed or user-provided audio files.","ગીતોની યાદી. સંપૂર્ણ ઓડિયો માટે લાઇસન્સવાળા અથવા યુઝર આપેલા ઓડિયો ફાઇલ જરૂરી છે.","गीत सूची। पूरा ऑडियो चलाने के लिए लाइसेंस प्राप्त या उपयोगकर्ता द्वारा दी गई ऑडियो फ़ाइलें चाहिए।","Liste de chansons. La lecture audio complète nécessite des fichiers audio sous licence ou fournis par l’utilisateur."),style=MaterialTheme.typography.bodySmall)
                songs.forEach{Text("🎶 $it",modifier=Modifier.padding(vertical=5.dp))}
            }}
        }
    }
}

'''
    s=s[:insert]+comp+s[insert:]

# Route Entertainment options to the offline composable instead of web links.
needle='selected?.let{o->\n            val context=LocalContext.current'
if needle in s and 'EntertainmentV9(o);return@let' not in s:
    s=s.replace(needle,'selected?.let{o->\n            if(service.id=="entertainment"){EntertainmentV9(o);return@let}\n            val context=LocalContext.current',1)

# Make product/master controls render automatically in Admin, without requiring a Manage button.
product_heading='Text("PRODUCT MANAGEMENT",color=Purple,fontWeight=FontWeight.Bold)'
if product_heading in s and 'ProductManagerV7(session){msg=it};Card' not in s:
    s=s.replace(product_heading,product_heading+';ProductManagerV7(session){msg=it}',1)
master_heading='Text("MASTER MANAGEMENT",color=Purple,fontWeight=FontWeight.Bold)'
if master_heading in s and 'MasterManagerV7(session){msg=it};Card' not in s:
    s=s.replace(master_heading,master_heading+';MasterManagerV7(session){msg=it}',1)

p.write_text(s,encoding='utf-8')
