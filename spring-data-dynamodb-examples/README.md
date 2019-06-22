# 📚 Spring Data DynamoDB Examples

Examples for Spring-Data-DynamoDB available at [<img width="16" src="https://derjust.github.io/spring-data-dynamodb/banner/spring-data-dynamodb.png" />github.com/derjust/spring-data-dynamodb](https://github.com/derjust/spring-data-dynamodb)

Please also check the [📖 Wiki](https://github.com/derjust/spring-data-dynamodb/wiki)

The following examples exist and how they can be executed

## 🚀 Simple Repository

This example show the most basic usage also referenced by the [README.md](https://github.com/derjust/spring-data-dynamodb/) of the main project.

Further explanation can be found 
* in the [simple/README.md](simple/README.md)
* as also the [code](simple/src/main/java/com/github/derjust/spring_data_dynamodb_examples/simple)

## 📗 Multi Repository

This example shows how to use multiple *Spring Data* repository types to access different storage backend. 

In this example `DynamoDB` and `JPA-H2` is used.

Further explanation can be found 
* in the [multirepo/README.md](multirepo/README.md)
* as also the [code](multirepo/src/main/java/com/github/derjust/spring_data_dynamodb_examples/multirepo)

### 📜 Code sample
The respective repository providers must be informed which interface to take care of. 
In a `@Configurable` bean the interfaces have to be assigned via `include`/`exclude` filters:

```java
@SpringBootApplication
@EnableJpaRepositories(
  includeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
      CustomerRepository.class}
  )}
)
@EnableDynamoDBRepositories(
  includeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
      DeviceRepository.class}
  )}
)
@Configuration
public class Application {
```

## 📕 Custom repository methods

This example shows how to use custom methods as part of *Spring Data* repository interfaces to implement custom logic.

Further explanation can be found 
* in the [custom/README.md](custom/README.md)
* as also the [code](custom/src/main/java/com/github/derjust/spring_data_dynamodb_examples/custom)


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


## 📘 REST integration

This example shows how to use `spring-data-dynamodb` with `spring-data-rest` to automatically expose CRUD operations on DynamoDB entities via REST endpoints.

Further explanation can be found 
* in the [rest/README.md](rest/README.md)
* as also the [code](rest/src/main/java/com/github/derjust/spring_data_dynamodb_examples/rest)

### 📜 Code sample
An additional DynamoDB bean must be registered and injected - everything else happens via auto-configuration:

```java
@EnableDynamoDBRepositories(
		mappingContextRef = "dynamoDBMappingContext",
		basePackageClasses = UserRepository.class)


@Bean
public DynamoDBMappingContext dynamoDBMappingContext() {
    return new DynamoDBMappingContext();
}

```

