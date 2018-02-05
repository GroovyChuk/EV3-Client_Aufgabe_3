package src.Client;

/**
 * Created by Kalaman on 17.01.18.
 */
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
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

            int randomX = -1;
            int randomY = -1;

            while (!isValidPosition(randomX,randomY)) {
                randomX = (int) (Math.random() * roomMap.getSvgDiagram().getWidth());
                randomY = (int) (Math.random() * roomMap.getSvgDiagram().getHeight());
            }

            float randomDeg = (float) (Math.random() * 360) ;
            particleList.add(new Particle(randomX,randomY,randomDeg,1f / JConstants.PARTICLE_AMOUNT));
        }
    }

    /**
     * Performs the resampling of Particles according to the weights
     * @param sensorRange
     */
    public void filterParticles(float sensorRange){
        ArrayList<Particle> temp_particleList = new ArrayList<>();
        double sumValue = 0;

        for (Particle particle : particleList) {
            if (!first)
                particle.move(10);
            particle.evaluateParticle(roomMap,sensorRange);
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
        int newX = -1, newY = -1;

        while (!isValidPosition(newX,newY)) {
            if (random){
                newX = (int) (Math.random() * roomMap.getSvgDiagram().getWidth());
                newY = (int) (Math.random() * roomMap.getSvgDiagram().getHeight());
            } else {
                newX = ThreadLocalRandom.current().nextInt(oldParticle.getPositionX() - 10, oldParticle.getPositionX() + 10 + 1) ;
                newY = ThreadLocalRandom.current().nextInt(oldParticle.getPositionY() - 10, oldParticle.getPositionY() + 10 + 1) ;
            }
        }
        int newDegree =  ThreadLocalRandom.current().nextInt(oldParticle.getDegree() - 25, oldParticle.getDegree() + 25 + 1) ;

        Particle newParticle = new Particle(newX, newY, newDegree,oldParticle.getWeight());
        return newParticle;
    }

    public boolean isValidPosition (int x,int y) {
        ArrayList<Line> lines = roomMap.getRoomLines();

        for (int j = 0; j < lines.size(); ++j) {
            Line currentLine = lines.get(j);
            if (x >= currentLine.getX1() && x < currentLine.getX2() &&
                    y >= currentLine.getY2()) {
                return true;
            }
        }
        return false;
    }
}