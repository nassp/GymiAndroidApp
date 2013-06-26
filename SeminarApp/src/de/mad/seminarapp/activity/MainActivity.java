package de.mad.seminarapp.activity;

import de.mad.seminarapp.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * An example App for the Seminar
 *
 * @author Patrick Na§
 */
public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // how to get view context
//        final View contentView = findViewById(R.id.fullscreen_content);



    }


    
    public void goToGameView(View view) {
    	// start new intent
    	Intent nextScreen = new Intent(getApplicationContext(), GameActivity.class); 
    	// send password to next view
    	final TextView text = (TextView)findViewById(R.id.textView1);
    	nextScreen.putExtra("password", text.getText().toString());
    	// Log for debug
    	Log.e("goToGameView: ", text.getText().toString()+".");
    	
    	startActivity(nextScreen);
    	//finish();
    }


//    /**
//     * Touch listener to use for in-layout UI controls to delay hiding the
//     * system UI. This is to prevent the jarring behavior of controls going away
//     * while interacting with activity UI.
//     */
//    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View view, MotionEvent motionEvent) {
//            
//            return false;
//        }
//    };



}
