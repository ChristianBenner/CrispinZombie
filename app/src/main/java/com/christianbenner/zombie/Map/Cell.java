package com.christianbenner.zombie.Map;

import com.christianbenner.crispinandroid.render.util.ShaderProgram;
import com.christianbenner.crispinandroid.render.util.VertexArray;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;
import static com.christianbenner.zombie.Map.CellType.BRICK_WALL;
import static com.christianbenner.zombie.Map.CellType.COBBLE;
import static com.christianbenner.zombie.Map.CellType.GRASS;
import static com.christianbenner.zombie.Map.CellType.WALL;

/**
 *  Cell class. Contains important data on cells such as the type, the position in the cell array,
 *  and data that assists in the A* path finding calculation.
 *
 *  @author     Christian Benner
 *  @version    1.0
 *  @since      2018-07-14
 */
public class Cell
{
    // The size of each cell in NDC
    private static final float CELL_SIZE = 0.5f;

    private static final int FLOATS_PER_VERTEX = 3;
    private static final int FLOATS_PER_TEXEL = 2;
    private static final int FLOATS_PER_NORMAL = 3;
    private static final int VERTEX_COUNT = 6;

    // The width and height of the texture chart
    public static final int TEXTURE_CHART_SIZE = 4;

    // Location of each type of tile
    public static final int GRASS_LOCATION = 0;
    public static final int COBBLE_LOCATION = 1;
    public static final int UNKNOWN_TEXTURE_LOCATION =
            (TEXTURE_CHART_SIZE * TEXTURE_CHART_SIZE) - 1;

    // The vertex and normal data
    private static final float[] VERTEX_AND_NORMAL_DATA =
    {
        // X Y Z
        0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, CELL_SIZE,
        CELL_SIZE, 0.0f, CELL_SIZE,
        0.0f, 0.0f, 0.0f,
        CELL_SIZE, 0.0f, CELL_SIZE,
        CELL_SIZE, 0.0f, 0.0f,

        // nX nY nZ
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f
    };

    // The texel data (gets defined in the constructor depending on what tile type it is)
    private final float[] TEXEL_DATA;

    // Vertex/normal and texel arrays
    private VertexArray vertexAndNormalArray;
    private VertexArray texelArray;

    // The parent cell of this cell, used in A* path finding algorithm
    private Cell parent = null;

    // The g-cost of this cell (distance from the start cell)
    private float gCost = 0.0f;

    // The f-cost of this cell (g-cost + heuristic cost to the goal cell)
    private float fCost = 0.0f;

    // The type of cell
    private int type = CellType.UNDEFINED;

    // X position in the cell array
    private int positionX = 0;

    // Z position in the cell array
    private int positionZ = 0;

    // Construct the cell
    public Cell(int type, int positionX, int positionZ)
    {
        setType(type);
        setPosition(positionX, positionZ);

        TEXEL_DATA = establishTexelCoordinates();

        vertexAndNormalArray = new VertexArray(VERTEX_AND_NORMAL_DATA);
        texelArray = new VertexArray(TEXEL_DATA);
    }

    // To optimise the rendering, only one texture file is bound before rendering the cells in the
    // map. For this reason, each cell has different texture co-ordinates that point to there
    // textures within that texture file.
    private float[] establishTexelCoordinates()
    {
        final int X;
        final int Y;

        switch (type)
        {
            case GRASS:
                X = GRASS_LOCATION % TEXTURE_CHART_SIZE;
                Y = GRASS_LOCATION / TEXTURE_CHART_SIZE;
                break;
            case COBBLE:
                X = COBBLE_LOCATION % TEXTURE_CHART_SIZE;
                Y = COBBLE_LOCATION / TEXTURE_CHART_SIZE;
                break;
            default:
                X = UNKNOWN_TEXTURE_LOCATION % TEXTURE_CHART_SIZE;
                Y = UNKNOWN_TEXTURE_LOCATION / TEXTURE_CHART_SIZE;
                break;
        }

        final float START_S = X / (float)TEXTURE_CHART_SIZE;
        final float END_S = (X + 1) / (float)TEXTURE_CHART_SIZE;
        final float START_T = Y / (float)TEXTURE_CHART_SIZE;
        final float END_T = (Y + 1) / (float)TEXTURE_CHART_SIZE;

        final float[] TEXELS =
        {
                START_S, START_T,
                START_S, END_T,
                END_S, END_T,
                START_S, START_T,
                END_S, END_T,
                END_S, START_T
        };

        return TEXELS;
    }

    // Bind the cell rendering data
    public void bindData(ShaderProgram shader)
    {
        vertexAndNormalArray.setVertexAttribPointer(0, shader.getPositionAttributeLocation(),
                FLOATS_PER_VERTEX, 0);

        if(shader.getTextureCoordinatesAttributeLocation() != -1)
        {
            texelArray.setVertexAttribPointer(0, shader.getTextureCoordinatesAttributeLocation(),
                    FLOATS_PER_TEXEL, 0);
        }

        if(shader.getNormalAttributeLocation() != -1)
        {
            vertexAndNormalArray.setVertexAttribPointer((FLOATS_PER_VERTEX * VERTEX_COUNT), shader.getNormalAttributeLocation(),
                    FLOATS_PER_NORMAL, 0);
        }
    }

    // Draw the cell rendering data
    public void draw()
    {
        glDrawArrays(GL_TRIANGLES, 0, VERTEX_COUNT);
    }

    // Set the parent cell
    public void setParent(Cell parent)
    {
        this.parent = parent;
    }

    // Get the parent cell
    public Cell getParent()
    {
        return parent;
    }

    // Set the g-cost
    public void setGCost(float gCost)
    {
        this.gCost = gCost;
    }

    // Get the g-cost
    public float getGCost()
    {
        return gCost;
    }

    // Set the f-cost
    public void setFCost(float fCost)
    {
        this.fCost = fCost;
    }

    // Get the f-cost
    public float getFCost()
    {
        return fCost;
    }

    // Get the cell type
    public int getType()
    {
        return type;
    }

    // Set the cell type
    public void setType(int type)
    {
        this.type = type;
    }

    // Check if the cell is a collidable type
    public boolean isCollidable()
    {
        // Switch through the different cell types that have collision properties
        switch (type)
        {
            case WALL:
            case BRICK_WALL:
                return true;
        }

        return false;
    }

    // Set both the x and z
    public void setPosition(int x, int z)
    {
        setPositionX(x);
        setPositionZ(z);
    }

    // Get x position
    public int getPositionX()
    {
        return positionX;
    }

    // Set x position
    public void setPositionX(int positionX)
    {
        this.positionX = positionX;
    }

    // Get z position
    public int getPositionZ()
    {
        return positionZ;
    }

    // Set z position
    public void setPositionZ(int positionZ)
    {
        this.positionZ = positionZ;
    }
}