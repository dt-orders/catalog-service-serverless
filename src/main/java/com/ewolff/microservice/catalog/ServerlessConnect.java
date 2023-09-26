package com.ewolff.microservice.catalog;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ServerlessConnect {

    private final Logger log = LoggerFactory.getLogger(ServerlessConnect.class);
    
    @Value("${find.by.name.url}")
    private String findByNameContainsUrl;

    @Value("${serverless.db.actions.url}")
    private String serverlessDBActionsUrl;


    public List<Item> findByNameContains(String query){
        String jsonPayload = "{\"name\":\"" + query + "\"}";

        try {
            URL url = new URL(findByNameContainsUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(jsonPayload);
            out.flush();
            out.close();

            BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            List<Item> itemList = convertJsonToItemList(content.toString());

            return itemList;
        } catch (MalformedURLException e){
            log.error("MALFORMED ERROR");
            log.error(e.toString());
            return null;
        } catch (IOException e) {
            log.error("IOEXC ERROR");
            log.error(e.toString());
            return null;
        } catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    public List<Item> findAll(){
        String jsonPayload = "{\"function\":\"findAll\"}";

        String response = makeURLRequest(serverlessDBActionsUrl, jsonPayload);
        List<Item> itemList = convertJsonToItemList(response.toString());

        return itemList;

    }

    public Item findById(Long id) {
        String jsonPayload = "{\"function\":\"findById\", \"id\":" + Long.toString(id) + "}";
        String response = makeURLRequest(serverlessDBActionsUrl, jsonPayload);
        Item item = convertJsonToItemList(response.toString()).get(0);
        
        return item;
    }

    public void insertItem(Item item){
        String jsonPayload = "{\"function\":\"insertItem\", \"name\":\"" + item.getName() + "\",\"price\":\"" + item.getPrice() +"\"}";
        makeURLRequest(serverlessDBActionsUrl, jsonPayload);

    }

    public void updateItemById(Item item){
        String jsonPayload = "{\"function\":\"updateItemById\", \"name\":\"" + item.getName() + "\",\"price\":\"" + item.getPrice() +"\", \"id\":" + Long.toString(item.getId()) + "}";
        makeURLRequest(serverlessDBActionsUrl, jsonPayload);

    }

    public void deleteItemById(Long id){
        log.info("DELETED FUNCTION CALL");

        String jsonPayload = "{\"function\":\"deleteItemById\", \"id\":" + Long.toString(id) + "}";
        makeURLRequest(serverlessDBActionsUrl, jsonPayload);

    }

    private String makeURLRequest(String inputUrl, String payload){
        try{
            URL url = new URL(inputUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(payload);
            out.flush();
            out.close();

            BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            log.info("BONNIE OUTPUT");
            log.info(content.toString());
            return content.toString();
        }  catch (MalformedURLException e){
            log.error("MALFORMED ERROR");
            log.error(e.toString());
            return null;
        } catch (IOException e) {
            log.error("IOEXC ERROR");
            log.error(e.toString());
            return null;
        }
    }

    private List<Item> convertJsonToItemList(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Item> items = mapper.readValue(json, new TypeReference<List<Item>>() {
            });
            log.info(items.toString());
            return items;
        } catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }
}