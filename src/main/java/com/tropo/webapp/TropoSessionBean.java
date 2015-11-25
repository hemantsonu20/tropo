package com.tropo.webapp;

import java.io.Serializable;

import com.voxeo.tropo.TropoSession;

public class TropoSessionBean implements Serializable {
    
    private static final long serialVersionUID = 3741507526529704913L;
    
    private TropoSession session;
    
    /**
     * @return the session
     */
    public TropoSession getSession() {
    
        return session;
    }
    
    /**
     * @param session the session to set
     */
    public void setSession(TropoSession session) {
    
        this.session = session;
    }

    @Override
    public String toString() {
    
        StringBuilder builder = new StringBuilder();
        builder.append("TropoSessionBean [");
        if (session != null) {
            builder.append("session=");
            builder.append(session);
        }
        builder.append("]");
        return builder.toString();
    }
    
    
}
