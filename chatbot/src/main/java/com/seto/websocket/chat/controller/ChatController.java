package com.seto.websocket.chat.controller;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse;
import com.google.cloud.dialogflow.v2beta1.QueryInput;
import com.google.cloud.dialogflow.v2beta1.QueryResult;
import com.google.cloud.dialogflow.v2beta1.SessionName;
import com.google.cloud.dialogflow.v2beta1.SessionsClient;
import com.google.cloud.dialogflow.v2beta1.TextInput;
import com.google.cloud.dialogflow.v2beta1.TextInput.Builder;
import com.seto.websocket.chat.model.ChatMessage;

@Controller
public class ChatController {

	/*
	 * @MessageMapping("/chat.sendMessage")
	 * 
	 * @SendTo("/channel/public") public ChatMessage sendMessage(@Payload
	 * ChatMessage chatMessage) { return chatMessage; }
	 */
	@MessageMapping("/chat.sendMessage")
	@SendToUser
	public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
		String projectId = "setovoicebot-9f3df";
		String sessionId = UUID.randomUUID().toString();
		String languageCode = "en-US";
		try {
			chatMessage.setSender("SetoBot");
			chatMessage.setContent(detectIntentTexts(projectId, chatMessage.getContent(), sessionId, languageCode));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return chatMessage;
	}

	public String detectIntentTexts(String projectId, String text, String sessionId, String languageCode)
			throws Exception {
		// Instantiates a client
		QueryResult queryResult = null;
		try (SessionsClient sessionsClient = SessionsClient.create()) {
			// Set the session name using the sessionId (UUID) and projectID (my-project-id)
			SessionName session = SessionName.of(projectId, sessionId);
			System.out.println("Session Path: " + session.toString());

			// Detect intents for each text input
			// Set the text (hello) and language code (en-US) for the query
			Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode(languageCode);

			// Build the query with the TextInput
			QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();

			// Performs the detect intent request
			DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);

			// Display the query result
			queryResult = response.getQueryResult();

			System.out.println("====================");
			System.out.format("Query Text: '%s'\n", queryResult.getQueryText());
			System.out.format("Detected Intent: %s (confidence: %f)\n", queryResult.getIntent().getDisplayName(),
					queryResult.getIntentDetectionConfidence());
			System.out.format("Fulfillment Text: '%s'\n", queryResult.getFulfillmentText());
		}
		return String.format("'%s'", queryResult.getFulfillmentText());
	}

	@MessageMapping("/chat.addUser")
	@SendTo("/channel/public")
	public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
		headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
		return chatMessage;
	}

}
