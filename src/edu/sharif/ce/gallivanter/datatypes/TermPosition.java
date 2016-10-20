package edu.sharif.ce.gallivanter.datatypes;

/**
 * Created by mohammad on 10/21/16.
 */
public class TermPosition implements Comparable<TermPosition>{
    private long line;
    private long position;

    public TermPosition(long line, long position) {
        this.line = line;
        this.position = position;
    }

    public long getLine() {
        return line;
    }

    public void setLine(long line) {
        this.line = line;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    @Override
    public int compareTo(TermPosition termPosition) {
        if(this.getLine()==termPosition.getLine()&&this.getPosition()==termPosition.getLine())
            throw new RuntimeException("These shouldn't be Identical...");
        else if(this.getLine()>termPosition.getLine()||(this.getLine()==termPosition.getLine()&&this.getPosition()>termPosition.getPosition()))
            return 1;
        else
            return -1;
    }
}
