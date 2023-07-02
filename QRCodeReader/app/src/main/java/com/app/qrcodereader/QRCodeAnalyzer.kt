package com.app.qrcodereader

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class QRCodeAnalyzer(val onDetectListener: OnDetectLinstener) : ImageAnalysis.Analyzer {

    // 바코드 스캐닝 객체 생성
    private val scanner = BarcodeScanning.getClient()

    @SuppressLint("UnsafeOpInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val  mediaImage = imageProxy.image
        if(mediaImage != null){
            // 이미지 찍힐 당시 카메라의 회전각도를 고려하여 입력 이미지를 생성
            val image =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            // scanner.process(image)를 통해 이미지 분석
            scanner.process(image)
                .addOnSuccessListener { qrCodes ->
                    //리스너가 들어갈 자리
                    for(qrCode in qrCodes){
                        onDetectListener.onDetect(qrCode.rawValue ?: "")
                    }
                }
                .addOnFailureListener{
                    it.printStackTrace()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}