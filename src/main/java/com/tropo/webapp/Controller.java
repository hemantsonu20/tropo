package com.tropo.webapp;

import static com.voxeo.tropo.Key.ATTEMPTS;
import static com.voxeo.tropo.Key.BARGEIN;
import static com.voxeo.tropo.Key.EVENT;
import static com.voxeo.tropo.Key.MODE;
import static com.voxeo.tropo.Key.NAME;
import static com.voxeo.tropo.Key.NEXT;
import static com.voxeo.tropo.Key.TIMEOUT;
import static com.voxeo.tropo.Key.VALUE;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.voxeo.tropo.Tropo;
import com.voxeo.tropo.actions.Do;
import com.voxeo.tropo.enums.Mode;

@Path("/")
public class Controller {
	
	@Context
	UriInfo info;
	
	public static boolean flag = true;

	@GET
	@Path("jmeter")
	public Response jmeterGet(@Context HttpServletRequest request) {
		
		System.out.println("get request: " + info.getQueryParameters());
		
		Enumeration headerNames = request.getHeaderNames();
		   
	     while (headerNames.hasMoreElements()) {
	     String key = (String) headerNames.nextElement();
	         String value = request.getHeader(key);
	         System.out.println(key+"::"+value);
	     }
		
		
		return Response.status(200).entity("get request").build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("jmeter")
	public Response jmeterPost(@Context HttpServletRequest request, Object entity) {
		
		System.out.println("post request: " + entity);
		
		Enumeration headerNames = request.getHeaderNames();
		   
	     while (headerNames.hasMoreElements()) {
	     String key = (String) headerNames.nextElement();
	         String value = request.getHeader(key);
	         System.out.println(key+"::"+value);
	     }
		
		
		return Response.status(201).entity("post request").build();
	}
	
	
	@GET
	@Path("tropo")
	public Response ping() throws InterruptedException {
		Thread.sleep(10000000);
		return Response.status(200).entity("pinging").build();
	}
	
	@Path("tropobad")
	public Response pingbadreq() throws InterruptedException {
		return Response.status(500).entity("server error").build();
	}
	
	@POST
	@Path("tropomusic")
	public Response tropoSay(TropoSessionBean session) {

		System.out.println("*****session*****");
		System.out.println(session.toString());
		System.out.println("*****tropo*****");

		Tropo tropo = new Tropo();
		
		tropo.say("welcome to webex");
		tropo.on(EVENT("continue"), NEXT("asksuccess"));
		tropo.on(EVENT("incomplete"), NEXT("askfailure"));
		tropo.on(EVENT("error"), NEXT("askfailure"));
		
		
		//tropo.on(EVENT("incomplete"), NEXT("askfailure"));		
		//tropo.on(EVENT("error"), NEXT("askfailure"));
		
		System.out.println(tropo.text());
		System.out.println("*****************");
		return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.build();
	}
	
	@POST
	@Path("asksuccess")
	public Response askSuccess(Object sessionResult) {

		System.out.println("*****sessionResult*****");
		System.out.println(sessionResult);
		System.out.println("*****tropo*****");

		Tropo tropo = new Tropo();
		tropo.say("thank you");
		tropo.hangup();
		System.out.println(tropo.text());
		System.out.println("*****************");
		return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.build();

	}
	
	@POST
	@Path("askfailure")
	public Response askFailure(Object sessionResult) {

		System.out.println("*****sessionResult*****");
		System.out.println(sessionResult);
		System.out.println("*****tropo*****");

		Tropo tropo = new Tropo();
		tropo.say("you provided wrong information");
		tropo.hangup();
		System.out.println(tropo.text());
		System.out.println("*****************");
		return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.build();

	}
	
	@POST
	@Path("tropo")
	public Response getTropo(TropoSessionBean session) {

		System.out.println("*****session*****");
		System.out.println(session.toString());
		System.out.println("*****tropo*****");

		Tropo tropo = new Tropo();
		tropo.say("Welcome to our company. Please enter the number of the department you wish to be forwarded to:");
		tropo.ask(NAME("userChoice"), BARGEIN(true), MODE(Mode.DTMF), TIMEOUT(10f), ATTEMPTS(2)).and(
			Do.say(VALUE("Sorry, I didn't hear anything"),EVENT("timeout"))
			  .say("Press #1 for Customer Support. Press #2 for sales. Press #3 for emergencies. Press #4 for any other thing."),
			Do.choices(VALUE("[1 DIGIT]"))
		);
		tropo.on(EVENT("continue"), NEXT("asksuccess"));
		tropo.on(EVENT("incomplete"), NEXT("askfailure"));
		System.out.println(tropo.text());
		System.out.println("*****************");
		return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.build();
	}
}