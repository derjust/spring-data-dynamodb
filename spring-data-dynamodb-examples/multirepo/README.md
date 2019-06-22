# 📚 Spring Data DynamoDB Examples - 📗 Multi Repository

This example shows how to use multiple *Spring Data* repository types to access different storage backend. 

In this example `DynamoDB` and `MySQL` is used.

Further explanation can be found 
* as also the [code](src/main/java/com/github/derjust/spring_data_dynamodb_examples/multirepo)


### 📜 Explanation
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

### 📝 How to prepare:
* Update `src/main/resources/application.properties`

| Key                          | Sample value                           | Description                                       |
|------------------------------|----------------------------------------|---------------------------------------------------|
| `spring.datasource.url`      | `jdbc:mysql://localhost:3306/customer` | MySQL connection url including the database name  |
| `spring.datasource.username` | `root`                                 | MySQL user with `CREATE`/`INSERT`/`SELECT` grants |
| `spring.datasource.password` | `root`                                 | MySQL user's password                             |
| `amazon.aws.accesskey`       | N/A                                    | AWS accesskey for DynamoDB                        |
| `amazon.aws.secretkey`       | N/A                                    | AWS secretkey for DynamoDB                        |

### ▶️ How to run: 
```
 mvn -pl spring-data-dynamodb-examples/multirepo --also-make verify
```

### 📃 Output should look like:
```
2018-02-06 23:58:18.738  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : Customers found with findAll():
2018-02-06 23:58:18.738  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : -------------------------------
2018-02-06 23:58:18.910  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : Customer[id=1, firstName='Jack', lastName='Bauer']
2018-02-06 23:58:18.910  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : Customer[id=2, firstName='Chloe', lastName='O'Brian']
2018-02-06 23:58:18.910  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : Customer[id=3, firstName='Kim', lastName='Bauer']
2018-02-06 23:58:18.911  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : Customer[id=4, firstName='David', lastName='Palmer']
2018-02-06 23:58:18.911  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : Customer[id=5, firstName='Michelle', lastName='Dessler']
2018-02-06 23:58:18.911  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : 
2018-02-06 23:58:18.925  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : Customer found with findOne(1L):
2018-02-06 23:58:18.925  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : --------------------------------
2018-02-06 23:58:18.925  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : Customer[id=1, firstName='Jack', lastName='Bauer']
2018-02-06 23:58:18.925  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : 
2018-02-06 23:58:18.925  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : Customer found with findByLastName('Bauer'):
2018-02-06 23:58:18.925  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : --------------------------------------------
2018-02-06 23:58:18.954  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : Customer[id=1, firstName='Jack', lastName='Bauer']
2018-02-06 23:58:18.954  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : Customer[id=3, firstName='Kim', lastName='Bauer']
2018-02-06 23:58:18.954  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : 
2018-02-06 23:58:19.389  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : Table Device found
2018-02-06 23:58:19.537  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : Devices found with findAll():
2018-02-06 23:58:19.538  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : -------------------------------
2018-02-06 23:58:19.573  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : Device[id=DeviceKey[vendorId=2, product='Product CB'], name='C', lastChange='Tue Feb 06 23:58:19 EST 2018']
2018-02-06 23:58:19.573  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : Device[id=DeviceKey[vendorId=1, product='Product A'], name='A', lastChange='Tue Feb 06 23:58:19 EST 2018']
2018-02-06 23:58:19.574  INFO 14962 --- [lication.main()] c.g.s.examples.multirepo.Application     : Device[id=DeviceKey[vendorId=1, product='Product B'], name='B', lastChange='Tue Feb 06 23:58:19 EST 2018']
```
