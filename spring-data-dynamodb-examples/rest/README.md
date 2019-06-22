# 📚 Spring Data DynamoDB Examples - 📘 REST integration

This example shows how to use `spring-data-dynamodb` with `spring-data-rest` to automatically expose CRUD operations on DynamoDB entities via REST endpoints.

Further explanation can be found 
* as also the [code](src/main/java/com/github/derjust/spring_data_dynamodb_examples/rest)

### 📜 Code sample


```java

@EnableDynamoDBRepositories(
		mappingContextRef = "dynamoDBMappingContext",
		includeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
				UserRepository.class}
		)}
)


@Bean
public DynamoDBMappingContext dynamoDBMappingContext() {
    return new DynamoDBMappingContext();
}

```


### 📝 How to prepare:
* Update `src/main/resources/application.properties`

| Key                          | Sample value                           | Description                                       |
|------------------------------|----------------------------------------|---------------------------------------------------|
| `amazon.aws.accesskey`       | N/A                                    | AWS accesskey for DynamoDB                        |
| `amazon.aws.secretkey`       | N/A                                    | AWS secretkey for DynamoDB                        |

### ▶️ How to run: 
```
 mvn -pl spring-data-dynamodb-examples/rest --also-make verify
```

### 📃 Output should look like:
```
2018-07-02 19:20:42.300  INFO 22828 --- [lication.main()] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2018-07-02 19:20:42.304  INFO 22828 --- [lication.main()] c.g.d.s.rest.Application                 : Started Application in 4.645 seconds (JVM running for 8.315)
2018-07-02 19:20:42.789  INFO 22828 --- [lication.main()] c.g.d.s.common.DynamoDBConfig            : Table Device found
2018-07-02 19:20:42.900  INFO 22828 --- [lication.main()] c.g.d.s.rest.Application                 : Users found with findAll():
2018-07-02 19:20:42.900  INFO 22828 --- [lication.main()] c.g.d.s.rest.Application                 : -------------------------------
2018-07-02 19:20:42.941  INFO 22828 --- [lication.main()] c.g.d.s.rest.Application                 : User{id=034446ed-e6f1-45bd-9de2-a4d7867e01c9, firstname='you', lastname='you'}
2018-07-02 19:20:42.941  INFO 22828 --- [lication.main()] c.g.d.s.rest.Application                 : User{id=da5966e8-ded5-425d-8ddf-df8ac49093e1, firstname='me', lastname='me'}
2018-07-02 19:20:42.944  INFO 22828 --- [lication.main()] c.g.d.s.rest.Application                 : 
2018-07-02 19:20:42.944  INFO 22828 --- [lication.main()] c.g.d.s.rest.Application                 : 
2018-07-02 19:20:42.944  INFO 22828 --- [lication.main()] c.g.d.s.rest.Application                 : Run curl -v http://localhost:8080/users and follow the HATEOS links
2018-07-02 19:20:42.944  INFO 22828 --- [lication.main()] c.g.d.s.rest.Application                 : 
2018-07-02 19:20:42.944  INFO 22828 --- [lication.main()] c.g.d.s.rest.Application                 : Press <enter> to shutdown
```
