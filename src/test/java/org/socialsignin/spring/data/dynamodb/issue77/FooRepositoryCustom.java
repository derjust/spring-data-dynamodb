package org.socialsignin.spring.data.dynamodb.issue77;

public interface FooRepositoryCustom {
	Foo doNonstandardThing(Foo foo);
    
	// just here for testing...
	boolean isDoNonstandardThingCalled();
    boolean isSaveCalled();
}
