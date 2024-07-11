//package com.example.musicapp.presenter.service
//
//import android.annotation.SuppressLint
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.app.Service
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import android.content.res.Resources
//import android.graphics.Bitmap
//import android.graphics.drawable.BitmapDrawable
//import android.media.AudioManager
//import android.media.MediaPlayer
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.os.CountDownTimer
//import android.os.IBinder
//import android.os.SystemClock
//import android.support.v4.media.MediaMetadataCompat
//import android.support.v4.media.session.MediaSessionCompat
//import android.support.v4.media.session.PlaybackStateCompat
//import androidx.annotation.RequiresApi
//import androidx.core.app.NotificationCompat
//import com.example.mymusicservise.R
//import com.example.mymusicservise.data.model.MusicData
//import com.example.mymusicservise.data.model.ServiceEnum
//import com.example.mymusicservise.data.sourse.MyShar
//import com.example.mymusicservise.presenter.activity.MainActivity
//import com.example.mymusicservise.utils.MyMusicModel
//import com.example.uzummarketclient.utils.getMusicDataByPosition
//import com.example.uzummarketclient.utils.myErrorLog
//import com.example.uzummarketclient.utils.myLog
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.flow.flow
//import kotlinx.coroutines.launch
//import java.io.File
//import kotlin.random.Random
//
//
//@SuppressLint("MissingPermission", "UseCompatLoadingForDrawables")
//class MyService : Service() {
//    private val CHANNEL = "DEMO"
//    private var _mediaPlayer: MediaPlayer? = null
//    private val mediaPlayer get() =   _mediaPlayer!!
//    private val scope = CoroutineScope(Dispatchers.IO+ Job())
//    private val receiver = MyBroadcast()
//    override fun onBind(intent: Intent?): IBinder? = null
//    private var job:Job?=null
//     private var oldPos:Int = -1
//    private lateinit var mediaSession: MediaSessionCompat
//    var timer:CountDownTimer?=null
//    private lateinit var myShar:MyShar
//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    override fun onCreate() {
//        super.onCreate()
//        _mediaPlayer = MediaPlayer()
//        createMediaSession()
//        createChanel()
//        val intent=IntentFilter()
//        intent.addAction(Intent.ACTION_MEDIA_BUTTON)
//        intent.addAction(Intent.ACTION_TIME_TICK)
//            registerReceiver(receiver,intent,Context.RECEIVER_EXPORTED)
//        MyShar.init(this)
//        myShar= MyShar.getInstance()
//    }
//    private fun createChanel() {
//        if (Build.VERSION.SDK_INT >= 26) {
//            val channel =
//                NotificationChannel("DEMO", CHANNEL, NotificationManager.IMPORTANCE_DEFAULT)
//            channel.setSound(null, null)
//            val service = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//            service.createNotificationChannel(channel)
//        }
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        try {
//            val command = intent!!.extras?.getSerializable("COMMAND") as ServiceEnum
//            doneCommand(command)
//        }catch (ex:Exception){
//
//        }
//        return START_STICKY
//    }
//
//    private fun doneCommand(command: ServiceEnum) {
//        val data: MusicData =
//            if (MyMusicModel.isFavorite)MyMusicModel.favoriteList!![MyMusicModel.pos]
//        else MyMusicModel.cursor?.getMusicDataByPosition(MyMusicModel.pos,this)!!
//        when (command) {
//            ServiceEnum.PLAY -> {
//                MyMusicModel.fullTime = data.duration
//                MyMusicModel.id=data.id
//
//                MyMusicModel.isPlayingLiveData.value = true
//                if (oldPos!=MyMusicModel.pos){
//                    mediaPlayer.seekTo(0)
//                    mediaPlayer.stop()
//                    _mediaPlayer = MediaPlayer.create(this, Uri.fromFile(File(data.data)))
//                    MyMusicModel.playMusicLiveData.value=data
//                }
//                mediaPlayer.start()
//                mediaPlayer.setOnCompletionListener { doneCommand(ServiceEnum.NEXT) }
//                mediaPlayer.seekTo(MyMusicModel.currentTime.toInt())
//                job?.cancel()
//
//                job=scope.launch {
//                    changeProgress().collectLatest {
//                        MyMusicModel.currentTime = mediaPlayer.currentPosition.toLong()
//                        MyMusicModel.currentTimeLiveData.postValue(mediaPlayer.currentPosition.toLong())
//                    }
//                }
//                MyMusicModel.newMusic.value=Unit
//                startMyService(true)
//                oldPos=MyMusicModel.pos
//            }
//
//            ServiceEnum.NEXT -> {
//                MyMusicModel.currentTimeLiveData.value=0
//                MyMusicModel.currentTime = 0
//                if (myShar.isShuffle()){
//                    MyMusicModel.pos=ranDomNumber(MyMusicModel.pos)
//                }else{
//                    if (!MyMusicModel.isFavorite){
//                        if (MyMusicModel.pos + 1 == MyMusicModel.cursor!!.count) MyMusicModel.pos = 0
//                        else MyMusicModel.pos++
//                    }else{
//                        if (MyMusicModel.pos + 1 == MyMusicModel.favoriteList!!.size) MyMusicModel.pos = 0
//                        else MyMusicModel.pos++
//                    }
//                }
//
//                doneCommand(ServiceEnum.PLAY)
//            }
//
//            ServiceEnum.PREV -> {
//                MyMusicModel.currentTimeLiveData.value=0
//                MyMusicModel.currentTime = 0
//                if (myShar.isShuffle()){
//                    MyMusicModel.pos=ranDomNumber(MyMusicModel.pos)
//                }else{
//                    if (!MyMusicModel.isFavorite){
//                        if (MyMusicModel.pos == 0) MyMusicModel.pos = MyMusicModel.cursor!!.count - 1
//                        else MyMusicModel.pos--
//                    }else{
//                        if (MyMusicModel.pos == 0) MyMusicModel.pos = MyMusicModel.favoriteList!!.size-1
//                        else MyMusicModel.pos--
//                    }
//                }
//                doneCommand(ServiceEnum.PLAY)
//            }
//            ServiceEnum.MERGE -> {
//                if (mediaPlayer.isPlaying) doneCommand(ServiceEnum.PAUSE)
//                else doneCommand(ServiceEnum.PLAY)
//            }
//            ServiceEnum.PAUSE -> {
//                job?.cancel()
//
//                mediaPlayer.pause()
//                MyMusicModel.isPlayingLiveData.value = false
//                startMyService(false)
//            }
//            ServiceEnum.SEEK->{
//                mediaPlayer.seekTo(MyMusicModel.currentTime.toInt())
//                job?.cancel()
//
//                job=scope.launch {
//                    changeProgress().collectLatest {
//                        MyMusicModel.currentTime = mediaPlayer.currentPosition.toLong()
//                        MyMusicModel.currentTimeLiveData.postValue(mediaPlayer.currentPosition.toLong())
//                    }
//                }
//            }
//        }
//    }
//    private fun changeProgress(): Flow<Long> = flow {
//        for (i in MyMusicModel.currentTime until MyMusicModel.fullTime step 300) {
//            delay(300)
//            emit(i)
//        }
//        "changeProgress:  success".myErrorLog()
//    }
//    override fun onDestroy() {
//        super.onDestroy()
//        job?.cancel()
//
//        unregisterReceiver(receiver)
//    }
//    private fun startMyService(isPlaying:Boolean){
//        val intent = Intent(this, MainActivity::class.java).apply {
//            putExtra("OPEN_FRAGMENT", true)
//        }
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//        val pendingIntent = PendingIntent.getActivity(
//            this,
//            0,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val musicData =if (MyMusicModel.isFavorite)MyMusicModel.favoriteList!![MyMusicModel.pos] else MyMusicModel.cursor?.getMusicDataByPosition(MyMusicModel.pos,this)!!
//        val bitmap:Bitmap
//        if (musicData.image==null){
//            val res: Resources = this.resources
//            val drawable = res.getDrawable(R.drawable.music_icon)
//            val bitmapDrawable = drawable as BitmapDrawable
//            bitmap = bitmapDrawable.bitmap;
//        }else{
//            bitmap=musicData.image
//        }
//        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, "DEMO")
//            .setSmallIcon(R.drawable.icon_music,10000)
//            .setLargeIcon(bitmap)
//            .setOngoing(isPlaying)
//            .setContentIntent(pendingIntent)
//            .setStyle(
//                androidx.media.app.NotificationCompat.MediaStyle()
//                    .setShowActionsInCompactView(0 ,1,2)
//                    .setMediaSession(mediaSession.getSessionToken())
//            )
//        mediaSession.setMetadata(MediaMetadataCompat.Builder()
//            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
//            .putString(
//                MediaMetadataCompat.METADATA_KEY_ARTIST,
//                musicData.artist
//            )
//            .putString(
//                MediaMetadataCompat.METADATA_KEY_TITLE,
//                musicData.title
//            )
//            .putLong(
//                MediaMetadataCompat.METADATA_KEY_DURATION,
//                musicData.duration
//            )
//            .build()
//        )
//
////        val notificationManager = NotificationManagerCompat.from(this)
////        notificationManager.notify(0, builder.build())
//        startForeground(1,builder.build())
//
//    }
//    private fun updateMediaPlaybackState(currentPos: Long) {
//        val state=if (mediaPlayer.isPlaying)PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
//        val com= PlaybackStateCompat.Builder()
//            .setActions(
//                PlaybackStateCompat.ACTION_PLAY
//                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
//                        or PlaybackStateCompat.ACTION_PAUSE
//                        or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
//                        or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
//                        or PlaybackStateCompat.ACTION_SEEK_TO
//            )
//            .setState(
//                state,
//                currentPos,
//                1f,
//                SystemClock.elapsedRealtime()
//            )
//            .build()
//
//
//        mediaSession.setPlaybackState(
//           com
//        )
//    }
//    private fun createMediaSession(){
//        mediaSession = MediaSessionCompat(this, "tag")
//
//        mediaSession.setFlags(
//            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
//                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
//        )
//        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
//            override fun onPlay() {
//                super.onPlay()
//                doneCommand(ServiceEnum.PLAY)
//            }
//
//            override fun onPause() {
//                super.onPause()
//                doneCommand(ServiceEnum.PAUSE)
//
//            }
//
//            override fun onSkipToNext() {
//                super.onSkipToNext()
//                doneCommand(ServiceEnum.NEXT)
//            }
//
//            override fun onSkipToPrevious() {
//                super.onSkipToPrevious()
//                doneCommand(ServiceEnum.PREV)
//            }
//
//            override fun onSeekTo(pos: Long) {
//                super.onSeekTo(pos)
//                MyMusicModel.currentTime=pos
//                doneCommand(ServiceEnum.SEEK)
//            }
//        })
//        timer=object :CountDownTimer(Long.MAX_VALUE,300){
//            override fun onTick(millisUntilFinished: Long) {
//                updateMediaPlaybackState(MyMusicModel.currentTime)
//            }
//            override fun onFinish() {
//            }
//
//        }.start()
//    }
//
//    @RequiresApi(Build.VERSION_CODES.S)
//    fun isMusicActive(context: Context): Boolean {
//        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//
//
//        return audioManager.isMusicActive
//    }
//    private fun ranDomNumber(pos:Int):Int{
//        if (!MyMusicModel.isFavorite){
//            if (MyMusicModel.cursor!!.count==1)return 0
//            val ran=Random.nextInt(0,MyMusicModel.cursor!!.count)
//            if (ran!=pos){
//                return ran
//            }
//        }else{
//            if (MyMusicModel.favoriteList!!.size==1)return 0
//            val ran=Random.nextInt(0,MyMusicModel.favoriteList!!.size)
//            if (ran!=pos){
//                return ran
//            }
//        }
//        return ranDomNumber(pos)
//    }
//}
