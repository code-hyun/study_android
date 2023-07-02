package com.app.qrcodereader

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.app.qrcodereader.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// 뷰바인딩 : findViewById()를 대체해서 편리하게 사용 가능하다
// 안드로이드 젯팩 : 구글 공식 라이브러리   ex)CameraX : 미리보기, 이미지분석, 이미지캡쳐 기능 제공

/*구글 ML키트
* 구글 머신러닝 기술을 안드로이드, ios  모바일 기기에서 사용하게 해주는 라이브러리
* 바코드스캐닝, 얼굴인식, 텍스트 인식 같은 기능 사용*/

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding // 바인딩 변수 생성
    // ListenableFuture에 태스크가 재대로 끝났을 때 동작을 지정해줄 수 있다. (Future은 안드로이드 병렬 프로그래밍에서 태스크가 재대로 끝났는지 확인)
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    //태그 기능을 하는 코드, 나중에 권한 요청 후 onRequestPermissionResult에서 받을 때 필요
    private val PERMISSIONS_REQUEST_CODE = 1
    // 카메라 권한을 지정
    private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

    //이미지 분석이 실시간 계속 이뤄지므로 onDetect()함수가 여러번 호출 될수 있다, 이를 막는데 변수 isDetected 생성
    private var isDetected = false

    override fun onResume() {
        super.onResume()
        isDetected = false
    }

    fun getImageAnalysis() : ImageAnalysis {
        val cameraExecutor : ExecutorService = Executors.newSingleThreadExecutor()
        val imageAnalysis  = ImageAnalysis.Builder().build()

        // Analyzer를 설정
        imageAnalysis.setAnalyzer(cameraExecutor,
                QRCodeAnalyzer(object : OnDetectLinstener{
                    override fun onDetect(msg: String) {
                        if(!isDetected){// QR코드 인식 된적 있는지 검사
                            isDetected = true
                            val intent = Intent(this@MainActivity,
                            ResultActivity::class.java)
                            intent.putExtra("msg", msg)
                            startActivity(intent)
                        }
                    }
        }))
        return imageAnalysis
    }
    // 미리보기와 이미지분석 시작
    fun startCamera(){
        cameraProviderFuture = ProcessCameraProvider.getInstance(this) // cameraProviderFuture의 객체 참조값 할당
        cameraProviderFuture.addListener(Runnable { // 태스크가 끝나면 실행
            // 카메라 생명 주기를 액티비티나 프래그먼트와 같은 생명주기에 바인드 해주는 ProcessCameraProvider객체 가져옴
            val cameraProvider = cameraProviderFuture.get()
            // 미리 보기 객체를 가져옴
            val preview = getPreview()
            // imageAnalysis 클래스의 객체 생성
            val imageAnalysis = getImageAnalysis()
            // 후면카메라를 선택
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            // 미리보기, 이미지 분석, 이미지 캡쳐 중 무엇을 쓸지 지정
            cameraProvider.bindToLifecycle(this, cameraSelector,preview,imageAnalysis)

          }, ContextCompat.getMainExecutor(this))

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 뷰 바인딩 설정
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if(!hasPermissions(this)){
            // 카메라 권한을 요청
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        }else {
            // 이미 권한이 있다면 카메라 시작
            startCamera()
        }

        startCamera() // 카메라 시작
    }

    // 권한이 있는지 없는지 확인, all은 PERMISSIONS_REQUIRED 배열의 원소가 모두 조건무을 만족하면 true 아니면 false
                                                // 함수에서 '=' 뒤는 자동 함수의 반환값으로 인식
    private fun hasPermissions(context : Context) = PERMISSIONS_REQUIRED.all { // Context는 액티비티, 서비스, 어플리케이션 등 안드로이드의 주요 컴포넌트가 모두 상속받는 클래스, 거의 모든 곳에 접근 가능
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
    // 권한 요청에 대한 콜백 함수 Activity 클래스에 포함된 함수 이므로 오버라이드
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 권한이 수락 되면 startCamera()를 호출하고 거부되는 경우 액티비티 종료
        if(requestCode == PERMISSIONS_REQUEST_CODE){
            if(PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()){
                // Toast는 짧은 메세지를 잠깐 보여주는 역할
                Toast.makeText(this@MainActivity, "권한 요청이 승인 되었습니다.", Toast.LENGTH_LONG).show()
                startCamera()
            }else {
                Toast.makeText(this@MainActivity, "권한 요청이 거부 되었습니다.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }


    // 미리보기 객체 변환
    fun getPreview() : Preview {
        val preview : Preview = Preview.Builder().build() // Preview 객체 생성
        preview.setSurfaceProvider(binding.barcodePreview.surfaceProvider)
        return preview
    }
}
