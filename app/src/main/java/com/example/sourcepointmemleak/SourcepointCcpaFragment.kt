package com.example.sourcepointmemleak

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.sourcepoint.ccpa_cmplibrary.CCPAConsentLib
import com.sourcepoint.ccpa_cmplibrary.ConsentLibBuilder
import kotlinx.android.synthetic.main.fragment_sourcepoint.*

class SourcepointCcpaFragment : Fragment(R.layout.fragment_sourcepoint) {

    private var ccpaConsentLib: CCPAConsentLib? = null

    private var callbacks: SourcepointCcpaFragmentCallbacks? = null
    private var uiWasShown: Boolean = false

    private val sourcepointConfig = SourcepointConfig(
        accountId = 1257,
        propertyName = "GuardianLiveApps",
        propertyId = 8504,
        privacyManagerId = "5ee8ca0c1c62fe48116de34a"
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = parentFragment as SourcepointCcpaFragmentCallbacks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            initSourcepoint()
        } catch (error: Throwable) {
            finish(CcpaStatus.UNKNOWN, error)
        }
    }

    private fun initSourcepoint() {
        ccpaConsentLib = newConsentLibBuilder()
            .setOnConsentUIReady(updateRepositoryThen { consentLib, status ->
                Log.i("SourcePoint", "setOnConsentUIReady: $status")
                if (status == CcpaStatus.APPLIES) {
                    showWebView(consentLib.webView)
                } else {
                    finish(status)
                }
            })
            .setOnConsentUIFinished(updateRepositoryThen { _, _ ->

            })
            .setOnConsentReady(updateRepositoryThen { _, status ->

                finish(status)
            })
            .setOnError(updateRepositoryThen { consentLib, status ->

                finish(status, consentLib.error)
            })
            .build()

        ccpaConsentLib?.run() // SUSPECTED code, commenting out this line does not cause any memory leak
    }

    private fun showWebView(webView: WebView) {
        if (webView.parent == null) {
            webView.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            webView.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.colorAccent
                )
            )
            flWebViewContainer.addView(webView)
            uiWasShown = true
        }
    }

    private fun updateRepositoryThen(func: (CCPAConsentLib, CcpaStatus) -> Unit) =
        CCPAConsentLib.Callback {
            var ccpaStatus = CcpaStatus.DOES_NOT_APPLY // fake value
            try {
                // Update the CCPA consent repo:
//            sourcepointCcpaRepository.writeCcpaUserConsent(it.userConsent)
//            sourcepointCcpaRepository.ccpaApplies = it.ccpaApplies

                // Finally do callback-specific stuff:
                func(it, ccpaStatus)
            } catch (error: Throwable) {
                finish(ccpaStatus, error)
            }
        }

    private fun finish(ccpaStatus: CcpaStatus, error: Throwable? = null) {
        Log.i("SourcePoint", "finish")
        // If UI was shown, either an onboarding message or the privacy manager, the users consent
        // settings may have changed to allow more SDKs to be enabled:
        callbacks?.onCcpaConsentFinished(ccpaStatus, uiWasShown)
    }

    private fun newConsentLibBuilder(): ConsentLibBuilder {
        return CCPAConsentLib.newBuilder(
            sourcepointConfig.accountId,
            sourcepointConfig.propertyName,
            sourcepointConfig.propertyId,
            sourcepointConfig.privacyManagerId,
            requireActivity()
        ).setStagingCampaign(false)
    }

    interface SourcepointCcpaFragmentCallbacks {
        fun onCcpaConsentFinished(ccpaStatus: CcpaStatus, uiWasShown: Boolean)
    }

    companion object {
        const val ARG_SHOW_PRIVACY_MANAGER: String = "showPrivacyManager"

        fun newInstance(showPrivacyManager: Boolean): SourcepointCcpaFragment {
            return SourcepointCcpaFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_SHOW_PRIVACY_MANAGER, showPrivacyManager)
                }
            }
        }
    }
}