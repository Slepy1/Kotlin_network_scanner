package uk.ac.abertay.cmp309.networkscanner

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.*
import java.net.Socket
import java.time.LocalDateTime

class scanHostForPorts : AppCompatActivity() {

    private val CHANNEL_ID_NORMAL = "NetworkScannerNormal"
    private var notificationManager: NotificationManager? = null
    private var textNotification: Notification.Builder? = null
    private val NOTIFICATION_ID_TEXT = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_host_for_ports)

        //Check for open ports by trying to open a socket.
        var iPAddress = "192.168.0.1"
        iPAddress = intent.getStringExtra("IPAddress").toString()

        //created adapter for the listview
        val listOfOpenPorts = mutableListOf("")
        val arrayAdapter: ArrayAdapter<*>
        val mListView = findViewById<ListView>(R.id.openPortsListView)
        arrayAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1, listOfOpenPorts
        )
        mListView.adapter = arrayAdapter

        //get range of ports to scan from settings file
        var fileHelper: FileHelper? = null
        fileHelper = FileHelper.getInstance(applicationContext)
        var numberOfPorts = ""
        try {
            val settings = fileHelper?.loadFromInternalStorage("networkScannerSettings.txt")//get the settings file
            val splitNumberOfPorts: List<String>? = settings?.split("")
            for (counter in 1..settings?.count()!!-1) {
                //for some reason there is some character at the back of the data from internal storage preventing .toInt() function from working. also dropLast() function did not remove this weird thing so I have to use this loop.
                numberOfPorts += splitNumberOfPorts?.get(counter)
            }
        }
        catch (e : Exception) {
            Log.e("DEBUG_TAG", e.toString())
            numberOfPorts = "65535"
        }

        //create a notification with progress bar
        val progressBarMax = numberOfPorts.toInt()
        var progressBarCurrent = 0
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        initNotificationChannels()//set up channels
        textNotification = Notification.Builder(applicationContext,CHANNEL_ID_NORMAL)
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .setContentTitle("Scanning $iPAddress ...")
            .setAutoCancel(false)

        textNotification!!.setProgress(progressBarMax, progressBarCurrent, false)
        notificationManager!!.notify(NOTIFICATION_ID_TEXT, textNotification!!.build())

        //onClick listener - starts new activity that tries to share the text report
        findViewById<Button>(R.id.shareButton).setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, "The scan of $iPAddress started at " + LocalDateTime.now().toString() + " has found following network ports to be open - " + listOfOpenPorts.joinToString(" "))
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent, "Share To:"))
        }

        //start the scan
         lifecycle.coroutineScope.launch(Dispatchers.IO) {//this cool "thread like thing" allows app to run in the background
            for (port in 1..numberOfPorts.toInt()) {
                try {
                    val socket = Socket(iPAddress, port)//if connection is successful then the port is open
                    if (socket.isConnected)
                    {
                        listOfOpenPorts.add(port.toString())
                        runOnUiThread {//update listview when new item was added, it has to be done from the ui thread
                            arrayAdapter.notifyDataSetChanged()
                        }
                        Log.d("open port", "Port $port is open")
                    }
                    socket.close()
                } catch (e: Exception) { // An error is thrown when Socket cannot be created, that means its closed, or something went wrong... works in both cases.
                    Log.e("error", "scanning port $port - $e")
                }

                if ((port % 30) == 1 ) { //use modulo function to limit number of progress bar notification updates , y % x means it will update every x times
                    textNotification!!.setProgress(progressBarMax, port, false)//updates progress bar in the notification
                    notificationManager!!.notify(NOTIFICATION_ID_TEXT, textNotification!!.build())
                }

            }
            textNotification!!.setContentText("Scan finished!").setProgress(0, 0, false)
            notificationManager!!.notify(NOTIFICATION_ID_TEXT, textNotification!!.build())

        }
    }

    override fun onBackPressed() {
        super.onBackPressed()//when user tries to go back stop the scan and finish the notification
        textNotification!!.setContentText("Scan Stopped!").setProgress(0, 0, false)
        notificationManager!!.notify(NOTIFICATION_ID_TEXT, textNotification!!.build())
        onDestroy()//there are better ways of doing this but im low on time...
    }

    private fun initNotificationChannels() {
        /* If using older version which does not support channels, ignore this */
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        /* Create all channels and add them to the list */
        val channelList =
            ArrayList<NotificationChannel>()
        channelList.add(
            NotificationChannel(
                CHANNEL_ID_NORMAL,//don't use high, high displays a weird pop up
                "Default",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )

        /* Register all channels from the list. */if (notificationManager != null) notificationManager!!.createNotificationChannels(
            channelList
        )
    }

}