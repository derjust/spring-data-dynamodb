package org.socialsignin.spring.data.dynamodb.issue77;

public class FooRepositoryImpl implements FooRepositoryCustom {
	
	private ThreadLocal<Boolean> saveCalled = new ThreadLocal<>();
	private ThreadLocal<Boolean> doNonstandardThingCalled = new ThreadLocal<>();
	
	public FooRepositoryImpl() {
		saveCalled.set(false);
		doNonstandardThingCalled.set(false);
	}
	
    public Foo save(Foo foo) {
        // Do things
    	saveCalled.set(true);
    	return foo;
    }
    
    public Foo doNonstandardThing(Foo foo) {
    	doNonstandardThingCalled.set(true);
    	return foo;
    }
    
    public boolean isDoNonstandardThingCalled() {
    	return doNonstandardThingCalled.get();
    }
    
    public boolean isSaveCalled() {
    	return saveCalled.get();
    }
}