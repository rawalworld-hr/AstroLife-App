package com.astrolife.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class Service(val icon: String, val title: String, val subtitle: String, val options: List<String>)
data class Booking(val service: String, val name: String, val mobile: String, val city: String, val date: String, val note: String)
data class Product(val name: String, val description: String, val price: String, val url: String?)
data class AdminData(
    val bookingCount: Int,
    val astrologyCount: Int,
    val productCount: Int,
    val orderCount: Int,
    val bookingLines: List<String>,
    val astrologyLines: List<String>
)

private const val SUPABASE_URL = "https://hcpvuripnlhofxfczyyb.supabase.co"
private const val SUPABASE_KEY = "sb_publishable_J8YoD4yenQO-nlEMoC1kvA_3_vJgGjg"
private const val PREFS = "rawalworld_prefs"
private val Brand = Color(0xFF8F3D2B)
private val Hero = Color(0xFF7D2D1F)
private val WarmBg = Color(0xFFFFF9F5)

private val services = listOf(
    Service("🔮", "Astrology", "Horoscope, Kundli & consultation", listOf("Daily Horoscope", "Kundli / Birth Chart", "Marriage Matching", "Ask an Astrologer", "Muhurat & Puja")),
    Service("🎉", "Events", "Weddings, birthdays & corporate events", listOf("Wedding", "Birthday", "Engagement", "Anniversary", "Corporate Event", "Religious Event")),
    Service("🌸", "Decoration", "Themes, flowers, stage & lighting", listOf("Wedding Decoration", "Stage Decoration", "Birthday Theme", "Flower Decoration", "Mandap", "Lighting")),
    Service("🍽️", "Catering", "Menus and packages for every occasion", listOf("Gujarati", "Punjabi", "South Indian", "Jain", "Continental", "Custom Package")),
    Service("💼", "Consultancy", "Business and professional services", listOf("Accounts & Finance", "HR", "Business Setup", "French Support", "Real Estate", "Documentation")),
    Service("✈️", "Tours & Travel", "Trips, hotels, visa & transport", listOf("Holiday Packages", "Hotels", "Flight Enquiry", "Visa Assistance", "Cab / Vehicle Rental", "Group Tours")),
    Service("🛍️", "Online Shopping", "Products, gifts and essentials", listOf("Puja Products", "Astrology Products", "Gifts", "Decoration Items", "Travel Accessories", "Local Products"))
)

private val details = mapOf(
    "Daily Horoscope" to "Daily horoscope gives a simple overview for career, money, relationships, health and general outlook.",
    "Kundli / Birth Chart" to "A Kundli is a Vedic birth chart prepared from your birth date, exact time and place. It can show Lagna, Moon sign, planets and houses.",
    "Marriage Matching" to "Marriage matching compares two birth charts for traditional compatibility and relationship guidance.",
    "Ask an Astrologer" to "Send your birth details and question for a personal consultation on career, finance, marriage, business, property or travel.",
    "Muhurat & Puja" to "Muhurat helps identify traditionally favorable timing for important events.",
    "Wedding" to "Plan venue, decoration, catering, photography, transport and coordination.",
    "Birthday" to "Birthday themes, decoration, cake, catering and entertainment.",
    "Engagement" to "Stage, decoration, catering and guest arrangements.",
    "Anniversary" to "Decoration, dining, gifts and celebration packages.",
    "Corporate Event" to "Meetings, launches, conferences and staff events.",
    "Religious Event" to "Decoration, catering and support for puja and religious functions.",
    "Wedding Decoration" to "Mandap, stage, floral, lighting and entrance decoration packages.",
    "Stage Decoration" to "Customized stage decoration for all event types.",
    "Birthday Theme" to "Birthday themes with balloons, backdrops and customized decor.",
    "Flower Decoration" to "Fresh and artificial flower decoration.",
    "Mandap" to "Traditional and modern mandap decoration.",
    "Lighting" to "Decorative and ambient event lighting.",
    "Gujarati" to "Gujarati catering menus for functions and weddings.",
    "Punjabi" to "Punjabi menu packages with starters, mains and desserts.",
    "South Indian" to "South Indian meal and live-counter options.",
    "Jain" to "Jain-friendly menu options.",
    "Continental" to "Continental snacks and buffet options.",
    "Custom Package" to "Custom catering based on guest count and budget.",
    "Accounts & Finance" to "Bookkeeping, MIS, budgeting and finance support.",
    "HR" to "Recruitment support, documentation and HR processes.",
    "Business Setup" to "Business planning and setup support.",
    "French Support" to "French language communication and translation support.",
    "Real Estate" to "Property search and documentation coordination.",
    "Documentation" to "General business documentation support.",
    "Holiday Packages" to "Domestic and international holiday planning.",
    "Hotels" to "Hotel enquiry and accommodation planning.",
    "Flight Enquiry" to "Flight route and fare enquiry.",
    "Visa Assistance" to "Visa checklist and application-support guidance.",
    "Cab / Vehicle Rental" to "Cab, car, pickup and group transport rental.",
    "Group Tours" to "Customized group tour planning.",
    "Puja Products" to "Browse puja essentials.",
    "Astrology Products" to "Browse astrology products and digital services.",
    "Gifts" to "Browse gifting options.",
    "Decoration Items" to "Browse event and home decoration items.",
    "Travel Accessories" to "Browse useful travel accessories.",
    "Local Products" to "Discover selected local and regional products."
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Brand, surface = Color.White, background = WarmBg)) {
                RawalworldApp()
            }
        }
    }
}

