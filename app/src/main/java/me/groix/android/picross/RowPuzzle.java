package me.groix.android.picross;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RowPuzzle extends ArrayAdapter<String> {

	private ArrayList<String> dirs;
	private AssetManager assets;
	private Context context;
	private GamesDB gamesDB;


	public RowPuzzle(Context context, int textViewResourceId, ArrayList<String> dirs) {
		super(context, textViewResourceId, dirs);
		this.dirs = dirs;
		this.context=context;
        gamesDB = new GamesDB(context);
        gamesDB.open();
		assets = context.getAssets();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); //Instantiates a layout XML file into its corresponding View objects
			v = vi.inflate(R.layout.rowpuzzle, null);
		}
		String dir = dirs.get(position);
		if (dir != null) {
			TextView tt = (TextView) v.findViewById(R.id.toptext);
			TextView mt = (TextView) v.findViewById(R.id.middletext);
			TextView bt = (TextView) v.findViewById(R.id.bottomtext);
			
			try {
				tt.setText(PicrossReader.readTitle(assets.open("puz/"+dir)));
				mt.setText(PicrossReader.readDimension(assets.open("puz/"+dir))+
						" by "+PicrossReader.readAuthor(assets.open("puz/"+dir)));
				Cursor cursor = gamesDB.getCursor(dir); //Cursor is the Interface which represents a 2 dimensional table of any database.
				// When you try to retrieve some data using SELECT statement,
				// then the database will first create a CURSOR object and return its reference to you.
				if (cursor.getCount()>0) {
					cursor.moveToFirst();
					//// TODO: 11/01/2017 change this
					bt.setText("Meilleur score: "+
							cursor.getInt(1) + " erreurs, "+
							cursor.getInt(2)+"s"); 
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return v;
	}
}