# 📚 Spring Data DynamoDB Examples - 📕 Custom repository methods

This example shows how to use custom methods as part of *Spring Data* repository interfaces to implement custom logic.

Further explanation can be found 
* as also the [code](src/main/java/com/github/derjust/spring_data_dynamodb_examples/custom)

### 📜 Code sample
The repository interface is extend by an additional interface. It's implementation is found by the `Impl` suffix in the same package.
*Spring Data* itself takes care of weaving in the implementation at runtime:

```java
public interface UserRepository extends CrudRepository<User, Long>, CustomUserRepositoryMethods { }


public interface CustomUserRepositoryMethods {
    User calculateAge(User user);
}


public class CustomUserRepositoryMethodsImpl implements CustomUserRepositoryMethods {

    @Override
    public User calculateAge(User user) {
        /* custom code */
    }
}



// Regular method of the repository interface
User user = userRepository.findOne(id);
// Custom method available via the interface, too
userRepository.calculateAge(user);

```

### 📝 How to prepare:
* Update `src/main/resources/application.properties`

| Key                          | Sample value                           | Description                                       |
|------------------------------|----------------------------------------|---------------------------------------------------|
| `amazon.aws.accesskey`       | N/A                                    | AWS accesskey for DynamoDB                        |
| `amazon.aws.secretkey`       | N/A                                    | AWS secretkey for DynamoDB                        |

### ▶️ How to run: 
```
 mvn -pl spring-data-dynamodb-examples/custom --also-make verify
```

### 📃 Output should look like:
```
2018-07-02 00:29:46.360  INFO 13591 --- [lication.main()] c.g.d.s.custom.Application               : Table USER found
2018-07-02 00:29:46.438  INFO 13591 --- [lication.main()] c.g.d.s.custom.Application               : Created user: User{id=084941a8-6d1a-41d6-94ee-4412b8ca6d72, firstname='Sebastian', lastname='Mueller', birthday=1970-01-17T22:29:31.154Z, age=0}
2018-07-02 00:29:46.479  INFO 13591 --- [lication.main()] c.g.d.s.custom.Application               : Called custom method: User{id=084941a8-6d1a-41d6-94ee-4412b8ca6d72, firstname='Sebastian', lastname='Mueller', birthday=1970-01-17T22:29:31.154Z, age=48}
2018-07-02 00:29:46.506  INFO 13591 --- [lication.main()] c.g.d.s.custom.Application               : Comparison - Old entity: User{id=084941a8-6d1a-41d6-94ee-4412b8ca6d72, firstname='Sebastian', lastname='Mueller', birthday=1970-01-17T22:29:31.154Z, age=48}
2018-07-02 00:29:46.506  INFO 13591 --- [lication.main()] c.g.d.s.custom.Application               : Comparison - New entity: User{id=084941a8-6d1a-41d6-94ee-4412b8ca6d72, firstname='Sebastian', lastname='Mueller', birthday=1970-01-17T22:29:31.154Z, age=48}
```
