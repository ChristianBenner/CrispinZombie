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

    protected Boolean buttonDown = false;
    private boolean clickTransitioning = false;
    private Colour clickColour = new Colour(0.0f, 0.0f, 0.0f);

    private final float defaultClickDiscolour = 0.3f;
    private final float oneSecond = 60.0f;
    private final float discolourLength = 0.25f;
    private final float discolourStep = defaultClickDiscolour / (oneSecond * discolourLength);

    private VertexArray vertexArray;
    private int vertexCount;
    protected Pointer pointer;

    public GLButton(UIDimension dimensions, Colour colour, Texture texture)
    {
        this.dimensions = dimensions;
        this.colour = colour;
        this.texture = texture;
        this.pointer = null;
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

    private float[] vertexData = {
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



    public boolean sendClick(Pointer pointer)
    {
        PointF pos = pointer.getPosition();

        // Stops other UI from being clicked by the same pointer
        if(pointer != null && !pointer.isInUse() &&
                pos.x > dimensions.x && pos.x < dimensions.x + getWidth() &&
                pos.y < dimensions.y + getHeight() && pos.y > dimensions.y)
        {
            pointer.setInUse(true);

            // Store so that we can release it later
            this.pointer = pointer;

          //  clickedOn();
            buttonDown = true;
            return true;
        }

        return false;
    }

    public boolean sendRelease(Pointer pointer)
    {
        if(buttonDown && this.pointer == pointer)
        {
            buttonDown = false;
           // sendReleaseEvent();
            return true;
        }

        return false;
    }

    public void forceRelease()
    {
        buttonDown = false;
        sendReleaseEvent(new PointF(0.0f, 0.0f));
    }

    public boolean sendDrag(Pointer pointer)
    {
        PointF pos = pointer.getPosition();

        if(buttonDown) {
            if (!(pos.x > dimensions.x && pos.x < dimensions.x + getWidth() &&
                    pos.y < dimensions.y + getHeight() && pos.y > dimensions.y)) {
                if(pointer == this.pointer)
                {
                    buttonDown = false;
                  //  sendReleaseEvent();
                    return true;
                }
            }
        }

        return false;
    }

    public void update(float deltaTime)
    {
        // If buttonDown, send buttonDown listeners
       /* if(buttonDown)
        {
            TouchEvent be = new TouchEvent(this);
            be.setEvent(TouchEvent.Event.DOWN);
            for(TouchListener bl : buttonListeners)
            {
                bl.touchEvent(be);
            }
        }*/

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

    public boolean isButtonDown()
    {
        return this.buttonDown;
    }

    protected void sendClickEvent(PointF position)
    {
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

        TouchEvent be = new TouchEvent(this);
        be.setEvent(TouchEvent.Event.CLICK);
        for(TouchListener bl : buttonListeners)
        {
            bl.touchEvent(be, position);
        }
    }

    protected void sendReleaseEvent(PointF position)
    {
        TouchEvent be = new TouchEvent(this);
        be.setEvent(TouchEvent.Event.RELEASE);
        for(TouchListener bl : buttonListeners)
        {
            bl.touchEvent(be, position);
        }
    }

    protected void sendDragEvent(PointF position)
    {
        // Send the drag touch event
        TouchEvent be = new TouchEvent(this);
        be.setEvent(TouchEvent.Event.DOWN);
        for(TouchListener bl : buttonListeners)
        {
            bl.touchEvent(be, position);
        }
    }

    @Override
    public void click(PointF position)
    {
        buttonDown = true;
        sendClickEvent(position);
    }

    @Override
    public void release(PointF position)
    {
        if(buttonDown)
        {
            sendReleaseEvent(position);
            buttonDown = false;
        }
    }

    @Override
    public void drag(PointF position)
    {
        sendDragEvent(position);
    }
}
