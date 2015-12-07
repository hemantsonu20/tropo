package com.tropo.webapp;

import static com.voxeo.tropo.Key.ATTEMPTS;
import static com.voxeo.tropo.Key.BARGEIN;
import static com.voxeo.tropo.Key.EMAIL_FORMAT;
import static com.voxeo.tropo.Key.EVENT;
import static com.voxeo.tropo.Key.ID;
import static com.voxeo.tropo.Key.INTERDIGIT_TIMEOUT;
import static com.voxeo.tropo.Key.MAX_SILENCE;
import static com.voxeo.tropo.Key.MODE;
import static com.voxeo.tropo.Key.NAME;
import static com.voxeo.tropo.Key.NEXT;
import static com.voxeo.tropo.Key.TERMINATOR;
import static com.voxeo.tropo.Key.TIMEOUT;
import static com.voxeo.tropo.Key.URL;
import static com.voxeo.tropo.Key.VALUE;
import static com.voxeo.tropo.Key.createKey;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
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
import com.cisco.wx2.email.EmailEngineNotification;
import com.cisco.wx2.email.EmailHandler;
import com.cisco.wx2.email.event.EmailEvent;
import com.cisco.wx2.email.event.EmailEventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxeo.tropo.ActionResult;
import com.voxeo.tropo.Tropo;
import com.voxeo.tropo.TropoResult;
import com.voxeo.tropo.actions.AskAction;
import com.voxeo.tropo.actions.Do;
import com.voxeo.tropo.actions.RecordAction;
import com.voxeo.tropo.enums.Mode;

@Path("/")
public class Controller {
    
    @Context
    UriInfo info;
    
    @Context
    HttpServletRequest request;
    
    private static final int DEFAULT_BUFFER_SIZE = 10240;
    
    public static int flag = 1;
    
    public static int recording = 1;
    
    public static String askBody;
    public static String recordBody;
    
    private static File attachment;
    
    private static int dumpCode = 200;
    
    private static String messageID;
    
