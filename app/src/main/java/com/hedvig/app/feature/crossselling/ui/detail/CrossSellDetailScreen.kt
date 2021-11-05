package com.hedvig.app.feature.crossselling.ui.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import coil.size.Scale
import com.commit451.coiltransformations.CropTransformation
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.systemBarsPadding
import com.hedvig.app.R
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.ui.compose.composables.appbar.FadingTopAppBar
import com.hedvig.app.ui.compose.composables.buttons.LargeContainedButton
import com.hedvig.app.ui.compose.composables.list.SectionTitle
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.rememberBlurHash

@Composable
fun CrossSellDetailScreen(
    onCtaClick: () -> Unit,
    onUpClick: () -> Unit,
    onCoverageClick: () -> Unit,
    onFaqClick: () -> Unit,
    data: CrossSellData,
) {
    val scrollState = rememberScrollState()
    val scrollFromTopInDp = with(LocalDensity.current) {
        scrollState.value.toDp()
    }
    val imageHeight = 260.dp
    val topAppBarBackgroundColorAlpha by derivedStateOf {
        val percentageOfImageScrolledPast = scrollFromTopInDp.coerceAtMost(imageHeight) / imageHeight
        percentageOfImageScrolledPast
    }
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        ScrollableContent(
            crossSellData = data,
            onCoverageClick = onCoverageClick,
            onFaqClick = onFaqClick,
            imageHeight = imageHeight,
            scrollState = scrollState,
        )
        FadingTopAppBar(
            backgroundAlpha = topAppBarBackgroundColorAlpha,
            contentPadding = rememberInsetsPaddingValues(
                insets = LocalWindowInsets.current.statusBars
            ),
            navigationIcon = {
                IconButton(onClick = onUpClick) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
            },
        )
        LargeContainedButton(
            onClick = onCtaClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .systemBarsPadding(bottom = true)
                .padding(16.dp)
        ) {
            Text(text = data.callToAction)
        }
    }
}

@Composable
private fun ScrollableContent(
    crossSellData: CrossSellData,
    onCoverageClick: () -> Unit,
    onFaqClick: () -> Unit,
    imageHeight: Dp,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
) {
    val placeholder by rememberBlurHash(
        crossSellData.backgroundBlurHash,
        64,
        32,
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        Image(
            painter = rememberImagePainter(
                data = crossSellData.backgroundUrl,
                builder = {
                    scale(Scale.FILL)
                    transformations(CropTransformation())
                    placeholder(placeholder)
                    crossfade(true)
                },
            ),
            contentDescription = null,
            modifier = Modifier
                .height(imageHeight)
                .fillMaxWidth(),
            contentScale = ContentScale.FillBounds,
        )
        Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = crossSellData.title,
                style = MaterialTheme.typography.h5,
            )
            Spacer(Modifier.height(16.dp))
            crossSellData.highlights.forEach { highlight ->
                Highlight(
                    title = highlight.title,
                    description = highlight.description,
                )
                Spacer(Modifier.height(24.dp))
            }
            SectionTitle(
                text = stringResource(R.string.cross_sell_info_about_title),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = crossSellData.about,
                style = MaterialTheme.typography.body2,
            )
            Spacer(Modifier.height(24.dp))
            SectionTitle(
                text = stringResource(R.string.cross_sell_info_learn_more_title),
            )
        }
        ClickableListItem(
            onClick = onCoverageClick,
            icon = R.drawable.ic_insurance,
            text = stringResource(R.string.cross_sell_info_full_coverage_row),
        )
        ClickableListItem(
            onClick = onFaqClick,
            icon = R.drawable.ic_info_toolbar,
            text = stringResource(R.string.cross_sell_info_common_questions_row),
        )
        val bottomSystemBarInset = with(LocalDensity.current) {
            LocalWindowInsets.current.systemBars.bottom.toDp()
        }
        Spacer(Modifier.height(104.dp + bottomSystemBarInset))
    }
}

@Preview(showBackground = true)
@Composable
fun CrossSellDetailScreenPreview() {
    HedvigTheme {
        CrossSellDetailScreen(
            onCtaClick = {},
            onUpClick = {},
            onCoverageClick = {},
            onFaqClick = {},
            data = CrossSellData(
                title = "Accident Insurance",
                description = "179 kr/mo.",
                callToAction = "Calculate price",
                crossSellType = "ACCIDENT",
                typeOfContract = "SE_ACCIDENT",
                action = CrossSellData.Action.Chat,
                backgroundUrl = "https://images.unsplash.com/photo-1628996796855-0b056a464e06",
                backgroundBlurHash = "LJC6\$2-:DiWB~WxuRkayMwNGo~of",
                displayName = "Accident Insurance",
                about = "If you or a family member is injured in an accident insurance, Hedvig is able to compensate" +
                    " you for a hospital stay, rehabilitation, therapy and dental injuries. \n\n" +
                    "In case of a permanent injury that affect your your quality of life and ability to work, an " +
                    "accident insurance can complement the support from the social welfare system and your employer.",
                perils = emptyList(),
                terms = emptyList(),
                highlights = listOf(
                    CrossSellData.Highlight(
                        title = "Covers dental injuries",
                        description = "Up to 100 000 SEK per damage.",
                    ),
                    CrossSellData.Highlight(
                        title = "Compensates permanent injuries",
                        description = "A fixed amount up to 2 000 000 SEK is payed out in " +
                            "the event of a permanent injury.",
                    ),
                    CrossSellData.Highlight(
                        title = "Rehabilitation and therapy is covered",
                        description = "After accidents and sudden events, such as the death of a close family member.",
                    ),
                ),
                faq = emptyList(),
                insurableLimits = emptyList(),
            ),
        )
    }
}