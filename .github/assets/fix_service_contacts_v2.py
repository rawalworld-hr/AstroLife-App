from pathlib import Path

p = Path('app/src/main/java/com/astrolife/app/MainActivityV2.kt')
s = p.read_text(encoding='utf-8')
old = 'private fun fetchContact(key:String,onDone:(RWContact?)->Unit){Thread{var r:RWContact?=null;try{val a=getArray("service_contacts?select=service_key,contact_name,phone,email,whatsapp&service_key=eq.${URLEncoder.encode(key,"UTF-8")}&limit=1");if(a.length()>0){val x=a.getJSONObject(0);r=RWContact(x.optString("service_key"),x.optString("contact_name"),x.optString("phone"),x.optString("email"),x.optString("whatsapp"))}}catch(_:Exception){};ui{onDone(r)}}.start()}'
new = 'private fun fetchContact(key:String,onDone:(RWContact?)->Unit){Thread{var r:RWContact?=null;try{val a=getArray("service_contacts?select=service_key,contact_name,phone,email,whatsapp&service_key=eq.${URLEncoder.encode(key,"UTF-8")}&limit=1");if(a.length()>0){val x=a.getJSONObject(0);fun clean(k:String):String{val v=x.optString(k,"");return if(v=="null")"" else v};r=RWContact(clean("service_key"),clean("contact_name"),clean("phone"),clean("email"),clean("whatsapp"))}}catch(_:Exception){};ui{onDone(r)}}.start()}'
if old not in s:
    raise SystemExit('fetchContact pattern not found')
p.write_text(s.replace(old,new), encoding='utf-8')
