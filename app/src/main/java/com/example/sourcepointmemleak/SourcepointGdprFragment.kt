package com.example.sourcepointmemleak

import android.os.Bundle
import androidx.fragment.app.Fragment

class SourcepointGdprFragment: Fragment(R.layout.fragment_sourcepoint) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // empty: just to demonstrate the structure
    }

    companion object {
        const val ARG_SHOW_PRIVACY_MANAGER: String = "showPrivacyManager"

        fun newInstance(showPrivacyManager: Boolean): SourcepointGdprFragment {
            return SourcepointGdprFragment().apply {
                Bundle().apply {
                    putBoolean(ARG_SHOW_PRIVACY_MANAGER, showPrivacyManager)
                }
            }
        }
    }
}