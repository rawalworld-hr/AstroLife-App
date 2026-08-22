package com.astrolife.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Service(
    val icon: String,
    val title: String,
    val subtitle: String,
    val options: List<String>
)

private val services = listOf(
    Service("🔮", "Astrology", "Horoscope, Kundli & consultation",
        listOf("Daily Horoscope", "Kundli / Birth Chart", "Marriage Matching", "Ask an Astrologer", "Muhurat & Puja")),
    Service("🎉", "Events", "Weddings, birthdays & corporate events",
        listOf("Wedding", "Birthday", "Engagement", "Anniversary", "Corporate Event", "Religious Event")),
    Service("🌸", "Decoration", "Themes, flowers, stage & lighting",
        listOf("Wedding Decoration", "Stage Decoration", "Birthday Theme", "Flower Decoration", "Mandap", "Lighting")),
    Service("🍽️", "Catering", "Menus and packages for every occasion",
        listOf("Gujarati", "Punjabi", "South Indian", "Jain", "Continental", "Custom Package")),
    Service("💼", "Consultancy", "Business and professional services",
        listOf("Accounts & Finance", "HR", "Business Setup", "French Support", "Real Estate", "Documentation")),
    Service("✈️", "Tours & Travel", "Trips, hotels, visa & transport",
        listOf("Holiday Packages", "Hotels", "Flight Enquiry", "Visa Assistance", "Cab / Vehicle Rental", "Group Tours")),
    Service("🛍️", "Online Shopping", "Products, gifts and essentials",
        listOf("Puja Products", "Astrology Products", "Gifts", "Decoration Items", "Travel Accessories", "Local Products"))
)

private val Brand = Color(0xFF8F3D2B)
private val Hero = Color(0xFF7D2D1F)
private val WarmBg = Color(0xFFFFF9F5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Brand,
                    surface = Color.White,
                    background = WarmBg
                )
            ) {
                AstroLifeApp()
            }
        }
    }
}

@Composable
fun AstroLifeApp() {
    var selectedService by remember { mutableStateOf<Service?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; selectedService = null },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; selectedService = null },
                    icon = { Icon(Icons.Default.DateRange, null) },
                    label = { Text("Bookings") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2; selectedService = services.last() },
                    icon = { Icon(Icons.Default.ShoppingCart, null) },
                    label = { Text("Shop") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3; selectedService = null },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                selectedService != null -> ServiceScreen(
                    service = selectedService!!,
                    onBack = { selectedService = null; selectedTab = 0 }
                )
                selectedTab == 1 -> PlaceholderScreen("My Bookings", "Your confirmed and pending bookings will appear here.")
                selectedTab == 3 -> PlaceholderScreen("Profile", "Login, personal details, language and saved addresses will appear here.")
                else -> HomeScreen(onOpenService = { selectedService = it })
            }
        }
    }
}

@Composable
fun HomeScreen(onOpenService: (Service) -> Unit) {
    var query by remember { mutableStateOf("") }

    val filtered = services.filter {
        query.isBlank() ||
        it.title.contains(query, true) ||
        it.subtitle.contains(query, true) ||
        it.options.any { option -> option.contains(query, true) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(14.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("AstroLife", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text("Your lifestyle & services super app", style = MaterialTheme.typography.bodySmall)
            }
            AssistChip(
                onClick = {},
                label = { Text("EN · ગુ · हिं · FR") }
            )
        }

        Spacer(Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Hero),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.padding(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Everything you need,\nin one app.",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        "Astrology, celebrations, consultancy, travel and shopping.",
                        color = Color.White.copy(alpha = .86f)
                    )
                }
                Text("✨", style = MaterialTheme.typography.displayMedium)
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            placeholder = { Text("Search astrology, catering, travel...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(15.dp)
        )

        Spacer(Modifier.height(18.dp))
        Text("Explore services", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(filtered) { service ->
                Card(
                    onClick = { onOpenService(service) },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.height(155.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
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
fun ServiceScreen(service: Service, onBack: () -> Unit) {
    var message by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, null)
            Spacer(Modifier.width(4.dp))
            Text("Back to home")
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(service.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text(service.subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Text(service.icon, style = MaterialTheme.typography.displaySmall)
        }

        Spacer(Modifier.height(18.dp))

        service.options.forEach { option ->
            Card(
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(option, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    FilledTonalButton(onClick = { message = "$option selected" }) {
                        Text("Open")
                    }
                }
            }
        }

        Button(
            onClick = { message = "Booking / quotation request started for ${service.title}" },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Request Booking / Quotation")
        }

        if (message.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(message, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, subtitle: String) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(8.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
