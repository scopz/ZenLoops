package org.oar.zenloops

import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import org.oar.zenloops.elements.LinkGraphics
import org.oar.zenloops.utils.ScreenProperties

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        ScreenProperties.load(this);
        LinkGraphics.loadGraphics(this)

        startGame(0)
    }

    private fun startGame(level: Int) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("level", level)
        startActivity(intent)
    }
}