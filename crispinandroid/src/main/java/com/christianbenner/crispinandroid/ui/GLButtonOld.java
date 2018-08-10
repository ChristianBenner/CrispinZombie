package com.christianbenner.crispinandroid.ui;

import android.content.Context;
import android.opengl.GLES20;

import com.christianbenner.crispinandroid.data.Texture;
import com.christianbenner.crispinandroid.data.VertexArray;
import com.christianbenner.crispinandroid.data.objects.FlexibleSquare;
import com.christianbenner.crispinandroid.programs.ColourShaderProgram;
import com.christianbenner.crispinandroid.programs.TextureShaderProgram;
import com.christianbenner.crispinandroid.util.ShaderProgram;

import java.util.ArrayList;

/**
 * Created by Christian Benner on 25/12/2017.
 */

@Deprecated
public class GLButtonOld extends UIBaseOld {
    private ArrayList<TouchListener> buttonListeners = new ArrayList<>();
    public void addButtonListener(TouchListener listener) { buttonListeners.add(listener); }
    public void removeButtonListener(TouchListener listener) { buttonListeners.remove(listener); }

    // Note, the line length of the GLText should be a little less wide as the button in NDC
    private GLText text;
    private Texture texture;
    private Context context;
    private boolean outline;
    private FlexibleSquare vertexObject;
    private TextureShaderProgram textureShaderProgram;
    private ColourShaderProgram colourShaderProgram;
    private Boolean buttonDown = false;

    private final float[] defaultMatrix =
    {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    };

    // position, width, height, colour
    private VertexArray lineVertArray;
    private float[] outlineColour = new float[3];
    private float outlineWidth = 1.0f;
    private float[] clickColour = new float[3];
    private boolean clickTransitioning = false;
    private final float defaultClickDiscolour = 0.3f;
    private final float oneSecond = 60.0f;
    private final float discolourLength = 0.25f;
    private final float discolourStep = defaultClickDiscolour / (oneSecond * discolourLength);

    public GLButtonOld(Context context)
    {
        this.context = context;
        this.outline = true;
        this.vertexObject = new FlexibleSquare(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.f, 1.0f, 1.0f);
        this.textureShaderProgram = new TextureShaderProgram(context);
        this.colourShaderProgram = new ColourShaderProgram(context);
        setPosition(0.0f, 0.0f);
        setWidth(1.0f);
        setHeight(1.0f);
        setColour(0.8f, 0.8f, 0.8f, 1.0f);
        setOutlineColour(0.0f, 0.0f, 0.0f);
        outline = true;
        setOutlineWidth(5.0f);
    }

    public void bindData(ShaderProgram shader) {
        vertexObject.bindData(shader);
    }

    public void draw()
    {
        vertexObject.draw();
    }

    public void render()
    {
        if(texture != null)
        {
            // Draw texture
            textureShaderProgram.useProgram();
            vertexObject.bindData(textureShaderProgram);
            textureShaderProgram.setUniforms(defaultMatrix, this.texture.getTextureId(),
                    getRedValue(), getGreenValue(), getBlueValue(), getAlphaValue());
        }else
        {
            colourShaderProgram.useProgram();
            vertexObject.bindData(colourShaderProgram);
            colourShaderProgram.setUniforms(getRedValue(), getGreenValue(), getBlueValue(), getAlphaValue());
        }

        vertexObject.draw();

        if(outline == true)
        {
            final int lineVertexCount = 8;
            final int components = 2;
            float[] linesVert = new float[lineVertexCount * components];

            // P1
            linesVert[0] = vertexObject.getVertexData(0);
            linesVert[1] = vertexObject.getVertexData(1);
            // P2
            linesVert[2] = vertexObject.getVertexData(4);
            linesVert[3] = vertexObject.getVertexData(5);

            // P3
            linesVert[4] = linesVert[2];
            linesVert[5] = linesVert[3];
            // P4
            linesVert[6] = vertexObject.getVertexData(20);
            linesVert[7] = vertexObject.getVertexData(21);

            // P5
            linesVert[8] = linesVert[6];
            linesVert[9] = linesVert[7];
            // P6
            linesVert[10] = vertexObject.getVertexData(8);
            linesVert[11] = vertexObject.getVertexData(9);

            // P7
            linesVert[12] = linesVert[10];
            linesVert[13] = linesVert[11];
            // P8
            linesVert[14] = linesVert[0];
            linesVert[15] = linesVert[1];

            colourShaderProgram.useProgram();
            lineVertArray = new VertexArray(linesVert);
            lineVertArray.setVertexAttribPointer(
                    0,
                    colourShaderProgram.getPositionAttributeLocation(),
                    components,
                    0);
            colourShaderProgram.setUniforms(getOutlineColourRed(), getOutlineColourGreen(),
                    getOutlineColourBlue(), getAlphaValue());
            GLES20.glLineWidth(outlineWidth);
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, lineVertexCount);
        }

