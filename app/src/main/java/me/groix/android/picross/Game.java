package me.groix.android.picross;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

enum action {
	NOTHING, BLACKEN, WHITEN
}

public class Game extends Activity {

	private Plateau plateau;
	private GamesDB gamesDB;


	//UI components
	FrameLayout mainDisplay;
	RelativeLayout plateauView;
	RelativeLayout labelsColView;
	RelativeLayout labelsLigView;
	private TextView[][] columnLabels;
	private TextView[][] rowLabels;
	private ImageView[][] squaresList;
	Button IBwhite;
	Button IBcross;
	Button IBblack;
	
	TextView numError;
	Chronometer chrono;
	TextView bravo;


	private int lastClickedSquareX;
	private int lastClickedSquareY;

	private float lastTouchX;
	private float lastTouchY;
	private float absolutePosX;
	private float absolutePosY;
	
	private action lastAction;

	//a few constantes
	private static final int SQUARELEN = 50;
	private static final int LABALELEN = 60;
	private static final int LONGLABELCOL = 80;
	private static final int MARGIN = 20;
	private static final int MARGINCOL = 30;
	private static final int LONGCHARLIG = 15;
	private static final int LONGCHARCOL = 20;
	private static final int CHARSIZE = 20;
	private static final int CHARSIZELITE = 15;

