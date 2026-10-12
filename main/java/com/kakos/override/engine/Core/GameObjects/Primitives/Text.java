package com.kakos.override.engine.Core.GameObjects.Primitives;

import java.io.File;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.kakos.override.editor.Console;
import com.kakos.override.engine.Core.GameObject;
import com.kakos.override.engine.Core.Project;
import com.kakos.override.engine.Core.ResourceManager;
import com.kakos.override.engine.Core.nrs;

public class Text extends GameObject {
    public String text;
    public BitmapFont font;
    public float width;
    public float height;
    public String align = "left";
    public float fontSize = 20;

    private final GlyphLayout textLayout = new GlyphLayout();

    public void updateFont(String fontPath) {
        File fontFile = new File(Project.projectRootPath + File.separator + fontPath);
        if (fontFile.exists()) {
            font = ResourceManager.getFont(fontFile.getPath());
            Console.log(Console.Modifier.FILESYSTEM, "Font loaded: " + fontFile.getAbsolutePath());
        } else {
            Console.logError(Console.Modifier.FILESYSTEM, "Can`t load font: " + fontFile.getAbsolutePath());
        }
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, SpriteBatch spriteBatch) {
        if (this.font == null || this.text == null || this.text.isEmpty())
            return;

        nrs.beginSprite();

        BitmapFont.BitmapFontData fontData = this.font.getData();
        float originalScaleX = fontData.scaleX;
        float originalScaleY = fontData.scaleY;

        this.font.setColor(
                this.color.get("r").tofloat(),
                this.color.get("g").tofloat(),
                this.color.get("b").tofloat(),
                this.color.get("a").tofloat());

        float baseCapHeight = fontData.capHeight / fontData.scaleY;
        float targetScale = this.fontSize / baseCapHeight;
        fontData.setScale(targetScale, targetScale);

        int hAlign = Align.left;
        if ("center".equalsIgnoreCase(this.align)) {
            hAlign = Align.center;
        } else if ("right".equalsIgnoreCase(this.align)) {
            hAlign = Align.right;
        }

        boolean useWrap = this.width > 0;
        textLayout.setText(this.font, this.text, this.font.getColor(), this.width, hAlign, useWrap);

        float totalWidth = useWrap ? this.width : textLayout.width;
        float localX = 0.0f - (totalWidth * this.anchorX);

        float localY = (textLayout.height * (1.0f - this.anchorY));

        float pixelPerfectX = Math.round(localX);
        float pixelPerfectY = Math.round(localY);

        this.font.draw(spriteBatch, textLayout, pixelPerfectX, pixelPerfectY);

        fontData.setScale(originalScaleX, originalScaleY);
    }

    public Text(String text, String fontPath, float x, float y) {
        super(x, y);
        this.text = text;
        updateFont(fontPath);
    }
}
