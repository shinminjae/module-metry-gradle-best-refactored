package com.daedong.agmtms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Configuration
@Slf4j
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @PostConstruct
    public void initializeDatabase() {
        try {
            // 데이터베이스 파일 경로 추출
            String dbPath = databaseUrl.replace("jdbc:sqlite:", "");
            Path dbFilePath = Paths.get(dbPath);
            
            // data 디렉토리 생성
            File dataDir = dbFilePath.getParent().toFile();
            if (!dataDir.exists()) {
                dataDir.mkdirs();
                log.info("Created data directory: {}", dataDir.getAbsolutePath());
            }
            
            // 데이터베이스 파일이 존재하지 않으면 생성
            if (!Files.exists(dbFilePath)) {
                Files.createFile(dbFilePath);
                log.info("Created database file: {}", dbFilePath.toAbsolutePath());
            }
            
            // 스키마 생성
            createSchema();
            
            log.info("Database initialization completed successfully");
        } catch (Exception e) {
            log.error("Error during database initialization: {}", e.getMessage(), e);
        }
    }
    
    private void createSchema() {
        try {
            // schema.sql 파일 읽기
            ClassPathResource resource = new ClassPathResource("sql/mapper/schema.sql");
            String schemaSql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            
            // SQLite 연결
            try (Connection connection = DriverManager.getConnection(databaseUrl);
                 Statement statement = connection.createStatement()) {
                
                // 스키마 실행
                statement.execute(schemaSql);
                log.info("Database schema created successfully");
                
            } catch (Exception e) {
                log.error("Error creating database schema: {}", e.getMessage(), e);
            }
            
        } catch (IOException e) {
            log.error("Error reading schema file: {}", e.getMessage(), e);
        }
    }
} 