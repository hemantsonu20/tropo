package com.tropo.webapp;

import com.voxeo.tropo.TropoSession;

public class TropoSessionBean {
	
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
		builder.append("TropoSession [");
		if (session.accountId != null) {
			builder.append("accountId=");
			builder.append(session.accountId);
			builder.append(", ");
		}
		if (session.callId != null) {
			builder.append("callId=");
			builder.append(session.callId);
			builder.append(", ");
		}
		if (session.id != null) {
			builder.append("id=");
			builder.append(session.id);
			builder.append(", ");
		}
		if (session.initialText != null) {
			builder.append("initialText=");
			builder.append(session.initialText);
			builder.append(", ");
		}
		if (session.timestamp != null) {
			builder.append("timestamp=");
			builder.append(session.timestamp);
			builder.append(", ");
		}
		if (session.userType != null) {
			builder.append("userType=");
			builder.append(session.userType);
			builder.append(", ");
		}
		if (session.getTo() != null) {
			builder.append("to=");
			builder.append(session.getTo().toString());
			builder.append(", ");
		}
		if (session.getFrom() != null) {
			builder.append("from=");
			builder.append(session.getFrom().toString());
			builder.append(", ");
		}
		if (session.getHeaders() != null) {
			builder.append("headers=");
			builder.append(session.getHeaders().toString());
			builder.append(", ");
		}
		if (session.getParameters() != null) {
			builder.append("parameters=");
			builder.append(session.getParameters().toString());
		}
		builder.append("]");
		return builder.toString();
	}

}