    @POST
    @Path("record")
    public Response record(TropoSessionBean session) {
    
        if (attachment != null) {
            attachment.delete();
            attachment = null;
        }
        
        System.out.println("*****session*****");
        System.out.println(session);
        
        return Response.status(200).entity((null == recordBody) ? formRecordSampleBody() : recordBody)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("upload")
    public Response upload(String upload, @QueryParam("content") String content) {
    
        switch (content) {
        
            case "record":
                Controller.recordBody = upload;
                break;
            case "ask":
                Controller.askBody = upload;
                break;
        
        }
        
        return Response.status(200).entity(upload).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("download")
    public Response download(@QueryParam("content") String content) {
    
        String text = null;
        switch (content) {
        
            case "record":
                text = formRecordSampleBody();
                break;
            case "ask":
                text = formAskSampleBody();
                break;
        
        }
        return Response.status(200).entity(text).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("continue")
    public Response continueEvent(String json) throws IOException {
    
        Tropo tropo = new Tropo();
        TropoResult sessionResult = tropo.parse(json);
        
        System.out.println("******result gson***********");
        System.out.println(sessionResult);
        System.out.println("******result jackson***********");
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readValue(json, Object.class)));
        
        return Response.status(200).entity((null == askBody) ? formAskSampleBody() : askBody)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("incomplete")
    public Response inComplete(Object sessionResult) {
    
        printHeaders();
        System.out.println("******result***********");
        System.out.println(sessionResult);
        
        Tropo tropo = new Tropo();
        tropo.hangup();
        return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("hangup")
    public Response hangup(String json) throws EmailEngineException, IOException {
    
        Tropo tropo = new Tropo();
        TropoResult sessionResult = tropo.parse(json);
        
        System.out.println("******result gson***********");
        System.out.println(sessionResult);
        System.out.println("******result jackson***********");
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readValue(json, Object.class)));
        
        if (null != attachment) {
            sendMail();
        }
        
        tropo.hangup();
        return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("error")
    public Response error(Object sessionResult) {
    
        System.out.println("******result***********");
        System.out.println(sessionResult);
        
        Tropo tropo = new Tropo();
        tropo.hangup();
        return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("dumpcode")
    public Response dumpCode(@QueryParam("code") String code) {
    
        return Response.ok().entity(dumpCode = Integer.parseInt(code)).build();
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
        return Response.status(dumpCode).build();
    }
    
    private void processBody(InputStream in, FormDataContentDisposition fileDetail) throws IOException, EmailEngineException {
    
        String fileName = null;
        if (fileDetail != null) {
            fileName = fileDetail.getFileName();
        }
        else {
            fileName = "recording.wav";
        }
        
        String prefix = fileName;
        String suffix = "";
        if (fileName.contains(".")) {
            prefix = fileName.substring(0, fileName.lastIndexOf('.'));
            suffix = fileName.substring(fileName.lastIndexOf('.'));
        }
        
        attachment = File.createTempFile(recording++ + "recording-" + prefix, suffix);
        
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(attachment), DEFAULT_BUFFER_SIZE)) {
            
            byte buffer[] = new byte[DEFAULT_BUFFER_SIZE];
            
            for (int length = 0; (length = in.read(buffer)) > 0;) {
                out.write(buffer, 0, length);
            }
            
        }
        System.out.println("*******fileName: " + attachment.getName());
        
    }
    
    @POST
    @Path("sendmail")
    public Response sendMail(String json, @QueryParam("event") String event) throws EmailEngineException, MalformedURLException {
    
        System.out.println("******result***********");
        System.out.println(json);
        
        Tropo tropo = new Tropo();
        if (null == attachment) {
            tropo.say("recording not received for some reason, please try again");
            return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        }
        
        if (event == null) {
            tropo.say("this should not happen, hangup is null");
            return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        }
        
        switch (event) {
        
            case "hangup":
                sendMail();
                tropo.hangup();
                return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
                
            case "incomplete":
                sendMail();
                tropo.say("your message has been sent, good bye");
                return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
                
            default:
                break;
        
        }
        
        TropoResult result = tropo.parse(json);
        
        String dtmfInput = "";
        ActionResult action = result.getActions().get(0);
        
        if ("record-input".equals(action.getName())) {
            dtmfInput = action.getValue();
        }
        
        switch (dtmfInput) {
        
            case "4":
                tropo.say("message cancelled, good bye");
                break;
            
            default:
                sendMail();
                tropo.say("your message has been sent, good bye");
                break;
        }
        
        attachment.delete();
        return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("email")
    public Response email() throws MalformedURLException, EmailEngineException {
    
        sendMail();
        return Response.ok().build();
        
    }
    
    @POST
    @Path("notify")
    public Response notify(String body) {
    
        System.out.println("******notification***********");
        printHeaders();
        System.out.println(body);
        
        return Response.ok().build();
        
    }
    
    @POST
    @Path("query")
    public Response query(String body) throws EmailEngineException, JsonProcessingException {
    
        System.out.println("******query***********");
        printHeaders();
        System.out.println(body);
        
        EmailEngineFactory factory = EmailEngineFactory.builder().emailEngineConfig(new PersonalEmailEngineConfig()).build();
        EmailHandler handler = factory.newEmailHandler("admin");
        Iterator<EmailEvent> event = handler.queryEmailEvent(messageID, EmailEventType.opened, EmailEventType.clicked, EmailEventType.delivered,
                EmailEventType.failed);
        
        while(event.hasNext()){
            
            System.out.println("**********email event***********");
            System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(event.next()));
        }
        
        return Response.ok().build();
        
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
    
    private String formRecordSampleBody() {
    
        Tropo tropo = new Tropo();
        
        tropo.on("continue", "/continue");
        
        tropo.on("incomplete", "/incomplete").say("no message recorded, good bye");
        
        tropo.on("hangup", "/hangup");
        
        tropo.on("error", "/error");
        
        RecordAction recordAction = tropo.record(NAME("phprecord"), URL(info.getBaseUri() + "dump"), BARGEIN(false), MAX_SILENCE(3.0f),
                createKey("maxTime", 300.0f), ATTEMPTS(1), TIMEOUT(5.0f), INTERDIGIT_TIMEOUT(1));
        
        recordAction
                .and(Do.say(VALUE("Sorry, the person you are trying to reach is not available, record your message after the tone, When you are finished, hangup or press pound for more options...")));
        
        recordAction.transcription(ID("phprecord" + flag++), URL("mailto:pratapat@cisco.com"), EMAIL_FORMAT("encoded"));
        recordAction.choices(TERMINATOR("#"));
        
        return tropo.text();
    }
    
    private String formAskSampleBody() {
    
        Tropo tropo = new Tropo();
        
        tropo.on(EVENT("continue"), NEXT("/sendmail?event=continue"));
        
        tropo.on("incomplete", "/sendmail?event=incomplete");
        
        tropo.on("hangup", "/sendmail?event=hangup");
        
        tropo.on("error", "/error");
        
        AskAction askAction = tropo.ask(NAME("record-input"), BARGEIN(true), ATTEMPTS(2), TIMEOUT(5.0f), MODE(Mode.DTMF), INTERDIGIT_TIMEOUT(1));
        
        askAction
                .and(Do.say(VALUE("invalid input..."), EVENT("nomatch")).say(VALUE("to send this message, press pound or hangup, to discard  4...")));
        
        askAction.choices(VALUE("4,#"), MODE(Mode.DTMF), TERMINATOR("a"));
        
        return tropo.text();
    }
    
    private void sendMail() throws EmailEngineException, MalformedURLException {
    
        String to = "pratapat@cisco.com";
        String cc2 = "hemantsonu20@gmail.com";
        String from = "abhibane@cisco.com";
        String subject = "You got a voicemail";
        String textBody = "Someone left a message for you, please find recording in attachment";
        
        EmailEngineFactory factory = EmailEngineFactory.builder().emailEngineConfig(new PersonalEmailEngineConfig()).build();
        EmailHandler handler = factory.newEmailHandler("admin");
        Email email = new Email();
        email.setSender(from);
        email.addRecipient(to);
        email.addCcRecipient(cc2);
        email.setSubject(subject);
        email.setTextContent(textBody);
        if (null != attachment) {
            email.addAttachment(attachment);
        }
        
        System.out.println(info.getBaseUri() + "notify");
        EmailEngineNotification notification = new EmailEngineNotification(new URL(info.getBaseUri() + "notify"));
        notification.addEmailEvent(EmailEventType.clicked);
        notification.addEmailEvent(EmailEventType.opened);
        notification.addEmailEvent(EmailEventType.failed);
        notification.addEmailEvent(EmailEventType.delivered);
        
        messageID = handler.send(email, notification);
        System.out.println("email sent, smtp id: " + messageID);
        
    }
    
}