package org.oar.zenloops

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowInsets
import org.oar.zenloops.elements.Grid
import org.oar.zenloops.ui.views.GameView

class GameActivity : Activity() {
    private val gameView: GameView by lazy { findViewById(R.id.panel) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_playing_grid)

        window.insetsController?.apply {
            hide(WindowInsets.Type.statusBars())
            hide(WindowInsets.Type.navigationBars())
        }

        val grid = gameView.loadState()
            ?: Grid(gameView, 46, 49).apply { randomFill() }

        gameView.viewTreeObserver.addOnGlobalLayoutListener(
            object: ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    // at this point, view dimensions are set (view.width and view.height)
                    gameView.setGrid(grid)
                    gameView.postInvalidate()
                    // make sure it is only executed once:
                    gameView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        )
    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        gameView.saveState()
    }

}