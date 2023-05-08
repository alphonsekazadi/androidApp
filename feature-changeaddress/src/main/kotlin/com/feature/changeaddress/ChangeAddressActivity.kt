package com.feature.changeaddress

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.view.WindowCompat
import com.feature.changeaddress.navigation.ChangeAddressNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.navigation.activity.Navigator
import org.koin.android.ext.android.inject

class ChangeAddressActivity : AppCompatActivity() {

  private val activityNavigator: Navigator by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      HedvigTheme {
        ChangeAddressNavHost(
          windowSizeClass = calculateWindowSizeClass(this@ChangeAddressActivity),
          navController = rememberAnimatedNavController(),
          openChat = {
            activityNavigator.navigateToChat(this@ChangeAddressActivity)
          },
          navigateUp = { onSupportNavigateUp() },
          finish = { finish() },
        )
      }
    }
  }
}
