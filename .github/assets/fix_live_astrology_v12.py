from pathlib import Path
p=Path('app/src/main/java/com/astrolife/app/MainActivityV2.kt')
s=p.read_text(encoding='utf-8')

s=s.replace(
'RWService("astrology","🔮","Astrology","Horoscope, Kundli & consultation",listOf("Daily Horoscope","Kundli / Birth Chart","Marriage Matching","Ask an Astrologer","Muhurat & Puja"))',
'RWService("astrology","🔮","Astrology","Horoscope, calendars, Kundli & consultation",listOf("Daily Rashi","Hindu Calendar","Gujarati Calendar","Daily Horoscope","Kundli / Birth Chart","Marriage Matching","Ask an Astrologer","Muhurat & Puja"))'
)

needle='private fun directUrlV10(option:String):String?=when(option){\n'
if needle in s and '"Daily Rashi"->' not in s:
    live='''private fun directUrlV10(option:String):String?=when(option){\n    "Daily Rashi"->"https://www.google.com/search?q=today+daily+rashi+rashifal"\n    "Hindu Calendar"->"https://www.drikpanchang.com/"\n    "Gujarati Calendar"->"https://www.google.com/search?q=today+Gujarati+calendar+Vikram+Samvat+tithi+festival"\n'''
    s=s.replace(needle,live,1)

p.write_text(s,encoding='utf-8')
