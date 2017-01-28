package com.apoapsiselectronics.GanjaWoodcutter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.swing.JOptionPane;

import org.dreambot.api.script.AbstractScript;

public class Feedback {
    private URL serverURL;
    private static long lastTime = 0;

    public Feedback() {
        try {
            serverURL = new URL("http://ganja.toliga.com/rest/message/new");
        } catch (IOException e) {
            AbstractScript.log(e.getMessage());
        }
    }

    public void SendString(String message, String name) {
        boolean debug = false;
        String trueMessage;

        if (message.startsWith("/debug")) {
            debug = true;
            message = message.replace("/debug", "");
        }

        if (!message.equals("") && !(trueMessage = message.trim()).equals("")) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastTime > 300000 || debug) {
                lastTime = currentTime;

                String urlParameters = "unique_identifier=R2FuamEgV29vZGN1dHRlcg==" + "&message=" + trueMessage + "&username=" + name;
                byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;

                Thread feedbackThread = new Thread(() ->
                {
                    HandleConnection(postDataLength, postData);
                });

                feedbackThread.setName("Feedback Thread");
                feedbackThread.start();
            } else {
                JOptionPane.showMessageDialog(null, "You need to wait at least 5 minutes to send another feedback.", "Ganja Woodcutter - Feedback", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please write something in the message box.", "Ganja Woodcutter - Feedback", JOptionPane.WARNING_MESSAGE);
        }
    }

    private synchronized void HandleConnection(int postDataLength, byte[] postData) {
        try {
            HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            connection.setUseCaches(false);

            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {

                String incomingData = reader.readLine();

                switch (incomingData) {
                    case "ok":
                        JOptionPane.showMessageDialog(null, "Your message has been sent. Thanks for your corcerns. Please rate us on 'https://dreambot.org/sdn'.",
                                "Ganja Woodcutter - Feedback", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    case "bad":
                        JOptionPane.showMessageDialog(null, "Your message could not be sent because of a server error. Please report this error in the relevant forum topic.",
                                "Ganja Woodcutter - Feedback", JOptionPane.WARNING_MESSAGE);
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "Your message could not be sent. Please check your firewall and internet settings.", "Ganja Woodcutter - Feedback",
                                JOptionPane.ERROR_MESSAGE);
                        break;
                }

                AbstractScript.log(reader.readLine());

            }
        } catch (IOException e) {
            AbstractScript.log(e.getMessage());
        }
    }
}
