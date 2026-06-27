package com.pokedex.app.presentation.screens.team

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.pokedex.app.domain.model.PokemonDetail
import com.pokedex.app.presentation.components.TypeBadge
import com.pokedex.app.presentation.theme.typeColor

@Composable
actual fun PlatformTeamContent(
    pokemons: List<TeamMember>,
    onRemove: (pokemonId: Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFE63946), Color(0xFFFF6B6B))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                Column {
                    Text("Meu Time", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${pokemons.size}/6 Pokémons", fontSize = 13.sp, color = Color.White.copy(0.8f))
                }
            }
        }

        if (pokemons.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Seu time está vazio", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Adicione Pokémons na tela de detalhes",
                        fontSize = 14.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier            = Modifier.fillMaxSize()
            ) {
                items(pokemons, key = { it.pokemon.id }) { pokemon ->
                    MaterialTeamCard(teamMember = pokemon, onRemove = { onRemove(pokemon.pokemon.id) })
                }
            }
        }
    }
}

@Composable
private fun MaterialTeamCard(teamMember: TeamMember, onRemove: () -> Unit) {
    val pokemon = teamMember.pokemon
    val typeClr = typeColor(pokemon.types.firstOrNull() ?: "normal")

    ElevatedCard(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(typeClr.copy(alpha = 0.60f), typeClr.copy(alpha = 0.15f))
                    )
                )
                .padding(12.dp)
        ) {
            Box(
                modifier        = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(typeClr.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model              = pokemon.imageUrl,
                    contentDescription = pokemon.name,
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier.size(60.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "#${pokemon.id.toString().padStart(3, '0')}",
                    fontSize   = 11.sp,
                    color      = Color.White.copy(0.7f)
                )
                Text(
                    text       = pokemon.name,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    pokemon.types.forEach { TypeBadge(it) }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White.copy(0.85f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Capturado em: ${teamMember.capturedLocation}",
                        fontSize = 12.sp,
                        color = Color.White.copy(0.85f)
                    )
                }

                if (teamMember.photoPath != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Color.White.copy(0.85f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Foto arquivada",
                            fontSize = 11.sp,
                            color = Color.White.copy(0.85f)
                        )
                    }
                }
            }

            FilledIconButton(
                onClick = onRemove,
                colors  = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.25f)
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Remover", tint = Color.White)
            }
        }
    }
}