private fun openWeb(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

private fun loadBookings(context: Context): List<Booking> {
    val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("bookings", "") ?: ""
    if (raw.isBlank()) return emptyList()
    return raw.split("\u001e").mapNotNull { row ->
        val parts = row.split("\u001f")
        if (parts.size < 6) null else Booking(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5])
    }
}

private fun saveBookings(context: Context, bookings: List<Booking>) {
    val raw = bookings.joinToString("\u001e") { listOf(it.service, it.name, it.mobile, it.city, it.date, it.note).joinToString("\u001f") }
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("bookings", raw).apply()
}

private fun postJson(table: String, payload: JSONObject, onDone: (Boolean) -> Unit) {
    Thread {
        var success = false
        try {
            val connection = URL("$SUPABASE_URL/rest/v1/$table").openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("apikey", SUPABASE_KEY)
            connection.setRequestProperty("Prefer", "return=minimal")
            connection.doOutput = true
            connection.outputStream.use { it.write(payload.toString().toByteArray()) }
            success = connection.responseCode in 200..299
            connection.disconnect()
        } catch (_: Exception) {
        }
        Handler(Looper.getMainLooper()).post { onDone(success) }
    }.start()
}

private fun fetchProducts(onDone: (List<Product>) -> Unit) {
    Thread {
        val result = mutableListOf<Product>()
        try {
            val endpoint = "$SUPABASE_URL/rest/v1/products?select=name,description,price,currency,external_url,is_free&is_active=eq.true"
            val connection = URL(endpoint).openConnection() as HttpURLConnection
            connection.setRequestProperty("apikey", SUPABASE_KEY)
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val array = JSONArray(body)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val isFree = item.optBoolean("is_free")
                result.add(Product(item.optString("name"), item.optString("description"), if (isFree) "FREE" else "${item.optString("currency")} ${item.optString("price")}", item.optString("external_url").takeIf { it.isNotBlank() }))
            }
            connection.disconnect()
        } catch (_: Exception) {
        }
        Handler(Looper.getMainLooper()).post { onDone(result) }
    }.start()
}

private fun adminLogin(email: String, password: String, onDone: (String?, String) -> Unit) {
    Thread {
        var token: String? = null
        var message = "Login failed."
        try {
            val connection = URL("$SUPABASE_URL/auth/v1/token?grant_type=password").openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("apikey", SUPABASE_KEY)
            connection.doOutput = true
            val payload = JSONObject().put("email", email).put("password", password)
            connection.outputStream.use { it.write(payload.toString().toByteArray()) }
            val code = connection.responseCode
            val body = if (code in 200..299) connection.inputStream.bufferedReader().use { it.readText() } else connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code in 200..299) {
                token = JSONObject(body).optString("access_token").takeIf { it.isNotBlank() }
                message = if (token != null) "Login successful." else "Login response was incomplete."
            } else {
                message = "Login failed. Check email/password."
            }
            connection.disconnect()
        } catch (_: Exception) {
            message = "Unable to connect to admin login."
        }
        Handler(Looper.getMainLooper()).post { onDone(token, message) }
    }.start()
}

