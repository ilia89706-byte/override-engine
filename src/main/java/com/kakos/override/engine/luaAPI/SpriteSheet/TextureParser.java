package com.kakos.override.engine.luaAPI.SpriteSheet;

import com.kakos.override.engine.Core.Project;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class TextureParser {

    
    public static LuaTable parseAtlas(String xmlPath) {

        LuaTable mainTable = LuaValue.tableOf();

        try {
            File file = new File(Project.projectRootPath + File.separator + xmlPath);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodes = doc.getElementsByTagName("SubTexture");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);


                String name = el.getAttribute("name");
                int x = Integer.parseInt(el.getAttribute("x"));
                int y = Integer.parseInt(el.getAttribute("y"));
                int width = Integer.parseInt(el.getAttribute("width"));
                int height = Integer.parseInt(el.getAttribute("height"));


                LuaTable boundsArray = LuaValue.listOf(new LuaValue[]{
                        LuaValue.valueOf(x),
                        LuaValue.valueOf(y),
                        LuaValue.valueOf(width),
                        LuaValue.valueOf(height)
                });


                mainTable.set(LuaValue.valueOf(name), boundsArray);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при чтении XML или создании LuaTable: " + e.getMessage());
            e.printStackTrace();
        }

        return mainTable;
    }
}
