package com.users.be.controller;


import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.users.be.model.UsersModel;



@RestController
@RequestMapping("/users")
public class UsersController {
	private final String API_BASE_URL = "https://randomuser.me/api/";
	private final int DEFAULT_USERS_QUANTITY = 10;
	
	@GetMapping("")
	public Map<String, Object> getUsers(@RequestParam("limit") Optional<String> limit,
			@RequestParam("categorize") Optional<String> categorize){
		
		String URL = API_BASE_URL+"?results=";
		
		if(limit.isEmpty())
			URL = URL+DEFAULT_USERS_QUANTITY;
		else
			URL = URL+limit.get();
		
		RestTemplate restTemplate = new RestTemplate();
		String apiResult = restTemplate.getForObject(URL, String.class);
		JSONObject jsonObjectResult = new JSONObject(apiResult);
		JSONArray jsonArrayResults = (JSONArray)jsonObjectResult.get("results");
		
		JSONObject response;
		
		if(!categorize.isEmpty() && categorize.get().equalsIgnoreCase("gender"))
			response = getJSONUsersCategorized(jsonArrayResults);
		else
			response = getJSONUsers(jsonArrayResults);
		
		return response.toMap();
	}
	
	private JSONObject getJSONUsers(JSONArray jsonArrayResults) {
		ArrayList<UsersModel> resultList = new ArrayList<>();
		JSONObject response = new JSONObject();
		
		for (Object json : jsonArrayResults) {
			UsersModel model = buildUserModel(json);
			
			resultList.add(model);
		}
		
		response.append("users", resultList);
		
		return response;
	}
	
	private JSONObject getJSONUsersCategorized(JSONArray jsonArrayResults) {
		ArrayList<UsersModel> listMale = new ArrayList<>();
		ArrayList<UsersModel> listFemale = new ArrayList<>();
		JSONObject response = new JSONObject();
		
		for (Object json : jsonArrayResults) {
			UsersModel model = buildUserModel(json);
			
			if(model.getGender().equalsIgnoreCase("female"))
				listFemale.add(model);
			else
				listMale.add(model);
		}
		
		JSONObject jsonMale = new JSONObject();
		jsonMale.append("male", listMale);
		
		JSONObject jsonFemale = new JSONObject();
		jsonFemale.append("female", listFemale);
		
		response.append("users", jsonFemale);
		response.append("users", jsonMale);
		
		return response;
	}
	
	private UsersModel buildUserModel(Object json) {
		JSONObject jsonObj = (JSONObject)json;
		UsersModel model = new UsersModel();
		
		JSONObject jsonUserName = (JSONObject)jsonObj.get("name");
		model.setName( jsonUserName.getString("first") + " "+jsonUserName.getString("last") );
		
		JSONObject jsonLocation = (JSONObject)jsonObj.get("location");
		JSONObject jsonStreet = (JSONObject)jsonLocation.get("street");
		model.setAddress( jsonStreet.getString("name") + " "+jsonStreet.getInt("number")+", "
				+jsonLocation.getString("state")+" "+jsonLocation.getString("country") );
		
		model.setGender(jsonObj.getString("gender"));
		model.setEmail(jsonObj.getString("email"));
		model.setPhone(jsonObj.getString("phone"));
		model.setCell(jsonObj.getString("cell"));
		
		return model;
	}
}
