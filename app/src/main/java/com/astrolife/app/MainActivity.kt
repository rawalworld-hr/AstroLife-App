package com.astrolife.app

import android.content.Context
import android.os.Bundle
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Service(val icon: String, val title: String, val subtitle: String, val options: List<String>)
data class Booking(val service: String, val name: String, val mobile: String, val city: String, val date: String, val note: String)

private val services = listOf(
    Service("🔮", "Astrology", "Horoscope, Kundli & consultation", listOf("Daily Horoscope", "Kundli / Birth Chart", "Marriage Matching", "Ask an Astrologer", "Muhurat & Puja")),
    Service("🎉", "Events", "Weddings, birthdays & corporate events", listOf("Wedding", "Birthday", "Engagement", "Anniversary", "Corporate Event", "Religious Event")),
    Service("🌸", "Decoration", "Themes, flowers, stage & lighting", listOf("Wedding Decoration", "Stage Decoration", "Birthday Theme", "Flower Decoration", "Mandap", "Lighting")),
    Service("🍽️", "Catering", "Menus and packages for every occasion", listOf("Gujarati", "Punjabi", "South Indian", "Jain", "Continental", "Custom Package")),
    Service("💼", "Consultancy", "Business and professional services", listOf("Accounts & Finance", "HR", "Business Setup", "French Support", "Real Estate", "Documentation")),
    Service("✈️", "Tours & Travel", "Trips, hotels, visa & transport", listOf("Holiday Packages", "Hotels", "Flight Enquiry", "Visa Assistance", "Cab / Vehicle Rental", "Group Tours")),
    Service("🛍️", "Online Shopping", "Products, gifts and essentials", listOf("Puja Products", "Astrology Products", "Gifts", "Decoration Items", "Travel Accessories", "Local Products"))
)

private val Brand = Color(0xFF8F3D2B)
private val Hero = Color(0xFF7D2D1F)
private val WarmBg = Color(0xFFFFF9F5)
private const val PREFS = "rawalworld_prefs"

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

private fun loadBookings(context: Context): List<Booking> {
    val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("bookings", "") ?: ""
    if (raw.isBlank()) return emptyList()
    return raw.split("\u001e").mapNotNull { row ->
        val p = row.split("\u001f")
        if (p.size < 6) null else Booking(p[0], p[1], p[2], p[3], p[4], p[5])
    }
}

private fun saveBookings(context: Context, bookings: List<Booking>) {
    val raw = bookings.joinToString("\u001e") { listOf(it.service, it.name, it.mobile, it.city, it.date, it.note).joinToString("\u001f") }
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("bookings", raw).apply()
}

