package com.hedvig.android.feature.odyssey.search.commonclaims.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.odyssey.model.ItemType
import com.hedvig.android.feature.odyssey.search.commonclaims.SearchableClaim
import java.util.*

@Composable
internal fun CommonClaims(
  selectClaim: (SearchableClaim) -> Unit,
  commonClaims: List<SearchableClaim>,
) {
  Column {
    Surface(
      modifier = Modifier.padding(
        start = 22.dp,
        end = 22.dp,
        bottom = 80.dp,
      ),
      shape = RoundedCornerShape(8.dp),
    ) {
      LazyColumn {
        itemsIndexed(
          items = commonClaims,
          key = { _, item -> item.entryPointId },
          contentType = { _, _ -> "Common claim" },
        ) { index, claim ->
          Column(
            modifier = Modifier.clickable {
              selectClaim(claim)
            },
          ) {
            Row(
              modifier = Modifier
                .padding(22.dp)
                .align(Alignment.Start)
                .fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(claim.displayName, Modifier.align(Alignment.CenterVertically))
              Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Arrow",
              )
            }
            if (index < commonClaims.lastIndex) {
              Divider(Modifier.padding(horizontal = 8.dp))
            }
          }
        }
      }
    }
  }
}

@HedvigPreview
@Composable
fun PreviewCommonClaims() {
  HedvigTheme {
    CommonClaims(
      selectClaim = {},
      commonClaims = listOf(
        SearchableClaim(
          entryPointId = UUID.randomUUID().toString(),
          displayName = "Broken phone",
          itemType = ItemType("PHONE"),
        ),
        SearchableClaim(
          entryPointId = UUID.randomUUID().toString(),
          displayName = "Stolen phone",
          itemType = ItemType(""),
        ),
        SearchableClaim(
          entryPointId = UUID.randomUUID().toString(),
          displayName = "Broken computer",
          itemType = ItemType("COMPUTER"),
        ),
        SearchableClaim(
          entryPointId = UUID.randomUUID().toString(),
          displayName = "Stolen computer",
          itemType = ItemType(""),
        ),
        SearchableClaim(
          entryPointId = UUID.randomUUID().toString(),
          displayName = "Broken headphones",
          itemType = ItemType(""),
        ),
        SearchableClaim(
          entryPointId = UUID.randomUUID().toString(),
          displayName = "Stolen headphones",
          itemType = ItemType(""),
        ),
      ),
    )
  }
}