private fun fetchAdminData(token: String, onDone: (AdminData?, String) -> Unit) {
    Thread {
        try {
            fun getArray(path: String): JSONArray {
                val connection = URL("$SUPABASE_URL/rest/v1/$path").openConnection() as HttpURLConnection
                connection.setRequestProperty("apikey", SUPABASE_KEY)
                connection.setRequestProperty("Authorization", "Bearer $token")
                val code = connection.responseCode
                if (code !in 200..299) {
                    connection.disconnect()
                    throw IllegalStateException("Admin access denied")
                }
                val text = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                return JSONArray(text)
            }

            val bookings = getArray("bookings?select=service,customer_name,mobile,city,status&order=created_at.desc&limit=20")
            val astrology = getArray("astrology_requests?select=request_type,customer_name,mobile,birth_place,status&order=created_at.desc&limit=20")
            val products = getArray("products?select=id&is_active=eq.true")
            val orders = getArray("orders?select=id")

            val bookingLines = mutableListOf<String>()
            for (i in 0 until bookings.length()) {
                val x = bookings.getJSONObject(i)
                bookingLines.add("${x.optString("service", "Booking")} • ${x.optString("customer_name")} • ${x.optString("mobile")} • ${x.optString("city")} • ${x.optString("status", "submitted")}")
            }
            val astrologyLines = mutableListOf<String>()
            for (i in 0 until astrology.length()) {
                val x = astrology.getJSONObject(i)
                astrologyLines.add("${x.optString("request_type", "Astrology")} • ${x.optString("customer_name")} • ${x.optString("mobile")} • ${x.optString("birth_place")} • ${x.optString("status", "submitted")}")
            }

            val data = AdminData(bookings.length(), astrology.length(), products.length(), orders.length(), bookingLines, astrologyLines)
            Handler(Looper.getMainLooper()).post { onDone(data, "Dashboard updated.") }
        } catch (_: Exception) {
            Handler(Looper.getMainLooper()).post { onDone(null, "Unable to load admin data. This account may not have admin access.") }
        }
    }.start()
}

