package com.baeldung.readonlytransactions.mysql.spring;

import com.baeldung.readonlytransactions.mysql.dao.MyRepoSpring;
import com.baeldung.readonlytransactions.mysql.spring.entities.TransactionEntity;
import com.baeldung.readonlytransactions.mysql.spring.repositories.TransactionRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(basePackageClasses = Config.class, enableDefaultTransactions = false, repositoryBaseClass = CustomSimpleJpaRepository.class)
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class Config {

    @Bean
    public MyRepoSpring repoSpring(TransactionRepository repository) {
        return new MyRepoSpring(repository);
    }

    private DataSource dataSource(boolean readOnly, boolean isAutoCommit) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost/baeldung?useUnicode=true&characterEncoding=UTF-8");
        config.setUsername("baeldung");
        config.setPassword("baeldung");
        config.setReadOnly(readOnly);
        config.setAutoCommit(isAutoCommit);
        return new HikariDataSource(config);
    }

    @Bean
    public DataSource dataSource() {
        return new RoutingDS(dataSource(false, false), dataSource(true, true));
    }

    @Bean
    public EntityManagerFactory entityManagerFactory(DataSource dataSource) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);

        LocalContainerEntityManagerFactoryBean managerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        managerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        managerFactoryBean.setPackagesToScan(TransactionEntity.class.getPackage().getName());
        managerFactoryBean.setDataSource(dataSource);

        Properties properties = new Properties();

        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
        properties.setProperty("hibernate.hbm2ddl.auto", "validate");

        managerFactoryBean.setJpaProperties(properties);
        managerFactoryBean.afterPropertiesSet();

        return managerFactoryBean.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
