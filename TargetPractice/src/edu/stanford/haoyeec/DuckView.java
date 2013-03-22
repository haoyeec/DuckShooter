package edu.stanford.haoyeec;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.view.*;
import android.widget.TextView;
public class DuckView extends View{

	// settings
	private int level;
	private final float minSpeed;
	private final int rangeSpeed;
	private final int lives;
	
	// status
	private int score;
	private int fail;
	private boolean inGame;

	private long mUpdateTime;  // time of last run
	private long mClickTime;
	
	private float x_t;
	private float y_t;

	private float dx_t;
	private float dy_t;
	
	private float x_dead;
	private float y_dead;
	private boolean dead;

	// sound
	private SoundPool sp;
	private int soundID;
	
	private SoundPool endGameSp;
	private int endGameSoundID;
	private int endGameStreamID;

	private MediaPlayer startMp; // SoundPool is used for tiny clips and effects
	
	// images
	private Bitmap target;
	private Bitmap shot;
	private Bitmap life;
	private Bitmap shit;
	
	// display
	private TextView scoreboard;
	private TextView levelboard;
	private AlertDialog alertDialog;

		
	public DuckView(Context context){
		super(context);

		// images
		setBackgroundResource(R.drawable.pond);
		target = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.duck), 150, 150,  false);
		shot = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.dead), 75, 75,  false);
		life = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.rubberduck), 50, 50,  false);
		
		
		if(life == null) System.out.println("HOHO");
		// settings
		minSpeed = 100;
		level = 1;
		rangeSpeed = 200;
		score = 0;
		fail = 0;
		inGame = false;
		lives = 3;
		
		// position
		x_t = -1.0f * target.getWidth();
		y_t = 50;
		
		dx_t = 100;
		dy_t = 0;
		
		sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		soundID = sp.load(context, R.raw.quack, 1);
		
		endGameSp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		endGameSoundID = endGameSp.load(getContext(), R.raw.endgame, 1);

		startMp = MediaPlayer.create(context, R.raw.startgame);
		startMp.setLooping(true);
		
		
		dead = false;
		
	}

	@Override
	protected void onDetachedFromWindow(){
		super.onDetachedFromWindow();
		endGameSp.stop(endGameStreamID);
		startMp.reset();
	}
	
	public void stop(){
		startMp.stop();
		try{
			startMp.prepare();
			startMp.seekTo(0);
		}catch(Exception e){
			startMp = MediaPlayer.create(getContext(), R.raw.startgame);
			startMp.setLooping(true);
			
		}
		getHandler().removeCallbacks(mUpdateState);
		randomizeHeight();
		inGame = false;
		invalidate();
		if(fail >= lives){

			endGameStreamID = endGameSp.play(endGameSoundID, 1.0f, 1.0f, 0, -1, 1.0f);
		}

	}
	
	// Notification of touch events on the view
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		int action = event.getAction();

		if (!inGame){
			if (scoreboard != null && levelboard != null){
				levelboard.setText("Press Start!");
				scoreboard.setText("");
			}
			return false;
		}
	

		if (action == MotionEvent.ACTION_DOWN) {
			if (x_t <= x && x < x_t + target.getWidth() &&
					y_t <= y && y < y_t + target.getHeight()) {

				// update displays
				mClickTime = System.currentTimeMillis();
				score+=1;
				scoreboard.setText("Kills: " + Integer.toString(score));
				x_dead = x - shot.getWidth()/2;
				y_dead = y - shot.getWidth()/2;
				
				level = (score - 1)/ 5 + 1;
				levelboard.setText("Level " + Integer.toString(level));
				
				// play death sound
				sp.play(soundID, 1,1,0,0,1);
				
				// update graphics
				
				randomizeHeight();
				invalidate();

			}
		}


		return true;
	}

	public void beginGame(){
		
		// hack so that I can getWidth() and getHeight() without crashing : View needs to be created first
		shit = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.shit), getWidth(), getHeight(),  false);

		// start music
		endGameSp.pause(endGameStreamID);
		startMp.start();
		
		//prompt 
		alertDialog = new AlertDialog.Builder(getContext()).create();
		alertDialog.setTitle("Shoot them pesky birds!");
		alertDialog.setMessage("Ducks have been invading my favorite spot! GRRR.... Don't let them escape. If 3 of them escape, they'll make a mess of my park.");
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				startMoving();

			}
		});
		alertDialog.show();
		
	}
	
	public void startMoving(){
		// reset
		score = 0;
		level = 1;
		fail = 0;
		randomizeHeight();
		invalidate();
		inGame = true;
		scoreboard = (TextView) ((View)this.getParent()).findViewById(R.id.score);
		scoreboard.setText("Kills: " + Integer.toString(score));
		levelboard = (TextView) ((View)this.getParent()).findViewById(R.id.level);
		levelboard.setText("Level " + Integer.toString(level));
		
		
		// start moving
		mUpdateTime = System.currentTimeMillis();
		getHandler().removeCallbacks(mUpdateState);  // Here being cautious -- avoid proliferating runnables
		getHandler().post(mUpdateState);
		
	}

	public void onDraw(Canvas canvas){
		// target 
		canvas.drawBitmap(target, x_t, y_t, null);
		
		// blood splatter
		if(dead){
			canvas.drawBitmap(shot, x_dead, y_dead, null);
		}
		
		// lives
		if(inGame)
			for (int i = 0; i < lives - fail; i++){
				canvas.drawBitmap(life, getWidth() - (i+1)*life.getWidth() - 10, getHeight() - life.getHeight(), null);
		
			}
		
		// post game
		if (fail >= lives && !inGame){
			// hack onDraw
			
			canvas.drawBitmap(shit, 0, 0, null);
		}

	}

	// Code we'll have run 50x a second.
	private Runnable mUpdateState = new Runnable() {
		public void run() {

			// Running 50x per second is approximate -- fit the computation
			// to the exact elapsed time.
			long now = System.currentTimeMillis();
			float elapsed = (now - mUpdateTime)/1000.0f;
			mUpdateTime = now;

			if(mUpdateTime - mClickTime < 1000){
				dead = true;
			}
			else{
				dead = false;
			}
			
			x_t += dx_t * elapsed;  // mDX is pixels per second, so scale to elapsed time
			y_t += dy_t * elapsed;

			wrapAround();
			invalidate();


			// We post a pointer to ourselves to run again in 20ms
			if(fail < lives && inGame)
				getHandler().postDelayed(this, 20);
		}
	};

	public void wrapAround() {

		// let a target escape
		if (x_t >= getWidth()){
			randomizeHeight();
			fail++;
			if (fail == lives){
				levelboard.setText("Game Over!");
				stop();
			}
		}

		if (y_t < 0) y_t += getHeight();
		if (y_t >= getHeight()) y_t -= getHeight();
	}

	
	/** Randomizes dx/dy */
	public void randomizeHeight() {
		dx_t = (float)(Math.random() * rangeSpeed+ minSpeed * level);
		dy_t = 0.0f;
		
		x_t = -1.0f * target.getWidth();
		y_t = (float)(Math.random() * (getHeight() - target.getHeight() - life.getHeight()));
		
	}

}
