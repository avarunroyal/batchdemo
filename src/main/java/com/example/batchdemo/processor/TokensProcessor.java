package com.example.batchdemo.processor;

import java.util.concurrent.ExecutionException;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import com.example.batchdemo.entity.Tokens;
import com.example.batchdemo.events.Producer;
import com.google.gson.Gson;

@Component
public class TokensProcessor implements ItemProcessor<Tokens,Tokens> {

	@Autowired
	private Producer producer;

	@Override
	public Tokens process(final Tokens token) throws Exception {

		Tokens tk = new Tokens();
		tk.setId(token.getId());
		tk.setTokenValue(token.getTokenValue());
		tk.setStatus(Boolean.toString(send(new Gson().toJson(token))));
		return tk;
	}

	public boolean send(String liveSeatsStr) {

		ListenableFuture<SendResult<String, String>> listenableFuture = producer.sendMessage("REFRESH_TOKENS_DATA",
				"IN_KEY", liveSeatsStr);
		try {
			listenableFuture.get();
			return true;
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return false;
		}
	}
}