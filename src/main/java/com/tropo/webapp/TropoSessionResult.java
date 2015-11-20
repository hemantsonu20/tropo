package com.tropo.webapp;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TropoSessionResult implements Serializable {
    
    private static final long serialVersionUID = 2945689744982202192L;
    
    private Map<String, Object> result;
    
    public Map<String, Object> getResult() {
    
        return result;
    }
    
    public void setResult(Map<String, Object> result) {
    
        this.result = result;
    }
    
    @Override
    public String toString() {
    
        StringBuilder builder = new StringBuilder();
        builder.append("TropoSessionResult [");
        if (result != null) {
            builder.append("result=");
            builder.append(result);
        }
        builder.append("]");
        return builder.toString();
    }
    
    /*
     * @Override public String toString() { StringBuilder builder = new StringBuilder();
     * builder.append("TropoSessionResult ["); if (result.getActions() != null) {
     * builder.append("actions="); builder.append(result.getActions());
     * builder.append(", "); } if (result.getComplete() != null) {
     * builder.append("complete="); builder.append(result.getComplete());
     * builder.append(", "); } if (result.getError() != null) { builder.append("error=");
     * builder.append(result.getError()); builder.append(", "); } if (result.getSequence()
     * != null) { builder.append("sequence="); builder.append(result.getSequence());
     * builder.append(", "); } if (result.getSessionDuration() != null) {
     * builder.append("sessionDuration="); builder.append(result.getSessionDuration());
     * builder.append(", "); } if (result.getSessionId() != null) {
     * builder.append("sessionId="); builder.append(result.getSessionId());
     * builder.append(", "); } if (result.getState() != null) {
     * builder.append("callState="); builder.append(result.getState()); }
     * builder.append("]"); return builder.toString(); }
     */
    
}
