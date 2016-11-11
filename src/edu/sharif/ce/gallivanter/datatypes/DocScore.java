package edu.sharif.ce.gallivanter.datatypes;

/**
 * Created by mohammad on 11/11/16.
 */
public class DocScore implements Comparable<DocScore>{
    private String docID;
    private Double score;

    public DocScore(String docID, Double score) {
        this.docID = docID;
        this.score = score;
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Override
    public int compareTo(DocScore docScore) {
        if(docScore.getScore().equals(this.getScore()))
            return 0;
        if(this.getScore()>docScore.getScore())
            return -1;
        return 1;
    }
}
