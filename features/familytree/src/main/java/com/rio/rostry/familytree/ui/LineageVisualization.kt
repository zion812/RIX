package com.rio.rostry.familytree.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rio.rostry.core.data.model.LineageFowl
import com.rio.rostry.core.data.model.LineageInfo

/**
 * Composable for visualizing fowl lineage in a 2-generation pedigree chart
 */
@Composable
fun LineageVisualization(
    lineageInfo: LineageInfo,
    onFowlSelected: (fowlId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Subject fowl (center)
        FowlCard(
            fowl = lineageInfo.subjectFowl,
            isSubject = true,
            onClick = { onFowlSelected(lineageInfo.subjectFowl.id) }
        )
        
        // Relationship lines and parents
        if (lineageInfo.parents.isNotEmpty()) {
            // Lines connecting parents to subject
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Left parent line
                if (lineageInfo.parents.size > 0) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Canvas(modifier = Modifier.size(24.dp)) {
                            drawLine(
                                color = Color.Gray,
                                start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                                end = androidx.compose.ui.geometry.Offset(size.width / 2, size.height),
                                strokeWidth = 4f
                            )
                        }
                    }
                }
                
                // Center line down to subject
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(modifier = Modifier.size(24.dp)) {
                        drawLine(
                            color = Color.Gray,
                            start = androidx.compose.ui.geometry.Offset(size.width / 2, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width / 2, size.height),
                            strokeWidth = 4f
                        )
                    }
                }
                
                // Right parent line
                if (lineageInfo.parents.size > 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Canvas(modifier = Modifier.size(24.dp)) {
                            drawLine(
                                color = Color.Gray,
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(size.width / 2, size.height),
                                strokeWidth = 4f
                            )
                        }
                    }
                }
            }
            
            // Parents row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // First parent (left)
                if (lineageInfo.parents.size > 0) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        FowlCard(
                            fowl = lineageInfo.parents[0],
                            onClick = { onFowlSelected(lineageInfo.parents[0].id) }
                        )
                    }
                } else {
                    Box(modifier = Modifier.weight(1f))
                }
                
                // Empty center space for subject
                Box(modifier = Modifier.weight(1f))
                
                // Second parent (right)
                if (lineageInfo.parents.size > 1) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        FowlCard(
                            fowl = lineageInfo.parents[1],
                            onClick = { onFowlSelected(lineageInfo.parents[1].id) }
                        )
                    }
                } else {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
        
        // Children section
        if (lineageInfo.children.isNotEmpty()) {
            // Lines connecting subject to children
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Center line up from subject
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Canvas(modifier = Modifier.size(24.dp)) {
                        drawLine(
                            color = Color.Gray,
                            start = androidx.compose.ui.geometry.Offset(size.width / 2, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width / 2, size.height),
                            strokeWidth = 4f
                        )
                    }
                }
            }
            
            // Children row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                lineageInfo.children.forEach { child ->
                    Box(
                        modifier = Modifier
                            .weight(1f / lineageInfo.children.size)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        FowlCard(
                            fowl = child,
                            onClick = { onFowlSelected(child.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FowlCard(
    fowl: LineageFowl,
    isSubject: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick),
        elevation = if (isSubject) 8.dp else 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fowl image or placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        if (fowl.isDeceased) Color.Gray else 
                            when (fowl.gender) {
                                "MALE" -> Color.Blue.copy(alpha = 0.2f)
                                "FEMALE" -> Color.Red.copy(alpha = 0.2f)
                                else -> Color.Gray.copy(alpha = 0.2f)
                            }
                    ),
                contentAlignment = Alignment.Center
            ) {
                fowl.photoReference?.let { photoRef ->
                    AsyncImage(
                        model = photoRef,
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } ?: Text(
                    text = fowl.name?.firstOrNull()?.toString() ?: "F",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Deceased indicator
                if (fowl.isDeceased) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✝",
                            color = Color.White,
                            fontSize = 24.sp
                        )
                    }
                }
            }
            
            // Fowl name
            Text(
                text = fowl.name ?: "Unnamed",
                fontSize = 12.sp,
                fontWeight = if (isSubject) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
            
            // Breed
            Text(
                text = fowl.breedPrimary,
                fontSize = 10.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}