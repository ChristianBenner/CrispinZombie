package com.christianbenner.crispinandroid.ui;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.text.TextMesh;
import com.christianbenner.crispinandroid.render.util.ShaderProgram;
import com.christianbenner.crispinandroid.render.util.UIRenderer;
import com.christianbenner.crispinandroid.render.util.VertexArray;
import com.christianbenner.crispinandroid.util.Dimension2D;
import com.christianbenner.crispinandroid.util.Geometry;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;
import static com.christianbenner.crispinandroid.Constants.BYTES_PER_FLOAT;

/*
    Text has some horrible problems because of the way text is generated. Essentially Text
    will build a string by cutting out loads of textures from a texture file. It does this by
    using a information file that tells it where each letter is/how big each letter is etc.

    Ok so the bad part...
    When it comes to using it like other UI it is different because it is generated AROUND vertex
    positions x = 0.0f and y = 0.0f. That means some verticies are negative and some are positive.
    Because I wrote the code so long ago, its very complicated and VERY uncommented, I have
    completely forgotten how it works. Instead of fixing the problem at it's
    roots I took the easier approach and adjusted positions in the Text class itself, but it also
    has to be adjusted in the UI renderer (because that's what looks at the vertices). Therefor
    this is the only UI class that is slightly different to the others and it must be treated
    differently.

    Anyway end of story - we all learn from our mistakes. Maybe one day I will look into re-writing
    the text generating code entirely.
 */
public class Text extends UIBase {
    // Data Constants
    private static final int POSITION_STRIDE = (POSITION_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    private static final int TEXTURE_STRIDE = (TEXTURE_COORDINATES_COMPONENT_COUNT) *
            BYTES_PER_FLOAT;

    // Rendering stuff
    private VertexArray vertexArray;
    private VertexArray textureArray;
    private int vertexCount;

    // Formatting Data
    private float maxLineSize;
    private int numberOfLines;
    private boolean centerText;
    private float maxLineVertexWidth;
    private float vertexWidth;
    private float vertexHeight;
    private float canvasWidth;
    private float canvasHeight;

    // Text data
    private String text;
    private Font font;
    private float fontSize;

    private boolean generatedText = false;

    public Text(String text, float fontSize, Font font, boolean centered) {
        this.text = text;
        this.fontSize = fontSize;
        this.font = font;
        this.maxLineVertexWidth = maxLineSize;
        this.maxLineSize = maxLineVertexWidth / this.canvasWidth;
        this.centerText = centered;
        this.texture = font.getTextureAtlas();
    }

    public Text(String text, float fontSize, Font font, boolean centered, Colour colour)
    {
        this(text, fontSize, font, centered);
        setColour(colour);
    }

    public void generateText(float uiCanvasWidth, float uiCanvasHeight, int maxLineLength)
    {
        this.canvasWidth = uiCanvasWidth;
        this.canvasHeight = uiCanvasHeight;

        this.maxLineVertexWidth = maxLineLength;
        this.maxLineSize = maxLineVertexWidth / this.canvasWidth;

        setText(text);
    }

    @Override
    public void bindData(ShaderProgram shader) {
        if(generatedText)
        {
            vertexArray.setVertexAttribPointer(
                    0,
                    shader.getPositionAttributeLocation(),
                    POSITION_COMPONENT_COUNT,
                    POSITION_STRIDE);

            textureArray.setVertexAttribPointer(
                    0,
                    shader.getTextureCoordinatesAttributeLocation(),
                    TEXTURE_COORDINATES_COMPONENT_COUNT,
                    TEXTURE_STRIDE);
        }
        else {
            System.err.println("ERROR [TEXT UI]: You must generate the text before rendering it." +
                    " Use method 'generateText' for the text '" + text + "'");
        }

    }

    @Override
    public void draw() {
        if(generatedText)
        {
            glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        }
    }

    public void setText(String text) {
        this.text = text;
        TextMesh data = this.font.loadText(this);
        this.vertexArray = new VertexArray(data.getVertexPositions());
        this.textureArray = new VertexArray(data.getTextureCoords());
        this.vertexCount = data.getVertexCount();
        this.vertexWidth = data.getVertexWidth();
        this.vertexHeight = data.getVertexHeight();
        this.generatedText = true;
    }

    @Override
    public void setPosition(Geometry.Point position)
    {
        super.setPosition(new Geometry.Point(position.x, position.y + getHeight(), position.z));
    }

    @Override
    public void setDimensions(Dimension2D dimensions)
    {
        System.err.println("[Text] You cannot set dimensions of Text");
        super.setPosition(new Geometry.Point(dimensions.x, dimensions.y + getHeight(),
                super.getPosition().z));
    }

    @Override
    public float getHeight()
    {
        return vertexHeight * canvasHeight;
    }

    public void setNumberOfLines(int number)
    {
        this.numberOfLines = number;
    }

    public boolean isCentered()
    {
        return this.centerText;
    }

    public float getMaxLineSize()
    {
        return this.maxLineSize;
    }

    public String getTextString()
    {
        return this.text;
    }

    public float getFontSize()
    {
        return fontSize;
    }
}
