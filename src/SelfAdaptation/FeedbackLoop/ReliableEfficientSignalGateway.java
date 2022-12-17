package SelfAdaptation.FeedbackLoop;

import IotDomain.*;
import SelfAdaptation.AdaptationGoals.IntervalAdaptationGoal;
import SelfAdaptation.Instrumentation.FeedbackLoopGatewayBuffer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.OptionalDouble;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;

/**
 * A class representing the signal based adaptation approach.
 */
public class ReliableEfficientSignalGateway extends GenericFeedbackLoop {
    private static Logger LOGGER = Logger.getLogger(ReliableEfficientSignalGateway.class.getName());

    /**
     * Constructs a new instance of the signal based adaptation approach with a given quality of service.
     *
     * @param qualityOfService The quality of service for the received signal strength.
     */
    public ReliableEfficientSignalGateway(QualityOfService qualityOfService) {
        super("Signal-based");
        this.qualityOfService = qualityOfService;
        gatewayBuffer = new FeedbackLoopGatewayBuffer();
        reliableMinPowerBuffers = new HashMap<>();


    }

    /**
     * A HashMap representing the buffers for the approach.
     */

    private HashMap<Mote, LinkedList<Double>> reliableMinPowerBuffers;

    /**
     * Returns the algorithm buffers.
     *
     * @return The algorithm buffers.
     */

    private HashMap<Mote, LinkedList<Double>> getReliableMinPowerBuffers() {
        return this.reliableMinPowerBuffers;
    }

    /**
     * Puts an reliableMinPowerBuffer in the reliableMinPowerBuffers under mote.
     *
     * @param mote                   The mote where to put the entry.
     * @param reliableMinPowerBuffer The buffer to put in the buffers.
     */

    private void putReliableMinPowerBuffer(Mote mote, LinkedList<Double> reliableMinPowerBuffer) {
        this.reliableMinPowerBuffers.put(mote, reliableMinPowerBuffer);
    }

    /**
     * returns a map with gateway buffers.
     *
     * @return A map with gateway buffers.
     */
    private FeedbackLoopGatewayBuffer getGatewayBuffer() {
        return gatewayBuffer;
    }

    /**
     * A map to keep track of which gateway has already sent the packet.
     */

    private FeedbackLoopGatewayBuffer gatewayBuffer;
    /**
     * A QualityOfService representing the required quality of service.
     */

    private QualityOfService qualityOfService;

    /**
     * Returns the lower bound of the approach.
     *
     * @return The lower bound of the approach.
     */

    public Double getLowerBound() {
        return ((IntervalAdaptationGoal) qualityOfService.getAdaptationGoal("reliableCommunication")).getLowerBoundary();
    }

    /**
     * Returns the upper bound of the approach.
     *
     * @return The upper bound of the approach.
     */

    public Double getUpperBound() {

        return ((IntervalAdaptationGoal) qualityOfService.getAdaptationGoal("reliableCommunication")).getUpperBoundary();
    }

    public LoraWanPacket getDataBackUP() {
        return dataBackUP;
    }


    public void setDataBackUP(LoraWanPacket dataBackUP) {
        this.dataBackUP = dataBackUP;
    }

    private LoraWanPacket dataBackUP;
    private Mote moteToAdapt ;

    public void setMoteToAdapt(Mote moteToAdapt) {
        this.moteToAdapt = moteToAdapt;
    }


    OptionalDouble chosenDistance=null;



    private Mote faultyMote = null;



    @Override
    public void adapt(Mote mote, Gateway dataGateway) {
        LinkedList<Double> reliableMinPowerBuffer = new LinkedList<>();
        //Gateway pings mote to check it's status but cannot be actually checked since the mote
        //doesnot have actual ip adress,they have generated UID.
        //pingMotes(mote, dataGateway);
        /**
         First we check if we have received the message already from all gateways.
         */
        getGatewayBuffer().add(mote, dataGateway);
        if (getGatewayBuffer().hasReceivedAllSignals(mote)) {
            /**
             * check what is the highest received signal strength.
             */
            Double receivedPower = getPowerSettingConfigurationOfMote(mote, dataGateway);
            /**
             * If the buffer has an entry for the current mote, the new highest received signal strength is added to it,
             * else a new buffer is created and added to which we can add the signal strength.
             */
            if (getReliableMinPowerBuffers().keySet().contains(mote)) {
                reliableMinPowerBuffer = getReliableMinPowerBuffers().get(mote);
            }
            reliableMinPowerBuffer.add(receivedPower);
            putReliableMinPowerBuffer(mote, reliableMinPowerBuffer);
            LinkedList<Mote> allMotesToadapt = new LinkedList<>();
            allMotesToadapt.add(mote);
            chooseWhichMoteToAdapt(mote, allMotesToadapt);


        }



    }

