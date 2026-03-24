package com.interiordesign3d.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.interiordesign3d.data.models.*
import com.interiordesign3d.data.repository.FurnitureRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(onFurnitureClick: (String) -> Unit) {
    val repo = remember { FurnitureRepository() }
    var selectedCategory by remember { mutableStateOf<FurnitureCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var furnitureList by remember { mutableStateOf<List<FurnitureItem>>(emptyList()) }

    LaunchedEffect(selectedCategory, searchQuery) {
        repo.getAllFurniture().collect { all ->
            furnitureList = all
                .filter { if (selectedCategory != null) it.category == selectedCategory else true }
                .filter { if (searchQuery.isBlank()) true else
                    it.name.contains(searchQuery, true) || it.brand.contains(searchQuery, true) }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // ── Top Bar ──────────────────────────────────────────────────────────
        CatalogTopBar(searchQuery = searchQuery, onSearchChange = { searchQuery = it })

        // ── Category Filter ──────────────────────────────────────────────────
        CategoryFilterRow(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = if (selectedCategory == it) null else it }
        )

        // ── Grid ─────────────────────────────────────────────────────────────
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(furnitureList, key = { it.id }) { item ->
                FurnitureGridCard(item = item, onClick = { onFurnitureClick(item.id) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogTopBar(searchQuery: String, onSearchChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Furniture Catalog",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        SearchBar(
            query = searchQuery,
            onQueryChange = onSearchChange,
            onSearch = {},
            active = false,
            onActiveChange = {},
            placeholder = { Text("Search sofas, chairs, tables…") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {}
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: FurnitureCategory?,
    onCategorySelected: (FurnitureCategory) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(FurnitureCategory.entries) { category ->
            val isSelected = selectedCategory == category
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { Text("${category.icon} ${category.displayName}") },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FurnitureGridCard(item: FurnitureItem, onClick: () -> Unit) {
    var isFavorite by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(240.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Thumbnail area
            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(
                            color = Color(
                                android.graphics.Color.parseColor(
                                    item.availableColors.firstOrNull() ?: "#F5F0EB"
                                )
                            ).copy(alpha = 0.25f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.category.icon, fontSize = 56.sp)
                }

                // Favorite button
                IconButton(
                    onClick = { isFavorite = !isFavorite },
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // AR badge
                Surface(
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Filled.ViewInAr, contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Text("3D+AR", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Info
            Column(modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(item.brand, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$${item.price.toInt()}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
