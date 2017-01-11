package me.groix.android.picross;

import java.io.Serializable;

public class Square implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3873923163669617419L;
	State state;
	Type type;
	
	
	private Plateau plateau; //משטח

	private int x;
	private int y;


	public Square(Type type, int X, int Y, Plateau plateau) {
		this.state = State.UNDISCOVERED; // לא ידוע
		this.type = type;
		this.x = X;
		this.y = Y;
		this.plateau = plateau;
	}

	/**
	 * 
	 * Return true if the player made an error, false otherwise
	 */
	//@ TODO: 10/01/2017 chk logic
	public boolean blacken() {
		if (type==Type.WHITE) {
			if (state==State.UNDISCOVERED) {
				setState(State.CROSS); //marked square
				plateau.error();
				return true;
			} else { if (state==State.CROSS) { //marked square
				setState(State.UNDISCOVERED);
				return false;
			} else {
				System.err.println("Error: illegal state"); //
				return false;
			}
			}
		} else {
			if (state==State.UNDISCOVERED) {
				setState(State.DISCOVERED);
				return false;
			} else { if (state==State.CROSS) {
				setState(State.UNDISCOVERED);
				return false;
			} else {
				return false;
			}
			}
		}
	}
	
	public boolean crossing() {
		if (type==Type.WHITE) { // The box must remain white / cross
			if (state==State.UNDISCOVERED) {
				setState(State.CROSS);
				return false;
			} else { if (state==State.CROSS) {
				setState(State.UNDISCOVERED);
				return false;
			} else {
				System.err.println("Error: illegal state");
				return false;
			}
			}
		} else { //The box is in blacken
			if (state==State.UNDISCOVERED) {
				setState(State.CROSS);
				return false;
			} else { if (state==State.CROSS) {
				setState(State.UNDISCOVERED);
				return false;
			} else { // the square is already blackened
					//nothing
				return false;
			}
			}
		}
	}


	/**
	 * A method of determining whether a box is correctly guessed by the player
	 * @return
	 */
	public boolean IsDiscovery() {
		return (this.type==Type.WHITE || this.state==State.DISCOVERED);
	}
	
	/**
	 * Method of knowing whether a box is of the Black type (whatever its
	 * Appearance on the board)
	 * @return
	 */
	public boolean estNoire() {
		return (this.type==Type.BLACK);
	}


	public void setState(State state) {
		this.state = state;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public Plateau getPlateau() {
		return plateau;
	}

	/**
	 * @return the state
	 */
	public State getState() {
		return state;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}
	
	

}

enum State {
	UNDISCOVERED, CROSS, DISCOVERED
}

enum Type {
	WHITE, BLACK
}
