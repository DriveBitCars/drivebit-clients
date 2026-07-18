package my.drivebit.viewmodels

import my.drivebit.repositories.SearchFiltersRepository

interface SearchPageDateEndViewModel {
    fun set(date: String?)
}

class SearchPageDateEndViewModelImpl(
    private val searchFiltersRepository: SearchFiltersRepository,
) : SearchPageDateEndViewModel {
    override fun set(date: String?) {
        searchFiltersRepository.updateEndDate(date)
    }
}
