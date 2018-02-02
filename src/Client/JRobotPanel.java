package src.Client;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

import javax.swing.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class JRobotPanel extends JPanel{
    Graphics2D graphics2D;
    public static Localizator localizator;
    RoomMap roomMap;

    public JRobotPanel() {
        setPreferredSize(new Dimension(JConstants.PANEL_ROBOT_SIZE_X,JConstants.WINDOW_SIZE_Y));
        setLayout(new BorderLayout());

        roomMap = new RoomMap("/src/files/houses.svg");
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
        at.setToScale(getWidth()/svgDiagram.getWidth(), getWidth()/svgDiagram.getWidth());
        graphics2D.transform(at);

        try {
            roomMap.getSvgDiagram().render(graphics2D);
        }
        catch (Exception e){ e.printStackTrace();}

        drawParticles(Color.RED,graphics2D);
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

    public void testParticle(Graphics2D graphics2D){
        Particle particle = new Particle(260,70,193,0);
        Point p=particle.findIntersection(245,0,245,50);
        drawParticle(particle,Color.RED, (Graphics2D) graphics2D);
        particle.drawParticleLine(Color.RED, (Graphics2D) graphics2D);

        System.out.println(particle.getDistanceToWall(roomMap.getRoomLines()));
        graphics2D.drawOval( (int) particle.intersection.getX() - 2, (int) particle.intersection.getY() -2, 4,4);

//        particle.evaluateParticle(roomMap,19.0f);
//        System.out.println(particle.getWeight());

//        localizator.filterParticles(20,1);
//        try {
//            TimeUnit.SECONDS.sleep(3);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        repaint();

//        Particle p = new Particle(50,50,270,1);
//        localizator.generateNewParticle(p);
    }
}
