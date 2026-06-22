package com.trybild.attendr.ui.help

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.trybild.attendr.ui.components.AttendrBackground
import com.trybild.attendr.ui.components.AttendrCard
import com.trybild.attendr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(slug: String, navController: NavController) {
    val article = HelpArticles.bySlug[slug]

    AttendrBackground(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        article?.title ?: "Article",
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (article == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Article not found", color = AttendrTextSecondary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(article.title, style = MaterialTheme.typography.displayLarge, color = AttendrTextPrimary)
            Spacer(Modifier.height(24.dp))

            article.sections.forEach { section ->
                Text(section.heading, style = MaterialTheme.typography.headlineMedium, color = AttendrTextPrimary)
                Spacer(Modifier.height(8.dp))
                section.bullets.forEach { bullet ->
                    Row(modifier = Modifier.padding(bottom = 6.dp)) {
                        Text("• ", style = MaterialTheme.typography.bodyLarge, color = AttendrTextSecondary)
                        Text(bullet, style = MaterialTheme.typography.bodyLarge, color = AttendrTextSecondary)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            HorizontalDivider(color = AttendrDivider)
            Spacer(Modifier.height(24.dp))

            val relatedArticles = article.relatedSlugs.mapNotNull { HelpArticles.bySlug[it] }
            if (relatedArticles.isNotEmpty()) {
                Text("Related articles", style = MaterialTheme.typography.headlineMedium, color = AttendrTextPrimary)
                Spacer(Modifier.height(12.dp))
                relatedArticles.forEach { related ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("help/article/${related.slug}") }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            related.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = AttendrNavy,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AttendrNavy)
                    }
                    HorizontalDivider(color = AttendrDivider)
                }
                Spacer(Modifier.height(32.dp))
            }

            HelpfulCard(
                onYes = { navController.popBackStack() },
                onNo = { navController.navigate("support?issue=${article.title}") }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
    }
}

@Composable
private fun HelpfulCard(onYes: () -> Unit, onNo: () -> Unit) {
    AttendrCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Did this solve your problem?",
                style = MaterialTheme.typography.headlineMedium,
                color = AttendrTextPrimary
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onYes,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Yes")
                }
                OutlinedButton(
                    onClick = onNo,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("No")
                }
            }
        }
    }
}
