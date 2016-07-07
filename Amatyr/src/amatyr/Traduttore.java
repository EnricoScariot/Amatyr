
package amatyr;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.*;
import org.json.*;
import org.w3c.dom.Element;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author skari
 * 
 */


public class Traduttore {
      
        
    /**
     * This method create an array of JSONObjects. Each object represent a weather station: the first key of the object is the station id "idstaz" and 
     * the following keys are the id of the sensors of that weather station, in this method i pass a start and an end date because the URL want those 
     * variables and i prefer to have every day the start and the end date up to date because if a new weather station is added we can see it on our array
     * 
     * This function create the jsonarray with 2 keys: idstaz and temp, the method "aggiungi_sensore" add the other sensors
     * 
     * @param inizio: start date of the detection
     * @param fine end date of the detection
     * @return the list of stations
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws JSONException 
     */
    public static JSONArray elenco_stazioni(String inizio, String fine) throws MalformedURLException, IOException, ParserConfigurationException, SAXException, JSONException{
       
        URL url = new URL("http://tlsirav000.arpa.veneto.it/xml/GetXML.php?inizio="+inizio+"&fine="+fine+"&sensori=temp");
        URLConnection conn = url.openConnection();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(conn.getInputStream());
        JSONArray stazioni=new JSONArray();
       
        NodeList n=doc.getElementsByTagName("STAZIONE");
        
        for(int i=0;i<n.getLength();i++){
            JSONObject jo = new JSONObject();
            String id_staz=null;
            String id_sens=null;
            Node mio_nodo=n.item(i);
            NodeList staz=mio_nodo.getChildNodes();
            for(int j=0;j<staz.getLength();j++){
                if("IDSTAZ".equals(staz.item(j).getNodeName()) ){
                   id_staz=staz.item(j).getTextContent();
                }
                if("SENSORE".equals(staz.item(j).getNodeName())){
                   Node mio_nodo_sensore=staz.item(j);
                   NodeList sens=mio_nodo_sensore.getChildNodes();
                   for(int k=0;k<sens.getLength();k++){
                       if("ID".equals(sens.item(k).getNodeName())){
                            id_sens=sens.item(k).getTextContent();
                       }
                    }
                }
            }
            jo.put("idstaz", id_staz);
            jo.put("temp", id_sens);
            stazioni.put(jo);
        }
        return stazioni;
    }
    
    
    /**
     * This method add the sensors to the jsonarray previously created with the method "elenco_stazioni" if a station is not present (because it don't have 
     * the temp sensor), it will be added.
     * @param elenco_stazioni the JSONArray previously created
     * @param sensore the kind of sensor that i want
     * @param inizio start date of the detection
     * @param fine end date of the detection
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws JSONException 
     */
    public static void aggiungi_sensore(JSONArray elenco_stazioni, String sensore, String inizio, String fine) throws MalformedURLException, IOException, ParserConfigurationException, SAXException, JSONException {
        
        URL url = new URL("http://tlsirav000.arpa.veneto.it/xml/GetXML.php?inizio="+inizio+"&fine="+fine+"&sensori="+sensore);
        URLConnection conn = url.openConnection();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(conn.getInputStream());
        
       NodeList n=doc.getElementsByTagName("STAZIONE");
        
        for(int i=0;i<n.getLength();i++){
            String id_sens=null;
            int indice=-1;
            Node mio_nodo=n.item(i);
            NodeList staz=mio_nodo.getChildNodes();
            for(int j=0;j<staz.getLength();j++){
                if("IDSTAZ".equals(staz.item(j).getNodeName()) ){
                   //mi posiziono nell'array di json nel posto giusto
                   
                   while(++indice<elenco_stazioni.length()&&!(elenco_stazioni.getJSONObject(indice).getString("idstaz")).equals(staz.item(j).getTextContent())){
                     //  System.out.println("sto confrontando " + elenco_stazioni.getJSONObject(indice).getString("idstaz")+" con "+staz.item(j).getTextContent());
                   }
                   if(indice==elenco_stazioni.length()){
                       JSONObject jo = new JSONObject();
                       jo.put("idstaz", staz.item(j).getTextContent());
                       elenco_stazioni.put(jo);
                   }
                  // System.out.println(elenco_stazioni.getJSONObject(indice).getString("idstaz")+ " e "+staz.item(j).getTextContent() + " sono uguali");
                }
                if("SENSORE".equals(staz.item(j).getNodeName())){
                   Node mio_nodo_sensore=staz.item(j);
                   NodeList sens=mio_nodo_sensore.getChildNodes();
                   for(int k=0;k<sens.getLength();k++){
                       if("ID".equals(sens.item(k).getNodeName())){
                            id_sens=sens.item(k).getTextContent();
                       }
                    }
                }
            }
           
            elenco_stazioni.getJSONObject(indice).put(sensore, id_sens);
            
        }
       
        
    }
    
    
    /**
     * This method create a jsonarray that store all the weather variables acquired from a single weather station in a particular timerange 
     * (defined by the 2 input parameters "inizio" and "fine"). Each jsonobjet of the array represent a single acquisition, it can an hourly or daily,
     * it depends on the parameters "valori".
     * 
     * Every object of the json can't contain "null" values, because of that at the and of the creation of any jsonobject the method controls if there is a
     * "null" value on the "idstaz" key, it control only that key because the key in inizialized "null" before adding the real idstaz parameter (it doesn't
     * control the "nome" key because "idstaz" and "nome" are inizialized together. If there is a null value the jsonobject is removed from the array
     * 
     * 
     * @param stazione the json that of the station that i want (is a json fron te array created with "elenco_stazioni" and "aggiungi_sensore"
     * @param inizio start date of the acquisition
     * @param fine end date of the acquisition
     * @param valori frequence of acquisition (hourly or daily in our case)
     * @return the array that Amatyr want to display correctly the craph
     * @throws JSONException
     * @throws IOException
     * @throws MalformedURLException
     * @throws ParserConfigurationException
     * @throws SAXException 
     */
    