@Composable
fun RawalworldApp() {
    val context = LocalContext.current
    var screen by remember { mutableStateOf("home") }
    var selectedService by remember { mutableStateOf<Service?>(null) }
    var bookings by remember { mutableStateOf(loadBookings(context)) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(screen == "home", { screen = "home"; selectedService = null }, { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                NavigationBarItem(screen == "bookings", { screen = "bookings"; bookings = loadBookings(context) }, { Icon(Icons.Default.DateRange, null) }, label = { Text("Bookings") })
                NavigationBarItem(screen == "service" && selectedService?.title == "Online Shopping", { selectedService = services.last(); screen = "service" }, { Icon(Icons.Default.ShoppingCart, null) }, label = { Text("Shop") })
                NavigationBarItem(screen == "profile", { screen = "profile" }, { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (screen) {
                "service" -> selectedService?.let { service ->
                    ServiceScreen(service, onBack = { screen = "home" }, onBook = { screen = "booking" })
                }
                "booking" -> selectedService?.let { service ->
                    BookingScreen(service, onBack = { screen = "service" }) { booking ->
                        bookings = bookings + booking
                        saveBookings(context, bookings)
                    }
                }
                "bookings" -> BookingsScreen(bookings)
                "profile" -> ProfileScreen()
                else -> HomeScreen { service -> selectedService = service; screen = "service" }
            }
        }
    }
}

@Composable
fun HomeScreen(onOpenService: (Service) -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = services.filter { query.isBlank() || it.title.contains(query, true) || it.subtitle.contains(query, true) || it.options.any { o -> o.contains(query, true) } }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Rawalworld", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text("Gujarat lifestyle & services super app", style = MaterialTheme.typography.bodySmall)
            }
            AssistChip(onClick = {}, label = { Text("EN · ગુ · हिं · FR") })
        }
        Spacer(Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Hero), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Everything you need,\nin one app.", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(7.dp))
                    Text("Astrology, celebrations, consultancy, travel and shopping across Gujarat.", color = Color.White.copy(alpha = .86f))
                }
                Text("RW", color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(query, { query = it }, leadingIcon = { Icon(Icons.Default.Search, null) }, placeholder = { Text("Search services...") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp))
        Spacer(Modifier.height(12.dp))
        Text("📞 +91 77093 78969  •  ✉ rawalworld@gmail.com", style = MaterialTheme.typography.bodySmall)
        Text("📍 Gujarat, India  •  💳 Google Pay", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))
        Text("Explore services", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
            items(filtered) { service ->
                Card(onClick = { onOpenService(service) }, shape = RoundedCornerShape(18.dp), modifier = Modifier.height(155.dp)) {
                    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(service.icon, style = MaterialTheme.typography.headlineMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(service.title, fontWeight = FontWeight.ExtraBold)
                            Text(service.subtitle, style = MaterialTheme.typography.bodySmall)
                        }
                        Text("Tap to open →", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceScreen(service: Service, onBack: () -> Unit, onBook: () -> Unit) {
    var message by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        TextButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null); Spacer(Modifier.width(4.dp)); Text("Back to home") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(service.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text(service.subtitle)
            }
            Text(service.icon, style = MaterialTheme.typography.displaySmall)
        }
        Spacer(Modifier.height(18.dp))
        service.options.forEach { option ->
            Card(shape = RoundedCornerShape(15.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(option, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    FilledTonalButton(onClick = { message = "$option selected" }) { Text("Open") }
                }
            }
        }
        Button(onClick = onBook, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Request Booking / Quotation") }
        if (message.isNotBlank()) { Spacer(Modifier.height(10.dp)); Text(message, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
fun BookingScreen(service: Service, onBack: () -> Unit, onSaved: (Booking) -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    var name by remember { mutableStateOf(prefs.getString("name", "") ?: "") }
    var mobile by remember { mutableStateOf(prefs.getString("mobile", "") ?: "") }
    var city by remember { mutableStateOf(prefs.getString("city", "") ?: "") }
    var date by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        TextButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null); Spacer(Modifier.width(4.dp)); Text("Back") }
        Text("Booking / Quotation", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text("${service.title} request", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(mobile, { mobile = it }, label = { Text("Mobile") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(city, { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(date, { date = it }, label = { Text("Preferred date (DD/MM/YYYY)") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(note, { note = it }, label = { Text("Requirement") }, minLines = 4, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(14.dp))
        Button(onClick = {
            if (name.isBlank() || mobile.isBlank() || city.isBlank()) {
                message = "Please enter name, mobile and city."
            } else {
                onSaved(Booking(service.title, name.trim(), mobile.trim(), city.trim(), date.trim(), note.trim()))
                message = "Request saved successfully. Rawalworld can contact you on ${mobile.trim()}."
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Submit Request") }
        if (message.isNotBlank()) { Spacer(Modifier.height(12.dp)); Text(message) }
        Spacer(Modifier.height(10.dp))
        Text("This version saves bookings on your device. Online admin/database will be connected next.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun BookingsScreen(bookings: List<Booking>) {
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("My Bookings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text("Recent Rawalworld requests", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        if (bookings.isEmpty()) Text("No booking requests yet.")
        bookings.asReversed().forEach { booking ->
            Card(shape = RoundedCornerShape(15.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text(booking.service, fontWeight = FontWeight.Bold)
                    Text("${booking.name} • ${booking.mobile} • ${booking.city}", style = MaterialTheme.typography.bodySmall)
                    if (booking.date.isNotBlank()) Text("Date: ${booking.date}", style = MaterialTheme.typography.bodySmall)
                    if (booking.note.isNotBlank()) Text(booking.note, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(5.dp))
                    AssistChip(onClick = {}, label = { Text("Submitted") })
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
    var language by remember { mutableStateOf(prefs.getString("language", "English") ?: "English") }
    var message by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text("Save your Rawalworld customer details", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(mobile, { mobile = it }, label = { Text("Mobile") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(city, { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(language, { language = it }, label = { Text("Preferred language") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(14.dp))
        Button(onClick = {
            prefs.edit().putString("name", name.trim()).putString("mobile", mobile.trim()).putString("email", email.trim()).putString("city", city.trim()).putString("language", language.trim()).apply()
            message = "Profile saved on this device."
        }, modifier = Modifier.fillMaxWidth()) { Text("Save Profile") }
        if (message.isNotBlank()) { Spacer(Modifier.height(12.dp)); Text(message) }
        Spacer(Modifier.height(18.dp))
        Text("Rawalworld Contact", fontWeight = FontWeight.Bold)
        Text("+91 77093 78969")
        Text("rawalworld@gmail.com")
        Text("Gujarat, India • Google Pay")
    }
}
