package com.tropo.webapp;

import static com.voxeo.tropo.Key.ASYNC_UPLOAD;
import static com.voxeo.tropo.Key.ATTEMPTS;
import static com.voxeo.tropo.Key.BARGEIN;
import static com.voxeo.tropo.Key.EMAIL_FORMAT;
import static com.voxeo.tropo.Key.EVENT;
import static com.voxeo.tropo.Key.ID;
import static com.voxeo.tropo.Key.INTERDIGIT_TIMEOUT;
import static com.voxeo.tropo.Key.MAX_SILENCE;
import static com.voxeo.tropo.Key.MAX_TIME;
import static com.voxeo.tropo.Key.METHOD;
import static com.voxeo.tropo.Key.MODE;
import static com.voxeo.tropo.Key.NAME;
import static com.voxeo.tropo.Key.NEXT;
import static com.voxeo.tropo.Key.PASSWORD;
import static com.voxeo.tropo.Key.TERMINATOR;
import static com.voxeo.tropo.Key.TIMEOUT;
import static com.voxeo.tropo.Key.TO;
import static com.voxeo.tropo.Key.URL;
import static com.voxeo.tropo.Key.USERNAME;
import static com.voxeo.tropo.Key.VALUE;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Base64;
import java.util.Iterator;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.cisco.wx2.email.Email;
import com.cisco.wx2.email.EmailEngineException;
import com.cisco.wx2.email.EmailEngineFactory;
import com.cisco.wx2.email.EmailHandler;
import com.cisco.wx2.email.event.EmailEvent;
import com.cisco.wx2.email.event.EmailEventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxeo.tropo.ActionResult;
import com.voxeo.tropo.Tropo;
import com.voxeo.tropo.TropoResult;
import com.voxeo.tropo.TropoSession;
import com.voxeo.tropo.TropoUtils;
import com.voxeo.tropo.actions.AskAction;
import com.voxeo.tropo.actions.Do;
import com.voxeo.tropo.actions.RecordAction;
import com.voxeo.tropo.actions.TransferAction;
import com.voxeo.tropo.enums.Mode;

@Path("/")
public class Controller {
    
    @Context
    UriInfo info;
    
    @Context
    HttpServletRequest request;
    
    private static final int DEFAULT_BUFFER_SIZE = 10240;
    
    private static String askBody;
    private static String recordBody;
    private static String dumpResponse;
    private static String diversionBody;
    private static String scriptBody;
    private static String wwwHeader = "Basic realm=\"pratapi\"";
    
    private final static String DIVERSION_HEADER_KEY = "diversion";
    private final static String DIVERSION_HEADER_VALUE = "pregoldtx2sl 1guisnr2 <sip:pregoldtx2sl+1guisnr2@gmail-com.wbx2.com;x-cisco-number=6337>;reason=no-answer;privacy=off;screen=yes;x-cisco-tenant=";
    
    private static File attachment;
    
    private static int dumpCode = 200;
    
    private static int scriptCode = 200;
    
    private static final PersonalEmailEngineConfig config = new PersonalEmailEngineConfig();
    
    @POST
    @Path("rna")
    public Response rna(String json, @QueryParam("num") String num) {
    
        getAndPrintSession(json);
        
        String diversion = "<sip:9512890915@cisco.com>;x-cisco-tenant=d46ed552-040a-4c32-9305-0a52f5501d5d;reason=no-answer;privacy=off;screen=yes;";
        
        Tropo tropo = new Tropo();
        tropo.on("continue", "/transfercomplete?event=continue").say("transfer completed, thank you");
        tropo.on("hangup", "/transfercomplete?event=hangup");
        tropo.on("incomplete", "/transfercomplete?event=incomplete");
        tropo.on("error", "/transfercomplete?event=error");
        tropo.say("transferring call towards Avril...");
        TransferAction transferAction = tropo.transfer(TO(num + "@sip.tropo.com"), NAME("php-transfer"), TIMEOUT(10.0f));
        transferAction.headers(new String[] { DIVERSION_HEADER_KEY, diversion });
        
        return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
    }
    
    @GET
    @Path("customer")
    public Response customer(String json) {
    
        printPrettyObject(json);
        String text = "{ \"name\" : \"Hemant\"  }";
        return Response.status(200).entity(text).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
    }
    
