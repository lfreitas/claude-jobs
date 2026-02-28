package com.claudejobs

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.claudejobs.notification.NotificationHelper
import com.claudejobs.ui.navigation.AppNavigation
import com.claudejobs.ui.theme.ClaudeJobsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* permission result handled silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val openResultId = intent.getLongExtra(NotificationHelper.EXTRA_RESULT_ID, -1L)

        setContent {
            ClaudeJobsTheme {
                AppNavigation(openResultId = openResultId)
            }
        }
    }
}
