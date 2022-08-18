/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.File;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;

/**
 *
 * @author Saffron
 */
public class MapleSkillInformationProvider {
     private final static MapleSkillInformationProvider instance = new MapleSkillInformationProvider();
    
    public static MapleSkillInformationProvider getInstance() {
        return instance;
    }
    
    protected MapleDataProvider skillPath;
    protected MapleDataProvider stringPath;
    protected MapleData jobPath;
    private String jobId;
    
    private MapleSkillInformationProvider() {
        skillPath = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/skill.wz"));
        stringPath = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/string.wz"));
        jobPath = stringPath.getData(getJobId() + ".img");
    }
    
    private String getJobId() {
        return jobId;
    }
    
    private void setJobId(String id) {
        this.jobId = id;
    }
}
