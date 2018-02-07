package src.Client;

/**
 * Created by Kalaman on 17.01.18.
 */
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import com.kitfox.svg.SVGDiagram;

public class Localizator {
    private int initParticleAmount;
    private ArrayList<Particle> particleList;
    private SVGDiagram map;
    private RoomMap roomMap;
    private boolean first = true;

    public Localizator (int particleAmount,RoomMap roomMap) {
        this.initParticleAmount = particleAmount;
        this.particleList = new ArrayList<Particle>();
        this.map = roomMap.getSvgDiagram();
        this.roomMap = roomMap;

        generateRandomParticles();
    }

    public ArrayList<Particle> getParticles() {
        return particleList;
    }

    public void generateRandomParticles() {

        ArrayList<Line> lines = roomMap.getRoomLines();
        for (int i=0;i<initParticleAmount;++i) {

            int randomX = 0;
            int randomY = 0;

            while (!isValidPosition(randomX,randomY)) {
                randomX = (int) (Math.random() * roomMap.getSvgDiagram().getWidth());
                randomY = (int) (Math.random() * roomMap.getSvgDiagram().getHeight());
            }

            float randomDeg = (float) (Math.random() * 360) ;

            particleList.add(new Particle(randomX,randomY,randomDeg,JConstants.PARTICLE_SENSOR_AMOUNT,1f / JConstants.PARTICLE_AMOUNT));
        }
    }

    /**
     * Performs the resampling of Particles according to the weights
     * @param sensorRanges
     */
    public void filterParticles(double [] sensorRanges){
        ArrayList<Particle> temp_particleList = new ArrayList<>();
        double sumValue = 0;

        for (Particle particle : particleList) {
            if (!first)
                particle.move(JRobotPanel.virtualRobotStep);
            particle.evaluateParticle(roomMap,sensorRanges);
            sumValue += particle.getWeight();
        }
        first = false;

        for (Particle particle: particleList) {
            particle.normalize(sumValue);
        }

        int index = (int) (Math.random() * particleList.size());
        float beta = 0.0f;

        for (int i = 0; i < particleList.size(); i++) {
            beta += (Math.random() * 2 * sumValue);
            while (beta > particleList.get(index).getWeight()){
                beta -= particleList.get(index).getWeight();
                index = (index + 1) % particleList.size();
            }

            temp_particleList.add(generateNewParticle(particleList.get(index),false));
        }

        /**
         * For debug purposes
         * Finds the particle with the highest weight
         */
        int hindex = 0;
        double hweight = 0;
        for (int i=0;i<temp_particleList.size();++i) {
            if (temp_particleList.get(i).getWeight() > hweight)
            {
                hweight = temp_particleList.get(i).getWeight();
                hindex = i;
            }
        }

        particleList = temp_particleList;
    }

    public Particle generateNewParticle(Particle oldParticle, boolean random){
        int newX = 0, newY = 0;

        while (!isValidPosition(newX,newY)) {
            if (random){
                newX = (int) (Math.random() * roomMap.getSvgDiagram().getWidth());
                newY = (int) (Math.random() * roomMap.getSvgDiagram().getHeight());
            } else {
                newX = ThreadLocalRandom.current().nextInt(oldParticle.getPositionX() - 10, oldParticle.getPositionX() + 10 + 1) ;
                newY = ThreadLocalRandom.current().nextInt(oldParticle.getPositionY() - 10, oldParticle.getPositionY() + 10 + 1) ;
            }
        }
        int newDegree =  ThreadLocalRandom.current().nextInt(oldParticle.getDegree() - 10, oldParticle.getDegree() + 10 + 1) ;

        Particle newParticle = new Particle(newX, newY, newDegree, JConstants.PARTICLE_SENSOR_AMOUNT,oldParticle.getWeight());
        newParticle.rotate(ThreadLocalRandom.current().nextInt(-30, 30));
        return newParticle;
    }

    public boolean isValidPosition (int x,int y) {
        ArrayList<Line> lines = roomMap.getRoomLines();

        if (x == 0 && y == 0)
            return false;

        if (y >= 150 && x >= 100){
            return false;
        }

        int threshold = 2;

        for (int j = 0; j < lines.size(); ++j) {
            Line currentLine = lines.get(j);
            if (((x > currentLine.getX1() && x <= currentLine.getX2())||(x > currentLine.getX2() && x <= currentLine.getX1()))
                    && (Math.abs(y - currentLine.getY1()) <= threshold )) {
                return false;
            } else if(((y > currentLine.getY1() && y <= currentLine.getY2()) || (y > currentLine.getY2() && y <= currentLine.getY1()))
                    && (Math.abs(x - currentLine.getX1()) <= threshold )) {
                return false;
            }
        }
        return true;
    }
}