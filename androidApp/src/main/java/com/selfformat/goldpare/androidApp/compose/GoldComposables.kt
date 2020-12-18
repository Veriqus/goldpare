package com.selfformat.goldpare.androidApp.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.viewModel
import com.selfformat.goldpare.androidApp.compose.ui.GoldpareTheme
import com.selfformat.goldpare.shared.model.GoldItem

@Composable
fun HomeView() {
    val viewModel: HomeViewModel = viewModel()
    viewModel.loadGoldItems()
    val state = viewModel.state.observeAsState().value
    when (state) {
        is HomeViewModel.State.Loaded -> {
            val context = AmbientContext.current
            Surface(modifier = Modifier.fillMaxSize()) {
                LazyColumn {
                        state.goldItems.forEach {
                            item {
                                GoldRow(it) {
                                    openWebPage(
                                        it.link,
                                        context = context
                                    )
                                }
                            }
                        }
                }
            }
        }
        is HomeViewModel.State.Error -> {}
        HomeViewModel.State.Loading -> {}
        null -> {}
    }
}

@Composable
fun GoldRow(item: GoldItem, onClick: (() -> Unit)) {
    Card(
        Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        val formattedWeightInGrams = "%.2f".format(item.weightInGrams)
        val formattedPricePerOunce = "%.2f".format(item.pricePerOunce)
        val formattedPriceMarkup = "%.2f".format(item.priceMarkup(6863.62))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column() {
                if (item.img_url != null) {
                    GlideSuperImage(
                        item.img_url!!,
                        modifier = Modifier.width(120.dp)
                    )
                }
            }
            Column(
                Modifier
                    .padding(16.dp)
            ) {
                Text(text = item.title, fontWeight = FontWeight.Bold)
                Text(text = "cena produktu: ${item.price.orEmpty()}")
                Text(text = "cena 1 uncji: $formattedPricePerOunce")
                Text(text = "waga w gramach: $formattedWeightInGrams")
                Text(text = "marża: $formattedPriceMarkup%")
                Text(text = "sklep: ${item.mintName}")
                Text(text = "typ: ${item.type}")
                if (item.quantity > 1) {
                    Text(text = "sztuk w zestawie: ${item.quantity}")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GoldpareTheme {
        GoldRow(item = GoldItem.fakeGoldItem, onClick = { })
    }
}