package com.app.simplemusicplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast

// MusicPlayerService 클래스 정의(MediaPlayer 인스턴스와 MusicPlayerBinder 인스턴스를 가진다)
class MusicPlayerService : Service() {

    var mMediaPlayer: MediaPlayer? = null // 미디어 플레이어 객체를 null로 초기화

    // MusicPlayerBinder 클래스 정의 : 내부 Binder 확장해서 서비스에 바인드 할 수 있는 기능 제공
    var mBinder: MusicPlayerBinder = MusicPlayerBinder() // 바인더 객체 생성

    // 바인더 클래스 정의 // inner class : 바깥쪽 클래스의 인스턴스에 엑세스 할 수 있음을 나타냄
    inner class MusicPlayerBinder : Binder() { //MusicPlayerBinder는 binder 상속 받음
        // 서비스 인스턴스 반환 메서드
        fun getService(): MusicPlayerService { // getService() 메소드는 클라이언트가 MusicPlayerService 인스턴스에 접근할 수 있게 한다
            return this@MusicPlayerService
        }
    }
    // 서비스가 처음 생성될 때 호출되는 콜백 함수
    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    // 클라이언트가 서비스에 바인드하려고 시도 할 때 호출되는 콜백 함수
    override fun onBind(intent: Intent?): IBinder? {
        // 바인드 반환
        return mBinder
    }

    // startService()를 호출하면 실행되는 콜백 함수
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 서비스가 종료된 후에도 재시작하도록 설정
        return START_STICKY
    }



    // 알림 채널 생성
    fun startForegroundService() {
        // 안드로이드 버전이 오레오 이상일 경우 알림채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val mChannel = NotificationChannel( // 알림 채널을 생성합니다.
                "CHANNEL_ID",
                "CHANNEL_NAME",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(mChannel)
        }

        // 알림 생성
        val notification: Notification = Notification.Builder(this, "CHANNEL_ID")                .setSmallIcon(R.drawable.ic_play) // 알림 아이콘입니다.
            .setContentTitle("뮤직 플레이어 앱")   // 알림의 제목을 설정
            .setContentText("앱이 실행 중입니다.")  // 알림의 내용을 설정
            .build()

        startForeground(1, notification)
    }

    // 서비스 중단 처리
    override fun onDestroy() {
        super.onDestroy()
        // 안드로이드 버전이 오레오 이사일 경우 포그라운드 서비스 중지
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
    }

    // 재생되고 있는지 확인
    fun isPlaying() : Boolean {
        return (mMediaPlayer != null && mMediaPlayer?.isPlaying ?: false)
    }


    // 재생 메서드
    fun play() {
        if (mMediaPlayer == null) {
            // 음악 파일 리소스 미디어 플레이어 객체 생성
            mMediaPlayer = MediaPlayer.create(this, R.raw.chocolate)
            // 볼륨 설정
            mMediaPlayer?.setVolume(1.0f, 1.0f)
            // 반복 재생 설정
            mMediaPlayer?.isLooping = true
            // 음악 재생
            mMediaPlayer?.start()
        } else {
            if (mMediaPlayer!!.isPlaying) {
                // 이미 재생중 일 경우 토스트 메세지 출력
                Toast.makeText(this,  "이미 음악이 실행 중입니다.", Toast.LENGTH_SHORT).show()
            } else {
                // 음악 재생
                mMediaPlayer?.start()
            }
        }
    }
    fun pause() { // ❷
        mMediaPlayer?.let {
            if (it.isPlaying) {
                it.pause() // 음악을 일시정지합니다.
            }
        }
    }
    fun stop() { // 미디어 중지 메서드
        // 재생 중일 경우만 중지
        mMediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                // 미디어 플레이어 지원 해제
                it.release()
                // mMediaPlayer null 로 설정
                mMediaPlayer = null
            }
        }

    }

}
