package io.quarkus.demo;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.Map;

@ApplicationScoped
public class NmtRepository {

    private Map<String, Long> nmtData;

    private final Object lock = new Object();

    public void updateNmtDate(Map<String, Long> data){
        synchronized (lock){
            if(data != null) {
                nmtData = Collections.unmodifiableMap(data);
            } else {
                nmtData = null;
            }
        }
    }

    public Map<String, Long> getNmtData(){
        return nmtData;
    }

}
