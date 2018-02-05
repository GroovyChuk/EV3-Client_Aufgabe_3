package src.Client;

import java.awt.*;
import java.util.ArrayList;


/**
 * Created by Kalaman on 12.01.18.
 */
public class Particle {
    private int positionX;
    private int positionY;
    private float degree;
    private float [] degreeSensor;
    private Point [] intersectionPoints;
    private double weight;
    public Point intersection = null;

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Particle (int posX, int posY, float deg, int degSensAmount, double particleWeight) {
        positionX = posX;
        positionY = posY;
        degree = deg % 360;

        int degIncr = 360 / degSensAmount;

        degreeSensor = new float [degSensAmount];
        intersectionPoints = new Point[degSensAmount];

        for (int i=0;i<degreeSensor.length;++i) {
            degreeSensor[i] = degIncr * (i + 1);
            intersectionPoints[i] = new Point(getPositionX() + (int)(Math.cos(Math.toRadians(degreeSensor[i])) * 1000),
                                              getPositionY() + (int)(Math.sin(Math.toRadians(degreeSensor[i])) * 1000));
        }

        weight = particleWeight;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public int getSensorDegree (int sensorNumber) {
        if (sensorNumber >= degreeSensor.length)
            return -1;
        return (int)degreeSensor[sensorNumber];
    }

    public int getDegree() {
        return (int)degree;
    }

    public void move (int distance) {
        positionX = getPositionX() + (int)(Math.cos(Math.toRadians(getDegree())) * distance );
        positionY = getPositionY() + (int)(Math.sin(Math.toRadians(getDegree())) * distance );
    }

    /**
     * Rotates all sensor positions
     * @param degree
     */
    public void rotateSensors (int degree) {
        for (int i=0;i<degreeSensor.length;++i){
            degreeSensor[i] = (degreeSensor[i] + degree) % 360;
            intersectionPoints[i] = new Point(getPositionX() + (int)(Math.cos(Math.toRadians(degreeSensor[i])) * 1000),
                    getPositionY() + (int)(Math.sin(Math.toRadians(degreeSensor[i])) * 1000));
        }

    }

    public Point[] getIntersectionPoints() {
        return intersectionPoints;
    }

    /**
     * Returns the distance of the particle to a wall.
     * @param lines list of all the walls in the room
     * @return distance to wall or -1 if no intersection was found
     */
    public float getDistanceToWall(ArrayList<Line> lines, int endX, int endY) {

        Point temp_intersection = null;
        float distance = -1,temp_distance = 0;
        for (Line line : lines) {
            temp_intersection = findIntersection(line.getX1(), line.getY1(), line.getX2(), line.getY2(),endX,endY);
            if (temp_intersection != null){
                temp_distance = (float) Math.sqrt(Math.pow(positionX-temp_intersection.getX(),2) + Math.pow(positionY-temp_intersection.getY(),2));
                if (temp_distance < distance || distance == -1){
                    distance = temp_distance;
                    intersection = temp_intersection;
                }
            }
        }

        if (intersection == null)
            return -1;
        else {
            return distance;
        }
    }

    /**
     * Sets the weight of the particle, depending on the measured distance
     * @param roomMap
     * @param sensorRange Distance measured with Ultrasonic sensor
     */
    public void evaluateParticle(RoomMap roomMap,float sensorRange){
        float distance = 0;//getDistanceToWall(roomMap.getRoomLines(),intersectionX[0],intersectionY[0]);

        if (distance == -1 )
            setWeight(0);
        else {
            if (sensorRange > distance)
                weight *= distance / sensorRange;
            else
                weight *= sensorRange / distance;
        }
    }

    public void normalize (double sumWeight) {
        setWeight(getWeight() / sumWeight);
    }

    public Point findIntersection(int x1, int y1, int x2, int y2, int endX, int endY){
        int d = (x1-x2)*(positionY-endY) - (y1-y2)*(positionX-endX);
        if (d == 0) return null;
        int xi = ((positionX-endX)*(x1*y2-y1*x2)-(x1-x2)*(positionX*endY-positionY*endX))/d;
        int yi = ((positionY-endY)*(x1*y2-y1*x2)-(y1-y2)*(positionX*endY-positionY*endX))/d;
        Point p = new Point(xi,yi);
        if (xi < Math.min(x1,x2) || xi > Math.max(x1,x2)) return null;
        if (xi < Math.min(positionX,endX) || xi > Math.max(positionX,endX)) return null;
        if (yi < Math.min(y1,y2) || yi > Math.max(y1,y2)) return null;
        if (yi < Math.min(positionY,endY) || yi > Math.max(positionY,endY)) return null;
        return p;
    }

}
