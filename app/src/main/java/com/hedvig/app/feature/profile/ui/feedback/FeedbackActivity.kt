package com.hedvig.app.feature.profile.ui.feedback

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.ratings.openPlayStore
import com.hedvig.app.util.extensions.setupToolbar
import com.hedvig.app.util.extensions.view.useEdgeToEdge
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import kotlinx.android.synthetic.main.activity_feedback.*

class FeedbackActivity : BaseActivity(R.layout.activity_feedback) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root.useEdgeToEdge()
        root.setEdgeToEdgeSystemUiFlags(true)

        setupToolbar(R.string.PROFILE_FEEDBACK_TITLE, R.drawable.ic_close, true, root) {
            onBackPressed()
        }

        bugReportEmail.setOnClickListener {
            startActivity(Intent(Intent.ACTION_SENDTO).apply {
                data =
                    Uri.parse("mailto:${getString(R.string.bug_report_mail)}?subject=Buggrapport")
            })
        }

        playLink.setOnClickListener {
            openPlayStore()
        }
    }
}
