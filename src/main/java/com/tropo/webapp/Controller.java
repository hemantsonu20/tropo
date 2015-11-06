package com.tropo.webapp;

import static com.voxeo.tropo.Key.ATTEMPTS;
import static com.voxeo.tropo.Key.BARGEIN;
import static com.voxeo.tropo.Key.EMAIL_FORMAT;
import static com.voxeo.tropo.Key.EVENT;
import static com.voxeo.tropo.Key.ID;
import static com.voxeo.tropo.Key.MODE;
import static com.voxeo.tropo.Key.NAME;
import static com.voxeo.tropo.Key.NEXT;
import static com.voxeo.tropo.Key.TERMINATOR;
import static com.voxeo.tropo.Key.TIMEOUT;
import static com.voxeo.tropo.Key.URL;
import static com.voxeo.tropo.Key.VALUE;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.cisco.wx2.email.Email;
import com.cisco.wx2.email.EmailEngineException;
import com.cisco.wx2.email.EmailEngineFactory;
import com.cisco.wx2.email.EmailHandler;
import com.voxeo.tropo.Tropo;
import com.voxeo.tropo.actions.Do;
import com.voxeo.tropo.actions.RecordAction;
import com.voxeo.tropo.enums.Mode;

@Path("/")
public class Controller {
    
    @Context
    UriInfo info;
    
    private static final int DEFAULT_BUFFER_SIZE = 10240;
    
    public static int flag = 1;
    
    public static int recording = 1;
    
    public static String upload;
    
    @Context
    HttpServletRequest request;
    
    @POST
    @Path("record")
    public Response record(Object session) {
    
        System.out.println("*****session*****");
        System.out.println(session);
        
        return Response.status(200).entity(Controller.upload).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("upload")
    public Response upload(String upload) {
    
        Controller.upload = upload;
        return Response.status(200).entity(upload).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("download")
    public Response download() {
    
        Tropo tropo = new Tropo();
        
        tropo.on("continue", "/continue").say("your message has been sent, thank you");
        
        tropo.on("incomplete", "/incomplete").say("record unsuccessful, please try again");
        
        tropo.on("hangup", "/hangup").say("event hangup occured");
        
        tropo.on("error", "/error").say("event error occured");
        
        RecordAction recordAction = tropo.record(NAME("phprecord"), URL(info.getBaseUri() + "dump"));
        
        recordAction.and(Do.say(
                VALUE("Sorry, User is not available, record your message after the tone, When you are finished, hangup or press pound")).say(
                VALUE("didn't hear anything, call again"), EVENT("timeout")));
        
        recordAction.transcription(ID("phprecord" + flag++), URL("mailto:pratapat@cisco.com"), EMAIL_FORMAT("encoded"));
        recordAction.choices(TERMINATOR("#"));
        
        System.out.println("******text***********");
        System.out.println(tropo.text());
        
        return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("continue")
    public Response continueEvent(Object sessionResult) {
    
        System.out.println("******result***********");
        System.out.println(sessionResult);
        Tropo tropo = new Tropo();
        return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("incomplete")
    public Response askFailure(Object sessionResult) {
    
        System.out.println("******result***********");
        System.out.println(sessionResult);
        
        Tropo tropo = new Tropo();
        return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("hangup")
    public Response hangup(Object sessionResult) {
    
        System.out.println("******result***********");
        System.out.println(sessionResult);
        
        Tropo tropo = new Tropo();
        tropo.say("record hangup");
        tropo.hangup();
        return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("error")
    public Response error(Object sessionResult) {
    
        System.out.println("******result***********");
        System.out.println(sessionResult);
        
        Tropo tropo = new Tropo();
        tropo.say("record error");
        tropo.hangup();
        return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("dump")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response dump(@FormDataParam("filename") InputStream in, @FormDataParam("filename") FormDataContentDisposition fileDetail)
            throws IOException {
    
        printHeaders();
        try {
            processBody(in, fileDetail);
        }
        catch (Exception e) {
            System.out.println("*******process exception************");
            e.printStackTrace();
        }
        Tropo tropo = new Tropo();
        tropo.say("recording dumped and email sent successfully");
        return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    private void processBody(InputStream in, FormDataContentDisposition fileDetail) throws IOException, EmailEngineException {
    
        String fileName = fileDetail.getFileName();
        System.out.println("*******fileName: " + fileName);
        String prefix = fileName;
        String suffix = "";
        if (fileName.contains(".")) {
            prefix = fileName.substring(0, fileName.lastIndexOf('.'));
            suffix = fileName.substring(fileName.lastIndexOf('.'));
        }
        
        File file = File.createTempFile(recording++ + "recording-" + prefix, suffix);
        
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file), DEFAULT_BUFFER_SIZE)) {
            
            byte buffer[] = new byte[DEFAULT_BUFFER_SIZE];
            
            for (int length = 0; (length = in.read(buffer)) > 0;) {
                out.write(buffer, 0, length);
            }
            
        }
        sendMail(file);
        file.delete();
        
    }
    
    @POST
    @Path("mail")
    public Response mail() throws EmailEngineException {
    
        sendMail(null);
        
        return Response.ok().build();
    }
    
    private void sendMail(File attachment) throws EmailEngineException {
    
        String to = "pratapat@cisco.com";
        String cc1 = "ataggarw@cisco.com";
        String cc2 = "hemantsonu20@gmail.com";
        String from = "abhibane@cisco.com";
        String subject = "You got a voicemail";
        String textBody = "Someone left a message for you, please find recording in attachment";
        
        EmailEngineFactory factory = EmailEngineFactory.builder().emailEngineConfig(new SimpleEmailEngineConfig()).build();
        EmailHandler handler = factory.newEmailHandler("admin");
        Email email = new Email();
        email.setSender(from);
        email.addRecipient(to);
        email.addCcRecipient(cc1);
        email.addCcRecipient(cc2);
        email.setSubject(subject);
        email.setTextContent(textBody);
        if (null != attachment) {
            email.addAttachment(attachment);
        }
        
        String smtpId = handler.send(email);
        System.out.println("email sent, smtp id: " + smtpId);
        
    }
    
    @POST
    @Path("tropoask")
    public Response getTropo(TropoSessionBean session) {
    
        System.out.println("*****session*****");
        System.out.println(session);
        
        Tropo tropo = new Tropo();
        tropo.say("Welcome to our company. Please enter the number of the department you wish to be forwarded to:");
        tropo.ask(NAME("userChoice"), BARGEIN(true), MODE(Mode.DTMF), TIMEOUT(10f), ATTEMPTS(2)).and(
                Do.say(VALUE("Sorry, I didn't hear anything"), EVENT("timeout")).say(
                        "Press #1 for Customer Support. Press #2 for sales. Press #3 for emergencies. Press #4 for any other thing."),
                Do.choices(VALUE("[1 DIGIT]")));
        tropo.on(EVENT("continue"), NEXT("asksuccess"));
        tropo.on(EVENT("incomplete"), NEXT("askfailure"));
        
        System.out.println("******text***********");
        System.out.println(tropo.text());
        
        return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
    }
    
    private void printHeaders() {
    
        System.out.println("*****dumpHeader*****");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            
            String headerName = headerNames.nextElement();
            System.out.println(headerName);
            
            Enumeration<String> headers = request.getHeaders(headerName);
            while (headers.hasMoreElements()) {
                System.out.println("\t" + headers.nextElement());
            }
        }
        
    }
    
}