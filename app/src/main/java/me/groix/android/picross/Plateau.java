package me.groix.android.picross;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

enum StateCursor {
	BLACKCURSOR, WHITECURSOR, CROSSCURSOR
}
public class Plateau implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean[][] squaresBool;
	private int nbRow; //num
	private int nbCol;
	private String title;
	private String author;
	private String id;
	
	private Square[][] grid;
	private List<List<Integer>> columns;
	private List<List<Integer>> rows;
	private List<List<Boolean>> columnsBool;
	private List<List<Boolean>> rowsBool;

	private StateCursor stateCursor;
	private int cptErreur;
	
	public static boolean[][] defaultPattern = {{true,false,false,false,true},
		{false,false,true,false,false},
		{false,false,true,false,false},
		{true,false,false,false,true},
		{false,true,true,true,false}
	};


	/**
	 * Crée un Plateau par défault
	 */
	public Plateau() {
		this(5,5,defaultPattern,"Test","Coco","null");
		stateCursor = StateCursor.BLACKCURSOR;
		cptErreur = 0;
	}

	/**
	 * Constructeur complet
	 //* @param nbLigne2
	 //* @param nbColonne2
	 //* @param pattern
	 //* @param titre
	 */
	public Plateau(int nbLigne, int nbColonne, boolean[][] caseBool,
			String title, String auteur,String id) {
		this.nbRow = nbLigne;
		this.nbCol = nbColonne;
		this.squaresBool = caseBool;
		this.title = title;
		this.author = auteur;
		this.id = id;
		
		columns = new ArrayList<List<Integer>>();
		rows = new ArrayList<List<Integer>>();;
		columnsBool = new ArrayList<List<Boolean>>();;
		rowsBool = new ArrayList<List<Boolean>>();
	
		
		grid = new Square[getRowNum()][getColNum()];
		for (int lig=0;lig<nbLigne;lig++) {
			for (int col=0;col<nbColonne;col++) {
				grid[lig][col] = new Square(squaresBool[lig][col]?Type.BLACK :Type.WHITE,lig,col,this);
			}
		}

		//remplit les Listes d'indice, entiers et booleens, par défaut: true
		int cpt=0;
		for(int lig = 0; lig< getRowNum(); lig++) {
			rows.add(new ArrayList<Integer>());
			rowsBool.add(new ArrayList<Boolean>());
			if (cpt>0) {
				rows.get(lig-1).add(cpt);
				rowsBool.get(lig-1).add(true);
				cpt=0;
			}
			for (int col = 0; col< getColNum(); col++) {
				if (squaresBool[lig][col]) {
					cpt++;
				} else {
					if (cpt>0) {
						rows.get(lig).add(cpt);
						rowsBool.get(lig).add(true);
						cpt=0;
					}
				}
			}
		}
		if (cpt>0) {
			rows.get(getRowNum() -1).add(cpt);
			rowsBool.get(getRowNum() -1).add(true);
			cpt=0;
		}
		for(int col = 0; col< getColNum(); col++) {
			columns.add(new ArrayList<Integer>());
			columnsBool.add(new ArrayList<Boolean>());
			if (cpt>0) {
				columns.get(col-1).add(cpt);
				columnsBool.get(col-1).add(true);
				cpt=0;
			}
			for (int lig = 0; lig< getRowNum(); lig++) {
				if (squaresBool[lig][col]) {
					cpt++;
				} else {
					if (cpt>0) {
						columns.get(col).add(cpt);
						columnsBool.get(col).add(true);
						cpt=0;
					}
				}
			}
		}
		if (cpt>0) {
			columns.get(getColNum() -1).add(cpt);
			columnsBool.get(getColNum() -1).add(true);
		}

		stateCursor = StateCursor.BLACKCURSOR;
		cptErreur = 0;
	}


	/**
	 * Met à jour les tableaux colonnesBool et lignesBool en fonction des cases déjà 
	 * découvertes
	 */
	public void updateIndices() {
		for (int lig = 0; lig< getRowNum(); lig++) {
			int col=0;
			int mot=0;
			int cpt=0;
			while (mot<rows.get(lig).size() && grid[lig][col].IsDiscovery()) {
				if (grid[lig][col].estNoire()) {
					cpt++;
				}
				if (rows.get(lig).get(mot)==cpt) {
					rowsBool.get(lig).set(mot,false);
					mot++;
					cpt=0;
				}
				col++;
				if (col>= getColNum()) {
					break;
				}
			}
			mot = rows.get(lig).size()-1;
			cpt = 0;
			col = getColNum() -1;
			while (mot>=0 && grid[lig][col].IsDiscovery()) {
				if (grid[lig][col].estNoire()) {
					cpt++;
				}
				if (rows.get(lig).get(mot)==cpt) {
					rowsBool.get(lig).set(mot,false);
					mot--;
					cpt = 0;
				}
				col--;
				if (col==0) {
					break;
				}
			}
		}
		for (int col = 0; col< getColNum(); col++) {
			int lig=0;
			int mot=0;
			int cpt=0;
			while (mot< columns.get(col).size() && grid[lig][col].IsDiscovery()) {
				if (grid[lig][col].estNoire()) {
					cpt++;
				}
				if (columns.get(col).get(mot)==cpt) {
					columnsBool.get(col).set(mot,false);
					mot++;
					cpt=0;
				}
				lig++;
				if (lig>= getRowNum()) {
					break;
				}
			}
			mot = columns.get(col).size()-1;
			cpt = 0;
			lig = getRowNum() -1;
			while (mot>=0 && grid[lig][col].IsDiscovery()) {
				if (grid[lig][col].estNoire()) {
					cpt++;
				}
				if (columns.get(col).get(mot)==cpt) {
					columnsBool.get(col).set(mot,false);
					mot--;
					cpt=0;
				}
				lig--;
				if (lig==0) {
					break;
				}
			}
		}
	}
	
	/**
	 * Méthode permettant de savoir si le joueur à découvert
	 * le motif
	 * @return true si le jeu est terminé
	 */
	public boolean Isfinished() {
		for (int lig = 0; lig<this.getRowNum(); lig++) {
			for (int col = 0; col<this.getColNum(); col++) {
				if (!grid[lig][col].IsDiscovery())
					return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Incrémente le compteur
	 */
	public void error() {
		cptErreur++;
	}

	/**
	 * retourne le nombre error
	 */
	public int getCptErreur() {
		return cptErreur;
	}
	
	/**
	 * Retourne le tableau des cases du jeu
	 */
	public Square[][] getGrille() {
		return grid;
	}
	
	public int getRowNum() {
		return nbRow;
	}
	
	public int getColNum() {
		return nbCol;
	}
	
	public void setStateCursor(StateCursor stateCursor) {
		this.stateCursor = stateCursor;
	}

	public StateCursor getStateCursor() {
		return stateCursor;
	}

	/**
	 * @return the columns
	 */
	public List<List<Integer>> getColumns() {
		return columns;
	}

	/**
	 * @return the lignes
	 */
	public List<List<Integer>> getLignes() {
		return rows;
	}

	/**
	 * @return the colonnesBool
	 */
	public List<List<Boolean>> getColonnesBool() {
		return columnsBool;
	}

	/**
	 * @return the lignesBool
	 */
	public List<List<Boolean>> getLignesBool() {
		return rowsBool;
	}

	/**
	 * @return the titre
	 */
	public String getTitre() {
		return title;
	}

	/**
	 * @return the auteur
	 */
	public String getAuteur() {
		return author;
	}
	
	/**
	 * Retourne l'ID
	 */
	public String getID() {
		return id;
	}
}