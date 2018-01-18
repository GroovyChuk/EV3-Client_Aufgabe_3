package src.Client;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

import javax.swing.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.net.URI;
import java.util.ArrayList;

public class JRobotPanel extends JPanel{
    Graphics2D graphics2D;
    Localizator localizator;
    RoomMap roomMap;

    public JRobotPanel() {
        setPreferredSize(new Dimension(JConstants.PANEL_ROBOT_SIZE_X,JConstants.WINDOW_SIZE_Y));
        setLayout(new BorderLayout());

        roomMap = new RoomMap("/src/files/houses.svg");
        localizator = new Localizator(0,roomMap.getSvgDiagram());
    }

    /**
     * Sets the weight of the particle, depending on the measured distance
     * @param particle list of all the walls in the room
     * @param sensorRange Distance measured with Ultrasonic sensor
     */
    public void filterParticle(Particle particle,float sensorRange){

        float distance = particle.getDistanceToWall(roomMap.getRoomLines());

         if (distance == -1 )
            particle.setWeight(0);
        else if (Math.abs(distance - sensorRange) < 1)
            particle.setWeight(3);
        else if (Math.abs(distance - sensorRange) < 5)
            particle.setWeight(2);
        else
            particle.setWeight(1);
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

    public void testParticle(){
        Graphics2D g = (Graphics2D) getGraphics();
        Particle particle = new Particle(260,70,185,0);
        AffineTransform at = new AffineTransform();
        at.setToScale(getWidth()/roomMap.getSvgDiagram().getWidth(), getWidth()/roomMap.getSvgDiagram().getWidth());
        g.transform(at);
        drawParticle(particle,Color.RED, (Graphics2D) g);
//        particle.drawParticleLine(Color.RED, (Graphics2D) g);

        System.out.println(particle.getDistanceToWall(roomMap.getRoomLines()));
        g.drawOval( (int) particle.intersection.getX() - 2, (int) particle.intersection.getY() -2, 4,4);
    }
}
