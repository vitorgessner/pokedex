package com.pokedex.app.presentation.screens.team

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.pokedex.app.domain.model.PokemonDetail
import com.pokedex.app.presentation.theme.typeColor

@Composable
actual fun PlatformTeamContent(
    pokemons: List<TeamMember>,
    onRemove: (pokemonId: Int) -> Unit
) {
    val iosBackground = MaterialTheme.colorScheme.background
    val iosGrouped    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(iosBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 12.dp)
        ) {
            Text(
                text       = "Meu Time",
                fontSize   = 34.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text     = "${pokemons.size} de 6 Pokémons selecionados",
                fontSize = 15.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (pokemons.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier        = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(iosGrouped),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⊕", fontSize = 36.sp)
                    }
                    Text(
                        "Nenhum Pokémon",
                        fontSize   = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Adicione Pokémons na tela de detalhes.",
                        fontSize = 14.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
                modifier            = Modifier.fillMaxSize()
            ) {
                items(pokemons, key = { it.pokemon.id }) { member ->
                    IOSTeamRow(
                        member   = member,
                        isFirst  = pokemons.indexOf(member) == 0,
                        isLast   = pokemons.indexOf(member) == pokemons.lastIndex,
                        onRemove = { onRemove(member.pokemon.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun IOSTeamRow(
    member: TeamMember,
    isFirst: Boolean,
    isLast: Boolean,
    onRemove: () -> Unit
) {
    val pokemon    = member.pokemon
    val typeClr    = typeColor(pokemon.types.firstOrNull() ?: "normal")
    val topRadius  = if (isFirst) 12.dp else 0.dp
    val botRadius  = if (isLast)  12.dp else 0.dp

    Surface(
        color    = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart     = topRadius,
                    topEnd       = topRadius,
                    bottomStart  = botRadius,
                    bottomEnd    = botRadius
                )
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Container da Imagem estilo iOS
            Box(
                modifier        = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(typeClr.copy(alpha = 0.12f))
                    .border(0.5.dp, Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (member.photoPath != null) {
                    AsyncImage(
                        model              = member.photoPath,
                        contentDescription = "Captura",
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                    // Miniatura do Pokémon no canto
                    Box(modifier = Modifier.fillMaxSize().padding(2.dp), contentAlignment = Alignment.BottomEnd) {
                        AsyncImage(
                            model = pokemon.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).background(Color.White.copy(0.7f), CircleShape)
                        )
                    }
                } else {
                    AsyncImage(
                        model              = pokemon.imageUrl,
                        contentDescription = pokemon.name,
                        contentScale       = ContentScale.Fit,
                        modifier           = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = pokemon.name.replaceFirstChar { it.uppercase() },
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text     = "📍 ${member.capturedLocation}",
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text     = pokemon.types.joinToString(" · ") { it.uppercase() },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color    = typeClr.copy(alpha = 0.8f)
                )
            }

            TextButton(
                onClick = onRemove,
                colors  = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF3B30)),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text("Remover", fontSize = 14.sp)
            }
        }

        if (!isLast) {
            HorizontalDivider(
                color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 0.5.dp,
                modifier  = Modifier.padding(start = 88.dp)
            )
        }
    }
}