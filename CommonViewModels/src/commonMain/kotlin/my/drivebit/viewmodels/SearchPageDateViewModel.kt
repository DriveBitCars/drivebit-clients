package my.drivebit.viewmodels

import my.drivebit.repositories.CurrentFiltersRepository

interface SearchPageDateViewModel {
    fun set(date: String)
}

class SearchPageDateViewModelImpl(
    private val currentFiltersRepository: CurrentFiltersRepository,
) : SearchPageDateViewModel {
    override fun set(date: String) {
        currentFiltersRepository.updateStartDate(date)
    }
}
