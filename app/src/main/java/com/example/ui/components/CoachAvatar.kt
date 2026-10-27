package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.CoachProfile
import com.example.ui.theme.OceanAzure
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanDeep

@Composable
fun CoachAvatar(
  coach: CoachProfile,
  modifier: Modifier = Modifier,
  size: Dp = 50.dp,
  showBorder: Boolean = true,
  onClick: (() -> Unit)? = null
) {
  var isImageError by remember(coach.id, coach.photoUrl) { mutableStateOf(false) }

  val borderModifier = if (showBorder) {
    Modifier.border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
  } else Modifier

  val clickModifier = if (onClick != null) {
    Modifier.clickable(onClick = onClick)
  } else Modifier

  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .then(borderModifier)
      .then(clickModifier)
      .testTag("avatar_coach_${coach.id}"),
    contentAlignment = Alignment.Center
  ) {
    if (coach.photoUrl.isNotBlank() && !isImageError) {
      AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
          .data(coach.photoUrl)
          .crossfade(true)
          .build(),
        contentDescription = "Foto ${coach.name}",
        contentScale = ContentScale.Crop,
        onError = { isImageError = true },
        modifier = Modifier.fillMaxSize()
      )
    } else {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.linearGradient(
              colors = listOf(OceanDeep, OceanBlue, OceanAzure)
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        val fontSize = (size.value * 0.38f).sp
        Text(
          text = coach.initials,
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = fontSize
        )
      }
    }
  }
}
