package com.app.simplemusicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
class MainActivity : AppCompatActivity(), View.OnClickListener {

    // 재생, 일시정지, 정지 버튼에 대한 Button 객체를 선언
    lateinit var btn_play: Button
    lateinit var btn_pause: Button
    lateinit var btn_stop: Button

    // 음악 플레이어와 상호작용할 서비스 변수를 선언
    var mService: MusicPlayerService? = null

    // ServiceConnection 객체를 선언하고, 서비스가 연결되었을 때와 연결이 끊어졌을 때의 동작을 정의
    val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // 서비스가 연결되면, 서비스를 MusicPlayerBinder 타입으로 캐스팅하여 mService 변수에 할당
            mService = (service as MusicPlayerService.MusicPlayerBinder).getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // 서비스 연결이 끊어지면 mService를 null로 설정
            mService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 각 버튼에 대한 참조를 가져옴
        btn_play = findViewById(R.id.btn_play)
        btn_pause = findViewById(R.id.btn_pause)
        btn_stop = findViewById(R.id.btn_stop)

        // 각 버튼의 클릭 리스너를 현재 액티비티로 설정
        btn_play.setOnClickListener(this)
        btn_pause.setOnClickListener(this)
        btn_stop.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        // 클릭된 버튼의 ID에 따라 적절한 메서드를 호출
        when (v?.id) {
            R.id.btn_play -> {
                play()
            }

            R.id.btn_pause -> {
                pause()
            }

            R.id.btn_stop -> {
                stop()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // 만약 서비스가 null이라면, 서비스를 시작하고 바인드
        if (mService == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this,
                    MusicPlayerService::class.java))
            } else {
                startService(Intent(applicationContext, MusicPlayerService::class.java))
            }
            val intent = Intent(this, MusicPlayerService::class.java)
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onPause() {
        super.onPause()

        // 서비스가 null이 아니고, 음악이 재생 중이지 않다면, 서비스를 중지하고 연결을 해제
        if (mService != null) {
            if (!mService!!.isPlaying()) {
                mService!!.stopSelf()
            }
            unbindService(mServiceConnection)
            mService = null
        }
    }

    // 각각 재생, 일시정지, 정지 메서드는 서비스의 해당 메서드를 호출
    private fun play() {
        mService?.play()
    }

    private fun pause() {
        mService?.pause()
    }

    private fun stop() {
        mService?.stop()
    }
}

