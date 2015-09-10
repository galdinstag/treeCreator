import treeCreator.TreeCreator;

/**
 * Created by gal on 9/2/2015.
 */
public class Main {
    public static void main(String[] args){
       TreeCreator tree = new TreeCreator();
        //{input file, intermediate files folder, output file folder, R script }
        tree.CreateTree("D:/Google_Drive/workspace/treeCreator/input/samples.json","D:/Google_Drive/workspace/treeCreator/midFiles"
                , "D:/Google_Drive/workspace/treeCreator/output","D:/Google_Drive/workspace/treeCreator/src/SeqTrakTreeCreator.R");
    }
}
