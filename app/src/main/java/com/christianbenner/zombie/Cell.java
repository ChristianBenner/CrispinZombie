package com.christianbenner.zombie;

import static com.christianbenner.zombie.CellType.WALL;

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