    public static JSONArray  creaJson (JSONObject stazione, String inizio, String fine, String valori) throws JSONException, IOException, MalformedURLException, ParserConfigurationException, SAXException{
        JSONArray mio_json=new JSONArray();
       
        Iterator<String> iter = stazione.keys();
        int i=0;
        while (iter.hasNext()) {
            String key = iter.next();
            try {
                Object value = stazione.get(key);
                
                
                if(i>0){
                    Traduttore.aggiungi_parametri(mio_json, value.toString(),inizio, fine, valori, key);
                }
                i++;
                
            } catch (JSONException e) {
                // Something went wrong!
            }
        }
        
        
        for(int a=0;a<mio_json.length();a++){
          if(mio_json.getJSONObject(a).getString("idstaz").equals("null")){
              mio_json.remove(a);
          }
        }
       
      return mio_json;
    }
    
    
 
    /**This method is called by the "crea_json" method. its aim is to create and add to the jsonarray passed in input the jsonobject
     * 
     * @param ja the jsonarray that i want to popolate with jsonobject
     * @param id the id of the sensor of the station
     * @param inizio start date of the acquisition
     * @param fine end date of the acquisition
     * @param valori frequence of acquisition (hourly or daily in our case)
     * @param tipodato the type of the data that i am acquiring
     * @throws MalformedURLException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws JSONException 
     */
    private static void aggiungi_parametri (JSONArray ja, String id, String inizio, String fine, String valori, String tipodato) throws MalformedURLException, IOException, ParserConfigurationException, SAXException, JSONException{
        
            URL url=new URL ("http://tlsirav000.arpa.veneto.it/xml/GetXML.php?inizio="+inizio+"&fine="+fine+"&id="+id+"&valori="+valori);
          //  System.out.println(url);
            URLConnection conn = url.openConnection();
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(conn.getInputStream());
            NodeList n1=doc.getElementsByTagName("STAZIONE");
            String id_staz=null;
            String nome=null;
            for(int i1=0;i1<n1.getLength();i1++){
                Node mio_nodo=n1.item(i1);
                NodeList staz=mio_nodo.getChildNodes();
                for(int j=0;j<staz.getLength();j++){
                    if("IDSTAZ".equals(staz.item(j).getNodeName()) ){
                       id_staz=staz.item(j).getTextContent();
                      
                    } 
                     if("NOME".equals(staz.item(j).getNodeName()) ){
                       nome=staz.item(j).getTextContent();
                      
                    }
                }
            }
           
            NodeList n=doc.getElementsByTagName("DATI");
         //  System.out.println(n.getLength());
           int lunghezza=0;
           
           while(!(n.item(lunghezza).getTextContent().equals(">>"))&&lunghezza<n.getLength()-1){
               lunghezza++;
          //      System.out.println(lunghezza);
           }
          
           for(int i=0;i<=lunghezza;i++){
               //vuol dire che devo fare una append dell'oggetto json
               if(ja.length()==i){
                   //inizializzo tutti i campi a null
                   JSONObject jo=Traduttore.inizializza();
                   ja.put(jo);
               }
               if(!(n.item(i).getTextContent().equals(">>"))){
                     NodeList tipo_valori=n.item(i).getChildNodes();
                     Node dato_da_inserire=null;
                     Node dato_da_inserire_max=null;
                     Node dato_da_inserire_min=null;
                     double datodainserire=0;
                     double datodainseriremax=0;
                     double datodainseriremin=0;
                     
                   //  System.out.println(tipo_valori.getLength());
                     for(int l=0;l<tipo_valori.getLength();l++){
                         if(tipo_valori.item(l).getNodeName().equals("VM")){
                             dato_da_inserire=tipo_valori.item(l);
                             datodainserire=Double.parseDouble(dato_da_inserire.getTextContent());
                         }
                         if(tipo_valori.item(l).getNodeName().equals("VMAX")){
                             dato_da_inserire_max=tipo_valori.item(l);
                             datodainseriremax=Double.parseDouble(dato_da_inserire_max.getTextContent());
                         }
                         if(tipo_valori.item(l).getNodeName().equals("VMIN")){
                             dato_da_inserire_min=tipo_valori.item(l);
                             datodainseriremin=Double.parseDouble(dato_da_inserire_min.getTextContent());
                         }
                     }
                     
                     Traduttore.aggiungi_parametro_specifico(ja.getJSONObject(i),tipodato,datodainserire,null);
                     Node mynode=n.item(i);
                     Element e=(Element)mynode;
                     String data=e.getAttribute("ISTANTE");
                     Traduttore.aggiungi_parametro_specifico(ja.getJSONObject(i),"istante",null ,data);
                     if(tipodato.equals("temp")){
                         if(dato_da_inserire_max!=null){
                             Traduttore.aggiungi_parametro_specifico(ja.getJSONObject(i),"tempmax",datodainseriremax,null);
                         }
                         if(dato_da_inserire_min!=null){
                              Traduttore.aggiungi_parametro_specifico(ja.getJSONObject(i),"tempmin",datodainseriremin,null);
                         }
                     }
                      Traduttore.aggiungi_parametro_specifico(ja.getJSONObject(i),"idstaz", null, id_staz);
                      Traduttore.aggiungi_parametro_specifico(ja.getJSONObject(i),"nome", null, nome);
                   //  System.out.println(data);
                    // System.out.println("Il parametro "+tipodato+" vale "+n.item(i).getTextContent());
               }
           }
    }
    
