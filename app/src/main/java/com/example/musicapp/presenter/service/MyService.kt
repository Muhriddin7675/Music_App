package com.example.musicapp.presenter.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Size
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.musicapp.MainActivity
import com.example.musicapp.R
import com.example.musicapp.data.ActionEnum
import com.example.musicapp.data.MusicData
import com.example.musicapp.util.MyAppManager
import com.example.musicapp.util.getMusicDataByPosition
import com.example.musicapp.util.myLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.File

class MyService : Service() {
    override fun onBind(p0: Intent?): IBinder? = null
    private val CHANNEL = "DEMO"
    private var _mediaPlayer: MediaPlayer? = null
    private val mediaPlayer get() = _mediaPlayer!!
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var job: Job? = null
    var timer: CountDownTimer? = null
    private var mediaSession: MediaSessionCompat? = null

    override fun onCreate() {
        super.onCreate()
        _mediaPlayer = MediaPlayer()
        createMediaSession()
        createChannel()
        startMyService()
    }

    private fun startMyService() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Music player")
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCustomContentView(createRemoteView())
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .build()

        startForeground(1, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel =
                NotificationChannel("DEMO", CHANNEL, NotificationManager.IMPORTANCE_DEFAULT)
            channel.setSound(null, null)
            val service = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(channel)
        }
    }

    private fun createRemoteView(): RemoteViews {
        val view = RemoteViews(this.packageName, R.layout.remote_view)
        val musicData = MyAppManager.cursor?.getMusicDataByPosition(MyAppManager.selectMusicPos)!!
        view.setTextViewText(R.id.textMusicName, musicData.title)
        view.setTextViewText(R.id.textArtistName, musicData.artist)
        if (mediaPlayer.isPlaying) {
            view.setImageViewResource(R.id.buttonManage, R.drawable.ic_pause)
        } else view.setImageViewResource(R.id.buttonManage, R.drawable.ic_play)

        view.setOnClickPendingIntent(R.id.buttonPrev, createPendingIntent(ActionEnum.PREV))
        view.setOnClickPendingIntent(R.id.buttonManage, createPendingIntent(ActionEnum.MANAGE))
        view.setOnClickPendingIntent(R.id.buttonNext, createPendingIntent(ActionEnum.NEXT))
        view.setOnClickPendingIntent(R.id.buttonCancel, createPendingIntent(ActionEnum.CANCEL))
        return view
    }

    private fun createPendingIntent(action: ActionEnum): PendingIntent {
        val intent = Intent(this, MyService::class.java)
        intent.putExtra("COMMAND", action)

        return PendingIntent.getService(this, action.pos, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val command = intent?.extras?.getSerializable("COMMAND") as ActionEnum
        doneCommand(command)
        return START_NOT_STICKY
    }

    private fun doneCommand(command: ActionEnum) {
        val data: MusicData =
            MyAppManager.cursor?.getMusicDataByPosition(MyAppManager.selectMusicPos)!!
        when (command) {
            ActionEnum.MANAGE -> {
                if (mediaPlayer.isPlaying) doneCommand(ActionEnum.PAUSE)
                else doneCommand(ActionEnum.PLAY)
            }

            ActionEnum.PLAY -> {
                if (mediaPlayer.isPlaying)
                    mediaPlayer.stop()
                _mediaPlayer = MediaPlayer.create(this, Uri.fromFile(File(data.data ?: "")))
                mediaPlayer.start()
                mediaPlayer.setOnCompletionListener {
                    if (MyAppManager.refresh == 0) {
                        doneCommand(ActionEnum.NEXT)
                    } else {
                        doneCommand(ActionEnum.REFRESH)
                    }
                }
                MyAppManager.fullTime = data.duration
                mediaPlayer.seekTo(MyAppManager.currentTime.toInt())
                job?.let { it.cancel() }
                job = scope.launch {
                    changeProgress().collectLatest {
                        MyAppManager.currentTime = it
                        MyAppManager.currentTimeLiveData.postValue(it)
                        updateNotification()
                    }
                }

                MyAppManager.isPlayingLiveData.value = true
                MyAppManager.playMusicLiveData.value = data
            }

            ActionEnum.PAUSE -> {
                job?.let { it.cancel() }
                mediaPlayer.pause()
                MyAppManager.isPlayingLiveData.value = false
                updateNotification()
            }

            ActionEnum.NEXT -> {
                MyAppManager.currentTime = 0
                if (MyAppManager.selectMusicPos + 1 == MyAppManager.cursor!!.count) MyAppManager.selectMusicPos =
                    0
                else MyAppManager.selectMusicPos++
                doneCommand(ActionEnum.PLAY)
            }

            ActionEnum.PREV -> {
                MyAppManager.currentTime = 0
                if (MyAppManager.selectMusicPos == 0) MyAppManager.selectMusicPos =
                    MyAppManager.cursor!!.count - 1
                else MyAppManager.selectMusicPos--
                doneCommand(ActionEnum.PLAY)
            }

            ActionEnum.CANCEL -> {
                mediaPlayer.stop()
                stopSelf()
            }

            ActionEnum.SEEKBAR -> {

                mediaPlayer.seekTo(MyAppManager.currentTime.toInt())
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                }
                job?.cancel()
                job = scope.launch {
                    changeProgress().collectLatest {
                        MyAppManager.currentTime = mediaPlayer.currentPosition.toLong()
                        MyAppManager.currentTimeLiveData.postValue(mediaPlayer.currentPosition.toLong())
                    }
                }
                updateNotification()
            }

            ActionEnum.REFRESH -> {
                MyAppManager.currentTime = 0
                doneCommand(ActionEnum.PLAY)
            }
        }
    }

    private fun changeProgress(): Flow<Long> = flow {
        for (i in MyAppManager.currentTime until MyAppManager.fullTime step 500) {
            delay(500)
            emit(i)
        }
    }

    private fun updateNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        val musicData = MyAppManager.cursor!!.getMusicDataByPosition(MyAppManager.selectMusicPos)

        val notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Music player")
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(
                        0,
                        1,
                        2
                    ) // Indexes of buttons in your custom layout
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(createPendingIntent(ActionEnum.CANCEL))
            )
            .setCustomContentView(createRemoteView())
            .build()

        val albumArtBitmap = if (musicData.albumId != null) {
            val albumArtUri =
                Uri.parse("content://media/external/audio/albumart/${musicData.albumId}")
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentResolver.loadThumbnail(albumArtUri, Size(256, 256), null)
                } else {
                }
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        val metadataBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, musicData.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, musicData.title)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, musicData.duration)
        albumArtBitmap?.let {
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, it as Bitmap?)
        }

        mediaSession?.setMetadata(metadataBuilder.build())

        startForeground(1, notification)
    }

    private fun createMediaSession() {
        mediaSession = MediaSessionCompat(baseContext, "My Music")

        mediaSession!!.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        mediaSession!!.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                super.onPlay()
                doneCommand(ActionEnum.PLAY)
            }

            override fun onPause() {
                super.onPause()
                doneCommand(ActionEnum.PAUSE)

            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                doneCommand(ActionEnum.NEXT)
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                doneCommand(ActionEnum.PREV)
            }

            override fun onSeekTo(pos: Long) {
                super.onSeekTo(pos)
                MyAppManager.currentTime = pos
                doneCommand(ActionEnum.SEEKBAR)
            }
        })
        timer = object : CountDownTimer(Long.MAX_VALUE, 250) {
            override fun onTick(millisUntilFinished: Long) {
                updateMediaPlaybackState(MyAppManager.currentTime)
            }

            override fun onFinish() {
            }

        }.start()
    }

    private fun updateMediaPlaybackState(currentPos: Long) {
        val state =
            if (mediaPlayer.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        val com = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
                        or PlaybackStateCompat.ACTION_PAUSE
                        or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        or PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(
                state,
                currentPos,
                1f,
                SystemClock.elapsedRealtime()
            )
            .build()


        mediaSession!!.setPlaybackState(
            com
        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
