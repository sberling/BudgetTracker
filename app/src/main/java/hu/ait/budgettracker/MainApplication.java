package hu.ait.budgettracker;

import android.app.Application;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by samberling on 5/21/17.
 */

public class MainApplication extends Application {
    private Realm transactionRealm;
    private ArrayList<String> incomeCategories;
    private ArrayList<String> outcomeCategories;
    private String FILENAME = "categories";

    @Override
    public void onCreate() {
        super.onCreate();

        incomeCategories = new ArrayList<>();
        outcomeCategories = new ArrayList<>();

        readCategories();

        Realm.init(this);
    }

    private void readCategories() {
        try {
            FileInputStream fin = openFileInput(FILENAME);
            int c;
            String rawStream="";
            while( (c = fin.read()) != -1){
                rawStream = rawStream + Character.toString((char)c);
            }
            fin.close();

            boolean oCatFirst = rawStream.startsWith("//");


            if(oCatFirst){ //we know the rawStream only contains "//" plus the OutcomeCategories
                String[] oCats = rawStream.substring(2).split(",");
                for (int i = 0; i < oCats.length; i++) {
                    outcomeCategories.add(oCats[i]);
                }
            }
            else{ //length could be 1 or 2, but it definitely starts with IncomeCategories
                String[] bothCats = rawStream.split("//"); // bothCats = [iCats] or [iCats, oCats]
                if(bothCats.length>1) { //bothCats = [iCats, oCats]
                    String[] iCats = bothCats[0].split(",");
                    String[] oCats = bothCats[1].split(",");

                    for (int i = 0; i < iCats.length; i++) {
                        incomeCategories.add(iCats[i]);
                    }
                    for (int i = 0; i < oCats.length; i++) {
                        outcomeCategories.add(oCats[i]);
                    }
                }
                else if(bothCats.length == 1){ //bothCats = [iCats]
                    String[] iCats = bothCats[0].split(",");
                    for (int i = 0; i < iCats.length; i++) {
                        incomeCategories.add(iCats[i]);
                    }

                }
            }


        } catch (java.io.IOException e) {
            if(e.getClass().equals(FileNotFoundException.class)){
                Log.d("File Not Found", "Must be the first time running the program or no categories have been created yet");
            }
            else {
                e.printStackTrace();
            }
        }
    }

    public void openRealm(){
        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        transactionRealm = Realm.getInstance(config);
    }


    public Realm getTransactionRealm() {
        return transactionRealm;
    }

    public ArrayList<String> getIncomeCategories() {
        return incomeCategories;
    }

    public ArrayList<String> getOutcomeCategories() {
        return outcomeCategories;
    }

    public void writeCategories() {
        try {
            FileOutputStream fOut = openFileOutput(FILENAME, MODE_PRIVATE);
            String str = "";
            for (int i = 0; i < incomeCategories.size(); i++) {
                str = str + incomeCategories.get(i) + ",";
            }
            str = str + "//";
            for (int i = 0; i < outcomeCategories.size(); i++) {
                str = str + outcomeCategories.get(i) + ",";
            }
            fOut.write(str.getBytes());
            fOut.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
