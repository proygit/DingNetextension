package SelfAdaptation.AdaptationGoals;




/**
 * An adaptation goal with a lower and upper value.
 */
public class IntervalAdaptationGoal extends AdaptationGoal {

    /**
     * A double representing the lower value of the goal
     */
    
    private final Double lowerBoundary;

    /**
     * A double representing the upper value of the goal
     */
    
    private final Double upperBoundary;

    /**
     * Constructs a IntervalAdaptationGoal with a given lower and upper value.
     * @param lowerBoundary The lower boundary of the goal.
     * @param upperBoundary The upper boundary of the goal.
     */
    public IntervalAdaptationGoal( Double lowerBoundary, Double upperBoundary){
        this.lowerBoundary = lowerBoundary;
        this.upperBoundary = upperBoundary;
    }

    /**
     * Returns the lower value of the goal.
     * @return The lower value of the goal.
     */
    
    public Double getLowerBoundary() {
        return lowerBoundary;
    }

    /**
     * Returns the upper value of the goal.
     * @return The upper value of the goal.
     */
    
    public Double getUpperBoundary() {
        return upperBoundary;
    }
}
