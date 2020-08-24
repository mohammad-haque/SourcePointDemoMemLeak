package com.example.sourcepointmemleak

import android.os.Bundle
import android.util.Log
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.container, SourcePointParentFragment(), null)
                    .commit()
        }
    }
}

class SourcePointParentFragment : Fragment(R.layout.parent_fragment), SourcepointCcpaFragment.SourcepointCcpaFragmentCallbacks {

    private var sourcepointCcpaFragment: SourcepointCcpaFragment? = null
    private var sourcepointGdprFragment: SourcepointGdprFragment? = null

    companion object {
        const val TAG_CCPA: String = "sourcepointCcpa"
        const val TAG_GDPR: String = "sourcepointGdpr"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startCcpaConsent()
    }

    private fun startCcpaConsent() {
        sourcepointCcpaFragment = findOrAddFragment(TAG_CCPA, R.id.parentContainer) {
            SourcepointCcpaFragment.newInstance(showPrivacyManager = false)
        }
    }

    private inline fun <reified T : Fragment> findOrAddFragment(tag: String, @IdRes containerViewId: Int, createFragment: () -> T): T {
        val existing = childFragmentManager.findFragmentByTag(tag) as? T
        return existing ?: createFragment().also { newFragment ->
            childFragmentManager
                    .beginTransaction()
                    .add(containerViewId, newFragment, tag)
                    .commit()
        }
    }

    override fun onCcpaConsentFinished(ccpaStatus: CcpaStatus, uiWasShown: Boolean) {
        Log.i("SourcePoint", "onCcpaConsentFinished status: $ccpaStatus")
        sourcepointCcpaFragment?.also {
            childFragmentManager
                    .beginTransaction()
                    .remove(it)
                    .commitAllowingStateLoss()
        }

        startGdprConsent()
    }

    private fun startGdprConsent() {
        Log.i("SourcePoint", "startGdprConsent")
        sourcepointGdprFragment = findOrAddFragment(TAG_GDPR, R.id.parentContainer) {
            SourcepointGdprFragment.newInstance(showPrivacyManager = false)
        }
    }
}

data class SourcepointConfig(
        val accountId: Int,
        val propertyName: String,
        val propertyId: Int,
        val privacyManagerId: String
)

enum class CcpaStatus {
    APPLIES, DOES_NOT_APPLY, UNKNOWN
}
