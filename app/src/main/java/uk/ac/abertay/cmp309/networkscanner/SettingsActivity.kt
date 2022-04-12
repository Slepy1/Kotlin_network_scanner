package uk.ac.abertay.cmp309.networkscanner

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //saves entered number in internal storage, this number is later used to determine range of ports to scan one to x ports
        var fileHelper: FileHelper? = null
        fileHelper = FileHelper.getInstance(applicationContext)

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            val portRange = findViewById<TextView>(R.id.portsToScanEditText)
            fileHelper?.saveToInternalStorage("networkScannerSettings.txt", portRange.text.toString())
            Toast.makeText(applicationContext, "Settings saved", Toast.LENGTH_LONG).show()
        }

        //get previously entered number and put it into textview
        //done this way as there is weird invisible character at the end of settings string...
        var numberOfPorts = ""
        try {
            var settings = fileHelper?.loadFromInternalStorage("networkScannerSettings.txt")//get the settings file
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
        findViewById<TextView>(R.id.portsToScanEditText).text = numberOfPorts

    }
}
