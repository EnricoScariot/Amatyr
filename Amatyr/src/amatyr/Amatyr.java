/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package amatyr;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.json.*;
import java.util.Calendar;




/**
 *
 * @author skari
 */
public class Amatyr {

    /**The aim of this project is to parse the xml files created by Arpa Veneto and create a jsonarray that can be upoloaded in a url and used like input 
     * by the weather display application "Amatyr". Instead of change the javascript files of Amatyr to adapt it to the Arpav xml's I have preferred to adapt 
     * the arpav data to the application.
     * In addition to the format one of the main difference between the Original data passed in input in amatyr (http://yr.hveem.no/) is that the original data
     * are stored by station, the Arpa files are stored by sensor id, in other words: each file of the original amatyr implementation contain all the variables
     * of a particualr weather station(temperature, pressoure, rain, humidity etc) every xml of Arpa Veneto contain a single type of date (only temperature
     * or only pressoure ecc...).
     * 
     * 
     * @param args the command line argument
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        
        // first of all i create the date that i pass in input to the methods
       String year; 
       String inizio_today;
       String inizio_yesterday;
       String inizio_3day;
       String inizio_week;
       String inizio_month;
       String inizio_year;
       String fine;
       String fine_today;
       String valori=null;
       
       Calendar date= Calendar.getInstance();
       DateFormat dateformat=new SimpleDateFormat("yyyyMMdd");
       date.add(Calendar.DATE, 0);
       inizio_today=dateformat.format(date.getTime())+"0000";
       date.add(Calendar.DATE, -1);
       inizio_yesterday=dateformat.format(date.getTime())+"0000";
       date.add(Calendar.DATE, -2);
       inizio_3day=dateformat.format(date.getTime())+"0000";
       date.add(Calendar.DATE, -4);
       inizio_week=dateformat.format(date.getTime())+"0000";
       date.add(Calendar.DATE, -23);
       inizio_month=dateformat.format(date.getTime())+"0000";
       DateFormat dateformat_year=new SimpleDateFormat("yyyy");
       date.add(Calendar.DATE, 0);
       year=dateformat_year.format(date.getTime());
       inizio_year=dateformat_year.format(date.getTime())+"01010000";
       date.add(Calendar.DATE, +30);
       fine=dateformat.format(date.getTime())+"0000";
       date.add(Calendar.DATE, +1);
       fine_today=dateformat.format(date.getTime())+"0000";

       JSONArray sensori=Traduttore.elenco_stazioni(inizio_today,fine_today);
       Traduttore.aggiungi_sensore(sensori, "umid",inizio_today, fine_today);
       Traduttore.aggiungi_sensore(sensori, "prec",inizio_today, fine_today);
       Traduttore.aggiungi_sensore(sensori, "vvento",inizio_today, fine_today);
       Traduttore.aggiungi_sensore(sensori, "dvento",inizio_today, fine_today);
       Traduttore.aggiungi_sensore(sensori, "ventoraff",inizio_today, fine_today);
       Traduttore.aggiungi_sensore(sensori, "press",inizio_today, fine_today);
    
       //  System.out.println(sensori);
     
     // a list of the station that i want add
     String [] stazioni_importanti=new String[70];
     int k=0;
     for (String line : Files.readAllLines(Paths.get("C:\\Users\\skari\\Desktop\\id_stazioni.txt"))) {
            stazioni_importanti[k]=line;
           // System.out.println(stazioni_importanti[k]);
            k++;
        }
     
     
       File worldDirectory = new File("C:\\Users\\skari\\Documents\\stazioni_arpav");
        if (!worldDirectory.exists()) {
            if (worldDirectory.mkdir()) {
                System.out.println("World directory is created!");
            } else {
              //  System.out.println("Failed to create World directory!");
            }
        }
     
     // i choose only the important station that are store in the array "stazioni_importanti"   
     for(k=0;k<stazioni_importanti.length;k++){
        
            for(int i=0;i<sensori.length();i++){
                if(sensori.getJSONObject(i).getString("idstaz").equals(stazioni_importanti[k])){
                        File subWorldDir = new File("C:\\Users\\skari\\Documents\\stazioni_arpav\\" +sensori.getJSONObject(i).getString("idstaz"));
                           if (!subWorldDir.exists()) {
                               subWorldDir.mkdir();
                             //  System.out.println("Created Sub World directory!");
                           } else { 
                            //   System.out.println("Failed to create Sub World directory!");
                           }
                       try (FileWriter file = new FileWriter("C:/Users/skari/Documents/stazioni_arpav/"+sensori.getJSONObject(i).getString("idstaz")+"/today.json")) {
                                       JSONArray geson=Traduttore.creaJson(sensori.getJSONObject(i),inizio_today,fine_today,"oggi");
                                       file.write(geson.toString());
                               }
                       try (FileWriter file = new FileWriter("C:/Users/skari/Documents/stazioni_arpav/"+sensori.getJSONObject(i).getString("idstaz")+"/yesterday.json")) {
                                       JSONArray geson=Traduttore.creaJson(sensori.getJSONObject(i),inizio_yesterday,fine,"ora");
                                       file.write(geson.toString());
                               }
                       try (FileWriter file = new FileWriter("C:/Users/skari/Documents/stazioni_arpav/"+sensori.getJSONObject(i).getString("idstaz")+"/3day.json")) {
                                       JSONArray geson=Traduttore.creaJson(sensori.getJSONObject(i),inizio_3day,fine,"ora");
                                       file.write(geson.toString());
                               }
                       try (FileWriter file = new FileWriter("C:/Users/skari/Documents/stazioni_arpav/"+sensori.getJSONObject(i).getString("idstaz")+"/week.json")) {
                                       JSONArray geson=Traduttore.creaJson(sensori.getJSONObject(i),inizio_week,fine,"ora");
                                       file.write(geson.toString());
                               }
                       try (FileWriter file = new FileWriter("C:/Users/skari/Documents/stazioni_arpav/"+sensori.getJSONObject(i).getString("idstaz")+"/month.json")) {
                                       JSONArray geson= Traduttore.creaJson(sensori.getJSONObject(i),inizio_month,fine,"giorno");
                                       file.write(geson.toString());
                               }
                       try (FileWriter file = new FileWriter("C:/Users/skari/Documents/stazioni_arpav/"+sensori.getJSONObject(i).getString("idstaz")+"/"+year+".json")) {
                                       JSONArray geson=Traduttore.creaJson(sensori.getJSONObject(i),inizio_year,fine,"giorno");
                                       file.write(geson.toString());
                               }
                        try (FileWriter file = new FileWriter("C:/Users/skari/Documents/stazioni_arpav/"+sensori.getJSONObject(i).getString("idstaz")+"/2015.json")) {
                                       JSONArray geson=Traduttore.creaJson(sensori.getJSONObject(i),"201501010000","201601010000","giorno");
                                       file.write(geson.toString());
                               }
                        try (FileWriter file = new FileWriter("C:/Users/skari/Documents/stazioni_arpav/"+sensori.getJSONObject(i).getString("idstaz")+"/2014.json")) {
                                       JSONArray geson=Traduttore.creaJson(sensori.getJSONObject(i),"201401010000","201501010000","giorno");
                                       file.write(geson.toString());
                               }
                        try (FileWriter file = new FileWriter("C:/Users/skari/Documents/stazioni_arpav/"+sensori.getJSONObject(i).getString("idstaz")+"/2013.json")) {
                                       JSONArray geson=Traduttore.creaJson(sensori.getJSONObject(i),"201301010000","201401010000","giorno");
                                       file.write(geson.toString());
                               }
              
                        System.out.println("completata la creazione dei file relativi alla stazione "+sensori.getJSONObject(i).getString("idstaz"));

                    } 
            }
     }
     
 
      
    }
}