    @POST
    @Path("script")
    public Response script(String json) {
    
        getAndPrintSession(json);
        
        Tropo tropo = new Tropo();
        tropo.say("Hello tropo scripting");
        
        return Response.status(scriptCode).entity(scriptBody == null ? tropo.text() : scriptBody)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
    }
    
    @POST
    @Path("diversion")
    public Response diversion(String json) {
    
        getAndPrintSession(json);
        String body = (null == diversionBody) ? formTransferSampleBody("sip:+12244207705@sip-trunk-bandwidth.tropo.com") : diversionBody;
        return Response.status(200).entity(body).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
    }
    
    @POST
    @Path("transfercomplete")
    public Response transferComplete(String json, @QueryParam("event") String event) {
    
        System.out.println("transfer result with event: " + event);
        getAndPrintResult(json);
        
        Tropo tropo = new Tropo();
        tropo.hangup();
        return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("record")
    public Response record(String json) {
    
        getAndPrintSession(json);
        
        if (attachment != null) {
            attachment.delete();
            attachment = null;
        }
        
        String body = (null == recordBody) ? formRecordSampleBody() : recordBody;
        return Response.status(200).entity(body).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
    }
    
    @POST
    @Path("upload")
    public Response upload(String upload, @QueryParam("content") String content) {
    
        switch (content) {
        
            case "record":
                Controller.recordBody = "null".equals(upload) ? null : upload;
                break;
            case "ask":
                Controller.askBody = "null".equals(upload) ? null : upload;
                break;
            case "diversion":
                Controller.diversionBody = "null".equals(upload) ? null : upload;
                break;
            case "scriptBody":
                Controller.scriptBody = "null".equals(upload) ? null : upload;
                break;
            case "scriptCode":
                Controller.scriptCode = "null".equals(upload) ? 200 : Integer.parseInt(upload);
                break;
            case "dumpCode":
                Controller.dumpCode = "null".equals(upload) ? 200 : Integer.parseInt(upload);
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
            case "diversion":
                text = formTransferSampleBody("tel:+19194761199");
                break;
        }
        return Response.status(200).entity(text).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
    }
    
    @POST
    @Path("continue")
    public Response continueEvent(String json) throws IOException {
    
        System.out.println("continue event at " + System.currentTimeMillis());
        getAndPrintResult(json);
        
        return Response.status(200).entity((null == askBody) ? formAskSampleBody() : askBody)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("incomplete")
    public Response inComplete(String json) {
    
        getAndPrintResult(json);
        
        Tropo tropo = new Tropo();
        tropo.hangup();
        return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("hangup")
    public Response hangup(String json) throws EmailEngineException, IOException {
    
        getAndPrintResult(json);
        
        if (null != attachment) {
            sendMail();
        }
        
        Tropo tropo = new Tropo();
        tropo.hangup();
        return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("error")
    public Response error(String json) {
    
        getAndPrintResult(json);
        Tropo tropo = new Tropo();
        tropo.hangup();
        return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
        
    }
    
    @POST
    @Path("/")
    public Response transcript(String json) {
    
        printPrettyObject(json);
        return Response.ok().build();
    }
    
    @POST
    @Path("dump")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response dumpPost(@FormDataParam("filename") InputStream in, @FormDataParam("filename") FormDataContentDisposition fileDetail)
            throws IOException {
    
        return handleDump(in, fileDetail);
    }
    
    @PUT
    @Path("dump")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response dumpPut(@FormDataParam("filename") InputStream in, @FormDataParam("filename") FormDataContentDisposition fileDetail)
            throws IOException {
    
        return handleDump(in, fileDetail);
        
    }
    
    private Response handleDump(InputStream in, FormDataContentDisposition fileDetail) {
    
        String actualauth = request.getHeader(HttpHeaders.AUTHORIZATION);
        String expectedAuth = "Basic " + Base64.getEncoder().encodeToString("pratapi:hemant".getBytes());
        System.out.println(String.format("Actual auth ]%s[ , expected auth ]%s[", actualauth, expectedAuth));
        if (!expectedAuth.equals(actualauth)) {
            return Response.status(401).header(HttpHeaders.WWW_AUTHENTICATE, wwwHeader).build();
        }
        
        try {
            processBody(in, fileDetail);
        }
        catch (Exception e) {
            System.out.println("*******process exception************");
            e.printStackTrace();
        }
        System.out.println("recording dump response at " + System.currentTimeMillis());
        return Response.status(dumpCode).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.LOCATION, "http://location-header.com/recording").header("h1", "v1").header("h2", "v2").header("h3", "v3")
                .entity(null != dumpResponse ? dumpResponse : new Bean()).build();
        
    }
    
    private void processBody(InputStream in, FormDataContentDisposition fileDetail) throws IOException, EmailEngineException {
    
        if (null == in) {
            System.out.println("input stream is null");
            return;
        }
        
        String fileName = null;
        if (fileDetail != null) {
            fileName = fileDetail.getFileName();
            System.out.println("Content-Disposition");
            System.out.println(fileDetail);
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
        
        attachment = File.createTempFile("recording-" + prefix, suffix);
        
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
    
        TropoResult result = getAndPrintResult(json);
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
    public Response email(@QueryParam("id") String id) throws MalformedURLException, EmailEngineException {
    
        String smtpid;
        if (null == id) {
            smtpid = sendMail();
        }
        else {
            smtpid = sendMail(id);
        }
        
        return Response.ok().entity(smtpid).build();
        
    }
    
    @POST
    @Path("query")
    public Response query(@QueryParam("id") String id) throws EmailEngineException, JsonProcessingException {
    
        System.out.println("******query***********");
        // printHeaders();
        System.out.println("query for " + id);
        
        EmailEngineFactory factory = EmailEngineFactory.builder().emailEngineConfig(config)/*
                                                                                            * . proxyHost (
                                                                                            * "proxy-wsa.esl.cisco.com"
                                                                                            * ) . proxyPort (
                                                                                            * 80 )
                                                                                            */.build();
        EmailHandler handler = factory.newEmailHandler("admin");
        Iterator<EmailEvent> event = handler.queryEmailEvent(id, EmailEventType.opened, EmailEventType.clicked, EmailEventType.delivered,
                EmailEventType.failed);
        
        boolean flag = false;
        int count = 0;
        while (event.hasNext()) {
            
            System.out.println("**********email event***********");
            System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(event.next()));
            flag = true;
            count++;
        }
        
        return Response.ok().entity(flag + " " + count).build();
        
    }
    
    private String formRecordSampleBody() {
    
        Tropo tropo = new Tropo();
        
        tropo.on("continue", "/continue");
        
        tropo.on("incomplete", "/incomplete").say("no message recorded, good bye");
        
        tropo.on("hangup", "/hangup");
        
        tropo.on("error", "/error");
        
        tropo.say("");
        
        RecordAction recordAction = tropo.record(NAME("phprecord"), URL(info.getBaseUri() + "dump"), BARGEIN(false), MAX_SILENCE(3.0f),
                MAX_TIME(300.0f), ATTEMPTS(1), TIMEOUT(5.0f), INTERDIGIT_TIMEOUT(1), ASYNC_UPLOAD(false), METHOD("POST"), USERNAME("pratapi"),
                PASSWORD("hemant"));
        
        recordAction
                .and(Do.say(VALUE("Sorry, the person you are trying to reach is not available, record your message after the tone, When you are finished, hangup or press pound for more options...")));
        
        recordAction.transcription(ID("phprecord"), URL(info.getBaseUri().toString()), EMAIL_FORMAT("encoded"));
        recordAction.choices(TERMINATOR("0,1,2,3,4,5,6,7,8,9,*,#"));
        
        return tropo.text();
    }
    
    private String formTransferSampleBody(String to) {
    
        // abhibane voicemail number -> tel:+19194761199
        
        // abhibane direct number -> tel:+19194761174
        // my number -> sip:+12244207705@sip-trunk-bandwidth.tropo.com
        // id:pin -> ;tel:+19194761199;postd=pp81001174#pp2297734#
        
        // sip:+19194761199@cluster-a-ucxn.sc-tx3.huron-dev.com:5061
        
        // ran ->
        // sip:+14084744438@trunk.tropoaa.huron-int.com;user=phone;transport=tls;x-cisco-tenant=dec55d7a-9d08-4a12-a9a7-052939c29ae0
        
        Tropo tropo = new Tropo();
        tropo.on("continue", "/transfercomplete?event=continue").say("transfer completed, thank you");
        tropo.on("hangup", "/transfercomplete?event=hangup");
        tropo.on("incomplete", "/transfercomplete?event=incomplete");
        tropo.on("error", "/transfercomplete?event=error");
        tropo.say("transferring call towards CPE Cloud...");
        TransferAction transferAction = tropo.transfer(TO(to), NAME("php-transfer"), TIMEOUT(10.0f));
        transferAction.headers(new String[] { DIVERSION_HEADER_KEY, DIVERSION_HEADER_VALUE + UUID.randomUUID() });
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
    
    private String sendMail(String... cc) throws EmailEngineException, MalformedURLException {
    
        String to = "pratapat@cisco.com";
        String from = "abhibane@cisco.com";
        String subject = "You got a voicemail";
        String textBody = "Someone left a message for you, please find recording in attachment";
        
        EmailEngineFactory factory = EmailEngineFactory.builder().emailEngineConfig(config)/*
                                                                                            * . proxyHost (
                                                                                            * "proxy-wsa.esl.cisco.com"
                                                                                            * ) . proxyPort (
                                                                                            * 80 )
                                                                                            */.build();
        EmailHandler handler = factory.newEmailHandler("admin");
        Email email = new Email();
        email.putUserVariable("sampleuserkey", "sampleuservalue");
        email.setSender(from);
        email.addRecipient(to);
        email.setTestMode(true);
        if (cc.length > 0) {
            email.addCcRecipient(cc[0]);
        }
        email.setSubject(subject);
        email.setTextContent(textBody);
        if (null != attachment) {
            email.addAttachment(attachment);
        }
        
        String id = handler.send(email);
        System.out.println("email sent, smtp id: " + id);
        return id;
    }
    
    @POST
    @Path("multiparts")
    @Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED })
    public Response multiparts(FormDataMultiPart multiPart) throws IOException {
    
        System.out.println("***all multiparts name***");
        multiPart.getFields().forEach((k, v) -> {
            System.out.println(k);
            v.forEach((b) -> {
                
                System.out.println("    " + b.getHeaders());
            }
            
            );
        });
        
        return Response.ok().build();
    }
    
    @POST
    @Path("emulatordump")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response emulatorDump(@FormDataParam("file") InputStream in, @FormDataParam("file") FormDataContentDisposition fileDetail)
            throws IOException {
    
        return handleDump(in, fileDetail);
    }
    
    @POST
    @Path("sleeptransfer")
    public Response sleepTransfer(String json) throws InterruptedException {
    
        getAndPrintSession(json);
        
        Tropo tropo = new Tropo();
        tropo.on("continue", "/sleep");
        tropo.say("transferring to sleep method");
        return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
    }
    
    @POST
    @Path("servererrortransfer")
    public Response errorTransfer(String json) throws InterruptedException {
    
        getAndPrintSession(json);
        
        Tropo tropo = new Tropo();
        tropo.on("continue", "/servererror");
        tropo.say("transferring to server error method");
        return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
    }
    
    @POST
    @Path("sleep")
    public Response sleep(String json) throws InterruptedException {
    
        getAndPrintSession(json);
        
        Thread.sleep(60000);
        
        Tropo tropo = new Tropo();
        tropo.say("sleep method");
        return Response.status(200).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
    }
    
    @POST
    @Path("servererror")
    public Response serverError(String json) {
    
        getAndPrintSession(json);
        
        Tropo tropo = new Tropo();
        tropo.say("serverError method");
        return Response.status(500).entity(tropo.text()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build();
    }
    
    private TropoSession getAndPrintSession(String json) {
    
        TropoSession session = null;
        System.out.println("////////////////////////////////////");
        try {
            session = new Tropo().session(json);
            System.out.println(session);
            
        }
        catch (Exception e) {
            System.out.println("Invalid session:" + json);
        }
        
        System.out.println("////////////////////////////////////");
        return session;
        
    }
    
    private TropoResult getAndPrintResult(String json) {
    
        TropoResult result = null;
        System.out.println("////////////////////////////////////");
        try {
            result = new Tropo().parse(json);
            System.out.println(result);
            
        }
        catch (Exception e) {
            System.out.println("Invalid result:" + json);
        }
        System.out.println("////////////////////////////////////");
        return result;
        
    }
    
    private void printPrettyObject(Object obj) {
    
        System.out.println("////////////////////////////////////");
        System.out.println(TropoUtils.toPrettyString(obj));
        System.out.println("////////////////////////////////////");
    }
    
    public static void main(String args[]) {
    
        System.out.println(new Controller().formTransferSampleBody("tel:+19194761199"));
    }
    
}