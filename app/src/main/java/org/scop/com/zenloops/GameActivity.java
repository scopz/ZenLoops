package org.scop.com.zenloops;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class GameActivity extends Activity {
    private GamePanel gamepanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Bundle extras = this.getIntent().getExtras();
        int level = 0;
        if (extras!=null){
            level = extras.getInt("level");
        }

        gamepanel = new GamePanel(this,true);
        setContentView(gamepanel);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gamepanel.saveState();
    }
}
