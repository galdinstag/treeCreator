package treeCreator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by gal on 9/2/2015.
 */
public class Vertex {

    private int id;
    private String date;
    private List<Abundance> abundance;
    //TODO: coordinates
    private Float longitude;
    private Float latitude;
    private String name;
    private List<String> symptoms;

    public Vertex(String date, int id, String name, double longitude, double latitude){
        this.name = name;
        this.id = id;
        this.date = date;
        abundance = new ArrayList<Abundance>();
        this.longitude = new Float(longitude);
        this.latitude =  new Float(latitude);
        symptoms = new ArrayList<String>();
    }

    public void AddAbundance(double abundance, String geneName){
        this.abundance.add(new Abundance(abundance, geneName));
    }
    public String getDate(){
        return date;
    }
    public int getListSize() { return abundance.size() ;}
    public double getRankAt(int index){ return abundance.get(index).getRank() ; }

    // set rank for each Abundamce for spearman calculation
    public void setRanks() {
        //sort Abundances by abundance
        ArrayList<Abundance> auxList = new ArrayList<Abundance>();
        for (Abundance currGene : abundance){
            auxList.add(currGene);
        }
        Collections.sort(auxList);


        int i = 1;
        double sum;
        int counter;
        double averageRank;
        for (int j = 0; j < auxList.size(); j++){
            // last abundance, the biggest one
            if(j == auxList.size()-1){
                auxList.get(j).setRank(i);
            }
            // check for abundance equality, if not the rank is unique
           else  if(auxList.get(j).getAbundance() < auxList.get(j+1).getAbundance()){
                auxList.get(j).setRank(i);
                i++;
            }
            else{
                // same abundance, same rank
                // see
                sum = i;
                i++;
                counter = j;
                while(j < auxList.size()-1 && auxList.get(j).getAbundance() == auxList.get(j+1).getAbundance()){
                    sum += i;
                    i++;
                    j++;
                }
                averageRank = sum/(j-counter);
                while(counter <= j){
                    auxList.get(counter).setRank(averageRank);
                    counter++;
                }
            }
        }
    }
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append("vertex id: " + id + "\n");
        str.append("date: " + date + "\n");
        str.append("abundance , rank\n");
        for(Abundance currAbu :  abundance){
            str.append(currAbu.getAbundance() + " , " + currAbu.getRank() +"\n");
        }
        str.append("symptoms:\n");
        for(String currSymptom :  symptoms){
            str.append(currSymptom +"\n");
        }
        return new String(str);
    }

    public Float getLatitude() {
        return latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public int getId() {
        return id;
    }

    public void addSymptom(String currSymptom) {
        symptoms.add(currSymptom);
    }

    public String getName() {
        return name;
    }

    public List<String> getSymptomsList() {
        return symptoms;
    }

    public List<Abundance> getAbundancesList() { return abundance;}
}
