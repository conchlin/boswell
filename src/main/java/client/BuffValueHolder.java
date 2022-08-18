/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

/**
 * Class to hold values of buffs to make it easier to work with them for
 * packets.
 *
 * @author Tyler
 */
public class BuffValueHolder {

    //TODO implement

    private int sourceLevel, sourceId, value;

    public BuffValueHolder(int src, int srcLevel, int val) {
        sourceId = src;
        sourceLevel = srcLevel;
        value = val;
    }

    public int getSourceLevel() {
        return sourceLevel;
    }

    public void setSourceLevel(int val) {
        sourceLevel = val;
    }

    public int getSourceID() {
        return sourceId;
    }

    public void setSourceID(int val) {
        sourceId = val;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int val) {
        value = val;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.sourceLevel;
        hash = 89 * hash + this.sourceId;
        hash = 89 * hash + this.value;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BuffValueHolder other = (BuffValueHolder) obj;
        if (this.sourceLevel != other.sourceLevel) {
            return false;
        }
        if (this.sourceId != other.sourceId) {
            return false;
        }
        if (this.value != other.value) {
            return false;
        }
        return true;
    }

}
