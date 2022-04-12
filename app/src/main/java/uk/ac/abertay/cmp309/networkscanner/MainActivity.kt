package uk.ac.abertay.cmp309.networkscanner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.text.format.Formatter
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity()  {
    private var connManager: ConnectivityManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()

        //check the network capabilities and displays appropriate message based on that
        connManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = connManager!!.activeNetwork
        val netCaps = connManager!!.getNetworkCapabilities(net)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Network report")
        alertDialogBuilder.setPositiveButton("Ok") { _, _ ->
        }
        when {//compiler cried about this being if....
            netCaps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> {//connected to wifi message
                alertDialogBuilder.setMessage("Connected to WIFI")
            }
            netCaps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> {//connected to cellular data message
                //I tried using "wifiManager.setWifiEnabled(true)" to turn on the wifi but that did not work for me, so just open the wifi settings
                alertDialogBuilder.setMessage("You are using your cellular data, the application might not operate properly.")
                alertDialogBuilder.setNegativeButton("Turn on WIFI") { _, _ ->
                    val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(intent)
                }
            }
            netCaps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> {//connected to Ethernet message
                alertDialogBuilder.setMessage("Connected to ethernet... somehow")
            }
            else -> {//failed message
                //I tried using "wifiManager.setWifiEnabled(true)" to turn on the wifi but that did not work for me, so just open the wifi settings
                alertDialogBuilder.setMessage("No connection to internet detected")
                alertDialogBuilder.setNegativeButton("Turn on WIFI") { _, _ ->
                    val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(intent)
                }
            }
        }
        alertDialogBuilder.show()

        //try to get users ip address
        var yourIPAddress = "192.168.0.1"
        try {
            val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager//try to find a different way
            yourIPAddress = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)

            findViewById<TextView>(R.id.yourIPAddressTextView).text = "Your IP address:$yourIPAddress"
        } catch(e : Exception) {
            findViewById<TextView>(R.id.yourIPAddressTextView).text = "Your IP address: Error"
            Log.e("error", "failed getting your IP - $e")
        }

        //onClick listener - starts new activity that pings all hosts on the users subnetwork and displays IPs that responded
        findViewById<Button>(R.id.displayHostsButton).setOnClickListener {
            intent = Intent(this, scanNetworkForHosts::class.java)
            intent.putExtra("yourIPAddress", yourIPAddress)
            startActivity(intent)
        }

        //onClick listener - starts new activity with options page, allows user to select between light and dark mode.
        findViewById<Button>(R.id.settingsButton).setOnClickListener {
            intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkPermissions(){//checks permissions
        val checkPermission1 = checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        val checkPermission2 = checkSelfPermission(Manifest.permission.INTERNET) //the internet permission technically does not need to be checked here but I still do it to be safe.
        val checkPermission3 = checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE)
        val checkPermission4 = checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE)
        val needToAsk =
            !((checkPermission1 == PackageManager.PERMISSION_GRANTED) and (checkPermission2 == PackageManager.PERMISSION_GRANTED) and (checkPermission3 == PackageManager.PERMISSION_GRANTED) and (checkPermission4 == PackageManager.PERMISSION_GRANTED))
        if (needToAsk) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE
                ), 0
            )
        }

    }
}