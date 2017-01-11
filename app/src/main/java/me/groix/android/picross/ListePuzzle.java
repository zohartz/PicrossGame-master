package me.groix.android.picross;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ListePuzzle extends Activity {
	
	
	ArrayList<String> listeDir;
	AssetManager assets; //Provides access to an application's raw asset files
	ListView lv;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listpuz);
        lv = (ListView) findViewById(R.id.list);
        fillList();
        //registerForContextMenu(getListView());
    }

	private void fillList() {
		listeDir = new ArrayList<String>();
		assets = getAssets(); //get data from asset
		try {
			for (String dir: assets.list("puz")) {
				listeDir.add(dir);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,long id) {
				// Get the item that was clicked
				String dir = "puz/" + listeDir.get(position);
				//getApplicationContext -Returns the context for all activities running in application
		        Intent i = new Intent(getApplicationContext(), Game.class);
		        try {
					i.putExtra("plateau",PicrossReader.read(assets.open(dir),listeDir.get(position)));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				i.putExtra("chrono",(long) 0);
		        startActivityForResult(i,1);
		        //TODO  changer ts les trucs statiques
			}
			
		});
		
		lv.setAdapter(new RowPuzzle(this,android.R.layout.simple_list_item_1,listeDir));
	}
	


}
