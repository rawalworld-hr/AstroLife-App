from pathlib import Path

p = Path('app/src/main/java/com/astrolife/app/MainActivity.kt')
s = p.read_text(encoding='utf-8')

s = s.replace(
    'service_contacts?select=service_id,contact_name,phone,email,whatsapp&service_id=eq.$e&limit=1',
    'service_contacts?select=service_key,contact_name,phone,email,whatsapp&service_key=eq.$e&limit=1'
)
s = s.replace(
    'x.optString("service_id")',
    'x.optString("service_key")'
)
s = s.replace(
    'JSONObject().put("service_id",cnt.serviceId)',
    'JSONObject().put("service_key",cnt.serviceId)'
)
s = s.replace(
    'service_contacts?on_conflict=service_id',
    'service_contacts?on_conflict=service_key'
)
s = s.replace(
    'val x=a.getJSONObject(0);r=ServiceContact(x.optString("service_key"),x.optString("contact_name"),x.optString("phone"),x.optString("email"),x.optString("whatsapp"))',
    'val x=a.getJSONObject(0);fun clean(k:String):String{val v=x.optString(k,"");return if(v=="null")"" else v};r=ServiceContact(clean("service_key"),clean("contact_name"),clean("phone"),clean("email"),clean("whatsapp"))'
)

p.write_text(s, encoding='utf-8')
