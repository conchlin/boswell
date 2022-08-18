package tools;

/**
 * tagSIZE
 * 
 * @author Eric
 */

public class Size {
    public int cx;
    public int cy;
    
    public Size(int cx, int cy) {
        this.cx = cx;
        this.cy = cy;
    }
    
    public Size(Size size) {
        this.cx = size.cx;
        this.cy = size.cy;
    }
    
    public int getCx() {
        return cx;
    }
    
    public int getCy() {
        return cy;
    }
    
    public void setCx(int cx) {
        this.cx = cx;
    }
    
    public void setCy(int cy) {
        this.cy = cy;
    }
}
