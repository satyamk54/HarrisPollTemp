package ca.adintel.service;

import ca.adintel.Pair;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.ModifyMessageRequest;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/** @noinspection Duplicates*/
public class EmailProcessorService {

    Gmail service = null;

    private void initService() {
        if (service==null) {
            GmailService factory = new GmailService();
            service = factory.getGmailService();
        }
    }


    public void markCompleted(List<String> subjects) {
        initService();

//        List<Pair<String>> storedRequestIds = new LinkedList<>();

        // Print the labels in the user's account.
        String user = "me";

        Label labelCompleted = null;
        Label labelNotProcessible = null;

        try {

            ListLabelsResponse labelId = service.users().labels().list(user).execute();
            for (Label l : labelId.getLabels()) {
                if (l.getName().equalsIgnoreCase("completed")) {
                    labelCompleted = l;
                }
                if (l.getName().equalsIgnoreCase("not_processible")) {
                    labelNotProcessible = l;
                }
            }

            ListMessagesResponse resp = service.users().messages().list(user).execute();//not_processible label
            for (Message m : resp.getMessages()) {
                try {

                    String id = m.getId();
                    Message m1 = service.users().messages().get(user, id).setPrettyPrint(true).execute();

                    ModifyMessageRequest mmr = new ModifyMessageRequest();
                    List<String> completed = new LinkedList<>();
                    completed.add(labelCompleted.getId());
                    mmr.setAddLabelIds(completed);


                    List<String> labels = m1.getLabelIds();

                    boolean processed = false;


                    if (labels != null) {
                        for (String label : labels) {
                            if (label.equalsIgnoreCase(labelCompleted.getId())) {
                                processed = true;
                                break;
                            }
                            if (label.equalsIgnoreCase(labelNotProcessible.getId())) {
                                processed = true;
                                break;
                            }

                        }
                    }

                    if (processed) {
//                        System.out.println("already processed");
                        continue;

                    }


                    if (m1.getPayload().getParts() != null) {
                        for (MessagePart mp : m1.getPayload().getParts()) {
                            String s = new String(mp.getBody().decodeData());
//                    System.out.println("**********MESSAGE PART******");
//                    System.out.println(s);
                            String storedRequestId = methodLocator1(s);
                            if (storedRequestId != null) {
                                String subject = getSubject(m1.getPayload());
                                if (subjects.contains(subject)) {
                                    service.users().messages().modify(user, id, mmr).execute();
                                }
//                                storedRequestIds.add(new Pair<>(subject,storedRequestId));
//                                System.out.println("found:" + storedRequestId);
                            }
                        }
                    } else {
                        String s = new String(m1.getPayload().getBody().decodeData());
//                System.out.println(s);
                        String storedRequestId = methodLocator1(s);
                        if (storedRequestId != null) {
                            String subject = getSubject(m1.getPayload());
                            if (subjects.contains(subject)) {
                                service.users().messages().modify(user, id, mmr).execute();
                            }
//                            storedRequestIds.add(new Pair<>(subject,storedRequestId));
//                            System.out.println("found:" + storedRequestId);
                        }
                    }
                } catch (Throwable t) {
                    System.out.println("not found");
                    //ignore this
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Pair<String>> getStoredRequestsFromEmails() {


        initService();

        List<Pair<String>> storedRequestIds = new LinkedList<>();

        // Print the labels in the user's account.
        String user = "me";

        Label labelCompleted = null;
        Label labelNotProcessible = null;

        try {

            ListLabelsResponse labelId = service.users().labels().list(user).execute();
            for (Label l : labelId.getLabels()) {
                if (l.getName().equalsIgnoreCase("completed")) {
                    labelCompleted = l;
                }
                if (l.getName().equalsIgnoreCase("not_processible")) {
                    labelNotProcessible = l;
                }
            }

            ListMessagesResponse resp = service.users().messages().list(user).execute();//not_processible label
            for (Message m : resp.getMessages()) {
                try {

                    String id = m.getId();
                    Message m1 = service.users().messages().get(user, id).setPrettyPrint(true).execute();

                    List<String> labels = m1.getLabelIds();

                    boolean processed = false;


                    if (labels != null) {
                        for (String label : labels) {
                            if (label.equalsIgnoreCase(labelCompleted.getId())) {
                                processed = true;
                                break;
                            }
                            if (label.equalsIgnoreCase(labelNotProcessible.getId())) {
                                processed = true;
                                break;
                            }

                        }
                    }

                    if (processed) {
                        System.out.println("already processed");
                        continue;

                    }


                    if (m1.getPayload().getParts() != null) {
                        for (MessagePart mp : m1.getPayload().getParts()) {
                            String s = new String(mp.getBody().decodeData());
//                    System.out.println("**********MESSAGE PART******");
//                    System.out.println(s);
                            String storedRequestId = methodLocator1(s);
                            if (storedRequestId != null) {
                                String subject = getSubject(m1.getPayload());
                                storedRequestIds.add(new Pair<>(subject,storedRequestId));
                                System.out.println("found:" + storedRequestId);
                            }
                        }
                    } else {
                        String s = new String(m1.getPayload().getBody().decodeData());
//                System.out.println(s);
                        String storedRequestId = methodLocator1(s);
                        if (storedRequestId != null) {
                            String subject = getSubject(m1.getPayload());
                            storedRequestIds.add(new Pair<>(subject,storedRequestId));
                            System.out.println("found:" + storedRequestId);
                        }
                    }
                } catch (Throwable t) {
                    System.out.println("not found");
                    //ignore this
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return storedRequestIds;
    }

    public String getSubject(MessagePart mp){
        Object obj = mp.get("headers");
        if (obj instanceof List){
            List<MessagePartHeader> headers = (List<MessagePartHeader>) obj;
            for(MessagePartHeader mph:headers) {
                if ("Subject".equalsIgnoreCase(mph.getName())){
                    return mph.getValue();
                }
            }

        }

        return null;
    }

    static String methodLocator1(String s){
        String text = "storedRequestId=";
        int index = s.indexOf(text);
        if (index==-1) return null;
        return s.substring(index+text.length(),index+text.length()+24);
    }
}
