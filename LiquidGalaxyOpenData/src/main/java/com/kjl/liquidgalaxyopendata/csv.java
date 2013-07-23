package com.kjl.liquidgalaxyopendata;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joan on 23/07/13.
 */
public class csv {

    String filename;
    BufferedReader csvReader;



    public BufferedReader getCsvReader() {
        return csvReader;
    }

    public void setCsvReader(String filename) {
        File csv = new File(Environment.getExternalStorageDirectory()+"/LGOD/"+filename);
        InputStream inputStream = null;
        try {
            inputStream= new FileInputStream(csv);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        this.filename = filename;
        this.csvReader = reader;
    }
    public ArrayList<String> getLine(int lineNumber){
        String line;
        ArrayList<String> values = new ArrayList<String>();
        int i = 0;
        try {
            setCsvReader(this.filename);
            while(((line = this.csvReader.readLine()) != null) || i<=lineNumber) {
                if(i==lineNumber){
                    String[] strings = line.split(";");
                    for(String s : strings) {
                        values.add(s);
                    }
                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return values;
    }

    //read a csv with a [x,y] cordinades field
    public ArrayList<Placemark> readCSV(int namePosition,
                                        int descriptionPosition,
                                        int coordinatesPosition){

        ArrayList<Placemark> placemarks = new ArrayList<Placemark>();
        Placemark currentPlacemark = new Placemark();
        String line;

        try {
            setCsvReader(this.filename);
            while((line = this.csvReader.readLine()) != null) {
                String[] strings = line.split(";");

                currentPlacemark = new Placemark();
                currentPlacemark.setTitle(strings[namePosition]);
                currentPlacemark.setDescription(strings[descriptionPosition]);
                currentPlacemark.setCoordinates(strings[coordinatesPosition]);

                placemarks.add(currentPlacemark);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return placemarks;
    }
    //read a csv with separate x and y cordinades field
    public ArrayList<Placemark> readCSV(int namePosition,
                                        int descriptionPosition,
                                        int xPosition, int yPosition){

        ArrayList<Placemark> placemarks = new ArrayList<Placemark>();
        Placemark currentPlacemark = new Placemark();
        String line;

        try {
            setCsvReader(this.filename);
            while((line = this.csvReader.readLine()) != null) {
                String[] strings = line.split(";");

                currentPlacemark = new Placemark();
                currentPlacemark.setTitle(strings[namePosition]);
                currentPlacemark.setDescription(strings[descriptionPosition]);
                currentPlacemark.setCoordinates(strings[xPosition]+","+strings[yPosition]);

                placemarks.add(currentPlacemark);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return placemarks;
    }

}
