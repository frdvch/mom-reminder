package com.tinhome.momreminder.alarm

import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.tinhome.momreminder.data.ReminderRepository
import kotlinx.coroutines.launch

class AlarmActivity : ComponentActivity() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        val title = intent.getStringExtra(EXTRA_REMINDER_TITLE).orEmpty()

        startAlerting()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AlarmScreen(
                        title = title,
                        onDone = { markDone(reminderId) },
                        onSnooze = { snooze(reminderId) }
                    )
                }
            }
        }
    }

    private fun startAlerting() {
        val uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ringtone = RingtoneManager.getRingtone(this, uri)?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            }
            play()
        }

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0, 800, 500)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    private fun stopAlerting() {
        ringtone?.stop()
        vibrator?.cancel()
    }

    private fun markDone(reminderId: Long) {
        stopAlerting()
        NotificationManagerCompat.from(this).cancel(reminderId.toInt())
        lifecycleScope.launch {
            val repo = ReminderRepository(applicationContext)
            val reminder = repo.getById(reminderId) ?: return@launch finish()
            ReminderCompletion.markDone(repo, reminder)
            finish()
        }
    }

    private fun snooze(reminderId: Long) {
        stopAlerting()
        NotificationManagerCompat.from(this).cancel(reminderId.toInt())
        lifecycleScope.launch {
            val repo = ReminderRepository(applicationContext)
            val reminder = repo.getById(reminderId) ?: return@launch finish()
            ReminderCompletion.snooze(repo, reminder)
            finish()
        }
    }

    override fun onDestroy() {
        stopAlerting()
        super.onDestroy()
    }
}

@Composable
private fun AlarmScreen(title: String, onDone: () -> Unit, onSnooze: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 32.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth().height(72.dp)) {
            Text(text = "Виконано", fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onSnooze, modifier = Modifier.height(64.dp)) {
            Text(text = "Відкласти на 10 хв", fontSize = 20.sp)
        }
    }
}
