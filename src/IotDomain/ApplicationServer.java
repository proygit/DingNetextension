package IotDomain;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApplicationServer extends UserApplication implements Serializable {
    private static final long serialVersionUID = 1L;
    private static Logger LOGGER = Logger.getLogger(ApplicationServer.class.getName());
    private MQTTServer MQTTServer;

    public static LinkedList<Gateway> getSubscribedGateways() {
        return subscribedGateways;
    }

    public static void setSubscribedGateways(LinkedList<Gateway> subscribedGateways) {
        ApplicationServer.subscribedGateways = subscribedGateways;
    }

    private static LinkedList<Gateway> subscribedGateways = new LinkedList<>();


    /**
     * Constructs a user application with a given EUI.
     *
     * @param appEUI
     */
    public ApplicationServer(long appEUI) {
        super(Long.valueOf("12677987"));
        this.MQTTServer = new MQTTServer();
        MQTTServer.subscribe(appEUI,this.getAppEUI());


    }
    /**
     * Returns the MQTT server used in this environment.
     * @return the MQTT server used in this environment.
     */
    public MQTTServer getMQTTServer() {
        return MQTTServer;
    }

    public static void takeAction(Mote mote) {
        LOGGER.log(Level.INFO,"Application Server takes action on Faulty mote  : " + mote.getEUI());

    }
    public static void addSubscription(Gateway gateway) {
        if (!getSubscribedGateways().contains(gateway)) {
            subscribedGateways.add(gateway);
            setSubscribedGateways(subscribedGateways);

        }
    }


}
