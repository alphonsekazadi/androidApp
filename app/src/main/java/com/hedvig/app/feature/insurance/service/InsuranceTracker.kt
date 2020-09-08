package com.hedvig.app.feature.insurance.service

import com.mixpanel.android.mpmetrics.MixpanelAPI

class InsuranceTracker(
    private val mixpanel: MixpanelAPI
) {
    fun showRenewal() = mixpanel.track("DASHBOARD_RENEWAL_PROMPTER_CTA")
    fun insuranceCertificate() = mixpanel.track("MY_DOCUMENTS_INSURANCE_CERTIFICATE")
    fun termsAndConditions() = mixpanel.track("MY_DOCUMENTS_INSURANCE_TERMS")
    fun changeHomeInfo() = mixpanel.track("CONTRACT_DETAIL_HOME_CHANGE_INFO")
    fun changeCoinsuredInfo() = mixpanel.track("CONTRACT_DETAIL_COINSURED_CHANGE_INFO")
    fun contractInformationCard() = mixpanel.track("CONTRACT_INFORMATION_CARD")
    fun myCoverage() = mixpanel.track("MY_COVERAGE")
    fun myDocuments() = mixpanel.track("MY_DOCUMENTS")
    fun retry() = mixpanel.track("RETRY")
}
