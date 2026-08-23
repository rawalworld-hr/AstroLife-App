from pathlib import Path

p = Path('app/src/main/java/com/astrolife/app/MainActivityV2.kt')
lines = p.read_text(encoding='utf-8').splitlines()
start = next(i for i, line in enumerate(lines) if line.startswith('@Composable fun AdminDashboardV2('))
end = next(i for i in range(start + 1, len(lines)) if lines[i].startswith('@Composable fun SimpleDropdown('))

replacement = r'''@Composable
fun AdminDashboardV2(session:RWSession, logout:()->Unit) {
    val context = LocalContext.current
    var msg by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var productCategory by remember { mutableStateOf("Puja Products") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var productPhoto by remember { mutableStateOf<Uri?>(null) }
    var categories by remember { mutableStateOf(baseServices.last().options) }
    var galleryTitle by remember { mutableStateOf("") }
    var galleryType by remember { mutableStateOf("Event") }
    var galleryCaption by remember { mutableStateOf("") }
    var galleryPhoto by remember { mutableStateOf<Uri?>(null) }
    var galleryRows by remember { mutableStateOf<List<RWGallery>>(emptyList()) }
    var masterType by remember { mutableStateOf("shop") }
    var masterName by remember { mutableStateOf("") }
    var service by remember { mutableStateOf(baseServices.first()) }
    var cn by remember { mutableStateOf("") }
    var cp by remember { mutableStateOf("") }
    var ce by remember { mutableStateOf("") }
    var cw by remember { mutableStateOf("") }

    val productPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        productPhoto = it
        if (it != null) msg = "Product photo selected."
    }
    val galleryPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        galleryPhoto = it
        if (it != null) msg = "Gallery photo selected."
    }

    fun loadGallery() { fetchGallery { galleryRows = it } }
    fun loadContact() {
        fetchContact(service.id) { c ->
            cn = c?.contactName ?: ""
            cp = c?.phone ?: ""
            ce = c?.email ?: ""
            cw = c?.whatsapp ?: ""
        }
    }
    fun addMaster() {
        if (masterName.isBlank()) { msg = "Enter master name."; return }
        Thread {
            val ok = try {
                writeJson("masters", "POST", JSONObject().put("master_type", masterType).put("name", masterName).put("is_active", true), session.access)
            } catch (_:Exception) { false }
            ui {
                msg = if (ok) "✅ Master added." else "Could not add master."
                if (ok) {
                    masterName = ""
                    fetchShopMasters { categories = it }
                }
            }
        }.start()
    }
    fun addProduct() {
        val uri = productPhoto
        val amount = price.toDoubleOrNull()
        if (productName.isBlank() || uri == null || amount == null) {
            msg = "Enter product, valid price and select photo."
            return
        }
        msg = "Compressing and uploading photo..."
        uploadImage(context, session.access, "product-gallery", uri) { url, uploadMsg ->
            if (url == null) {
                msg = uploadMsg
            } else {
                Thread {
                    val payload = JSONObject()
                        .put("name", productName)
                        .put("category", productCategory)
                        .put("description", description)
                        .put("price", amount)
                        .put("currency", "INR")
                        .put("image_url", url)
                        .put("is_free", amount == 0.0)
                        .put("is_active", true)
                    val ok = try { writeJson("products", "POST", payload, session.access) } catch (_:Exception) { false }
                    ui {
                        msg = if (ok) "✅ Product added with photo." else "Photo uploaded but product save failed."
                        if (ok) {
                            productName = ""
                            price = ""
                            description = ""
                            productPhoto = null
                        }
                    }
                }.start()
            }
        }
    }
    fun addGalleryPhoto() {
        val uri = galleryPhoto
        if (galleryTitle.isBlank() || uri == null) {
            msg = "Enter title and select a gallery photo."
            return
        }
        msg = "Uploading gallery photo..."
        uploadImage(context, session.access, "gallery", uri) { url, uploadMsg ->
            if (url == null) {
                msg = uploadMsg
            } else {
                Thread {
                    val payload = JSONObject()
                        .put("title", galleryTitle)
                        .put("gallery_type", galleryType)
                        .put("caption", galleryCaption)
                        .put("image_url", url)
                        .put("is_active", true)
                    val ok = try { writeJson("gallery", "POST", payload, session.access) } catch (_:Exception) { false }
                    ui {
                        msg = if (ok) "✅ Gallery photo published." else "Photo uploaded but gallery save failed."
                        if (ok) {
                            galleryTitle = ""
                            galleryCaption = ""
                            galleryPhoto = null
                            loadGallery()
                        }
                    }
                }.start()
            }
        }
    }
    fun saveContact() {
        Thread {
            val payload = JSONObject()
                .put("service_key", service.id)
                .put("service_name", service.title)
                .put("contact_name", cn)
                .put("phone", cp)
                .put("email", ce)
                .put("whatsapp", cw)
                .put("is_active", true)
            val ok = try {
                val c = conn("$SB_URL/rest/v1/service_contacts?on_conflict=service_key", session.access)
                c.requestMethod = "POST"
                c.setRequestProperty("Content-Type", "application/json")
                c.setRequestProperty("Prefer", "resolution=merge-duplicates,return=minimal")
                c.doOutput = true
                c.outputStream.use { it.write(payload.toString().toByteArray()) }
                val result = c.responseCode in 200..299
                c.disconnect()
                result
            } catch (_:Exception) { false }
            ui { msg = if (ok) "✅ ${service.title} contact saved." else "Could not save contact." }
        }.start()
    }

    LaunchedEffect(Unit) {
        fetchShopMasters {
            categories = it
            productCategory = it.firstOrNull() ?: "Puja Products"
        }
        loadGallery()
        loadContact()
    }
    LaunchedEffect(service.id) { loadContact() }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("🔐 Rawalworld Admin", style=MaterialTheme.typography.headlineMedium, fontWeight=FontWeight.ExtraBold)

        Text("MASTER MANAGEMENT", color=Purple, fontWeight=FontWeight.Bold)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Add Master", fontWeight=FontWeight.Bold)
                SimpleDropdown("Master Type", masterType, listOf("shop", "service", "gallery")) { masterType = it }
                OutlinedTextField(masterName, { masterName = it }, label={Text("Master name")}, modifier=Modifier.fillMaxWidth())
                Button(onClick={ addMaster() }, modifier=Modifier.fillMaxWidth()) { Text("Add Master") }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("PRODUCT MANAGEMENT", color=Purple, fontWeight=FontWeight.Bold)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                OutlinedTextField(productName, { productName = it }, label={Text("Product name")}, modifier=Modifier.fillMaxWidth())
                SimpleDropdown("Category", productCategory, categories) { productCategory = it }
                OutlinedTextField(price, { price = it }, label={Text("Price (INR)")}, modifier=Modifier.fillMaxWidth())
                OutlinedTextField(description, { description = it }, label={Text("Description")}, modifier=Modifier.fillMaxWidth())
                OutlinedButton(onClick={ productPicker.launch("image/*") }, modifier=Modifier.fillMaxWidth()) {
                    Text(if (productPhoto == null) "📷 Choose Product Photo" else "✅ Product Photo Selected")
                }
                Button(onClick={ addProduct() }, modifier=Modifier.fillMaxWidth()) { Text("Add Product") }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("PHOTO GALLERY", color=Purple, fontWeight=FontWeight.Bold)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Upload event, activity or achievement photos", fontWeight=FontWeight.Bold)
                OutlinedTextField(galleryTitle, { galleryTitle = it }, label={Text("Title")}, modifier=Modifier.fillMaxWidth())
                SimpleDropdown("Type", galleryType, listOf("Event", "Activity", "Achievement", "Other")) { galleryType = it }
                OutlinedTextField(galleryCaption, { galleryCaption = it }, label={Text("Caption (optional)")}, modifier=Modifier.fillMaxWidth())
                OutlinedButton(onClick={ galleryPicker.launch("image/*") }, modifier=Modifier.fillMaxWidth()) {
                    Text(if (galleryPhoto == null) "📸 Choose Gallery Photo" else "✅ Gallery Photo Selected")
                }
                Button(onClick={ addGalleryPhoto() }, modifier=Modifier.fillMaxWidth()) { Text("Add Gallery Photo") }
                galleryRows.take(6).forEach { g ->
                    Card(Modifier.fillMaxWidth().padding(vertical=4.dp)) {
                        Column(Modifier.padding(8.dp)) {
                            RemoteImageV2(g.imageUrl)
                            Text(g.title, fontWeight=FontWeight.Bold)
                            Text(g.type)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("SERVICE CONTACT MASTER", color=Purple, fontWeight=FontWeight.Bold)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                SimpleDropdown("Service", service.title, baseServices.map { it.title }) { name -> service = baseServices.first { it.title == name } }
                OutlinedTextField(cn, { cn = it }, label={Text("Contact name")}, modifier=Modifier.fillMaxWidth())
                OutlinedTextField(cp, { cp = it }, label={Text("Phone")}, modifier=Modifier.fillMaxWidth())
                OutlinedTextField(ce, { ce = it }, label={Text("Email")}, modifier=Modifier.fillMaxWidth())
                OutlinedTextField(cw, { cw = it }, label={Text("WhatsApp")}, modifier=Modifier.fillMaxWidth())
                Button(onClick={ saveContact() }, modifier=Modifier.fillMaxWidth()) { Text("Save Service Contact") }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(msg)
        OutlinedButton(onClick=logout, modifier=Modifier.fillMaxWidth()) { Text("Logout") }
        Spacer(Modifier.height(30.dp))
    }
}'''.splitlines()

new_lines = lines[:start] + replacement + lines[end:]
p.write_text('\n'.join(new_lines) + '\n', encoding='utf-8')
