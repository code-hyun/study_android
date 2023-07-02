package com.app.qrcodereader

interface OnDetectLinstener {
    fun onDetect(msg: String) // QR코드 인식 되었을 때 호출할 함수, 내용을 인수로 받는다
}