@Composable
fun RawalworldApp() {
    val context = LocalContext.current
    var screen by remember { mutableStateOf("home") }
    var selectedService by remember { mutableStateOf<Service?>(null) }
    var bookings by remember { mutableStateOf(loadBookings(context)) }

    Scaffold(bottomBar = {
        NavigationBar {
            NavigationBarItem(selected = screen == "home", onClick = { screen = "home"; selectedService = null }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
            NavigationBarItem(selected = screen == "bookings", onClick = { screen = "bookings"; bookings = loadBookings(context) }, icon = { Icon(Icons.Default.DateRange, null) }, label = { Text("Bookings") })
            NavigationBarItem(selected = screen == "service" && selectedService?.title == "Online Shopping", onClick = { selectedService = services.last(); screen = "service" }, icon = { Icon(Icons.Default.ShoppingCart, null) }, label = { Text("Shop") })
            NavigationBarItem(selected = screen == "profile", onClick = { screen = "profile" }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
            NavigationBarItem(selected = screen == "admin", onClick = { screen = "admin" }, icon = { Text("🔐") }, label = { Text("Admin") })
        }
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (screen) {
                "service" -> selectedService?.let { ServiceScreen(it, { screen = "home" }, { screen = "booking" }) }
                "booking" -> selectedService?.let { service -> BookingScreen(service, { screen = "service" }) { booking -> bookings = bookings + booking; saveBookings(context, bookings) } }
                "bookings" -> BookingsScreen(bookings)
                "profile" -> ProfileScreen()
                "admin" -> AdminScreen()
                else -> HomeScreen { selectedService = it; screen = "service" }
            }
        }
    }
}

@Composable
fun HomeScreen(onOpen: (Service) -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = services.filter { service -> query.isBlank() || service.title.contains(query, true) || service.subtitle.contains(query, true) || service.options.any { it.contains(query, true) } }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(14.dp))
        Text("Rawalworld", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text("Gujarat lifestyle & services super app", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(14.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Hero), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Everything you need,\nin one app.", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Text("Astrology, events, consultancy, travel and shopping.", color = Color.White.copy(alpha = 0.86f))
                }
                Text("RW", color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = query, onValueChange = { query = it }, leadingIcon = { Icon(Icons.Default.Search, null) }, placeholder = { Text("Search services...") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Text("📞 +91 77093 78969  •  ✉ rawalworld@gmail.com", style = MaterialTheme.typography.bodySmall)
        Text("📍 Gujarat, India  •  💳 Google Pay", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(14.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
            items(filtered) { service ->
                Card(onClick = { onOpen(service) }, shape = RoundedCornerShape(18.dp), modifier = Modifier.height(150.dp)) {
                    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Text(service.icon, style = MaterialTheme.typography.headlineMedium)
                        Column { Text(service.title, fontWeight = FontWeight.Bold); Text(service.subtitle, style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceScreen(service: Service, onBack: () -> Unit, onBook: () -> Unit) {
    val context = LocalContext.current
    var selectedInfo by remember { mutableStateOf<Pair<String, String>?>(null) }
    var dob by remember { mutableStateOf("") }
    var birthTime by remember { mutableStateOf("") }
    var birthPlace by remember { mutableStateOf("") }
    var astrologyMessage by remember { mutableStateOf("") }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var loadingProducts by remember { mutableStateOf(false) }

    LaunchedEffect(service.title) {
        if (service.title == "Online Shopping") {
            loadingProducts = true
            fetchProducts { products = it; loadingProducts = false }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        TextButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null); Spacer(Modifier.width(4.dp)); Text("Back to home") }
        Text("${service.icon} ${service.title}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text(service.subtitle)
        Spacer(Modifier.height(14.dp))
        service.options.forEach { option ->
            Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(option, Modifier.weight(1f))
                    FilledTonalButton(onClick = { selectedInfo = option to (details[option] ?: "More information coming soon.") }) { Text("Open") }
                }
            }
        }
        selectedInfo?.let { info -> Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Column(Modifier.padding(14.dp)) { Text(info.first, fontWeight = FontWeight.Bold); Text(info.second) } } }

        if (service.title == "Astrology") {
            Spacer(Modifier.height(12.dp))
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text("Personal Astrology Details", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(dob, { dob = it }, label = { Text("Date of birth YYYY-MM-DD") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(birthTime, { birthTime = it }, label = { Text("Birth time HH:MM") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(birthPlace, { birthPlace = it }, label = { Text("Birth place") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        if (dob.isBlank() || birthTime.isBlank() || birthPlace.isBlank()) {
                            astrologyMessage = "Please fill all birth details."
                        } else {
                            astrologyMessage = "Submitting online..."
                            val payload = JSONObject().put("date_of_birth", dob).put("birth_time", birthTime).put("birth_place", birthPlace).put("request_type", "kundli")
                            postJson("astrology_requests", payload) { astrologyMessage = if (it) "Astrology request submitted online successfully." else "Online submission failed. Please check internet and try again." }
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Submit Astrology Request") }
                    if (astrologyMessage.isNotBlank()) { Spacer(Modifier.height(8.dp)); Text(astrologyMessage, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }

        if (service.title == "Online Shopping") {
            Spacer(Modifier.height(12.dp))
            Text("Online Products", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (loadingProducts) CircularProgressIndicator() else if (products.isEmpty()) Text("Could not load online products.") else products.forEach { product ->
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(product.name, fontWeight = FontWeight.Bold)
                        Text(product.description, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                        Text(product.price, fontWeight = FontWeight.Bold)
                        product.url?.let { url -> Spacer(Modifier.height(8.dp)); Button(onClick = { openWeb(context, url) }) { Text("Open") } }
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Button(onClick = onBook, modifier = Modifier.fillMaxWidth()) { Text("Request Booking / Quotation") }
    }
}

@Composable
fun BookingScreen(service: Service, onBack: () -> Unit, onSaved: (Booking) -> Unit) {
    val prefs = LocalContext.current.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    var name by remember { mutableStateOf(prefs.getString("name", "") ?: "") }
    var mobile by remember { mutableStateOf(prefs.getString("mobile", "") ?: "") }
    var city by remember { mutableStateOf(prefs.getString("city", "") ?: "") }
    var date by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        TextButton(onClick = onBack) { Text("← Back") }
        Text("Booking / Quotation", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(service.title)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(mobile, { mobile = it }, label = { Text("Mobile") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(city, { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(date, { date = it }, label = { Text("Preferred date YYYY-MM-DD (optional)") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(note, { note = it }, label = { Text("Requirement") }, minLines = 3, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        Button(enabled = !submitting, onClick = {
            if (name.isBlank() || mobile.isBlank() || city.isBlank()) {
                message = "Please enter name, mobile and city."
            } else {
                val booking = Booking(service.title, name.trim(), mobile.trim(), city.trim(), date.trim(), note.trim())
                onSaved(booking)
                submitting = true
                message = "Submitting online..."
                val payload = JSONObject().put("service", booking.service).put("customer_name", booking.name).put("mobile", booking.mobile).put("city", booking.city).put("source", "android")
                if (Regex("\\d{4}-\\d{2}-\\d{2}").matches(booking.date)) payload.put("preferred_date", booking.date)
                if (booking.note.isNotBlank()) payload.put("requirement", booking.note)
                postJson("bookings", payload) { success ->
                    submitting = false
                    message = if (success) "✅ Request submitted online successfully." else "⚠️ Saved on phone, but online submission failed."
                }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text(if (submitting) "Submitting..." else "Submit Request") }
        if (message.isNotBlank()) { Spacer(Modifier.height(8.dp)); Text(message) }
    }
}

@Composable
fun BookingsScreen(items: List<Booking>) {
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("My Bookings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        if (items.isEmpty()) Text("No booking requests yet.")
        items.asReversed().forEach { booking ->
            Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(booking.service, fontWeight = FontWeight.Bold)
                    Text("${booking.name} • ${booking.mobile} • ${booking.city}", style = MaterialTheme.typography.bodySmall)
                    if (booking.date.isNotBlank()) Text("Date: ${booking.date}", style = MaterialTheme.typography.bodySmall)
                    if (booking.note.isNotBlank()) Text(booking.note, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    var name by remember { mutableStateOf(prefs.getString("name", "") ?: "") }
    var mobile by remember { mutableStateOf(prefs.getString("mobile", "") ?: "") }
    var email by remember { mutableStateOf(prefs.getString("email", "") ?: "") }
    var city by remember { mutableStateOf(prefs.getString("city", "") ?: "") }
    var message by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(mobile, { mobile = it }, label = { Text("Mobile") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(city, { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        Button(onClick = { prefs.edit().putString("name", name).putString("mobile", mobile).putString("email", email).putString("city", city).apply(); message = "Profile saved." }, modifier = Modifier.fillMaxWidth()) { Text("Save Profile") }
        if (message.isNotBlank()) { Spacer(Modifier.height(8.dp)); Text(message) }
    }
}

@Composable
fun AdminScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var token by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var data by remember { mutableStateOf<AdminData?>(null) }

    fun refreshAdmin() {
        val t = token ?: return
        loading = true
        message = "Loading dashboard..."
        fetchAdminData(t) { result, msg ->
            loading = false
            data = result
            message = msg
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("🔐 Rawalworld Admin", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text("Secure bookings and customer management", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))

        if (token == null) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Admin Login", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(email, { email = it }, label = { Text("Admin email") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(password, { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(10.dp))
                    Button(enabled = !loading, onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            message = "Enter admin email and password."
                        } else {
                            loading = true
                            message = "Signing in..."
                            adminLogin(email.trim(), password) { newToken, msg ->
                                loading = false
                                token = newToken
                                message = msg
                                if (newToken != null) {
                                    loading = true
                                    fetchAdminData(newToken) { result, dashboardMsg ->
                                        loading = false
                                        data = result
                                        message = dashboardMsg
                                    }
                                }
                            }
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text(if (loading) "Please wait..." else "Login") }
                    if (message.isNotBlank()) { Spacer(Modifier.height(8.dp)); Text(message, style = MaterialTheme.typography.bodySmall) }
                }
            }
        } else {
            val d = data
            if (loading) CircularProgressIndicator()
            if (d != null) {
                Spacer(Modifier.height(8.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.height(190.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { AdminCountCard("Bookings", d.bookingCount, "📅") }
                    item { AdminCountCard("Astrology", d.astrologyCount, "🔮") }
                    item { AdminCountCard("Products", d.productCount, "🛍️") }
                    item { AdminCountCard("Orders", d.orderCount, "📦") }
                }
                Text("Recent Bookings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                if (d.bookingLines.isEmpty()) Text("No bookings yet.") else d.bookingLines.forEach { line ->
                    Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) { Text(line, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall) }
                }
                Spacer(Modifier.height(8.dp))
                Text("Recent Astrology Requests", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                if (d.astrologyLines.isEmpty()) Text("No astrology requests yet.") else d.astrologyLines.forEach { line ->
                    Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) { Text(line, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall) }
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(onClick = { refreshAdmin() }, enabled = !loading, modifier = Modifier.fillMaxWidth()) { Text("Refresh Dashboard") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { token = null; data = null; password = ""; message = "Logged out." }, modifier = Modifier.fillMaxWidth()) { Text("Logout") }
            if (message.isNotBlank()) { Spacer(Modifier.height(8.dp)); Text(message, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun AdminCountCard(label: String, count: Int, icon: String) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column { Text(count.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text(label, style = MaterialTheme.typography.bodySmall) }
            Text(icon, style = MaterialTheme.typography.headlineMedium)
        }
    }
}
