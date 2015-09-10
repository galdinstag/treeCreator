package treeCreator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Created by gal on 9/1/2015.
 */
public class TreeCreator {

    private ArrayList<Vertex> vertexList;
    private static final Logger logger = Logger.getLogger(TreeCreator.class.getName());

    public TreeCreator(){
        vertexList = new ArrayList<Vertex>();
    }


    //main function:
    // 1) initializing vertex list containing the abundance vectors.
    // 2) calculating the weights for the edges.
    // 3) run "seqtrak" to obtain MST.
    // 4) write a .json file containing the tree.
    // 5) cleanup

    public void CreateTree(String inputFileLocation,String midFilesFolderLocation, String outPutFilesFolderLocation, String rScriptLocation){
        logger.info("starting tree processing at " + new Date());
        parseInputJSON(inputFileLocation);
        double[][] spearmanMatrix = createEdges(vertexList.size());
        String[] dates = createDatesArray();
        makeCSVFiles(spearmanMatrix, dates, midFilesFolderLocation);
        Process callSeqTrak = null;
        try {
            logger.info("executing R script at " + new Date());
            //calling r script to run seqTrak, an wait for the script
            //to finish before proceeding
            callSeqTrak = Runtime.getRuntime().exec("Rscript " + rScriptLocation + " \"" + midFilesFolderLocation + "\"");
            callSeqTrak.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("seqtrak running finished at " + new Date());
        buildOutputJson(midFilesFolderLocation, outPutFilesFolderLocation);
        cleanFiles(midFilesFolderLocation);
        logger.info("finished at " + new Date());
    }

    // parsing the .json input file to Vertexes
    // saved in vertexList
    public void parseInputJSON(String location){
        logger.info("starting json parsing");
        JSONParser parser = new JSONParser();
        try {

            //for the id's
            int counter = 1;
            //formal thing
            Object obj = parser.parse(new FileReader(location));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray samples = (JSONArray) jsonObject.get("samples");
            Iterator<JSONObject> iter = samples.iterator();
            //iterate over samples
            while(iter.hasNext()){
                JSONObject currSample = iter.next();
                Vertex currVertex = new Vertex(((String) currSample.get("date")),counter, ((String) currSample.get("name")));
                //iterate over symptoms
                JSONArray symptoms = (JSONArray) currSample.get("symptoms");
                Iterator<String> symptomIterator = symptoms.iterator();
                while(symptomIterator.hasNext()){
                    String currSymptom = symptomIterator.next();
                    currVertex.addSymptom(currSymptom);
                }
                //iterate over gene abundances
                JSONArray abundances = (JSONArray) currSample.get("GeneUnit");
                Iterator<JSONObject> geneIter = abundances.iterator();
                while(geneIter.hasNext()) {
                    JSONObject currGene = geneIter.next();
                    currVertex.AddAbundance(((Number) (currGene.get("abundance"))).doubleValue(), ((String) (currGene.get("name"))));
                }
                currVertex.setRanks();
                vertexList.add(currVertex);
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // calculate "all against all" spearman correlation coefficient
    // as input to "seqTrak"
    public double[][] createEdges(int n){
        double[][] spearmanMatrix = new double[n][n];
        double weight;
        for(int i=0; i < n-1; i++){
            for(int j=i+1; j < n; j++){
                weight = calculateSpearmanCorrelation(vertexList.get(i), vertexList.get(j));
                spearmanMatrix[i][j] = weight;
                spearmanMatrix[j][i] = weight;
            }
        }
        return spearmanMatrix;
    }

    // create dates array needed for seqTrak processing
    private String[] createDatesArray() {
        String[] dates = new String[vertexList.size()];
        for(int i = 0; i < vertexList.size(); i++){
            dates[i] = vertexList.get(i).getDate();
        }
        return dates;
    }

    // creating matrix.csv and dates.csv files needed
    // for seqTrak processing
    private void makeCSVFiles(double[][] spearmanMatrix, String[] datesArray, String midFilesFolderLocation) {
        try {
                //create matrix csv file
                File matrix = new File(midFilesFolderLocation+"\\matrix.csv");
                matrix.createNewFile();
                FileWriter matrixOutput = new FileWriter(matrix);
                matrixOutput.flush();
                for(int i = 0; i < spearmanMatrix.length; i++){
                    for(int j = 0; j < spearmanMatrix[0].length; j++){
                        matrixOutput.append("" + spearmanMatrix[i][j]);
                        if(j < spearmanMatrix[0].length-1){
                            matrixOutput.append(",");
                        }
                    }
                    if(i < spearmanMatrix.length-1){
                        matrixOutput.append("\n");
                    }
                    matrixOutput.flush();
                }
                matrixOutput.close();

                //create dates csv file
                File dates = new File(midFilesFolderLocation+"\\dates.csv");
                FileWriter datesOutput = new FileWriter(dates);
                datesOutput.append("id,collec.dates");
                datesOutput.append("\n");
                for(int i = 0; i < datesArray.length ; i++ ){
                    datesOutput.append(i+1 + "," +datesArray[i] + " ");
                    if(i < datesArray.length-1){
                        datesOutput.append("\n");
                    }
                }
                datesOutput.flush();
                datesOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    // generate .json file from seqTrak's results
    private void buildOutputJson(String midFilesFolderLocation, String outputFilesFolderLocation) {
        logger.info("output json processing");
        String line;
        String splitBy = ",";
        JSONObject JSONFILE = new JSONObject();

        // generate prefix
        JSONObject crs = new JSONObject();
        crs.put("type","name");
        JSONObject prefixProperties = new JSONObject();
        prefixProperties.put("name","flu");
        crs.put("properties",prefixProperties);
        JSONFILE.put("crs",crs);
        JSONFILE.put("type","FeatureCollection");


        try {
            BufferedReader br = new BufferedReader(new FileReader(midFilesFolderLocation+"\\res.csv"));
            // generate features
            JSONArray features = new JSONArray();

            //create points out of Vertexes
            //each vertex get a point
            for(Vertex currVertex : vertexList) {
                JSONObject currPoint = new JSONObject();
                currPoint.put("type", "Feature");

                JSONObject properties = new JSONObject();
                properties.put("name",currVertex.getName());
                JSONArray symptoms = new JSONArray();
                //add symptoms
                for(String currSymptom : currVertex.getSymptomsList()){
                    symptoms.add(currSymptom);
                }
                properties.put("symptoms",symptoms);
                //add genes and abundances
                JSONArray abundances = new JSONArray();
                for(Abundance abundance : currVertex.getAbundancesList()){
                    JSONObject currAbundance = new JSONObject();
                    currAbundance.put("name",abundance.getGeneName());
                    currAbundance.put("abundance",abundance.getAbundance());
                    abundances.add(currAbundance);
                }
                properties.put("abundances",abundances);

                currPoint.put("properties", properties);

                JSONObject geometry = new JSONObject();
                geometry.put("type", "Point");
                JSONArray coordinates = new JSONArray();
                coordinates.add(currVertex.getLongitude());
                coordinates.add(currVertex.getLatitude());
                geometry.put("coordinates", coordinates);
                currPoint.put("geometry", geometry);

                features.add(currPoint);
            }

            //converting results csv to matrix for convenience
            String[][] seqTrakResaults = new String[vertexList.size()][6];
            //dump first line
            line = br.readLine();
            int j = 0;
            //each line in "res.csv" is an edge
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] parsedLine = line.split(splitBy);
                for(int i = 0; i < parsedLine.length; i++){
                    seqTrakResaults[j][i] = parsedLine[i];
                }
                j++;
            }

            br.close();
            //create edges
            for(int i = 0; i < seqTrakResaults.length; i++){
                if(!seqTrakResaults[i][2].equals(new String("NA"))){
                    JSONObject currEdge = new JSONObject();
                    currEdge.put("type","Feature");

                    JSONObject properties = new JSONObject();
                    properties.put("edge weight",seqTrakResaults[i][3]);
                    currEdge.put("properties", properties);

                    JSONObject geometry = new JSONObject();
                    geometry.put("type", "LineString");
                    JSONArray coordinates = new JSONArray();

                    JSONArray source =  new JSONArray();
                    String ancestor = seqTrakResaults[new Integer(seqTrakResaults[i][2]).intValue() - 1][0];
                    Vertex sorceVertex = vertexList.get(new Integer(ancestor.substring(1,ancestor.length()-1))-1);
                    source.add(sorceVertex.getLongitude());
                    source.add(sorceVertex.getLatitude());
                    coordinates.add(source);

                    JSONArray destination = new JSONArray();
                    String dest = new String(seqTrakResaults[i][0]);
                    Vertex destinationVertex = vertexList.get(new Integer(dest.substring(1,dest.length()-1)).intValue()-1);
                    destination.add(destinationVertex.getLongitude());
                    destination.add(destinationVertex.getLatitude());
                    coordinates.add(destination);

                    geometry.put("coordinates", coordinates);
                    currEdge.put("geometry",geometry);

                    features.add(currEdge);
                }
            }

            JSONFILE.put("features", features);
            //formal
            FileWriter outputFile = new FileWriter(outputFilesFolderLocation+"\\outputFile.json");
            outputFile.write(JSONFILE.toJSONString());
            outputFile.flush();
            outputFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // clean all files on midFilesFolder
    private void cleanFiles(String midFilesFolderLocation) {
        try {
            //Files.delete(Paths.get(midFilesFolderLocation+"\\matrix.csv"));
            Files.delete(Paths.get(midFilesFolderLocation+"\\dates.csv"));
            //Files.delete(Paths.get(midFilesFolderLocation+"\\res.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   // for future use, if we can work with JRI
    private void CreateTransmissionTree(double[][] spearmanMatrix, String[] dates) {


    }

    // aux function
    private void printDates(String[] dates) {
        StringBuilder str = new StringBuilder();
        str.append("[");
        for(String i : dates){
            str.append(i + ",");
        }
        str.append("]");
        System.out.println(str);
    }



    //aux function
    private void printMatrix(String[][] matrix){
        for(int i=0; i < matrix.length; i++){
            for(int j=0; j < matrix[0].length; j++){
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }



    // calculates spearman correlation as explained in the literature
    // return 1-p (p being spearman's correlation)
    // because we want MST
    public double calculateSpearmanCorrelation(Vertex source, Vertex destination) {
        double numOfGenes = source.getListSize();
        double sum = 0;
        double row;
        for(int i = 0; i < numOfGenes; i++){
            sum += Math.pow(source.getRankAt(i)-destination.getRankAt(i),2);
        }
        row = 1 - ((6*sum)/(numOfGenes * ((Math.pow(numOfGenes,2)-1))));
        return 1 - row;
    }


}
