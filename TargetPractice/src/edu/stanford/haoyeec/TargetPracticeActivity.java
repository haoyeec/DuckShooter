package edu.stanford.haoyeec;


import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TargetPracticeActivity extends Activity {
	
	private DuckView duck1;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		LinearLayout top = (LinearLayout) findViewById(R.id.topcontainer);

		// Make the monkey view
		final DuckView duck = new DuckView(this);
		//if(duck == null) System.out.println("NULL");
		duck.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));

		// Install it
		top.addView(duck);
		duck1 = duck;
		// start shooting
		Button start = (Button) findViewById(R.id.button1);
		start.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				duck.beginGame();
				
			}
			
		});

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		TextView levelboard = (TextView) findViewById(R.id.level);
		levelboard.setText("Press Start");
    }
    
    
    @Override
    protected void onPause(){
    	super.onPause();
    	duck1.stop();
    }
}