package yio.tro.antiyoy.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.StringBuilder;
import yio.tro.antiyoy.*;
import yio.tro.antiyoy.gameplay.game_view.GameView;
import yio.tro.antiyoy.stuff.Fonts;
import yio.tro.antiyoy.stuff.FrameBufferYio;
import yio.tro.antiyoy.stuff.GraphicsYio;
import yio.tro.antiyoy.stuff.RectangleYio;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by ivan on 22.07.14.
 */
public class ButtonRenderer {
    private FrameBuffer frameBuffer;
    private final SpriteBatch batch;
    private RectangleYio pos;
    private TextureRegion buttonBackground1, buttonBackground2, buttonBackground3, bigButtonBackground;
    private ArrayList<String> text;
    private int horizontalOffset;


    public ButtonRenderer() {
        batch = new SpriteBatch();
        buttonBackground1 = GameView.loadTextureRegion("button_background_1.png", true);
        buttonBackground1.flip(false, true);
        buttonBackground2 = GameView.loadTextureRegion("button_background_2.png", true);
        buttonBackground2.flip(false, true);
        buttonBackground3 = GameView.loadTextureRegion("button_background_3.png", true);
        buttonBackground3.flip(false, true);
        bigButtonBackground = GameView.loadTextureRegion("big_button_background.png", true);
        bigButtonBackground.flip(false, true);
    }


    TextureRegion getButtonBackground(ButtonYio buttonYio) {
        switch (buttonYio.id % 3) {
            case 0:
                return buttonBackground1;
            case 1:
                return buttonBackground2;
            case 2:
                return buttonBackground3;
            default:
                return buttonBackground1;
        }
    }


    private void beginRender(ButtonYio buttonYio, BitmapFont font, int FONT_SIZE) {
        horizontalOffset = (int) (0.3f * FONT_SIZE);
        frameBuffer = FrameBufferYio.getInstance(Pixmap.Format.RGB565, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        frameBuffer.begin();
        Gdx.gl.glClearColor(buttonYio.backColor.r, buttonYio.backColor.g, buttonYio.backColor.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Matrix4 matrix4 = new Matrix4();
        int orthoWidth = getExpectedOrthoWidth(buttonYio, font);
        int orthoHeight = (orthoWidth / Gdx.graphics.getWidth()) * Gdx.graphics.getHeight();
        matrix4.setToOrtho2D(0, 0, orthoWidth, orthoHeight);
        batch.setProjectionMatrix(matrix4);
        batch.begin();
        if (buttonYio.position.height < 0.12 * Gdx.graphics.getHeight())
            batch.draw(getButtonBackground(buttonYio), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        else
            batch.draw(bigButtonBackground, 0, 0, orthoWidth, orthoHeight);
        batch.end();
        pos = new RectangleYio(buttonYio.position);
        initText(buttonYio, font);
    }


    private void initText(ButtonYio buttonYio, BitmapFont font) {
        text = new ArrayList<>();
        if (buttonYio.text.size() == 1) {
            text = buttonYio.text;
            return;
        }
        double currentX, currentWidth;
        StringBuilder builder = new StringBuilder();
        for (String srcLine : buttonYio.text) {
            currentX = horizontalOffset;
            ArrayList<String> tokens = convertSourceLineToTokens(srcLine);
            for (String token : tokens) {
                currentWidth = GraphicsYio.getTextWidth(font, token);
                if (currentX + currentWidth > Gdx.graphics.getWidth()) {
                    text.add(builder.toString());
                    builder = new StringBuilder();
                    currentX = 0;
                }
                builder.append(token);
                currentX += currentWidth;
            }
            text.add(builder.toString());
            builder = new StringBuilder();
        }
        while (text.size() > buttonYio.text.size()) {
            String lastLine = text.get(text.size() - 1);
            if (lastLine.length() > 2) break;
            text.remove(text.size() - 1);
        }
    }


    private ArrayList<String> convertSourceLineToTokens(String line) {
        ArrayList<String> tokens = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(line, " ");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            tokens.add(token + " ");
        }
        return tokens;
    }


    void endRender(ButtonYio buttonYio) {
        Texture texture = frameBuffer.getColorBufferTexture();
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        float f = ((FrameBufferYio) frameBuffer).f;
        buttonYio.textureRegion = new TextureRegion(texture, (int) (pos.width * f), (int) (pos.height * f));
        frameBuffer.end();
        frameBuffer.dispose();
    }


    private int getExpectedOrthoWidth(ButtonYio buttonYio, BitmapFont font) {
//        float longestLineLength = 0, currentLineLength;
//        for (String line : buttonLighty.text) {
//            currentLineLength = font.getBounds(line).width;
//            if (currentLineLength > longestLineLength) longestLineLength = currentLineLength;
//        }
//        longestLineLength += 0.1 * buttonLighty.position.width;
//        if (longestLineLength < Gdx.graphics.getWidth()) longestLineLength = Gdx.graphics.getWidth();
//        return (int)(longestLineLength);
//        if (buttonLighty.position.height < 0.2 * Gdx.graphics.getHeight())
        return Gdx.graphics.getWidth();
//        else return (int)(1.2 * Gdx.graphics.getWidth());
    }


    private int getTextWidth(String text, BitmapFont font) {
        return (int) YioGdxGame.getTextWidth(font, text);
    }


    public void renderButton(ButtonYio buttonYio, BitmapFont font, int FONT_SIZE) {
        beginRender(buttonYio, font, FONT_SIZE);
        float ratio = (float) (pos.width / pos.height);
        int lineHeight = (int) (1.2f * FONT_SIZE);
        if (text.size() == 1) {
            //if button has single line of text then center it
            float textWidth = getTextWidth(text.get(0), font);
            horizontalOffset = (int) (0.5 * (1.35f * FONT_SIZE * ratio - textWidth));
            if (horizontalOffset < 0) {
                horizontalOffset = (int) (0.3f * FONT_SIZE);
            }
        }
        int verticalOffset = (int) (0.3f * FONT_SIZE);
        int lineNumber = 0;
        float longestLineLength = 0, currentLineLength;
        batch.begin();
        font.setColor(0, 0, 0, 1);
        for (String line : text) {
            font.draw(batch, line, horizontalOffset, verticalOffset + lineNumber * lineHeight);
            currentLineLength = getTextWidth(line, font);
            if (currentLineLength > longestLineLength) longestLineLength = currentLineLength;
            lineNumber++;
        }
        batch.end();
        pos.height = text.size() * lineHeight + verticalOffset / 2;
        pos.width = pos.height * ratio;
        if (longestLineLength > pos.width - 0.3f * (float) lineHeight) {
            pos.width = longestLineLength + 2 * horizontalOffset;
        }
        endRender(buttonYio);
    }


    public void renderButton(ButtonYio buttonYio) {
        renderButton(buttonYio, Fonts.buttonFont, Fonts.FONT_SIZE);
    }
}
