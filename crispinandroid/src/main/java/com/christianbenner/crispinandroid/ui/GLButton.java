package com.christianbenner.crispinandroid.ui;

import android.graphics.PointF;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.data.Texture;
import com.christianbenner.crispinandroid.data.VertexArray;
import com.christianbenner.crispinandroid.util.ShaderProgram;

import java.util.ArrayList;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;

/**
 * Created by Christian Benner on 28/03/2018.
 */

public class GLButton extends UIBase implements Interactive {
    private ArrayList<TouchListener> buttonListeners = new ArrayList<>();
    public void addButtonListener(TouchListener listener) { buttonListeners.add(listener); }
    public void removeButtonListener(TouchListener listener) { buttonListeners.remove(listener); }

    protected boolean buttonDown = false;

    private boolean clickTransitioning = false;
    private Colour clickColour = new Colour(0.0f, 0.0f, 0.0f);

    private final float defaultClickDiscolour = 0.3f;
    private final float oneSecond = 60.0f;
    private final float discolourLength = 0.25f;
    private final float discolourStep = defaultClickDiscolour / (oneSecond * discolourLength);

    private VertexArray vertexArray;
    private int vertexCount;
    private PointF lastPointerPosition;

    public GLButton(UIDimension dimensions, Colour colour, Texture texture)
    {
        this.dimensions = dimensions;
        this.colour = colour;
        this.texture = texture;
        this.lastPointerPosition = new PointF(0.0f, 0.0f);
    }

    public GLButton(UIDimension dimensions)
    {
        this(dimensions, new Colour(1.0f, 1.0f, 1.0f), null);
    }

    public GLButton(UIDimension dimensions, Colour colour)
    {
        this(dimensions, colour, null);
    }

    public GLButton(UIDimension dimensions, Texture texture)
    {
        this(dimensions, new Colour(1.0f, 1.0f, 1.0f), texture);
    }

    private final float[] vertexData = {
            // XYST Triangles
            0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f,
            1f, 1f, 1f, 0f,
            1f, 1f, 1f, 0f,
            0f, 0f, 0f, 1f,
            1f, 0f, 1f, 1f
    };

    public void bindData(ShaderProgram shader)
    {
        vertexArray = new VertexArray(vertexData);
        vertexCount = vertexData.length /
                (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT);

        vertexArray.setVertexAttribPointer(
                0,
                shader.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);

        vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                shader.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE);
    }

    public void draw()
    {
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
    }

    public boolean interacts(Pointer pointer)
    {
        // The position of the pointer
        PointF pos = pointer.getPosition();

        // Check if the pointer is inside the button
        if(pos.x > dimensions.x && pos.x < dimensions.x + getWidth() &&
                pos.y < dimensions.y + getHeight() && pos.y > dimensions.y)
        {
            return true;
        }

        return false;
    }

    public void forceRelease()
    {
        buttonDown = false;
        sendReleaseEvent(new PointF(0.0f, 0.0f));
    }

    public boolean isButtonDown()
    {
        return this.buttonDown;
    }

    public void update(float deltaTime)
    {
        // If buttonDown, send buttonDown listeners
        if(buttonDown)
        {
            sendDownEvent(this.lastPointerPosition);
        }

        if(clickTransitioning)
        {
            if(clickColour.r < colour.r)
            {
                setColour(new Colour(colour.r - (discolourStep),
                        colour.g - (discolourStep),
                        colour.b - (discolourStep)));
            }else
            {
                setColour(clickColour);
                clickTransitioning = false;
            }
        }
    }

    protected void sendClickEvent(PointF position)
    {
        // While the click
        if(clickTransitioning)
        {
            setColour(clickColour);
            clickTransitioning = false;
        }

        clickColour = colour;
        setColour(new Colour(colour.r + defaultClickDiscolour,
                colour.g + defaultClickDiscolour,
                colour.b + defaultClickDiscolour));
        clickTransitioning = true;

        final TouchEvent CLICK_EVENT = new TouchEvent(this, TouchEvent.Event.CLICK, position);
        for(final TouchListener buttonListener : buttonListeners)
        {
            buttonListener.touchEvent(CLICK_EVENT);
        }
    }

    protected void sendReleaseEvent(PointF position)
    {
        final TouchEvent RELEASE_EVENT = new TouchEvent(this, TouchEvent.Event.RELEASE, position);
        for(final TouchListener buttonListener : buttonListeners)
        {
            buttonListener.touchEvent(RELEASE_EVENT);
        }
    }

    protected void sendDownEvent(PointF position)
    {
        // Send the drag touch event
        final TouchEvent DOWN_EVENT = new TouchEvent(this, TouchEvent.Event.DOWN, position);
        for(final TouchListener buttonListener : buttonListeners)
        {
            buttonListener.touchEvent(DOWN_EVENT);
        }
    }

    @Override
    public void click(PointF position)
    {
        // Set the button as down and send a click event
        buttonDown = true;
        this.lastPointerPosition = position;
        sendClickEvent(position);
    }

    @Override
    public void release(PointF position)
    {
        // If the button is down then send a release event
        if(buttonDown)
        {
            sendReleaseEvent(position);
            buttonDown = false;
        }
    }

    @Override
    public void drag(PointF position)
    {
        // Don't send a down event here because one should be send every update, instead update
        // the last position
        this.lastPointerPosition = position;
    }
}
