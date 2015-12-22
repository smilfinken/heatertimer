package models;

import java.util.logging.Logger;

import javax.persistence.*;

@Entity
@Table(name = "configuration")
public class Configuration {
    private static final Logger LOGGER = Logger.getLogger("GLOBAL");

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public long id;

    @Column(nullable = false)
    public String key;

    @Column(nullable = true)
    public Integer integerValue;

    @Column(nullable = true)
    public String stringValue;

    @Column(nullable = true)
    public Double doubleValue;

    public Configuration(String key, Integer value) {
        this.key = key;
        this.integerValue = value;
    }

    public Configuration(String key, String value) {
        this.key = key;
        this.stringValue = value;
    }

    public Configuration(String key, Double value) {
        this.key = key;
        this.doubleValue = value;
    }

    public Integer getValue(Integer defaultValue) {
        return integerValue != null ? integerValue : defaultValue;
    }

    public String getValue(String defaultValue) {
        return stringValue != null ? stringValue : defaultValue;
    }

    public Double getValue(Double defaultValue) {
        return doubleValue != null ? doubleValue : defaultValue;
    }
}
