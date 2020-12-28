package com.selfformat.goldpare.androidApp.compose.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.selfformat.goldpare.androidApp.compose.util.NO_PRICE_FILTERING
import com.selfformat.goldpare.androidApp.compose.util.SHOW_GOLD_SETS
import com.selfformat.goldpare.androidApp.compose.util.filterByCoinType
import com.selfformat.goldpare.androidApp.compose.util.filterByMint
import com.selfformat.goldpare.androidApp.compose.util.filterByWeight
import com.selfformat.goldpare.androidApp.compose.util.filterGoldType
import com.selfformat.goldpare.androidApp.compose.util.filterPriceFrom
import com.selfformat.goldpare.androidApp.compose.util.filterPriceTo
import com.selfformat.goldpare.androidApp.compose.util.searchFor
import com.selfformat.goldpare.androidApp.compose.util.showCoinSets
import com.selfformat.goldpare.androidApp.compose.util.sortBy
import com.selfformat.goldpare.shared.GoldSDK
import com.selfformat.goldpare.shared.cache.DatabaseDriverFactory
import com.selfformat.goldpare.shared.model.GoldCoinType
import com.selfformat.goldpare.shared.model.GoldItem
import com.selfformat.goldpare.shared.model.GoldType
import com.selfformat.goldpare.shared.model.Mint
import com.selfformat.goldpare.shared.model.SortingType
import com.selfformat.goldpare.shared.model.WeightRange
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    data class Filters(
        var coinTypeFilter: GoldCoinType = GoldCoinType.ALL,
        var sortingType: SortingType = SortingType.NONE,
        var showGoldSets: Boolean = SHOW_GOLD_SETS,
        var priceFromFilter: Double = NO_PRICE_FILTERING,
        var priceToFilter: Double = NO_PRICE_FILTERING,
        var goldTypeFilter: GoldType = GoldType.ALL,
        var mint: Mint = Mint.ALL,
        var weightFilter: WeightRange = WeightRange.ALL,
        var searchPhrase: String? = null
    ) {
        val isSortingApplied = sortingType != SortingType.NONE
        val isGoldTypeApplied = goldTypeFilter != GoldType.ALL
        val isCoinTypeApplied = coinTypeFilter != GoldCoinType.ALL
        val isWeightTypeApplied = weightFilter != WeightRange.ALL
        val isMintTypeApplied = mint != Mint.ALL
        val isPriceFromApplied = priceFromFilter != NO_PRICE_FILTERING
        val isPriceToApplied = priceToFilter != NO_PRICE_FILTERING
        val bothPricesApplied = isPriceFromApplied && isPriceToApplied
    }

    private val _appliedFilters = MutableLiveData(Filters())
    val appliedFilters: LiveData<Filters> = _appliedFilters
    private val sdk = GoldSDK(DatabaseDriverFactory(context = application))
    private lateinit var data: List<GoldItem>
    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    init {
        _state.value = State.Loading
        viewModelScope.launch {
            kotlin.runCatching {
                sdk.getGoldItems(false)
            }.onSuccess {
                data = it
                _state.value = loadedStateWithSortingAndFiltering()
            }.onFailure {
                _state.value = State.Error(it)
            }
        }
    }

    fun backToHome() {
        _state.value = loadedStateWithSortingAndFiltering()
    }

    fun showFiltering() {
        _state.value = State.Filtering
    }

    fun updateCoinTypeFiltering(goldCoinType: GoldCoinType) {
        _appliedFilters.value = appliedFilters.value?.copy(coinTypeFilter = goldCoinType)
    }

    fun updateSortingType(sorting: SortingType) {
        _appliedFilters.value = appliedFilters.value?.copy(sortingType = sorting)
    }

    fun updateGoldTypeFiltering(goldType: GoldType) {
        _appliedFilters.value = appliedFilters.value?.copy(goldTypeFilter = goldType)
    }

    fun updateMintFiltering(mint: Mint) {
        _appliedFilters.value = appliedFilters.value?.copy(mint = mint)
    }

    fun updateDisplayingGoldSets(show: Boolean) {
        _appliedFilters.value = appliedFilters.value?.copy(showGoldSets = show)
    }

    fun updatePriceToFiltering(priceTo: Double) {
        _appliedFilters.value = appliedFilters.value?.copy(priceToFilter = priceTo)
    }

    fun updatePriceFromFiltering(priceFrom: Double) {
        _appliedFilters.value = appliedFilters.value?.copy(priceFromFilter = priceFrom)
    }

    fun updateWeightFiltering(weight: WeightRange) {
        _appliedFilters.value = appliedFilters.value?.copy(weightFilter = weight)
    }

    fun updateSearchKeyword(searchedPhrase: String) {
        _appliedFilters.value = appliedFilters.value?.copy(searchPhrase = searchedPhrase)
    }

    fun clearCoinTypeFiltering() {
        _appliedFilters.value = appliedFilters.value?.copy(coinTypeFilter = GoldCoinType.ALL)
        showResults()
    }

    fun clearSortingType() {
        _appliedFilters.value = appliedFilters.value?.copy(sortingType = SortingType.NONE)
        showResults()
    }

    fun clearGoldTypeFiltering() {
        _appliedFilters.value = appliedFilters.value?.copy(goldTypeFilter = GoldType.ALL)
        showResults()
    }

    fun clearMintFiltering() {
        _appliedFilters.value = appliedFilters.value?.copy(mint = Mint.ALL)
        showResults()
    }

    fun clearDisplayingGoldSets() {
        _appliedFilters.value = appliedFilters.value?.copy(showGoldSets = SHOW_GOLD_SETS)
        showResults()
    }

    fun clearPriceToFiltering() {
        _appliedFilters.value = appliedFilters.value?.copy(priceToFilter = NO_PRICE_FILTERING)
        showResults()
    }

    fun clearPriceFromFiltering() {
        _appliedFilters.value = appliedFilters.value?.copy(priceFromFilter = NO_PRICE_FILTERING)
        showResults()
    }

    fun clearWeightFiltering() {
        _appliedFilters.value = appliedFilters.value?.copy(weightFilter = WeightRange.ALL)
        showResults()
    }

    fun clearSearchKeyword() {
        _appliedFilters.value = appliedFilters.value?.copy(searchPhrase = null)
        showResults()
    }

    fun showResults() {
        _state.value = searchResultsWithSortingAndFiltering()
    }

    fun clearFilters() {
        _appliedFilters.value = Filters()
    }

    private fun searchResultsWithSortingAndFiltering() =
        State.ShowResults(
            goldItems = data.searchFor(appliedFilters.value!!.searchPhrase)
                .filterGoldType(appliedFilters.value!!.goldTypeFilter)
                .filterByCoinType(appliedFilters.value!!.coinTypeFilter)
                .filterByWeight(appliedFilters.value!!.weightFilter)
                .showCoinSets(appliedFilters.value!!.showGoldSets)
                .filterByMint(appliedFilters.value!!.mint)
                .filterPriceFrom(appliedFilters.value!!.priceFromFilter)
                .filterPriceTo(appliedFilters.value!!.priceToFilter)
                .sortBy(appliedFilters.value!!.sortingType),
            title = appliedFilters.value!!.searchPhrase ?: "Wyniki wyszukiwania"
        )

    private fun loadedStateWithSortingAndFiltering() = State.Home(listOfFeaturedItems())

    private fun listOfFeaturedItems(): List<Pair<GoldItem, WeightRange>> {
        val nullableListOfFeaturedItems = listOfNotNull(
            bestAmongWeightRange(WeightRange.OZ_2),
            bestAmongWeightRange(WeightRange.OZ_1),
            bestAmongWeightRange(WeightRange.OZ_1_4),
            bestAmongWeightRange(WeightRange.OZ_1_2),
            bestAmongWeightRange(WeightRange.OZ_1_10),
        )
        return if (nullableListOfFeaturedItems.isNullOrEmpty()) emptyList() else nullableListOfFeaturedItems
    }

    private fun bestAmongWeightRange(weightRange: WeightRange): Pair<GoldItem, WeightRange>? {
        val goldItem = data.filterGoldType(GoldType.COIN)
            .filterByWeight(weightRange)
            .showCoinSets(false)
            .filter { it.priceDouble != null }
            .minByOrNull { it.priceDouble!! }
        return if (goldItem == null) {
            null
        } else {
            goldItem to weightRange
        }
    }

    sealed class State {
        data class Home(val goldItems: List<Pair<GoldItem, WeightRange>>) : State()
        data class Error(val throwable: Throwable) : State()
        data class ShowResults(val goldItems: List<GoldItem>, val title: String) : State()
        object Filtering : State()
        object Loading : State()
    }
}
