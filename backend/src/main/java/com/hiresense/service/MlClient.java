package com.hiresense.service;
import com.fasterxml.jackson.databind.ObjectMapper; import org.springframework.beans.factory.annotation.Value; import org.springframework.http.*; import org.springframework.stereotype.Service; import org.springframework.web.client.RestTemplate; import java.util.*;
@Service public class MlClient {private final RestTemplate http;private final ObjectMapper json;private final String base;public MlClient(RestTemplate h,ObjectMapper j,@Value("${ml.service.url:http://localhost:8000}")String b){http=h;json=j;base=b.replaceAll("/$","");}
 public Map<String,Object> parse(String text){Map<String,Object> body=Map.of("text",text);return post("/nlp/parse",body);}
 public Map<String,Object> match(Map<String,Object> body){return post("/match",body);}
 public Map<String,Object> attrition(Map<String,Object> body){return post("/attrition/predict",body);}
 public Map<String,Object> train(Object body){return post("/attrition/train",body);}
 @SuppressWarnings("unchecked") private Map<String,Object> post(String path,Object body){try{ResponseEntity<String> r=http.postForEntity(base+path,new HttpEntity<>(body,headers()),String.class);if(r.getStatusCode().is2xxSuccessful()&&r.getBody()!=null)return json.readValue(r.getBody(),Map.class);}catch(Exception ignored){}return Map.of();}
 private HttpHeaders headers(){HttpHeaders h=new HttpHeaders();h.setContentType(MediaType.APPLICATION_JSON);return h;}
}
