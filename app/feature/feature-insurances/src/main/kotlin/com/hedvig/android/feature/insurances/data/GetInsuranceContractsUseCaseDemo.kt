package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import giraffe.type.TypeOfContract

internal class GetInsuranceContractsUseCaseDemo : GetInsuranceContractsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<InsuranceContract>> {
    return either {
      listOf(
        InsuranceContract(
          id = "1",
          displayName = "Home Insurance",
          statusPills = listOf("Active"),
          detailPills = listOf("Road 10"),
          isTerminated = false,
          typeOfContract = TypeOfContract.SE_HOUSE,
        ),
      )
    }
  }
}