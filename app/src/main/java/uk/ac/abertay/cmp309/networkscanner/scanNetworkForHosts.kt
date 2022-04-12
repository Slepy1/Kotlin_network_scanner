package uk.ac.abertay.cmp309.networkscanner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetAddress


class scanNetworkForHosts : AppCompatActivity() {

    private var listOfPingableHosts = mutableListOf("")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_network_for_hosts)

        //get users IP from main activity
        val iPAddress = intent.getStringExtra("yourIPAddress").toString()
        //removes last part from IP. later this will be used by ping function
        val croppedIP = removeLastPartFromIP(iPAddress)

        //created adapter for the listview
        val arrayAdapter: ArrayAdapter<*>
        val mListView = findViewById<ListView>(R.id.hostsFoundListView)
        arrayAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1, listOfPingableHosts
        )
        mListView.adapter = arrayAdapter

        //selected IP on the viewlist is passed to another activity to scan its ports
        mListView.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as String
            intent = Intent(this, scanHostForPorts::class.java)
            intent.putExtra("IPAddress", selectedItem)
            startActivity(intent)
        }

        //onClick listener - allows user to enter IP into textview and scan ports that way (allows to scan non local IPs)
        findViewById<Button>(R.id.scanPortsButton).setOnClickListener {
                val suppliedIP = findViewById<TextView>(R.id.IPAddressTextView)
                intent = Intent(this, scanHostForPorts::class.java)
                intent.putExtra("IPAddress", suppliedIP.text.toString())
                startActivity(intent)
        }

        for (i in 0..255) { //it scans x.x.x.y where x is users ip and y is a counter 1 - 255, so technically not a subnetwork but close enough.
            lifecycle.coroutineScope.launch(Dispatchers.IO) { //lifecycle coroutine, its like a thread but is aware about livecycle components
             if(ping(i, croppedIP)) {
                 runOnUiThread {//update listview when new item was added, it has to be done from the ui thread
                     arrayAdapter.notifyDataSetChanged()
                 }
             }
            }
        }

    }

    private fun removeLastPartFromIP(IPAddress: String): String {//split supplied IP and return first 3 parts eg - 192.168.0.
        val splitIP = IPAddress.split(".")
        return splitIP[0] + "." + splitIP[1] + "." + splitIP[2] + "."
    }

    private fun ping(counter: Int, network: String): Boolean { //takes in string IP and then "pings" a selected ip address
        try {
            val connectionSuccessful = InetAddress.getByName(network + counter).isReachable(2000) //pings
            if (connectionSuccessful) {
                Log.d("ping", "$network$counter is open")
                listOfPingableHosts.add(network + counter)
                return true
            } else if (!connectionSuccessful) {
                Log.d("ping", "$network$counter is closed")
                return false
            }
        } catch (e: Exception) {
            Log.e("networking error", e.toString())
            return false
        }
        return false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the state of item position
        //outState.putAll()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

    }
}