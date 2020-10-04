package kg.own.minipsychapp

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.connect_ui.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var errorMessages = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        connect_to_usb.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            connect_to_usb.id -> startConnecting()
        }
    }

    private fun startConnecting(){
        val usbManager: UsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val usbDevices: HashMap<String, UsbDevice> = usbManager.deviceList
        if (usbDevices.isNotEmpty()){
            for ((key, value) in usbDevices){
                appendError("key: $key value $value" )
            }
        }
    }

    private fun appendError(msg:String){
        errorMessages += "\n"
        errorMessages += msg
        returnError()
    }

    private fun returnError(){
        textView_results.text = errorMessages
    }
}