        if(text != null)
        {
      //      text.render(getNormalisedX() + (getWidth() / 2.0f) - (text.getWidth() / 2.0f),
      //              -getNormalisedY() + (getHeight() / 2.0f) - text.getHeight());
        }
    }

    // Update the button when touch received
    public boolean sendClick(float x, float y, int pointer)
    {
            if(x > getNormalisedX() && x < getNormalisedX() + getWidth() &&
                    y < getNormalisedY() && y > getNormalisedY() - getHeight())
            {
                clickedOn();
                buttonDown = true;
                return true;
            }

        return false;
    }

    public boolean sendRelease(float x, float y, int pointer)
    {
        if(buttonDown)
        {
            if(x > getNormalisedX() && x < getNormalisedX() + getWidth() &&
                    y < getNormalisedY() && y > getNormalisedY() - getHeight())
            {
                buttonDown = false;
                return true;
            }
        }
        return false;
    }

    public void forceRelease()
    {
        buttonDown = false;
    }

    public boolean sendDrag(float x, float y, int pointer)
    {
        if(buttonDown) {
            if (!(x > getNormalisedX() && x < getNormalisedX() + getWidth() &&
                    y < getNormalisedY() && y > getNormalisedY() - getHeight())) {
                buttonDown = false;
                return true;
            }
        }

        return false;
    }

    public void update(float deltaTime)
    {
        // If buttonDown, send buttonDown listeners
        if(buttonDown)
        {
            TouchEvent be = new TouchEvent(this);
            be.setEvent(TouchEvent.Event.DOWN);
            for(TouchListener bl : buttonListeners)
            {
         //       bl.touchEvent(be);
            }
        }

        if(clickTransitioning)
        {
            if(clickColour[0] < getRedValue())
            {
                setColour(getRedValue() - (discolourStep),
                        getGreenValue() - (discolourStep),
                        getBlueValue() - (discolourStep));
            }else
            {
                setColour(clickColour[0],
                        clickColour[1],
                        clickColour[2]);
                clickTransitioning = false;
            }
        }

    }

    private void clickedOn()
    {
        if(clickTransitioning)
        {
            setColour(clickColour[0],
                    clickColour[1],
                    clickColour[2]);
            clickTransitioning = false;
        }

        clickColour[0] = getRedValue();
        clickColour[1] = getGreenValue();
        clickColour[2] = getBlueValue();
        setColour(getRedValue() + defaultClickDiscolour,
                getGreenValue() + defaultClickDiscolour,
                getBlueValue() + defaultClickDiscolour);
        clickTransitioning = true;

        TouchEvent be = new TouchEvent(this);
        be.setEvent(TouchEvent.Event.CLICK);
        for(TouchListener bl : buttonListeners)
        {
          //  bl.touchEvent(be);
        }
    }

//    private void clickedOff()
//    {
//
//    }

    public GLText getGLText()
    {
        return this.text;
    }

    public int getTextureID()
    {
        return this.texture.getTextureId();
    }

    public void setOutlineWidth(float width)
    {
        this.outlineWidth = width;
    }

    public float getOutlineWidth()
    {
        return this.outlineWidth;
    }

    public void setOutline(boolean outline)
    {
        this.outline = outline;
    }

    public void setText(String text, float fontSize, GLFont font, float orthoWidth,
                        float orthoHeight)
    {
        this.text = new GLText(context, text, fontSize, font, getWidth(), orthoWidth, orthoHeight,
                false);
    }

    public void setTexture(Texture texture)
    {
        outline = false;
        this.texture = texture;
    }

    public void setAlpha(float alpha)
    {
        super.setAlpha(alpha);
        text.setAlpha(alpha);
    }

    public void setOutlineColour(float red, float green, float blue)
    {
        outlineColour[0] = red;
        outlineColour[1] = green;
        outlineColour[2] = blue;
    }

    public float getOutlineColourRed()
    {
        return outlineColour[0];
    }

    public float getOutlineColourGreen()
    {
        return outlineColour[1];
    }

    public float getOutlineColourBlue()
    {
        return outlineColour[2];
    }

    public void setPosition(float normalisedX, float normalisedY)
    {
        setDimensions(this.getNormalisedX(), this.getNormalisedY(),
                this.getWidth(), this.getHeight());
    }

    public void setWidth(float width)
    {
        setDimensions(this.getNormalisedX(), this.getNormalisedY(),
                this.getWidth(), this.getHeight());
    }

    public void setHeight(float height)
    {
        setDimensions(this.getNormalisedX(), this.getNormalisedY(),
                this.getWidth(), this.getHeight());
    }

    public void setPosition(float[] pos)
    {
        setDimensions(this.getNormalisedX(), this.getNormalisedY(),
                this.getWidth(), this.getHeight());
    }

    public void setPosition(int xPixels, int yPixels, int viewportWidth, int viewportHeight)
    {
        setDimensions(this.getNormalisedX(), this.getNormalisedY(),
                this.getWidth(), this.getHeight());
    }

    public void setDimensions(float normalisedX, float normalisedY,
                              float normalisedW, float normalisedH)
    {
        super.setWidth(normalisedW);
        super.setHeight(normalisedH);
        super.setPosition(normalisedX, normalisedY);
        this.vertexObject.setDimensions(normalisedX, normalisedY,
                normalisedX + normalisedW, normalisedY - normalisedH);
    }

    public void setDimensions(int xPixels, int yPixels, int wPixels, int hPixels,
                              int viewportWidth, int viewportHeight)
    {
        setDimensions(
                ((xPixels / (float)viewportWidth) * 2.0f) - 1.0f,
                (((yPixels + hPixels) / (float)viewportHeight) * 2.0f) - 1.0f,
                (wPixels / (float)viewportWidth) * 2.0f,
                (hPixels / (float)viewportHeight) * 2.0f);
    }
}
