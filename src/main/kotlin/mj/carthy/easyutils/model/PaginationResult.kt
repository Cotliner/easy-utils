package mj.carthy.easyutils.model

import java.util.stream.Collectors

data class PaginationResult<T>(
        val content: Collection<T>,
        val page: Number,
        val size: Number,
        val totalElements: Number
) {
    fun createSubList(
            elements: Collection<T>,
            pageNumber: Number,
            pageSize: Number
    ): Collection<T> = elements.stream().skip(
            pageNumber.toLong() * size.toLong()
    ).limit(pageSize.toLong()).collect(Collectors.toList())
}