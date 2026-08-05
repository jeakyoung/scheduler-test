package com.scheduler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 환경별 설정 로더
 * 빌드 시 복사된 config.properties를 클래스패스에서 읽음
 */
public class Config {
    private static final Properties props = new Properties();

    static {
        try (InputStream is = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is == null) {
                throw new RuntimeException("[Config] config.properties를 찾을 수 없습니다. 빌드 스크립트를 확인하세요.");
            }
            props.load(is);
            System.out.println("[Config] 설정 로드 완료 (db.url: " + props.getProperty("db.url") + ")");
        } catch (IOException e) {
            throw new RuntimeException("[Config] config.properties 로드 실패: " + e.getMessage(), e);
        }
    }

    public static String getDbUrl()      { return props.getProperty("db.url"); }
    public static String getDbUsername() { return props.getProperty("db.username"); }
    public static String getDbPassword() { return props.getProperty("db.password"); }
    public static String getFcmUrl()     { return props.getProperty("fcm.url"); }
}
