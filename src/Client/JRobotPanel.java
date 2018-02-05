package src.Client;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class JRobotPanel extends JPanel{
    Graphics2D graphics2D;
    public static Localizator localizator;
    RoomMap roomMap;

    int virtualRobotX = 0;
    int virtualRobotStep = 10;

    public JRobotPanel() {
        setPreferredSize(new Dimension(JConstants.PANEL_ROBOT_SIZE_X,JConstants.WINDOW_SIZE_Y));
        setLayout(new BorderLayout());

        roomMap = new RoomMap("/src/files/room.svg");
        localizator = new Localizator(1000, roomMap);

        MQTTClient mqttClient = new MQTTClient();
        mqttClient.addMQTTListener(new MQTTClient.MQTTListener() {
            @Override
            public void onDriveReceived(float distanceInCM) {

            }

            @Override
            public void onUltrasonicDistanceReceived(float distanceInCM) {
                JConsolePanel.writeToConsole("New distance to wall: " + distanceInCM);
                JRobotPanel.localizator.filterParticles(distanceInCM);
                virtualRobotX += virtualRobotStep;
                repaint();
            }

            @Override
            public void onLogReceived(String log) {
                System.out.println(log);
            }
        });

        mqttClient.startListeningThread();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        graphics2D = (Graphics2D)g;

        SVGDiagram svgDiagram = roomMap.getSvgDiagram();

        AffineTransform at = new AffineTransform();
        at.setToScale(getHeight()/svgDiagram.getHeight(), getHeight()/svgDiagram.getHeight());
        graphics2D.transform(at);

        try {
            roomMap.getSvgDiagram().render(graphics2D);
        }
        catch (Exception e){ e.printStackTrace();}

        ArrayList<Particle> particles = localizator.getParticles();
        int hindex = 0;
        double hweight = 0f;
        for (int i=0;i<particles.size();++i)
        {
            if (particles.get(i).getWeight() > hweight) {
                hweight = particles.get(i).getWeight();
                hindex = i;
            }
        }

//        drawParticles(Color.RED,graphics2D);
        //drawParticle(new Particle(virtualRobotX,65,0,0,0),Color.BLUE,graphics2D);
//        drawParticle(particles.get(hindex),Color.ORANGE,graphics2D);

        Particle particle = new Particle(80,60,250,6,12);
        particle.rotateSensors(10);
        drawTestParticle(particle,Color.RED,graphics2D);
        JConsolePanel.writeToConsole("Highest weight: " + particles.get(hindex).getWeight());
    }

    public void drawTestParticles (Color color,Graphics2D graphics2D) {
        ArrayList<Particle> particles = localizator.getParticles();
        for (Particle particle : particles)
            drawParticle(particle,Color.RED,graphics2D);
    }

    public void drawParticles (Color color,Graphics2D graphics2D) {
        ArrayList<Particle> particles = localizator.getParticles();
        for (Particle particle : particles)
            drawParticle(particle,Color.RED,graphics2D);
    }

	private void drawParticle(Particle node, Color color, Graphics2D graphics2D) {
        Color oldColor = graphics2D.getColor();

        graphics2D.setColor(color);
        graphics2D.fillOval(node.getPositionX() - (JConstants.PARTICLE_WIDTH /2),node.getPositionY() - (JConstants.PARTICLE_HEIGHT /2),JConstants.PARTICLE_WIDTH,JConstants.PARTICLE_HEIGHT);

        int endX = node.getPositionX() + (int)(Math.cos(Math.toRadians(node.getDegree())) * JConstants.PARTICLE_DEGREE_MULTIPLICATOR);
        int endY = node.getPositionY() + (int)(Math.sin(Math.toRadians(node.getDegree())) * JConstants.PARTICLE_DEGREE_MULTIPLICATOR);

        graphics2D.drawLine(node.getPositionX() ,
                node.getPositionY() ,endX ,endY);

        graphics2D.setColor(oldColor);
    }

    private void drawTestParticle(Particle node, Color color, Graphics2D graphics2D) {
        Color oldColor = graphics2D.getColor();

        graphics2D.setColor(color);
        graphics2D.fillOval(node.getPositionX() - (JConstants.PARTICLE_WIDTH /2),node.getPositionY() - (JConstants.PARTICLE_HEIGHT /2),JConstants.PARTICLE_WIDTH,JConstants.PARTICLE_HEIGHT);

        for (int i=0;i<node.getIntersectionPoints().length;++i) {
            graphics2D.setColor(color);
            graphics2D.drawLine(node.getPositionX(), node.getPositionY(), (int)node.getIntersectionPoints()[i].getX(), (int)node.getIntersectionPoints()[i].getY());

            Point temp_intersection,intersection = null;

            float distance = -1,temp_distance = 0;
            ArrayList<Line> lines = roomMap.getRoomLines();
            for (Line line : lines) {
                temp_intersection = node.findIntersection(line.getX1(), line.getY1(), line.getX2(), line.getY2(),(int)node.getIntersectionPoints()[i].getX(),(int)node.getIntersectionPoints()[i].getY());
                if (temp_intersection != null){
                    temp_distance = (float) Math.sqrt(Math.pow(node.getPositionX()-temp_intersection.getX(),2) + Math.pow(node.getPositionY()-temp_intersection.getY(),2));
                    if (temp_distance < distance || distance == -1){
                        distance = temp_distance;
                        intersection = temp_intersection;
                    }
                }
            }

            graphics2D.setColor(Color.BLUE);
            graphics2D.drawOval((int)intersection.getX() - (JConstants.PARTICLE_WIDTH /2),(int)intersection.getY() - (JConstants.PARTICLE_HEIGHT /2),5,5);
        }




        graphics2D.setColor(oldColor);
    }

}
