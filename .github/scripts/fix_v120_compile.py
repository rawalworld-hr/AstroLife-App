from pathlib import Path

p = Path('app/src/main/java/com/astrolife/app/MainActivity.kt')
s = p.read_text()
start = s.find('@Composable fun ShopScreen(lang:String){')
end = s.find('@Composable fun CheckoutDialog', start)
if start < 0 or end < 0:
    raise SystemExit('ShopScreen markers not found')

shop = r'''@Composable fun ShopScreen(lang:String){
    val context=LocalContext.current
    var products by remember{mutableStateOf<List<ProductRow>>(emptyList())}
    var masterCategories by remember{mutableStateOf(listOf("Puja Products","Astrology Products","Gifts","Decoration Items","Travel Accessories","Local Products"))}
    var loading by remember{mutableStateOf(true)}
    var selected by remember{mutableStateOf<ProductRow?>(null)}
    var category by remember{mutableStateOf("All")}

    LaunchedEffect(Unit){
        Thread{
            val out=mutableListOf<ProductRow>()
            val cats=mutableListOf<String>()
            try{
                val a=getArray("products?select=id,name,category,description,price,is_active,image_url&is_active=eq.true&order=created_at.desc")
                for(i in 0 until a.length()){
                    val x=a.getJSONObject(i)
                    out+=ProductRow(x.optString("id"),x.optString("name"),x.optString("category"),x.optString("description"),x.optDouble("price"),true,x.optString("image_url"))
                }
            }catch(_:Exception){}
            try{
                val a=getArray("masters?select=name&master_type=eq.shop&is_active=eq.true&order=name.asc")
                for(i in 0 until a.length()){
                    val n=a.getJSONObject(i).optString("name")
                    if(n.isNotBlank())cats+=n
                }
            }catch(_:Exception){}
            ui{
                products=out
                masterCategories=(masterCategories+cats+out.map{it.category}).filter{it.isNotBlank()}.distinct()
                loading=false
            }
        }.start()
    }

    val categories=listOf("All")+masterCategories.distinct()
    val visible=if(category=="All")products else products.filter{it.category==category}
    val shareLabel=rwText(lang,"Share Product","પ્રોડક્ટ શેર કરો","प्रोडक्ट शेयर करें","Partager le produit")

    Column(Modifier.fillMaxSize().padding(14.dp).verticalScroll(rememberScrollState())){
        Text(rwText(lang,"🛍️ Online Shopping","🛍️ ઑનલાઇન શોપિંગ","🛍️ ऑनलाइन शॉपिंग","🛍️ Shopping en ligne"),style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
        Text(rwText(lang,"Shop category wise","કેટેગરી પ્રમાણે ખરીદી","कैटेगरी अनुसार शॉपिंग","Shopping par catégorie"),color=Purple,fontWeight=FontWeight.Bold)

        categories.chunked(2).forEach{cats->
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                cats.forEach{cat->
                    OutlinedButton(onClick={category=cat},modifier=Modifier.weight(1f)){
                        Text((if(category==cat)"✓ " else "")+cat)
                    }
                }
                if(cats.size==1)Spacer(Modifier.weight(1f))
            }
        }

        if(loading)CircularProgressIndicator(Modifier.padding(16.dp))
        if(!loading&&visible.isEmpty())Text(rwText(lang,"No active products found in this category.","આ કેટેગરીમાં કોઈ સક્રિય પ્રોડક્ટ નથી.","इस कैटेगरी में कोई सक्रिय प्रोडक्ट नहीं है।","Aucun produit actif dans cette catégorie."))

        visible.forEach{r->
            Card(Modifier.fillMaxWidth().padding(vertical=6.dp)){
                Column(Modifier.padding(14.dp)){
                    if(r.imageUrl.isNotBlank())RemoteImage(r.imageUrl)
                    Text(r.name,fontWeight=FontWeight.Bold)
                    Text(r.category,style=MaterialTheme.typography.bodySmall)
                    if(r.description.isNotBlank())Text(r.description)
                    Text("₹ ${String.format("%.2f",r.price)}",fontWeight=FontWeight.Bold)
                    Button(onClick={selected=r},modifier=Modifier.fillMaxWidth().padding(top=8.dp)){
                        Text(rwText(lang,"Buy Now","હમણાં ખરીદો","अभी खरीदें","Acheter"))
                    }
                    OutlinedButton(onClick={
                        val text="Rawalworld: ${r.name} - ₹ ${String.format("%.2f",r.price)}"
                        val shareIntent=Intent(Intent.ACTION_SEND)
                        shareIntent.type="text/plain"
                        shareIntent.putExtra(Intent.EXTRA_TEXT,text)
                        try{context.startActivity(Intent.createChooser(shareIntent,shareLabel))}catch(_:Exception){}
                    },modifier=Modifier.fillMaxWidth().padding(top=6.dp)){
                        Text(shareLabel)
                    }
                }
            }
        }
    }

    if(selected!=null){
        CheckoutDialog(context,selected!!){selected=null}
    }
}'''

s = s[:start] + shop + '\n\n' + s[end:]
p.write_text(s)
