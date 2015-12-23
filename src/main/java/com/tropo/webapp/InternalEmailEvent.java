package com.tropo.webapp;

import java.util.Date;
import java.util.Map;

public class InternalEmailEvent {

    private String domain;
    private String serviceName;
    private String event;
    private Date sendDate;
    private String messageId;
    private String subject;
    private String sender;
    private String recipient;
    private String url;
    private Map<String, String> userVariables;
    private EmailEventError error;


    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getUserVariables() {
        return userVariables;
    }

    public void setUserVariables(Map<String, String> userVariables) {
        this.userVariables = userVariables;
    }

    public EmailEventError getError() {
        return error;
    }

    public void setError(EmailEventError error) {
        this.error = error;
    }

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((error == null) ? 0 : error.hashCode());
        result = prime * result + ((event == null) ? 0 : event.hashCode());
        result = prime * result + ((messageId == null) ? 0 : messageId.hashCode());
        result = prime * result + ((recipient == null) ? 0 : recipient.hashCode());
        result = prime * result + ((sendDate == null) ? 0 : sendDate.hashCode());
        result = prime * result + ((sender == null) ? 0 : sender.hashCode());
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
        result = prime * result + ((subject == null) ? 0 : subject.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        result = prime * result + ((userVariables == null) ? 0 : userVariables.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        InternalEmailEvent other = (InternalEmailEvent) obj;
        if (error == null) {
            if (other.error != null) {
                return false;
            }
        } else if (!error.equals(other.error)) {
            return false;
        }
        if (event == null) {
            if (other.event != null) {
                return false;
            }
        } else if (!event.equals(other.event)) {
            return false;
        }
        if (messageId == null) {
            if (other.messageId != null) {
                return false;
            }
        } else if (!messageId.equals(other.messageId)) {
            return false;
        }
        
        if (recipient == null) {
            if (other.recipient != null) {
                return false;
            }
        } else if (!recipient.equals(other.recipient)) {
            return false;
        }
        if (sendDate == null) {
            if (other.sendDate != null) {
                return false;
            }
        } else if (!sendDate.equals(other.sendDate)) {
            return false;
        }
        if (sender == null) {
            if (other.sender != null) {
                return false;
            }
        } else if (!sender.equals(other.sender)) {
            return false;
        }
        if (domain == null) {
            if (other.domain != null) {
                return false;
            }
        } else if (!domain.equals(other.domain)) {
            return false;
        }
        if (serviceName == null) {
            if (other.serviceName != null) {
                return false;
            }
        } else if (!serviceName.equals(other.serviceName)) {
            return false;
        }
        if (subject == null) {
            if (other.subject != null) {
                return false;
            }
        } else if (!subject.equals(other.subject)) {
            return false;
        }
        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
            return false;
        }
        if (userVariables == null) {
            if (other.userVariables != null) {
                return false;
            }
        } else if (!userVariables.equals(other.userVariables)) {
            return false;
        }
        return true;
    }

}
