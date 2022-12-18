package IotDomain;

import SensorDataGenerators.*;

import java.security.NoSuchAlgorithmException;
import java.time.LocalTime;

/**
 * An enum representing sensors for the motes.
 */
public enum MoteSensor {

    SOOT(new SootDataGenerator()),FAULTY(new MoteStatus()),NORMAL(new MoteStatus()),OZONE(new OzoneDataGenerator()),CARBON_DIOXIDE(new CarbonDioxideDataGenerator()),PARTICULATE_MATTER(new ParticulateMatterDataGenerator());

    MoteSensor(SensorDataGenerator sensorDataGenerator){
        this.sensorDataGenerator = sensorDataGenerator;
    }

    private SensorDataGenerator sensorDataGenerator;

    public Byte getValue(Integer xpos, Integer ypos, LocalTime time) throws NoSuchAlgorithmException {
        return sensorDataGenerator.generateData(xpos,ypos,time);
    }
}
