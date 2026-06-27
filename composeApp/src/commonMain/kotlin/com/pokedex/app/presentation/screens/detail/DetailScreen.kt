package com.pokedex.app.presentation.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.pokedex.app.domain.model.PokemonDetail
import com.pokedex.app.presentation.components.StatBar
import com.pokedex.app.presentation.components.TypeBadge
import com.pokedex.app.presentation.screens.team.TeamViewModel
import com.pokedex.app.presentation.theme.typeColor
import dev.icerock.moko.geo.compose.BindLocationTrackerEffect
import dev.icerock.moko.geo.compose.LocationTrackerAccuracy
import dev.icerock.moko.geo.compose.rememberLocationTrackerFactory
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.location.LOCATION
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    pokemonId: Int,
    teamViewModel: TeamViewModel,
    onBack: () -> Unit,
    detailViewModel: DetailViewModel = viewModel { DetailViewModel() }
) {
    val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()
    val team by teamViewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val permissionsControllerFactory = rememberPermissionsControllerFactory()
    val permissionsController = remember(permissionsControllerFactory) {
        permissionsControllerFactory.createPermissionsController()
    }
    val locationTrackerFactory = rememberLocationTrackerFactory(LocationTrackerAccuracy.Best)
    val locationTracker = remember(locationTrackerFactory, permissionsController) {
        locationTrackerFactory.createLocationTracker(permissionsController)
    }
    
    BindEffect(permissionsController)
    BindLocationTrackerEffect(locationTracker)

    var showCaptureDialog by remember { mutableStateOf(false) }
    var capturedPhotoPath by remember { mutableStateOf<String?>(null) }
    var currentCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var locationName by remember { mutableStateOf("") }
    var isCapturingLocation by remember { mutableStateOf(false) }
    var permissionError by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays: List<ByteArray> ->
            if (byteArrays.isNotEmpty()) {
                scope.launch {
                    val fileName = "photo_${pokemonId}_${Clock.System.now().toEpochMilliseconds()}.jpg"
                    val path = com.pokedex.app.di.AppModule.imageStorage.saveImage(fileName, byteArrays.first())
                    capturedPhotoPath = path
                }
            }
        }
    )

    LaunchedEffect(pokemonId) {
        detailViewModel.loadPokemon(pokemonId)
    }

    if (showCaptureDialog && uiState is DetailUiState.Success) {
        val pokemon = (uiState as DetailUiState.Success).pokemon
        AlertDialog(
            onDismissRequest = { showCaptureDialog = false },
            title = { Text("Capturar Pokémon", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (permissionError != null) {
                        Text(permissionError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    // Preview da Foto / Botão de Câmera
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .border(2.dp, typeColor(pokemon.types.firstOrNull() ?: "normal"), RoundedCornerShape(12.dp))
                            .clickable {
                                scope.launch {
                                    try {
                                        cameraLauncher.launch()
                                    } catch (e: Exception) {
                                        permissionError = "Permissão de câmera negada."
                                    }
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            if (capturedPhotoPath != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CameraAlt, null, tint = MaterialTheme.colorScheme.primary)
                                    Text("Foto capturada!", color = MaterialTheme.colorScheme.primary)
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(40.dp))
                                    Text("Tirar Foto do Treinador", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }

                    // Botão de Localização Automática
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            scope.launch {
                                try {
                                    isCapturingLocation = true
                                    permissionsController.providePermission(Permission.LOCATION)
                                    locationTracker.startTracking()
                                    val location = locationTracker.getLocationsFlow().first()
                                    currentCoords = location.latitude to location.longitude
                                    locationTracker.stopTracking()
                                } catch (e: Exception) {
                                    permissionError = "Permissão de localização negada."
                                } finally {
                                    isCapturingLocation = false
                                }
                            }
                        }
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MyLocation, null, tint = if (currentCoords != null) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (currentCoords != null) "Coordenadas Obtidas" else "Obter GPS atual",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                if (currentCoords != null) {
                                    Text(
                                        "Lat: ${currentCoords?.first}, Lon: ${currentCoords?.second}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (isCapturingLocation) {
                                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        label = { Text("Nome da Cidade/Local") },
                        placeholder = { Text("Ex: Pallet Town") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        teamViewModel.addToTeam(
                            pokemon = pokemon,
                            capturedLocation = locationName.ifBlank { "Local Desconhecido" },
                            latitude = currentCoords?.first,
                            longitude = currentCoords?.second,
                            photoPath = capturedPhotoPath
                        )
                        showCaptureDialog = false
                    },
                    enabled = currentCoords != null && capturedPhotoPath != null
                ) { Text("Adicionar ao Time") }
            },
            dismissButton = {
                TextButton(onClick = { showCaptureDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = uiState) {
                is DetailUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DetailUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                            Button(onClick = { detailViewModel.loadPokemon(pokemonId) }) { Text("Tentar Novamente") }
                        }
                    }
                }
                is DetailUiState.Success -> {
                    val pokemon = state.pokemon
                    val inTeam = (team as? com.pokedex.app.presentation.screens.team.TeamUiState.Success)
                        ?.pokemons?.any { it.pokemon.id == pokemon.id } ?: false
                    
                    val teamFull = (team as? com.pokedex.app.presentation.screens.team.TeamUiState.Success)
                        ?.let { it.pokemons.size >= 6 && !inTeam } ?: false
                        
                    val headerColor = typeColor(pokemon.types.firstOrNull() ?: "normal")

                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        DetailHeader(pokemon, headerColor)

                        Surface(
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                            modifier = Modifier.fillMaxWidth().offset(y = (-20).dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                DetailTitleRow(pokemon)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)) {
                                    pokemon.types.forEach { TypeBadge(it) }
                                }
                                MeasureRow(pokemon)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                                
                                Text("Sobre", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(pokemon.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))

                                Text("Status Base", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                pokemon.stats.forEach { StatBar(name = it.name, value = it.value) }

                                Spacer(Modifier.height(32.dp))

                                Button(
                                    onClick = {
                                        if (inTeam) teamViewModel.removeFromTeam(pokemon.id)
                                        else showCaptureDialog = true
                                    },
                                    enabled = !teamFull || inTeam,
                                    colors = ButtonDefaults.buttonColors(containerColor = if (inTeam) MaterialTheme.colorScheme.error else headerColor),
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(if (inTeam) "Remover do Time" else if (teamFull) "Time Completo" else "Capturar e Adicionar", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(pokemon: PokemonDetail, color: Color) {
    Box(modifier = Modifier.fillMaxWidth().height(260.dp).background(Brush.verticalGradient(listOf(color.copy(alpha = 0.9f), color.copy(alpha = 0.4f), Color.Transparent)))) {
        AsyncImage(model = pokemon.imageUrl, contentDescription = pokemon.name, contentScale = ContentScale.Fit, modifier = Modifier.size(220.dp).align(Alignment.Center))
    }
}

@Composable
private fun DetailTitleRow(pokemon: PokemonDetail) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = pokemon.name.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text(text = "#${pokemon.id.toString().padStart(3, '0')}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MeasureRow(pokemon: PokemonDetail) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "${pokemon.heightM} m", fontWeight = FontWeight.Bold)
            Text(text = "Altura", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        VerticalDivider(modifier = Modifier.height(40.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "${pokemon.weightKg} kg", fontWeight = FontWeight.Bold)
            Text(text = "Peso", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
