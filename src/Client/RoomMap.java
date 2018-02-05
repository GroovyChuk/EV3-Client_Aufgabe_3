package src.Client;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;

public class RoomMap {
    SVGDiagram svgDiagram;
    ArrayList<Line> roomLines;

    public RoomMap(String svgSrc) {
        readSVGFile(svgSrc);
        readRoomLines(svgSrc);
    }

    private boolean readSVGFile(String svgSrc) {
        SVGUniverse svgUniverse = new SVGUniverse();
        try {
            URI svgURI = JMainFrame.class.getResource(svgSrc).toURI();
            svgUniverse.loadSVG(svgURI.toURL(),false);
            svgDiagram = svgUniverse.getDiagram(svgURI);
            svgDiagram.setIgnoringClipHeuristic(true);

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean  readRoomLines (String svgSrc) {
        roomLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(svgSrc.substring(1)))) {
            String line;
            while ((line = br.readLine()) != null) {

                String [] lineArray = line.split(" ");

                int x1 = 0,x2 = 0,y1 = 0,y2 = 0;

                for (String word : lineArray)
                {
                    if (word.contains("x1"))
                    {
                        word = word.replaceAll("x1=","");
                        word = word.replaceAll("\"","");

                        x1 = Integer.parseInt(word);
                    }
                    else if (word.contains("x2"))
                    {
                        word = word.replaceAll("x2=","");
                        word = word.replaceAll("\"","");
                        word = word.replaceAll("></line>","");

                        x2 = Integer.parseInt(word);
                    }
                    else if (word.contains("y1")) {
                        word = word.replaceAll("y1=","");
                        word = word.replaceAll("\"","");

                        y1 = Integer.parseInt(word);
                    }
                    else if (word.contains("y2")) {
                        word = word.replaceAll("y2=","");
                        word = word.replaceAll("\"","");
                        word = word.replaceAll("></line>","");

                        y2 = Integer.parseInt(word);
                    }
                }
                if (!(x1 == 0 && y1== 0 && x2 == 0 && y2 == 0))
                    roomLines.add(new Line(x1,x2,y1,y2));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public ArrayList<Line> getRoomLines () {
        return roomLines;
    }

    public SVGDiagram getSvgDiagram () {
        return svgDiagram;
    }

}
