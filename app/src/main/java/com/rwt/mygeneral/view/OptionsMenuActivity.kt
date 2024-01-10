package com.rwt.mygeneral.view

import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.rwt.mygeneral.R

/*
 *      OptionsMenuActivity
 */
open class OptionsMenuActivity : AppCompatActivity() {

    open val OTAG = "MyGeneral::OptionsMenuActivity"

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.i(OTAG, " onCreateOptionsMenu <")
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)
        Log.i(OTAG, " onCreateOptionsMenu >")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(OTAG, " onOptionsItemSelected <")
        // Handle item selection.
        return when (item.itemId) {
            R.id.menu_profile -> {
                Log.i(OTAG, " onOptionsItemSelected - menu_profile")
                //newGame()
                true
            }
            R.id.menu_settings -> {
                Log.i(OTAG, " onOptionsItemSelected - menu_settings")
                //newGame()
                true
            }
            R.id.menu_share -> {
                Log.i(OTAG, " onOptionsItemSelected - menu_share")
                //showHelp()
                true
            }
            R.id.menu_contactus -> {
                Log.i(OTAG, " onOptionsItemSelected - menu_contactus")
                //showHelp()
                true
            } else -> super.onOptionsItemSelected(item)
        }
    }

}

