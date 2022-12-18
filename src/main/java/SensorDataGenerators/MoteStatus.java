package SensorDataGenerators;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalTime;
import java.util.Random;

public class MoteStatus implements SensorDataGenerator {
    @Override
    public Byte generateData(Integer x, Integer y, LocalTime time) throws NoSuchAlgorithmException {
        Random random = SecureRandom.getInstanceStrong();
        if(x<200&&y< 230)
            return (byte)Math.floorMod((int) Math.round(97-20+(x+y)/250 +0.3*random.nextGaussian()),255);
        else if(x<1000&&y< 1000)
            return (byte)Math.floorMod((int) Math.round(90-20+Math.log10((x+y)/50)+0.3*random.nextGaussian()),255);
        else if(x<1400&&y< 1400)
            return (byte)Math.floorMod((int) Math.round(95 -20 +3*Math.cos(Math.PI*(x+y)/(150*8))+1.5*Math.sin(Math.PI*(x+y)/(150*6))+0.3*random.nextGaussian()),255);
        else
            return (byte)Math.floorMod((int) Math.round(85 -17.5 +(x+y)/200+0.1*random.nextGaussian()),255);

    }
}
