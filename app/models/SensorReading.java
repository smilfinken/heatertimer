package models;

import java.util.logging.Logger;
import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "sensorreadings")
public class SensorReading {
    private static final Logger LOGGER = Logger.getLogger("GLOBAL");

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public long id;

    @Column(nullable = false)
    public String sensorId;

    @Column(nullable = false)
    public Date timestamp;

    public double temperature;
    public double humidity;
    public double pressure;

    public SensorReading() { }

    public SensorReading(String sensorId, double temperature, double humidity, double pressure) {
        this.sensorId = sensorId;
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
        timestamp = new Date();
    }
}
