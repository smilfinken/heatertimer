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

    public double temperature;
    public double humidity;

    public SensorReading() { }

    public SensorReading(String sensorId, double temperature, double humidity) {
        this.sensorId = sensorId;
        this.temperature = temperature;
        this.humidity = humidity;
        timestamp = new Date();
    }
}
