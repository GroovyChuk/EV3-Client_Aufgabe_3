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
    private int endX;
    private int endY;
    private int weight;
    public Point intersection = null;

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public Particle (int posX, int posY, float deg, int particleWeight) {
        positionX = posX;
        positionY = posY;
        degree = deg % 360;
        weight = particleWeight;
        endX = getPositionX() + (int)(Math.cos(Math.toRadians(getDegree())) * 1000);
        endY = getPositionY() + (int)(Math.sin(Math.toRadians(getDegree())) * 1000);
    }

    public Particle(int nodeXPos, int nodeYPos, float nodeDeg) {
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public float getDegree() {
        return degree;
    }


    /**
     * Returns the distance of the particle to a wall.
     * @param lines list of all the walls in the room
     * @return distance to wall or -1 if no intersection was found
     */
    public float getDistanceToWall(ArrayList<Line> lines) {

        Point temp_intersection = null;
        float distance = -1,temp_distance = 0;
        for (Line line : lines) {
            temp_intersection = findIntersection(line.getX1(), line.getY1(), line.getX2(), line.getY2());
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
        int maxWeight = 20, calculatedWeight = 0;
        float distance = getDistanceToWall(roomMap.getRoomLines());

        if (distance == -1 )
            setWeight(0);
        else{
            calculatedWeight = maxWeight - (int) Math.abs(distance - sensorRange);
            if (calculatedWeight <= 0)
                calculatedWeight = 0;
        }
            setWeight(calculatedWeight);
    }

    public Point findIntersection(int x1, int y1, int x2, int y2){
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

	public void drawParticleLine(Color red, Graphics2D graphics2d) {
        graphics2d.drawLine(getPositionX() ,
                getPositionY(),endX,endY);
	}

}
