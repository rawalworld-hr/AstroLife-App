package com.astrolife.app

import android.content.Context
import android.content.Intent
import android.net.Uri
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

private val details = mapOf(
    "Daily Horoscope" to "Daily horoscope gives a simple overview for career, money, relationships, health and general outlook. For a personalized reading, enter date of birth, exact birth time and birth place.",
    "Kundli / Birth Chart" to "A Kundli is a Vedic birth chart prepared from your birth date, exact time and place. It can show Lagna, Moon sign, planetary positions, houses and major life themes.",
    "Marriage Matching" to "Marriage matching compares two birth charts for traditional compatibility. It can include Guna Milan, emotional compatibility and practical compatibility.",
    "Ask an Astrologer" to "Send your birth details and question for a personal consultation on career, finance, marriage, business, property, travel or family matters.",
    "Muhurat & Puja" to "Muhurat helps identify traditionally favorable timing for marriage, business opening, property registration, travel, vehicle purchase and other important events.",
    "Wedding" to "Plan venue, decoration, catering, photography, transport and coordination for your wedding.",
    "Birthday" to "Choose birthday themes, decoration, cake, catering, entertainment and return gifts.",
    "Engagement" to "Plan stage, decoration, catering, photography and guest arrangements for engagement functions.",
    "Anniversary" to "Arrange decoration, dining, gifts and celebration packages for anniversaries.",
    "Corporate Event" to "Support for meetings, launches, conferences, staff events and corporate celebrations.",
    "Religious Event" to "Decoration, catering and event support for puja, satsang and other religious functions.",
    "Wedding Decoration" to "Mandap, stage, floral, lighting and entrance decoration packages.",
    "Stage Decoration" to "Customized stage decoration for weddings, birthdays, corporate and family events.",
    "Birthday Theme" to "Kids and adult birthday themes with balloons, backdrops and customized decor.",
    "Flower Decoration" to "Fresh and artificial flower decoration for homes, venues and stages.",
    "Mandap" to "Traditional and modern mandap decoration options.",
    "Lighting" to "Decorative lighting, fairy lights, ambient lighting and event illumination.",
    "Gujarati" to "Gujarati catering menus for weddings, functions and corporate events.",
    "Punjabi" to "Punjabi menu packages including starters, mains, breads and desserts.",
    "South Indian" to "South Indian breakfast, meal and live-counter catering options.",
    "Jain" to "Jain-friendly menus prepared without onion, garlic and restricted ingredients.",
    "Continental" to "Continental snacks, buffet and event menu options.",
    "Custom Package" to "Build a custom catering package based on guest count, budget and menu preference.",
    "Accounts & Finance" to "Bookkeeping, MIS, budgeting, financial review and basic business finance support.",
    "HR" to "Recruitment support, HR documentation, policies and employee-process assistance.",
    "Business Setup" to "Basic support for business planning, documentation and operational setup.",
    "French Support" to "French language support for communication, translation and business coordination.",
    "Real Estate" to "Property search support, documentation coordination and basic advisory assistance.",
    "Documentation" to "General business and service documentation support.",
    "Holiday Packages" to "Domestic and international holiday package enquiry and planning.",
    "Hotels" to "Hotel enquiry and accommodation planning support.",
    "Flight Enquiry" to "Flight route and fare enquiry support.",
    "Visa Assistance" to "Visa-document checklist and application-support guidance.",
    "Cab / Vehicle Rental" to "Cab, car, pickup and group-transport rental enquiry.",
    "Group Tours" to "Customized group tour planning for families, companies and communities.",
    "Puja Products" to "Browse puja essentials and religious-use products.",
    "Astrology Products" to "Browse astrology-related products and digital services.",
    "Gifts" to "Browse gifting options for family, festivals and events.",
    "Decoration Items" to "Browse event and home decoration items.",
    "Travel Accessories" to "Browse useful accessories for travel and tours.",
    "Local Products" to "Discover selected local and regional products."
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

private fun openWeb(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
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
                "service" -> selectedService?.let { service -> ServiceScreen(service, onBack = { screen = "home" }, onBook = { screen = "booking" }) }
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
    val context = LocalContext.current
    var selectedInfo by remember { mutableStateOf<Pair<String, String>?>(null) }
    var dob by remember { mutableStateOf("") }
    var birthTime by remember { mutableStateOf("") }
    var birthPlace by remember { mutableStateOf("") }
    var astroMessage by remember { mutableStateOf("") }

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
                    FilledTonalButton(onClick = { selectedInfo = option to (details[option] ?: "More information will be added soon.") }) { Text("Open") }
                }
            }
        }

        selectedInfo?.let { (heading, text) ->
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(heading, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(text, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (service.title == "Astrology") {
            Spacer(Modifier.height(10.dp))
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Personal Astrology Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(dob, { dob = it }, label = { Text("Date of birth (DD/MM/YYYY)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(birthTime, { birthTime = it }, label = { Text("Exact birth time") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(birthPlace, { birthPlace = it }, label = { Text("Birth place") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = {
                        astroMessage = if (dob.isBlank() || birthTime.isBlank() || birthPlace.isBlank()) {
                            "Please enter date of birth, exact birth time and birth place."
                        } else {
                            "Details captured. A verified astrology calculation service is required for accurate Kundli, Moon sign, Lagna and planetary positions."
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Show Astrology Guidance") }
                    if (astroMessage.isNotBlank()) { Spacer(Modifier.height(8.dp)); Text(astroMessage, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }

        if (service.title == "Online Shopping") {
            Spacer(Modifier.height(14.dp))
            Text("Free & Sample Products", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            FreeResourceCard("Free Daily Horoscope", "Digital horoscope sample") {
                openWeb(context, "https://www.drikpanchang.com/astrology/prediction/vedic-astrology-horoscope.html")
            }
            FreeResourceCard("Free Panchang", "Tithi, Nakshatra and day details") {
                openWeb(context, "https://www.drikpanchang.com/panchang/day-panchang.html")
            }
            FreeResourceCard("Free Kundli Tool", "Basic birth chart calculator") {
                openWeb(context, "https://www.drikpanchang.com/astrology/vedic-astrology/birth-chart.html")
            }
        }

        Spacer(Modifier.height(14.dp))
        Button(onClick = onBook, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Request Booking / Quotation") }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun FreeResourceCard(title: String, subtitle: String, onOpen: () -> Unit) {
    Card(shape = RoundedCornerShape(15.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Text("FREE", color = Brand, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
            Button(onClick = onOpen) { Text("Open") }
        }
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
        Text("This version saves bookings on your device. Online admin/database connection requires a cloud project such as Firebase or Supabase.", style = MaterialTheme.typography.bodySmall)
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
        if (message.isNotBlank()) { Spacer(Modifier.height(10.dp)); Text(message) }
    }
}
