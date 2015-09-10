/**
 * Created by gal on 9/2/2015.
 */

package treeCreator;

public class Abundance implements Comparable{

    private String geneName;
    private double abundance;
    private double rank;
    public Abundance(double abundance, String geneName){
        this.geneName = geneName;
        this.abundance = abundance;
    }
    public void setRank(double rank){
        this.rank = rank;
    }

    public String getGeneName(){ return geneName;}
    public double getAbundance(){ return abundance; }
    public double getRank(){ return rank; }

    @Override
    public int compareTo(Object other) {
        if (abundance-((Abundance)other).getAbundance() > 0) { return 1 ; }
        else if (abundance-((Abundance)other).getAbundance() < 0) { return -1 ; }
        else return 0;
    }
}
