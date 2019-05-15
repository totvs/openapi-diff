package com.totvs.ttalk.apicompare.resources;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/teste")
public class teste {

    @RequestMapping(path = "/console", method = RequestMethod.GET)
    public String console(@RequestBody String body){
    	
        String meuTexto = "teste";
        return meuTexto;
    }
    
    @RequestMapping(path = "/json", method = RequestMethod.GET)
    public String json(@RequestParam(value="hasConsole", defaultValue="true") Boolean hasConsole,
    				@RequestBody String body){
    	
    	
        String response = "TESTES";
        return response;
    }
    
}
