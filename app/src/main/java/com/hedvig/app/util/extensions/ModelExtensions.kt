package com.hedvig.app.util.extensions

import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.R

// TODO fix real translation
fun TypeOfContract.getStringId() = when (this) {
    TypeOfContract.SE_HOUSE -> R.string.SWEDISH_HOUSE_LOB
    TypeOfContract.SE_APARTMENT_BRF -> R.string.SWEDISH_APARTMENT_LOB_BRF
    TypeOfContract.SE_APARTMENT_RENT -> R.string.SWEDISH_APARTMENT_LOB_RENT
    TypeOfContract.SE_APARTMENT_STUDENT_BRF -> R.string.SWEDISH_APARTMENT_LOB_STUDENT_BRF
    TypeOfContract.SE_APARTMENT_STUDENT_RENT -> R.string.SWEDISH_APARTMENT_LOB_STUDENT_RENT
    TypeOfContract.NO_HOME_CONTENT_OWN -> R.string.NORWEIGIAN_HOME_CONTENT_LOB_OWN
    TypeOfContract.NO_HOME_CONTENT_RENT -> R.string.NORWEIGIAN_HOME_CONTENT_LOB_RENT
    TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN -> R.string.NORWEIGIAN_HOME_CONTENT_LOB_STUDENT_OWN
    TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT -> R.string.NORWEIGIAN_HOME_CONTENT_LOB_STUDENT_OWN
    TypeOfContract.NO_TRAVEL -> R.string.CONTRACT_DISPLAY_NAME_NO_TRAVEL
    TypeOfContract.NO_TRAVEL_YOUTH -> R.string.CONTRACT_DISPLAY_NAME_NO_TRAVEL_YOUTH
    TypeOfContract.DK_HOME_CONTENT_OWN,
    TypeOfContract.DK_HOME_CONTENT_RENT,
    TypeOfContract.DK_HOME_CONTENT_STUDENT_OWN,
    TypeOfContract.DK_HOME_CONTENT_STUDENT_RENT,
    TypeOfContract.DK_TRAVEL_STUDENT,
    TypeOfContract.DK_TRAVEL,
    TypeOfContract.DK_ACCIDENT,
    TypeOfContract.DK_ACCIDENT_STUDENT
    -> R.string.PLACEHOLDER_CONTRACT_DISPLAY_NAME_DK_HOME_CONTENTS
    TypeOfContract.UNKNOWN__ -> R.string.dummy_string
}
