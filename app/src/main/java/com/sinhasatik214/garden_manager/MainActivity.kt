package com.sinhasatik214.garden_manager // <-- YOUR package name

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.UUID

// Import the theme from YOUR package
import com.sinhasatik214.garden_manager.ui.theme.GardenManagerTheme

// --- DATA MODEL ---
/**
 * Represents a single plant in the garden.
 * 'id' is a unique identifier to help Compose optimize the list.
 */
data class Plant(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val species: String,
    val wateringFrequencyDays: Int
)

// --- MAIN ACTIVITY ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Note: I'm removing enableEdgeToEdge() for simplicity with the Scaffold
        // You can add it back if you manage the insets properly
        setContent {
            GardenManagerTheme { // <-- YOUR theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // GardenApp is the root composable that manages state and navigation
                    GardenApp()
                }
            }
        }
    }
}

// --- APP NAVIGATION AND STATE ---
@Composable
fun GardenApp() {
    // This state controls which screen is visible.
    // 'rememberSaveable' ensures the state survives screen rotation.
    var currentScreen by rememberSaveable { mutableStateOf("list") }

    // This is our in-memory list of plants.
    // 'mutableStateListOf' ensures that Compose recomposes when the list changes.
    val plants = remember { mutableStateListOf<Plant>() }

    // Add a few dummy plants on first launch for demonstration
    LaunchedEffect(Unit) {
        if (plants.isEmpty()) {
            plants.add(Plant(name = "Monstera", species = "Monstera deliciosa", wateringFrequencyDays = 7))
            plants.add(Plant(name = "Snake Plant", species = "Dracaena trifasciata", wateringFrequencyDays = 14))
        }
    }


    // Simple "navigation" logic
    when (currentScreen) {
        "list" -> PlantListScreen(
            plants = plants,
            onAddPlantClicked = {
                currentScreen = "add" // Navigate to the add screen
            }
        )
        "add" -> AddPlantScreen(
            onPlantSaved = { newPlant ->
                plants.add(newPlant) // Add the new plant to our list
                currentScreen = "list" // Navigate back to the list
            },
            onNavigateBack = {
                currentScreen = "list" // Navigate back to the list
            }
        )
    }
}

// --- SCREEN 1: PLANT LIST ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantListScreen(
    plants: List<Plant>,
    onAddPlantClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🌿 My Garden") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPlantClicked) {
                Icon(Icons.Filled.Add, contentDescription = "Add Plant")
            }
        }
    ) { innerPadding ->

        if (plants.isEmpty()) {
            // Show a message if the garden is empty
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Your garden is empty.\nTap the '+' button to add a plant!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Show the list of plants
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(plants, key = { it.id }) { plant ->
                    PlantCard(plant = plant)
                }
            }
        }
    }
}

/**
 * A Composable that displays a single plant's info in a Card.
 */
@Composable
fun PlantCard(plant: Plant) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = plant.name, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = plant.species, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = "Water every\n${plant.wateringFrequencyDays} days",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

// --- SCREEN 2: ADD PLANT FORM ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantScreen(
    onPlantSaved: (Plant) -> Unit,
    onNavigateBack: () -> Unit
) {
    // State for the form fields
    var name by rememberSaveable { mutableStateOf("") }
    var species by rememberSaveable { mutableStateOf("") }
    var frequency by rememberSaveable { mutableStateOf("") }

    // State for validation feedback
    var isError by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Plant") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Fill in the details for your new plant.")

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Plant Name (e.g., 'Monstera')") },
                modifier = Modifier.fillMaxWidth(),
                isError = isError && name.isBlank()
            )

            OutlinedTextField(
                value = species,
                onValueChange = { species = it },
                label = { Text("Species (e.g., 'Monstera deliciosa')") },
                modifier = Modifier.fillMaxWidth(),
                isError = isError && species.isBlank()
            )

            OutlinedTextField(
                value = frequency,
                onValueChange = {
                    // Only allow numbers to be entered
                    if (it.all { char -> char.isDigit() }) {
                        frequency = it
                    }
                },
                label = { Text("Watering Frequency (in days)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = isError && frequency.isBlank()
            )

            if (isError) {
                Text(
                    text = "Please fill in all fields correctly.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f)) // Pushes the button to the bottom

            Button(
                onClick = {
                    val freqInt = frequency.toIntOrNull()
                    // Simple Validation
                    if (name.isBlank() || species.isBlank() || freqInt == null || freqInt <= 0) {
                        isError = true
                    } else {
                        isError = false
                        val newPlant = Plant(
                            name = name,
                            species = species,
                            wateringFrequencyDays = freqInt
                        )
                        onPlantSaved(newPlant)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Save Plant")
            }
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GardenManagerTheme { // <-- YOUR theme
        GardenApp()
    }
}