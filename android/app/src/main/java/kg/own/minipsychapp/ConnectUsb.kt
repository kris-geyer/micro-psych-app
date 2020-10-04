package kg.own.minipsychapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import com.felhr.usbserial.UsbSerialInterface.UsbReadCallback
import kotlinx.android.synthetic.main.connect_ui.*
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*


class ConnectUsb: Activity(), View.OnClickListener {

    private var errorMessages = ""
    private val serviceConnection: ServiceConnection? = null
    private var ardunioConnection: ArduinoConnection? = null
    private var serialDevice: UsbSerialDevice? = null
    private var handler: Handler? = null
    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connect_ui)
        handler = Handler()

        registerReceiver(ardunioConnection, IntentFilter())
        val intent: Intent? = intent
        if (intent!=null){
            appendError( "intent != null")
            val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
            if (device!= null){
                appendError( "device != null")
                val usbManager: UsbManager? = getSystemService(Context.USB_SERVICE) as UsbManager
                if (usbManager != null){
                    appendError( "Usb Manager is not null")
                    startToLoadService(device, usbManager)
                }
            }
        }

        button.setOnClickListener(this)
        stop_connect.setOnClickListener(this)
        send_message.setOnClickListener(this)
        make_blink.setOnClickListener(this)
    }

    private fun startToLoadService(usbDevice: UsbDevice, usbManager: UsbManager) {
        val deviceConnection = usbManager.openDevice(usbDevice)
        serialDevice = UsbSerialDevice.createUsbSerialDevice(usbDevice, deviceConnection)

        if (serialDevice != null) {
            appendError("Serial device != null")
            serialDevice!!.open()
            serialDevice!!.setBaudRate(9600)
            serialDevice!!.setDataBits(UsbSerialInterface.DATA_BITS_8)
            serialDevice!!.setStopBits(UsbSerialInterface.STOP_BITS_1)
            serialDevice!!.setParity(UsbSerialInterface.PARITY_NONE)
            serialDevice!!.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
            //Set up callback from cyton
            val cytonCallBack = UsbReadCallback { data: ByteArray? -> this.readDataFromArduino(data) }
            serialDevice!!.read(cytonCallBack)

            startTimer();


        }else{
            return
        }
    }

    private fun startTimer(){
        timer = Timer()
        val monitor = object : TimerTask() {
            override fun run() {
                sendMessage("h")
            }
        }
        timer!!.schedule(monitor, 1000, 1000)
    }

    private fun stopTimer(){
        timer?.cancel()
        timer?.purge()

    }

    private fun sendMessage(msg: String){
        serialDevice!!.write(msg.toByteArray())
    }

    private fun readDataFromArduino(data: ByteArray?) {
        if (data != null){
            appendError(StandardCharsets.UTF_8.decode(ByteBuffer.wrap(data)).toString())
        }
    }

    private fun appendError(msg:String){
        errorMessages += "\n"
        errorMessages += msg
    }

    private fun returnError(){
        //textView_result.text = errorMessages
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)

        alertDialog.setMessage(errorMessages)
            .create().show()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            button.id -> returnError()
            stop_connect.id -> stopTimer()
            send_message.id -> startTimer()
            make_blink.id -> sendMessage("e")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (timer!=null){
            timer!!.cancel()
            timer!!.purge()
        }

    }
}
