package edu.sharif.ce.gallivanter.datatypes;

/**
 * Created by mohammad on 10/21/16.
 */
public class TermPosition implements Comparable<TermPosition>{
    private long line;
    private int position;

    public TermPosition(long line, int position) {
        this.line = line;
        this.position = position;
        if(line<0||position<0)
            throw new RuntimeException("Position Under zero?! U kidding me?!");
    }

    public long getLine() {
        return line;
    }

    public void setLine(long line) {
        this.line = line;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public int compareTo(TermPosition termPosition) {
        if(this.getLine()==termPosition.getLine()&&this.getPosition()==termPosition.getPosition())
            throw new RuntimeException("These shouldn't be Identical...");
        else if(this.getLine()>termPosition.getLine()||(this.getLine()==termPosition.getLine()&&this.getPosition()>termPosition.getPosition()))
            return 1;
        else
            return -1;
    }
}