    /**
     * Checks if the mote is faulty if yes,it tries to reset it and informs the Application Server about it's status
     * then it is disabled after getting it's configuration and data to be backed-up by a nearby mote which could adapt
     * for this faulty mote
     * @param mote
     * @param dataGateway
     * @return
     */
    private Double getPowerSettingConfigurationOfMote(Mote mote, Gateway dataGateway) {
        Long checkMoteUID;
        LinkedList<LoraTransmission> receivedSignals = getGatewayBuffer().getReceivedSignals(mote);
        Double receivedPower = receivedSignals.getFirst().getTransmissionPower();
        for (LoraTransmission transmission : receivedSignals) {
            if (receivedPower < transmission.getTransmissionPower()) {
                receivedPower = transmission.getTransmissionPower();
                if (mote.getSensors().contains(MoteSensor.FAULTY)) {
                    if (transmission.getSender() instanceof NetworkEntity) {
                        checkMoteUID = transmission.getSender().getEUI();
                        if (checkMoteUID == mote.getEUI()) ;
                        faultyMote = mote;
                        LOGGER.log(Level.INFO, "Faulty mote uid" + faultyMote.getEUI());
                        LOGGER.log(Level.INFO, "Faulty mote contents backupdata" + transmission.getContent());
                        setDataBackUP(transmission.getContent());
                        //After data transfer the faulty mote is reset (Here an extensive research could be done
                        // on Self-Healing to fix the faulty mote)
                        // and then the information is sent to the Application server
                        // and after that it is disbaled.
                        //and then disbaled
                        faultyMote.reset();
                        dataGateway.informApplicationServer(faultyMote);
                       faultyMote.enable(false);
                    }
                }
            }
        }
        return receivedPower;
    }

    private void chooseWhichMoteToAdapt(Mote mote, LinkedList<Mote> allMotesToadapt) {
        /*
         * There could many motes which are eligible to adapt nearby, a selection is made based
         * on which one is nearest to the Gateway but also which one has lessnumber to request
         * to handle from the nearest gateway.
         * A mote which is in high demand might not be the good candidate to adapt as it is already overloaded with requeast
         * and data to send.
         *
         */
        if (mote.getSensors().contains(MoteSensor.NORMAL)) {
        for(Mote selectWhichMoteToadapt : allMotesToadapt){
            Double distance = getMoteProbe().getShortestDistanceToGateway(selectWhichMoteToadapt);
            DoubleStream stream = DoubleStream.of(distance);
             chosenDistance= stream.min();
             //We choose the nearest mote and the one which has less request to handle.
             if(chosenDistance.orElse(-1) <100 && selectWhichMoteToadapt.getNumberOfRequests()<10){
                 setMoteToAdapt(selectWhichMoteToadapt);
                 adjustPowerSetting(selectWhichMoteToadapt, getDataBackUP());
                 informAdatedMoteStatus(selectWhichMoteToadapt,getDataBackUP());
             }


            }
        }
    }

    public void informAdatedMoteStatus(Mote selectWhichMoteToadapt, LoraWanPacket dataBackUP){
        LOGGER.log(Level.INFO,"Chosen mote to adapt" + selectWhichMoteToadapt.getEUI());
        LOGGER.log(Level.INFO,"Faulty mote contents backed up" + dataBackUP);
        LOGGER.log(Level.INFO,"Distance to Gateway" + getMoteProbe().getShortestDistanceToGateway(selectWhichMoteToadapt));
    }


    private void adjustPowerSetting(Mote moteToAdapt, LoraWanPacket dataBackUP) {
        /**
         * If the buffer for the mote has 5 entries, the algorithm can start making adjustments.
         */
        if (getReliableMinPowerBuffers().get(moteToAdapt).size() == 5) {
            powerSettingAlgorith(moteToAdapt);
            getMoteEffector().setDataBackUP(moteToAdapt, dataBackUP);
            putReliableMinPowerBuffer(moteToAdapt, new LinkedList<>());


        }
 }

    private void powerSettingAlgorith(Mote moteToAdapt) {
        /**
         * The average is taken of the 5 entries.
         */
        double average = 0;
        for (Double power : getReliableMinPowerBuffers().get(moteToAdapt)) {
            average += power;
        }
        average = average / 5;
        /**
         * If the average of the signal strengths is higher than the upper bound, the transmitting power is decreased by 1;
         */
        if (average > getUpperBound()) {
            if (getMoteProbe().getPowerSetting(moteToAdapt) > -3) {
                getMoteEffector().setPower(moteToAdapt, getMoteProbe().getPowerSetting(moteToAdapt) - 1);
            }
        }
        /**
         * If the average of the signal strengths is lower than the lower bound, the transmitting power is increased by 1;
         */
        if (average < getLowerBound()) {
            if (getMoteProbe().getPowerSetting(moteToAdapt) < 14) {
                getMoteEffector().setPower(moteToAdapt, getMoteProbe().getPowerSetting(moteToAdapt) + 1);
            }
        }
    }


}

