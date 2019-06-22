/**
 * Copyright © 2013-2019 spring-data-dynamodb (https://github.com/derjust/spring-data-dynamodb/spring-data-dynamodb)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.socialsignin.spring.data.dynamodb.repository.support;

import com.amazonaws.util.VersionInfoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socialsignin.spring.data.dynamodb.core.DynamoDBOperations;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBCrudRepository;
import org.socialsignin.spring.data.dynamodb.repository.query.DynamoDBQueryLookupStrategy;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.util.Version;

import java.util.Optional;
import java.util.StringTokenizer;

import static org.springframework.data.querydsl.QuerydslUtils.QUERY_DSL_PRESENT;

/**
 * @author Michael Lavelle
 * @author Sebastian Just
 */
public class DynamoDBRepositoryFactory extends RepositoryFactorySupport {
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBRepositoryFactory.class);

	static {
		final String DEVELOPMENT = "DEVELOPMENT";

		String awsSdkVersion = VersionInfoUtils.getVersion();
		String springDataVersion = Version.class.getPackage().getImplementationVersion();

		String thisSpecVersion = DynamoDBRepositoryFactory.class.getPackage().getSpecificationVersion();
		String thisImplVersion = DynamoDBRepositoryFactory.class.getPackage().getImplementationVersion();
		if (thisImplVersion == null || thisSpecVersion == null) {
			thisSpecVersion = DEVELOPMENT;
			thisImplVersion = DEVELOPMENT;
		}

		LOGGER.info("Spring Data DynamoDB Version: {} ({})", thisImplVersion, thisSpecVersion);
		LOGGER.info("Spring Data Version:          {}", springDataVersion);
		LOGGER.info("AWS SDK Version:              {}", awsSdkVersion);
		LOGGER.info("Java Version:                 {} - {} {}", System.getProperty("java.version"),
				System.getProperty("java.vm.name"), System.getProperty("java.vm.version"));
		LOGGER.info("Platform Details:             {} {}", System.getProperty("os.name"),
				System.getProperty("os.version"));

		if (!DEVELOPMENT.equals(thisImplVersion) && !isCompatible(springDataVersion, thisSpecVersion)) {
			LOGGER.warn(
					"This Spring Data DynamoDB implementation might not be compatible with the available Spring Data classes on the classpath!"
							+ System.getProperty("line.separator")
							+ "NoDefClassFoundExceptions or similar might occur!");
		}
	}

	protected static boolean isCompatible(String spec, String impl) {
		if (spec == null && impl == null) {
			return false;
		} else if (spec == null) {
			spec = "";
		} else if (impl == null) {
			impl = "";
		}
		StringTokenizer specTokenizer = new StringTokenizer(spec, ".");
		StringTokenizer implTokenizer = new StringTokenizer(impl, ".");

		String specMajor = specTokenizer.hasMoreTokens() ? specTokenizer.nextToken() : "0";
		String specMinor = specTokenizer.hasMoreTokens() ? specTokenizer.nextToken() : "0";

		String implMajor = implTokenizer.hasMoreTokens() ? implTokenizer.nextToken() : "0";
		String implMinor = implTokenizer.hasMoreTokens() ? implTokenizer.nextToken() : "0";

		return specMajor.equals(implMajor) && specMinor.equals(implMinor);
	}

	private final DynamoDBOperations dynamoDBOperations;

	public DynamoDBRepositoryFactory(DynamoDBOperations dynamoDBOperations) {
		this.dynamoDBOperations = dynamoDBOperations;
	}

	@Override
	public <T, ID> DynamoDBEntityInformation<T, ID> getEntityInformation(final Class<T> domainClass) {

		final DynamoDBEntityMetadataSupport<T, ID> metadata = new DynamoDBEntityMetadataSupport<>(domainClass);
		return metadata.getEntityInformation();
	}

	@Override
	protected Optional<QueryLookupStrategy> getQueryLookupStrategy(Key key,
			QueryMethodEvaluationContextProvider evaluationContextProvider) {
		return Optional.of(DynamoDBQueryLookupStrategy.create(dynamoDBOperations, key));
	}

	/**
	 * Callback to create a {@link DynamoDBCrudRepository} instance with the given
	 * {@link RepositoryMetadata}
	 *
	 * @param <T>
	 *            Type of the Entity
	 * @param <ID>
	 *            Type of the Hash (Primary) Key
	 * @param metadata
	 *            Metadata of the entity
	 * @see #getTargetRepository(RepositoryInformation)
	 * @return the created {@link DynamoDBCrudRepository} instance
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected <T, ID> DynamoDBCrudRepository<?, ?> getDynamoDBRepository(RepositoryMetadata metadata) {
		return new SimpleDynamoDBPagingAndSortingRepository(getEntityInformation(metadata.getDomainType()),
				dynamoDBOperations, getEnableScanPermissions(metadata));
	}

	protected EnableScanPermissions getEnableScanPermissions(RepositoryMetadata metadata) {
		return new EnableScanAnnotationPermissions(metadata.getRepositoryInterface());
	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		if (isQueryDslRepository(metadata.getRepositoryInterface())) {
			throw new IllegalArgumentException("QueryDsl Support has not been implemented yet.");
		}
		return SimpleDynamoDBPagingAndSortingRepository.class;
	}

	private static boolean isQueryDslRepository(Class<?> repositoryInterface) {
		return QUERY_DSL_PRESENT && QuerydslPredicateExecutor.class.isAssignableFrom(repositoryInterface);
	}

	@Override
	protected Object getTargetRepository(RepositoryInformation metadata) {
		return getDynamoDBRepository(metadata);
	}

}
