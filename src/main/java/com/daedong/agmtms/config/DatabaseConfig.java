package com.daedong.agmtms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
            
            log.info("Database initialization completed successfully");
        } catch (Exception e) {
            log.error("Error during database initialization: {}", e.getMessage(), e);
        }
    }
} 