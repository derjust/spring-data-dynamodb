package org.socialsignin.spring.data.dynamodb.issue77;

import org.springframework.data.repository.CrudRepository;

public interface FooRepository extends CrudRepository<Foo, Long>, FooRepositoryCustom {

	// Workaround as per DATACMNS-1008
	//<X extends Foo> X save(X foo);
}
