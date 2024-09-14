package com.example.mongo;

import com.mongodb.client.result.DeleteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;


@Component
public class MMapper{
    @Autowired
    private MongoTemplate mongoTemplate;
    public <T> DeleteResult deleteById(Object id,T t){
        T byId = (T) mongoTemplate.findById(id, t.getClass());
        return mongoTemplate.remove(byId);
    }
}
