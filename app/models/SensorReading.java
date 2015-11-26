package models;

import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name = "sensorreadings")
public class SensorReading {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public String id;

    @Column(nullable = false)
    public String sensorId;

    @Column(nullable = false)
    public Date timestamp;

    public float temperature;
    public float humidity;

    public SensorReading() { }

    public SensorReading(String sensorId, float temperature, float humidity) {
        this.sensorId = sensorId;
        this.temperature = temperature;
        this.humidity = humidity;
        timestamp = new Date();
    }
}
