package com.trybild.attendr.ui.help

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.trybild.attendr.ui.components.AttendrBackground
import com.trybild.attendr.ui.components.AttendrCard
import com.trybild.attendr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(navController: NavController) {
    val vm: HelpViewModel = viewModel()
    val query by vm.query.collectAsState()
    val articles by vm.filteredArticles.collectAsState()

    AttendrBackground(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Help Center", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { vm.setQuery(it) },
                    placeholder = { Text("Search Help Center", color = AttendrTextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AttendrTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AttendrNavy,
                        unfocusedBorderColor = AttendrBorder
                    )
                )
                Spacer(Modifier.height(16.dp))
                Text("Most common issues", style = MaterialTheme.typography.headlineMedium, color = AttendrTextPrimary)
                Spacer(Modifier.height(12.dp))
            }

            items(articles) { item ->
                HelpItemRow(item = item, onClick = { navController.navigate("help/article/${item.slug}") })
                Spacer(Modifier.height(8.dp))
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
    }
}

@Composable
private fun HelpItemRow(item: HelpListItem, onClick: () -> Unit) {
    AttendrCard(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = AttendrNavy.copy(alpha = 0.08f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = helpIcon(item.iconKey),
                        contentDescription = null,
                        tint = AttendrNavy,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(item.title, style = MaterialTheme.typography.bodyLarge, color = AttendrTextPrimary, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AttendrTextSecondary)
        }
    }
}

private fun helpIcon(key: String): ImageVector = when (key) {
    "lock" -> Icons.Default.Lock
    "group" -> Icons.Default.Group
    "business" -> Icons.Default.Business
    "smartphone" -> Icons.Default.Smartphone
    "location_on" -> Icons.Default.LocationOn
    else -> Icons.Default.BugReport
}
