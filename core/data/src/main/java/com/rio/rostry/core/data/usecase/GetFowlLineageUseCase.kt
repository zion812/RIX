package com.rio.rostry.core.data.usecase

import com.rio.rostry.core.data.repository.FowlRepository
import com.rio.rostry.core.data.model.LineageInfo
import com.rio.rostry.core.common.model.Result
import javax.inject.Inject

/**
 * Use case for fetching fowl lineage information for visualization
 */
class GetFowlLineageUseCase @Inject constructor(
    private val fowlRepository: FowlRepository
) {
    /**
     * Get lineage information for a fowl
     *
     * @param fowlId The ID of the fowl to get lineage for
     * @return Result containing the lineage information or an error
     */
    suspend operator fun invoke(fowlId: String): Result<LineageInfo> {
        return fowlRepository.getLineageInfo(fowlId)
    }
}