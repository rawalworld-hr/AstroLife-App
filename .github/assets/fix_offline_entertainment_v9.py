from pathlib import Path
p=Path('app/src/main/java/com/astrolife/app/MainActivityV2.kt')
s=p.read_text(encoding='utf-8')

# Entertainment is online-only: Ludo, Solitaire, Chess and free music search links.
s=s.replace(
    'RWService("entertainment","🎮","Entertainment","Games and fun",listOf("Ludo","Solitaire"))',
    'RWService("entertainment","🎮","Entertainment","Online games and free music links",listOf("Ludo","Solitaire","Chess","English Songs","Hindi Songs","Gujarati Songs","French Songs"))'
)
s=s.replace(
    'RWService("entertainment","🎮","Entertainment","Offline games and multilingual music",listOf("Ludo","Solitaire","English Songs","Hindi Songs","Gujarati Songs","French Songs"))',
    'RWService("entertainment","🎮","Entertainment","Online games and free music links",listOf("Ludo","Solitaire","Chess","English Songs","Hindi Songs","Gujarati Songs","French Songs"))'
)

# Extend the option URL mapping created by the prior public-services patch.
old='''                "Ludo"->"https://www.crazygames.com/game/ludo-king"\n                "Solitaire"->"https://www.solitr.com/"\n                else->null'''
new='''                "Ludo"->"https://www.crazygames.com/game/ludo-king"\n                "Solitaire"->"https://www.solitr.com/"\n                "Chess"->"https://www.chess.com/play/online"\n                "English Songs"->"https://www.youtube.com/results?search_query=english+songs"\n                "Hindi Songs"->"https://www.youtube.com/results?search_query=hindi+songs"\n                "Gujarati Songs"->"https://www.youtube.com/results?search_query=gujarati+songs"\n                "French Songs"->"https://www.youtube.com/results?search_query=french+songs"\n                else->null'''
if old in s:
    s=s.replace(old,new,1)

# Make online entertainment wording clearer in the option detail card.
old_text='Text(trV5(lang,"Information and support available.","માહિતી અને સહાય ઉપલબ્ધ છે.","जानकारी और सहायता उपलब्ध है।","Informations et assistance disponibles."));if(url!=null)Button'
new_text='Text(if(service.id=="entertainment") trV5(lang,"Opens an online game or free music search. Internet connection required.","ઓનલાઇન ગેમ અથવા મફત મ્યુઝિક શોધ ખૂલે છે. ઇન્ટરનેટ જરૂરી છે.","ऑनलाइन गेम या मुफ्त संगीत खोज खुलेगी। इंटरनेट आवश्यक है।","Ouvre un jeu en ligne ou une recherche musicale gratuite. Connexion Internet requise.") else trV5(lang,"Information and support available.","માહિતી અને સહાય ઉપલબ્ધ છે.","जानकारी और सहायता उपलब्ध है।","Informations et assistance disponibles."));if(url!=null)Button'
if old_text in s:
    s=s.replace(old_text,new_text,1)

# Keep product and master controls visible automatically in Admin.
product_heading='Text("PRODUCT MANAGEMENT",color=Purple,fontWeight=FontWeight.Bold)'
if product_heading in s and 'ProductManagerV7(session){msg=it};Card' not in s:
    s=s.replace(product_heading,product_heading+';ProductManagerV7(session){msg=it}',1)
master_heading='Text("MASTER MANAGEMENT",color=Purple,fontWeight=FontWeight.Bold)'
if master_heading in s and 'MasterManagerV7(session){msg=it};Card' not in s:
    s=s.replace(master_heading,master_heading+';MasterManagerV7(session){msg=it}',1)

p.write_text(s,encoding='utf-8')
