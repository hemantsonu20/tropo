package com.tropo.webapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cisco.wx2.email.EmailEngineException;
import com.cisco.wx2.email.config.EmailEngineConfig;
import com.cisco.wx2.email.recorder.EmailEventMetrics;

public class PersonalEmailEngineConfig implements EmailEngineConfig {
    
    List<String> vendorList = new ArrayList<String>();
    Map<String, String> apiMap = new HashMap<String, String>();
    Map<String, String> idMap = new HashMap<String, String>();
    Map<String, String> credMap = new HashMap<String, String>();
    
    private final String vendor = "MAILGUN";
    private final String apiUrl = "https://api.mailgun.net/v3";
    private final String identity = "sandbox3538a7c6281049bbb67ff780f0274dbe.mailgun.org";
    private final String credential = "key-7b734e605d402c5ea9b8aca80ad8fd99";
    
    public PersonalEmailEngineConfig() {
    
        vendorList.add(vendor);
        apiMap.put(vendor, apiUrl);
        idMap.put(vendor, identity);
        credMap.put(vendor, credential);
        
    }
    
    @Override
    public List<String> getVendorName() throws EmailEngineException {
    
        return vendorList;
    }
    
    @Override
    public Map<String, String> getApiUrl() throws EmailEngineException {
    
        return apiMap;
    }
    
    @Override
    public Map<String, String> getIdentity() throws EmailEngineException {
    
        return idMap;
    }
    
    @Override
    public Map<String, String> getCredential() throws EmailEngineException {
    
        return credMap;
    }
    
    @Override
    public EmailEventMetrics getEmailEventRecoder() throws EmailEngineException {
    
        return null;
    }
    
    @Override
    public ServiceLevel getServiceLevel() throws EmailEngineException {
    
        return ServiceLevel.integration;
    }
    
    @Override
    public List<String> getDomainWhiteList() throws EmailEngineException {
    
        List<String> emailRegExWhiteList = new ArrayList<String>();
        emailRegExWhiteList.add("@outbound-test-wbx2.example.com");
        emailRegExWhiteList.add("@gmail.com");
        emailRegExWhiteList.add("@cisco.com");
        return emailRegExWhiteList;
    }
    
    @Override
    public String getEnd2EndPattern() throws EmailEngineException {
    
        return "([^\\s]+(test|example|gmail|cisco)(\\.(com))$)";
    }
}