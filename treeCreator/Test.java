package treeCreator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by gal on 9/10/2015.
 */
public class Test {

    public static void createTestJson(String inputFolderLocation, int numOfSamples, int numOfGenesPerSample){
        JSONObject JSONFILE = new JSONObject();
        JSONArray samples = new JSONArray();
        for(int i = 1; i <= numOfSamples ;i++){
            JSONObject currSample = new JSONObject();
            JSONArray symptoms = new JSONArray();
            currSample.put("name","s"+i);
            currSample.put("date",generateRandomDate());
            currSample.put("symptoms",symptoms);
            JSONArray GeneUnit = new JSONArray();
            for(int j = 1 ; j <= numOfGenesPerSample;j++){
                JSONObject currGene = new JSONObject();
                currGene.put("name", "g" + j);
                currGene.put("abundance",Math.random()*100);
                GeneUnit.add(currGene);
            }
            currSample.put("GeneUnit",GeneUnit);
            samples.add(currSample);
        }
        JSONFILE.put("samples",samples);
        FileWriter outputFile = null;
        try {
            outputFile = new FileWriter(inputFolderLocation+"/testInput.json");
            outputFile.write(JSONFILE.toJSONString());
            outputFile.flush();
            outputFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generateRandomDate() {
        int[] year = {2010,2011,2012,2013,2014};
        String date = new String("" + year[ThreadLocalRandom.current().nextInt(year.length)] + "-" + (ThreadLocalRandom.current().nextInt(11)+1) + "-" + (ThreadLocalRandom.current().nextInt(27)+1));
        return date;
    }
}
