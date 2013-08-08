package com.kjl.liquidgalaxyopendata;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by Joan on 7/08/13.
 */
public class dataBank {
    String bankpath;

    ArrayList<dataSource> dataSources;


    //create a dataBank from an stored databank file
    public dataBank() {
        dataSources = new ArrayList<dataSource>();
        bankpath = Environment.getExternalStorageDirectory()+"/LGOD/conf/databank.conf";
        File path = new File(Environment.getExternalStorageDirectory()+"/LGOD/conf/");
        path.mkdirs();
        File bank = new File(bankpath);
        // if file doesnt exists, then create it
        if (!bank.exists()) {
            try {
                bank.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        InputStream inputStream=null;
        try {
            inputStream= new FileInputStream(bank);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;

        try {
            while((line = reader.readLine()) != null) {
                String[] strings = line.split(";");
                //if the file doesn't exist ignore it
                if(new File(strings[2]).exists()){
                    this.dataSources.add(new dataSource(strings[0],strings[1],strings[2]));
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<dataSource> getDataSources() {
        return dataSources;
    }
    public void setDataSources(ArrayList<dataSource> dataSources) {
        this.dataSources = dataSources;
        this.saveDataBank();
    }

    @Override
    public String toString() {
        String dataString="";
        for (int i=0; i<dataSources.size(); i++){
            dataString+=dataSources.get(i).toString()+"\n";
        }
        return dataString;
    }
    public void addDataSource(String name, String url, String path){
        //check if this datasource already exists
        this.removeline(name);
        this.dataSources.add(new dataSource(name,url,path));
        this.saveDataBank();
    }

    public void saveDataBank(){
        try {
            File path = new File(Environment.getExternalStorageDirectory()+"/LGOD/conf/");
            path.mkdirs();

            File file = new File(bankpath);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(this.toString());
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeline(String name){
        for (int i = 0; i<dataSources.size(); i++){
            if (dataSources.get(i).getName().equalsIgnoreCase(name)){
                dataSources.remove(i);
                this.saveDataBank();
            }
        }
    }




}
