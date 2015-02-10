package nisargpatel.inertialnavigation.heading;

public class GyroHeadingInference {

    private double slope;
    private double intercept;

    private double degree;

    public GyroHeadingInference(double[] gyroInput, double[] radianInput) {
        double sumGyroInput = 0;
        double sumRadianInput = 0;
        double sumGyroInputTimesRadianInput = 0;
        double sumGyroInputSquared = 0;

        //finding the line of best fit between gyroData of orientation and degree measure of actual orientation
        //using method from http://cnfolio.com/public/M591_regression_notes.pdf
        for (double aGyroInput : gyroInput) sumGyroInput += aGyroInput;
        for (double aRadianInput : radianInput) sumRadianInput += aRadianInput;
        for (int i = 0; i < gyroInput.length; i++)
            sumGyroInputTimesRadianInput += gyroInput[i] * radianInput[i];
        for (double aGyroInput : gyroInput) sumGyroInputSquared += Math.pow(aGyroInput, 2);

        slope = ((gyroInput.length * sumGyroInputTimesRadianInput) - (sumGyroInput * sumRadianInput)) / (gyroInput.length * sumGyroInputSquared - Math.pow(sumGyroInput, 2));
        intercept = (sumRadianInput - slope * sumGyroInput) / gyroInput.length;
    }

    //calculate degrees by passing the raw gyro output through gyroData-to-degrees line
    public void calcDegrees(double gyroInput) {
        degree = (slope * gyroInput) + intercept;

        //mod degree by 360 to make sure its between 0 and 360
        degree = degree % 360;
    }

    //calculate x coordinate point given angle and radius
    public double getXPoint(double distance) {
        return distance * Math.cos(Math.toRadians(degree));
    }

    //calculate y coordinate point given angle and radius
    public double getYPoint(double distance) {
        return distance * Math.sin(Math.toRadians(degree));
    }

    //return the value of degree (for debugging)
    public double getDegree() {return degree;}

}