   /**
    * 
    * @return the jsonobject inizialized the rain in inizialized in 0 because it is good though the weather station didn't have the rain sensor
    * @throws JSONException 
    */
    private static JSONObject inizializza() throws JSONException{
        
        JSONObject jo=new JSONObject();
        String valore="null";
         jo.put("dayrain",0);
         jo.put("rain",0);
         jo.put("idstaz",valore);
         jo.put("nome",valore);
         return jo;
        
    }
    
    /**Once the jsonobject is created and inizialized from the method "aggiungi_parametri" the other keys can be added
     * 
     * @param jo object that i want update
     * @param tipo type of the data that i want add (temperature, pressoure, rain etc...)
     * @param valore value of the data
     * @param nome if it is a date, station name or id station it is a string
     * @throws JSONException 
     */
    private static void aggiungi_parametro_specifico(JSONObject jo, String tipo, Double valore, String nome) throws JSONException{
      
        
        if(tipo.equals("temp")){
          //  jo.remove("outtemp");
            jo.put("outtemp",valore);
            
        }
        if(tipo.equals("tempmax")){
//            jo.remove("tempmax");
            jo.put("tempmax",valore);
            
        }
        if(tipo.equals("tempmin")){
//            jo.remove("tempmin");
            jo.put("tempmin",valore);
            
        }
        
        if(tipo.equals("prec")){
            jo.remove("dayrain");
            jo.put("dayrain",valore);
            jo.remove("rain");
            jo.put("rain",valore);
            
        }
        
        if(tipo.equals("ventoraff")){
            jo.remove("windgust");
            jo.put("windgust",valore);
            
        }
        
        if(tipo.equals("press")){
           // jo.remove("barometer");
            jo.put("barometer",valore);
            
        }
        
        if(tipo.equals("dvento")){
//            jo.remove("winddir");
            jo.put("winddir",valore);
            
        }
        
        if(tipo.equals("vvento")){
//            jo.remove("windspeed");
            jo.put("windspeed",valore);
            
        }
        
         if(tipo.equals("umid")){
//             jo.remove("outhumidity");
             jo.put("outhumidity",valore);
            
        }
         if(tipo.equals("istante")){
//             jo.remove("datetime");
             jo.put("datetime",Traduttore.parseDate(nome));
         }
          if(tipo.equals("idstaz")){
             jo.remove("idstaz");
             jo.put("idstaz",nome);
         }
          if(tipo.equals("nome")){
             jo.remove("nome");
             jo.put("nome",nome);
         }
      
        
    } 
    
    /**
     * the javascript application want that the date is formatted in a particular way
     * 
     * @param data that i want parse
     * @return parsed data
     */
    private static String parseDate(String data){
        
      
        String anno=data.substring(0, 4);
        String mese=data.substring(4, 6);
        String giorno=data.substring(6, 8);
        String ora=data.substring(8, 10);
        String minuto=data.substring(10, 12);
        String risultato=anno+"-"+mese+"-"+giorno+" "+ora+":"+minuto+":00";
        
        return risultato;
    }
    
}
