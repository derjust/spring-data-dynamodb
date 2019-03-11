package org.socialsignin.spring.data.dynamodb.issue77;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FooService {
    private final FooRepository fooRepo;
    
    @Autowired
    public FooService(FooRepository fooRepo) {
        this.fooRepo = fooRepo;
    }

    public void processFoo(Foo foo) {
    	fooRepo.doNonstandardThing(foo);
        fooRepo.save(foo);
    }
}