	Options option;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);
		Bundle extras;
		if (savedInstanceState!=null) {
			extras = savedInstanceState;
		} else {
			extras = getIntent().getExtras();
		}
		//TODO To change a better lifecycle management
		//gets the plateau that was passed from listpuz activity
		plateau = (Plateau) extras.getSerializable("plateau");
        gamesDB = new GamesDB(this);

		//ADD MAINVIEW AND BUTTONS
		View mainLayout = ((View) findViewById(R.id.mainLayout));
		mainLayout.setBackgroundColor(Color.WHITE);
		mainDisplay = (FrameLayout) findViewById(R.id.mainDisplay);
		IBwhite = (Button) findViewById(R.id.BoutonCursorWhite);
		IBblack = (Button) findViewById(R.id.BoutonCursorBlack);
		IBcross = (Button) findViewById(R.id.BoutonCursorCross);
		switch (plateau.getStateCursor()) {
			case BLACKCURSOR:
				IBblack.setSelected(true);
				break;
			case WHITECURSOR:
				IBwhite.setSelected(true);
				break;
			case CROSSCURSOR:
				IBcross.setSelected(true);
				break;
		}




		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LABALELEN +plateau.getColNum()* SQUARELEN +MARGIN,
				LONGLABELCOL+plateau.getRowNum()* SQUARELEN +MARGIN);
		plateauView = new RelativeLayout(this);
		mainDisplay.addView(plateauView, params);


		labelsColView = new RelativeLayout(this);
		labelsLigView = new RelativeLayout(this);
		mainDisplay.addView(labelsColView);
		mainDisplay.addView(labelsLigView);


		squaresList = new ImageView[plateau.getRowNum()][plateau.getColNum()];
		//TODO mémoire gaspillée et risque d'erreurs: a voir si on doit modifier
		rowLabels = new TextView[plateau.getRowNum()][plateau.getColNum()];
		columnLabels = new TextView[plateau.getColNum()][plateau.getRowNum()];

		option = new Options();
		option.inScaled = false;

		numError = (TextView) findViewById(R.id.cptError);
		numError.setText("Without failures");

		chrono = (Chronometer) findViewById(R.id.chrono);
		chrono.setBase(SystemClock.elapsedRealtime()-extras.getLong("chrono"));
		bravo = (TextView) findViewById(R.id.bravo);

		if (plateau.Isfinished()) {
			bravo.setText(R.string.bravo);
		} else {
			chrono.start();
		}
		
		fillSquares();
		fillLabels();
		updateLabels();
		labelsColView.bringToFront();
		labelsLigView.bringToFront();


		lastClickedSquareX = plateau.getRowNum();
		lastClickedSquareY = plateau.getColNum();
		absolutePosX = 0;
		absolutePosY = 0;

		plateauView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				System.out.println("MO:"+event.getAction()+" "+event.getX()+event.getY()
						+" "+((int) (event.getX() - LABALELEN)/ SQUARELEN)
						+":"+((int) (event.getY() - LONGLABELCOL)/ SQUARELEN));
				if (plateau.getStateCursor()== StateCursor.WHITECURSOR ||
						((event.getX()+absolutePosX) < LABALELEN) ||
						((event.getY()+absolutePosY)<LONGLABELCOL) ||
						((event.getX()+absolutePosX) > (LABALELEN + SQUARELEN *plateau.getColNum()) ) ||
						((event.getY()+absolutePosY) > (LONGLABELCOL+ SQUARELEN *plateau.getRowNum()) )   ){
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						lastTouchX = event.getX();
						lastTouchY = event.getY();
						break;
					case MotionEvent.ACTION_MOVE:
						absolutePosX +=(lastTouchX - event.getX());
						absolutePosY +=(lastTouchY - event.getY());

						plateauView.scrollTo((int)(absolutePosX),
								(int)(absolutePosY));
						labelsLigView.scrollTo((int) absolutePosX,(int)(absolutePosY));
						labelsColView.scrollTo((int)(absolutePosX),(int) absolutePosY);
						lastTouchX = event.getX();
						lastTouchY = event.getY();
						
					}
					return true;
				} else {
					if (event.getAction()==MotionEvent.ACTION_DOWN 
							|| event.getAction()==MotionEvent.ACTION_MOVE) {

						for (int h = 0; h < event.getHistorySize(); h++) {
							float absolutTouchX = absolutePosX + event.getHistoricalX(h);
							float absolutTouchY = absolutePosY + event.getHistoricalY(h);
							if (absolutTouchX> LABALELEN && absolutTouchX<(LABALELEN + SQUARELEN *plateau.getColNum())
									&& absolutTouchY>LONGLABELCOL && absolutTouchY<(LONGLABELCOL+ SQUARELEN *plateau.getRowNum())) {
								int touchedCaseX = (int) (absolutTouchX - LABALELEN)/ SQUARELEN;
								int touchedCaseY = (int) (absolutTouchY - LONGLABELCOL)/ SQUARELEN;

								if(touchedCaseX != lastClickedSquareX || touchedCaseY != lastClickedSquareY) {
									auToucher(plateau.getGrille()[touchedCaseY][touchedCaseX]);
									plateau.updateIndices();
									squaresList[touchedCaseY][touchedCaseX].setImageBitmap(getBitMap(plateau.getGrille()[touchedCaseY][touchedCaseX]));
									lastClickedSquareX = touchedCaseX;
									lastClickedSquareY = touchedCaseY;
								}
							}
						}
						float relativePosX = absolutePosX + event.getX();
						float relativePosY = absolutePosY + event.getY();
						if (relativePosX> LABALELEN && relativePosX<(LABALELEN + SQUARELEN *plateau.getColNum())
								&& relativePosY>LONGLABELCOL && relativePosY<(LONGLABELCOL+ SQUARELEN *plateau.getRowNum())) {
							int touchedCaseX = (int) (relativePosX - LABALELEN)/ SQUARELEN;
							int touchedCaseY = (int) (relativePosY - LONGLABELCOL)/ SQUARELEN;

							if(touchedCaseX != lastClickedSquareX || touchedCaseY != lastClickedSquareY) {
								//On applique le tour
								//We apply the turn
								auToucher(plateau.getGrille()[touchedCaseY][touchedCaseX]);
								plateau.updateIndices();
								lastClickedSquareX = touchedCaseX;
								lastClickedSquareY = touchedCaseY;
								//UI:
								squaresList[touchedCaseY][touchedCaseX].setImageBitmap(getBitMap(plateau.getGrille()[touchedCaseY][touchedCaseX]));
								numError.setText(plateau.getCptErreur()==0?"Without failures":
									(plateau.getCptErreur()+ " error"+((plateau.getCptErreur()==1)?"":"s")));
								updateLabels();

								if (plateau.Isfinished()) {
									chrono.stop();
									gamesDB.open();
									gamesDB.newGame(plateau.getID(),plateau.getCptErreur(),
											(int) ((SystemClock.elapsedRealtime()-chrono.getBase())/1000));
									bravo.setText(R.string.bravo);
									gamesDB.close();
								}
							}
						}
					} else {
						lastAction = action.NOTHING;
						lastClickedSquareX = plateau.getRowNum();
						lastClickedSquareY = plateau.getColNum();
					}
					return true;
				}
			}

		});
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("plateau", plateau);
		outState.putLong("chrono",SystemClock.elapsedRealtime()-chrono.getBase());
	}

	/**
	 * Create the ImageView representing the boxes(squares), put them in the squaresList array and in the relativeLayout
	 */
	private void fillSquares() {
		for (int lig = 0; lig<plateau.getRowNum(); lig++) {
			for (int col = 0; col<plateau.getColNum(); col++) {
				ImageView iv = new ImageView(this);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(SQUARELEN, SQUARELEN);
				params.leftMargin = LABALELEN +col* SQUARELEN;
				params.topMargin = LONGLABELCOL+lig* SQUARELEN;
				iv.setImageBitmap(getBitMap(plateau.getGrille()[lig][col]));
				iv.setAdjustViewBounds(false);
				iv.setScaleType(ScaleType.FIT_CENTER);
				squaresList[lig][col] = iv;
				plateauView.addView(iv, params);
			}
		}
	}

	/**
	 * Create and fill the label "index"
	 */
	public void fillLabels() {
		for(int col = 0; col<plateau.getColNum(); col++) {
			for (int mot = 0; mot<plateau.getColumns().get(col).size(); mot++) {
				TextView tv = new TextView(this);
				tv.setText(" "+plateau.getColumns().get(col).get(plateau.getColumns().get(col).size()-1-mot));
				tv.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);
				tv.setTextSize(CHARSIZE);
				tv.setTextColor(Color.BLACK);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(30,30);
				params.leftMargin = LABALELEN +col* SQUARELEN +MARGIN;
				params.topMargin = LONGLABELCOL-LONGCHARCOL*(mot)-MARGINCOL;
				labelsColView.addView(tv, params);
				columnLabels[col][mot] = tv;
			}
		}
		for (int lig = 0; lig<plateau.getRowNum(); lig++) {
			for (int mot=0;mot<plateau.getLignes().get(lig).size();mot++) {
				TextView tv = new TextView(this);
				tv.setText(" "+plateau.getLignes().get(lig).get(plateau.getLignes().get(lig).size()-1-mot));
				tv.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);
				tv.setTextSize(CHARSIZE);
				tv.setTextColor(Color.BLACK);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(30,30);
				params.leftMargin = LABALELEN -LONGCHARLIG*(mot+1);
				params.topMargin = LONGLABELCOL+ SQUARELEN *lig+MARGIN;
				labelsLigView.addView(tv, params);
				rowLabels[lig][mot] = tv;
			}
		}
	}



	/**
	 * Updates the labels "index"
	 */
	public void updateLabels() {
		for(int col = 0; col<plateau.getColNum(); col++) {
			for (int mot = 0; mot<plateau.getColumns().get(col).size(); mot++) {
				columnLabels[col][mot].setText(""+plateau.getColumns().get(col).get(plateau.getColumns().get(col).size()-1-mot));
				if (!(plateau.getColonnesBool().get(col).get(plateau.getColumns().get(col).size()-1-mot))) {
					columnLabels[col][mot].setTypeface(Typeface.MONOSPACE,Typeface.ITALIC);
					columnLabels[col][mot].setTextSize(CHARSIZELITE);
				}
			}
		}
		for (int lig = 0; lig<plateau.getRowNum(); lig++) {
			for (int mot=0;mot<plateau.getLignes().get(lig).size();mot++) {
				rowLabels[lig][mot].setText(""+plateau.getLignes().get(lig).get(plateau.getLignes().get(lig).size()-1-mot));
				if (!(plateau.getLignesBool().get(lig).get(plateau.getLignes().get(lig).size()-1-mot))) {
					rowLabels[lig][mot].setTypeface(Typeface.MONOSPACE,Typeface.ITALIC);
					rowLabels[lig][mot].setTextSize(CHARSIZELITE);
				}
			}
		}
	}

	/**
	 A method that returns the bitMap corresponding to the input square, in
	 * Depends on its state and type
	 * @return The corresponding bitmap
	 */
	private Bitmap getBitMap(Square square) {
		if (square.getType()==Type.WHITE) {
			if (square.getState()==State.UNDISCOVERED) {
				//return BitmapFactory.decodeResource(this.getResources(), R.drawable.blanc);
				return getBitMapWhite(square);
			} else {
				return getBitMapCross(square);
			}
		} else {
			if (square.getState()==State.UNDISCOVERED) {
				//return BitmapFactory.decodeResource(this.getResources(), R.drawable.blanc);
				return getBitMapWhite(square);
			} else { if (square.getState()==State.CROSS) {
				return getBitMapCross(square);
			} else {
				return getBitMapBlack(square);
			}
			}
		}
	}

	private Bitmap getBitMapWhite(Square square) {
		if ((square.getX() %5)==0) {
			if ((square.getY()%5)==0) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.blancgh,option);
			}
			if((square.getY()%5)==4) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.blancdh,option);
			}
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.blanch,option);
		}
		if ((square.getX()%5)==4) {
			if ((square.getY()%5)==0) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.blancgb,option);
			}
			if((square.getY()%5)==4) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.blancdb,option);
			}
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.blancb,option);
		}
		if ((square.getY()%5)==0) {
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.blancg,option);
		}
		if((square.getY()%5)==4) {
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.blancd,option);
		}
		return BitmapFactory.decodeResource(this.getResources(), R.drawable.blanc,option);
	}

	private Bitmap getBitMapBlack(Square square) {
		if ((square.getX() %5)==0) {
			if ((square.getY()%5)==0) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.noirgh);
			}
			if((square.getY()%5)==4) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.noirdh);
			}
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.noirh);
		}
		if ((square.getX()%5)==4) {
			if ((square.getY()%5)==0) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.noirgb);
			}
			if((square.getY()%5)==4) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.noirdb);
			}
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.noirb);
		}
		if ((square.getY()%5)==0) {
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.noirg);
		}
		if((square.getY()%5)==4) {
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.noird);
		}
		return BitmapFactory.decodeResource(this.getResources(), R.drawable.noir);
	}

	private Bitmap getBitMapCross(Square square) {
		if ((square.getX() %5)==0) {
			if ((square.getY()%5)==0) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.croixgh);
			}
			if((square.getY()%5)==4) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.croixdh);
			}
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.croixh);
		}
		if ((square.getX()%5)==4) {
			if ((square.getY()%5)==0) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.croixgb);
			}
			if((square.getY()%5)==4) {
				return BitmapFactory.decodeResource(this.getResources(), R.drawable.croixdb);
			}
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.croixb);
		}
		if ((square.getY()%5)==0) {
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.croixg);
		}
		if((square.getY()%5)==4) {
			return BitmapFactory.decodeResource(this.getResources(), R.drawable.croixd);
		}
		return BitmapFactory.decodeResource(this.getResources(), R.drawable.croix);
	}

	/**
	 * applique la méthode correspondante à la case donnée, en fonction du curseur séléctionné
	 * @param square
	 */
	public void auToucher(Square square) {
		switch (square.getPlateau().getStateCursor()) {
		case BLACKCURSOR:
			square.blacken();
			break;
		case WHITECURSOR:
			break; //on ne fait rien
		case CROSSCURSOR:
			square.crossing(); break;
		}
	}



	public void boutonCurseurBlanc(View view) {
		plateau.setStateCursor(StateCursor.WHITECURSOR);
		IBwhite.setSelected(true);
		IBblack.setSelected(false);
		IBcross.setSelected(false);
		
	}

	public void boutonCurseurNoir(View view) {
		plateau.setStateCursor(StateCursor.BLACKCURSOR);
		IBwhite.setSelected(false);
		IBblack.setSelected(true);
		IBcross.setSelected(false);

	}

	public void boutonCurseurCroix(View view) {
		plateau.setStateCursor(StateCursor.CROSSCURSOR);
		IBwhite.setSelected(false);
		IBblack.setSelected(false);
		IBcross.setSelected(true);
	}



}

