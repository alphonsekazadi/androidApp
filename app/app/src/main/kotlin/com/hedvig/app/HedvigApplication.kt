package com.hedvig.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ProcessLifecycleOwner
import com.hedvig.android.hanalytics.android.tracking.ApplicationLifecycleTracker
import com.hedvig.app.feature.settings.Theme
import com.hedvig.app.feature.tracking.ActivityChangeTracker
import org.koin.android.ext.android.inject

open class HedvigApplication : Application() {
  private val applicationLifecycleTracker: ApplicationLifecycleTracker by inject()

  override fun onCreate() {
    super.onCreate()
    ProcessLifecycleOwner.get().lifecycle.addObserver(applicationLifecycleTracker)
    Theme.fromSettings(this)?.apply()
    registerActivityLifecycleCallbacks(ActivityChangeTracker())
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
  }
}