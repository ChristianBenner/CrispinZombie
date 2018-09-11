package com.christianbenner.zombie.Map;

import android.content.Context;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.render.model.RendererModel;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.render.data.Light;
import com.christianbenner.crispinandroid.render.util.Renderer;
import com.christianbenner.crispinandroid.render.util.RendererGroup;
import com.christianbenner.crispinandroid.render.data.RendererGroupType;
import com.christianbenner.crispinandroid.render.util.TextureHelper;
import com.christianbenner.zombie.Entities.Weapon;
import com.christianbenner.zombie.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import static com.christianbenner.zombie.Map.CellType.COBBLE;
import static com.christianbenner.zombie.Map.CellType.GRASS;
import static com.christianbenner.zombie.Map.CellType.WALL;

/**
 *  The Map class provides the ability to load maps from resource ID and find paths from A to B
 *
 *  @author     Christian Benner
 *  @version    1.0
 *  @since      2018-07-14
 */
public class Map
{
    // Context of the application
    private Context context = null;

    // Array of cells
    private Cell[][] cells = null;

    // Array of models that correspond to the cell array
    private RendererModel[][] models = null;

    // Groups of rendering objects with the same bind and same texture (key = type)
    private HashMap<Integer, RendererGroup> renderGroups = new HashMap<>();

    // Width of the map
    private int mapWidth = 0;

    // Height of the map
    private int mapHeight = 0;

    // State of the map (loaded or not)
    private boolean loaded = false;

    // The state of the render groups
    private boolean renderGroupsExist = false;

    // These characters represent that the currently processed character is not valid data (readMap)
    private final int TAB_CHAR = 9;
    private final int LF_CHAR = 10;
    private final int CR_CHAR = 13;
    private final int EOF_CHAR = 26;
    private final int FULL_STOP_CHAR = 46;
    private final int CURLY_BRACE_START = 123;
    private final int CURLY_BRACE_END = 125;
    private final int SLASH = 47;
    private final int SEMI_COLON = 59;
    private final int OPEN_BRACKET = 40;
    private final int CLOSE_BRACKET = 41;
    private final int COMMA = 44;
    private final int WEAPON_DATA_ELEMENTS = 3;
    private final int LIGHT_DATA_ELEMENTS = 6;

    // The size of each tile in the map (width and height because tiles are square)
    private final float TILE_SIZE = 0.5f;

    // The position of the first model in the map
    private final Geometry.Point MAP_START_POSITION = new Geometry.Point(0.0f, 0.0f, 0.0f);

    // The group of render models
    private RendererGroup rendererGroup = null;

    // The set of open nodes in the A* path-finding algorithm
    private ArrayList<Cell> closedSet = new ArrayList<>();

    // The set of closed nodes in the A* path-finding algorithm
    private ArrayList<Cell> openSet = new ArrayList<>();

    // Weapons loaded in from mapdata
    private ArrayList<Weapon> weapons = new ArrayList<>();

    // Construct the map object
    public Map(Context context, int resourceId)
    {
        // Context of the android application
        this.context = context;

        try
        {
            // Read the map from file and populate the cell data array
            readMap(resourceId);

            // Set the state of loaded to true
            loaded = true;
        }
        catch(Exception e)
        {
            System.err.println("Failed to load the map");
            e.printStackTrace();
        }
    }

    // Get the map width
    public int getMapWidth()
    {
        return mapWidth;
    }

    // Get the map height
    public int getMapHeight()
    {
        return mapHeight;
    }

    // Get the position of a model in the model array
    public Geometry.Point getModelPosition(Cell cell)
    {
        return models[cell.getPositionZ()][cell.getPositionX()].getPosition();
    }

    // Set the renderer of the map
    public boolean addToRenderer(Renderer renderer)
    {
        // If the map isn't loaded, don't set the renderer
        if(!loaded)
        {
            return loaded;
        }

        // Add all the same tiles to the same render groups (speeds up rendering)
        if(!renderGroupsExist) {
            // The models have not been loaded in before, instantiate the array
            models = new RendererModel[mapHeight][mapWidth];

            for (int z = 0; z < mapHeight; z++) {
                for (int x = 0; x < mapWidth; x++) {
                    // The type of the tile we are adding
                    int tileType = cells[z][x].getType();

                    // The map doesn't contain any tiles of the type we are adding so create a group
                    if (!renderGroups.containsKey(tileType)) {
                        // Add a new group that is SAME_BIND_SAME_TEX because the we are adding
                        // all the same tile models and textures per group
                        renderGroups.put(tileType,
                                new RendererGroup(RendererGroupType.SAME_BIND_SAME_TEX));
                    }

                    // First we need to create the models by loading in there model files and
                    // textures
                    // It would be nice if we could have a file associated to each tile that has
                    // the texture path and the model path
                    int objResourceId;
                    int texResourceId;
                    Geometry.Vector positionOffset = new Geometry.Vector(0.0f, 0.0f, 0.0f);
                    Geometry.Point scale = new Geometry.Point(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f, TILE_SIZE / 2.0f);
                    switch(cells[z][x].getType())
                    {
                        case GRASS:
                            objResourceId = R.raw.tile;
                            texResourceId = R.drawable.grass_tile;
                            break;
                        case COBBLE:
                            objResourceId = R.raw.tile;
                            texResourceId = R.drawable.brick;
                            break;
                        case WALL:
                            objResourceId = R.raw.box;
                            texResourceId = R.drawable.box;
                            positionOffset = new Geometry.Vector(TILE_SIZE / 2.0f,
                                    TILE_SIZE, -TILE_SIZE / 2.0f);
                            scale.y = TILE_SIZE;
                            break;
                        default:
                            // Any undefined models will be loaded as a tile with error texture
                            System.err.println("Unrecognized tile type! Missing code in Map.java");
                            objResourceId = R.raw.tile;
                            texResourceId = R.drawable.unknown_texture;
                            break;
                    }

                    // Create the model object and set obj/tex
                    models[z][x] = new RendererModel(context, objResourceId,
                            TextureHelper.loadTexture(context,
                                    texResourceId));

                    // Set the position of the model
                    models[z][x].setPosition(MAP_START_POSITION.
                            translate(new Geometry.Vector(x * TILE_SIZE, 0.0f, z * TILE_SIZE)).
                            translate(positionOffset));
                    models[z][x].setScale(scale.x, scale.y, scale.z);

                    // Add the model to the group
                    renderGroups.get(tileType).addModel(models[z][x]);
                }
            }

            // The render groups have now been created
            renderGroupsExist = true;
        }

        // Needs improvement
        RendererGroup rWeapons = new RendererGroup(RendererGroupType.SAME_BIND);
        for(Weapon weapon : weapons)
        {
            rWeapons.addModel(weapon.getModel());
        }
        renderer.addGroup(rWeapons);

        // Add all the renderer groups to the renderer
        for(RendererGroup group : renderGroups.values())
        {
            renderer.addGroup(group);
        }

        // Return true because we have successfully set the renderer
        return true;
    }

    // Print what the cell data represents
    @Deprecated
    public void printMap()
    {
        System.out.println("Weapons {");
        for(int i = 0; i < weapons.size(); i++)
        {
            System.out.println("\tProcessed: " + weapons.get(i));
        }
        System.out.println("}");

        System.out.println("Lights {");
        for(int i = 0; i < lights.size(); i++)
        {
            System.out.println("\tProcessed: " + lights.get(i));
        }
        System.out.println("}");

        for(int z = 0; z < getMapHeight(); z++)
        {
            for(int x = 0; x < getMapWidth(); x++)
            {
                System.out.print((char)cells[z][x].getType());

                if(x != getMapWidth() - 1)
                {
                    System.out.print(".");
                }
            }
            System.out.println();
        }
    }

    // Identify what type of data is being read into the map
    private enum DataTag
    {
        WEAPONS,
        LIGHTS,
        ZOMBIESPAWNS,
        PLAYERSPAWNS,
        DOORS,
        GRASS,
        MAPDATA,
        UNDEFINED
    }

    /* This function breaks down a data entity into data.
    For example the string dataTagEntity could be 'DOOR(0.0,1.0,2.0,1,2500);', the function
    will put the floats into the float array, ints into the int array and then return the
    string 'DOOR'

    returns String 'dataType'
     */
    private String processDataTagEntity(String dataTagEntity, ArrayList<Float> floats, ArrayList<Integer> ints)
    {
        // This is the type of data e.g. PISTOL
        String dataType = "";

        // Lets us know when the data is ready to process
        boolean foundData = false;

        // Is the data a float or int type
        boolean floatData = false;

        // Each data being processed gets processed in here
        String data = "";

        // Look through the data one character at a time
        for(int n = 0; n < dataTagEntity.length(); n++)
        {
            if(foundData)
            {
                switch (dataTagEntity.charAt(n))
                {
                    case CLOSE_BRACKET:
                    case COMMA:
                        // Parse the now finished string to data types
                        if(floatData)
                        {
                            floats.add(Float.parseFloat(data));
                        }
                        else
                        {
                            ints.add(Integer.parseInt(data));
                        }

                        // Reset data and states
                        floatData = false;
                        data = "";
                        break;
                    case FULL_STOP_CHAR:
                        // This type of data is a float
                        floatData = true;
                    default:
                        data += dataTagEntity.charAt(n);
                        break;
                }
            }

            // Reached the data
            if(dataTagEntity.charAt(n) == OPEN_BRACKET)
            {
                dataType = dataTagEntity.substring(0, n);
                foundData = true;
            }
        }

        return dataType;
    }

    // Takes a string full of data entities from under a data tag and seperates them
    private ArrayList<String> seperateDataEntities(String data)
    {
        // Array of data entities
        ArrayList<String> dataEntities = new ArrayList<>();

        // The start position of each data tag entity in reference to the data string
        int indexOfDataTagEntity = 0;

        for(int i = 0; i < data.length(); i++)
        {
            if(data.charAt(i) == SEMI_COLON)
            {
                // Cut out the individual data tag entities from the data
                dataEntities.add(data.substring(indexOfDataTagEntity, i));

                // The next data tag starts one character after this one
                indexOfDataTagEntity = i + 1;
            }
        }

        return dataEntities;
    }

    // Processes the weapon data in the given string
    private void processWeaponData(String data)
    {
        weapons.clear();

        ArrayList<String> dataEntities = seperateDataEntities(data);
        for(String s : dataEntities)
        {
            // Fetch the data from the entity
            ArrayList<Float> floats = new ArrayList<>();
            ArrayList<Integer> ints = new ArrayList<>();
            String type = processDataTagEntity(s, floats, ints);

            // Expecting at least 3 floats from the processing, so check
            if(floats.size() == WEAPON_DATA_ELEMENTS)
            {
                // We have processed the data for a weapon
                weapons.add(new Weapon(context, Weapon.WeaponType.valueOf(type),
                        new Geometry.Point(floats.get(0), floats.get(1), floats.get(2))));
            }
            else
            {
                System.err.println("Error processing weapon data: " + s);
            }
        }
    }

    private ArrayList<Light> lights = new ArrayList<>();
    private void processLightData(String data)
    {
        System.out.println("LIGHT DATA: " + data);

        lights.clear();

        ArrayList<String> dataEntities = seperateDataEntities(data);
        for(String s : dataEntities)
        {
            // Fetch the data from the entity
            ArrayList<Float> floats = new ArrayList<>();
            ArrayList<Integer> ints = new ArrayList<>();
            String type = processDataTagEntity(s, floats, ints);

            // Expecting at least 3 floats from the processing, so check
            if(floats.size() == LIGHT_DATA_ELEMENTS)
            {
                // We have processed the data for a weapon
                lights.add(new Light(Light.LightType.valueOf(type),
                        new Colour(floats.get(0), floats.get(1), floats.get(2)),
                        new Geometry.Point(floats.get(3), floats.get(4), floats.get(5))));
            }
            else
            {
                System.err.println("Error processing weapon data: " + s);
            }
        }
    }

    private void processZombieSpawnData(String data)
    {
        System.out.println("ZOMBIE DATA: " + data);
    }

    private void processPlayerSpawnData(String data)
    {
        System.out.println("PLAYER DATA: " + data);
    }

    private void processDoorData(String data)
    {
        System.out.println("DOOR DATA: " + data);
    }

    private void processGrassData(String data)
    {
        System.out.println("GRASS DATA: " + data);
    }

    private void processMapData(String data)
    {
        System.out.println("MAP DATA: " + data);

        // Finished reading map from file
        // Calculate the map height
        mapHeight = data.length() / mapWidth;

        // Create the cell array
        cells = new Cell[mapHeight][mapWidth];

        // Move into the cell array
        for(int i = 0; i < data.length(); i++)
        {
            int locationX = i % mapWidth;
            int locationZ = i / mapWidth;
            cells[locationZ][locationX] =
                    new Cell((int)data.charAt(i), locationX, locationZ);
        }
    }

    // Read a map file, create and populate the cell data array
    private void readMap(int resourceId)
    {
        BufferedReader reader = new BufferedReader
                (new InputStreamReader(context.getResources().openRawResource(resourceId)));
        String data = "";
        int readValue = 0;
        mapWidth = 0;
        mapHeight = 0;
        boolean comment = false;
        boolean firstSlash = false;

        DataTag tag = DataTag.UNDEFINED;

        try
        {
            // Check if the MAPDATA tag has been met
            while((readValue = reader.read()) != -1)
            {
                switch (readValue)
                {
                    case TAB_CHAR:
                    case LF_CHAR:
                    case CR_CHAR:
                    case EOF_CHAR:
                        // Completed reading the line
                        if(tag == DataTag.MAPDATA && mapWidth == 0)
                        {
                            mapWidth = data.length();
                        }
                        firstSlash = false;
                        comment = false;
                        break;
                    case SLASH:
                        if(firstSlash)
                        {
                            comment = true;
                        }
                        firstSlash = true;
                        break;
                    case CURLY_BRACE_START:
                        tag = DataTag.valueOf(data);
                        data = "";
                        break;
                    case CURLY_BRACE_END:
                        // Data now contains all of the TAG data
                        switch(tag)
                        {
                            case WEAPONS:
                                processWeaponData(data);
                                break;
                            case LIGHTS:
                                processLightData(data);
                                break;
                            case ZOMBIESPAWNS:
                                processZombieSpawnData(data);
                                break;
                            case PLAYERSPAWNS:
                                processPlayerSpawnData(data);
                                break;
                            case DOORS:
                                processDoorData(data);
                                break;
                            case GRASS:
                                processGrassData(data);
                                break;
                            case MAPDATA:
                                processMapData(data);
                                break;
                            default:
                                System.err.println("Unrecognized data, throwing away");
                                break;
                        }

                        data = "";
                        tag = DataTag.UNDEFINED;
                        break;
                    case FULL_STOP_CHAR:
                        if(tag != DataTag.MAPDATA)
                        {
                            data += (char)readValue;
                        }
                        break;
                    default:
                        if(!comment)
                        {
                            data += (char)readValue;
                        }
                        break;
                }
            }
        }
        catch(Exception e)
        {
            System.err.println("Error reading map");
            e.printStackTrace();
        }
    }

    // Use A Star path-finding algorithm to find the shortest path between two specified cells
    public LinkedList<Cell> findShortestPath(int startX, int startZ, int goalX, int goalZ)
    {
        // If the zombie or player is not on a valid node, don't bother
        if(startX < 0 || startX >= mapWidth ||
                startZ < 0 || startZ >= mapHeight ||
                goalX < 0 || goalX >= mapWidth ||
                goalZ < 0 || goalZ >= mapHeight ||
                cells[startZ][startX].isCollidable() ||
                cells[goalZ][goalX].isCollidable())
        {
            return null;
        }

        // Clear the closed and open set for the next path calculation
        closedSet.clear();
        openSet.clear();

        // Clear the cells parent nodes
        for(int z = 0; z < mapHeight; z++)
        {
            for(int x = 0; x < mapWidth; x++)
            {
                cells[z][x].setParent(null);
            }
        }

        // Define the start and end nodes
        Cell startNode = cells[startZ][startX];
        Cell goalNode = cells[goalZ][goalX];

        // Add the start node to the open list because it is our starting point
        openSet.add(startNode);

        // The distance from start to start is 0
        startNode.setGCost(0.0f);

        // The f cost is gCost + hCost but there is no gCost so fCost = heuristic in this case
        startNode.setFCost(heuristicCostEstimate(startNode, goalNode));

        // Iterate through the open set (cells that need processing)
        while(openSet.isEmpty() == false)
        {
            //Set the lowest fCost cell in the open list as the current cell
            Cell current = lowestFCost();

            // Check if the current cell is the goal cell
            if(current != null)
            {
                if(current == goalNode)
                {
                    // Reconstruct a path from the goal node to the start node
                    return reconstructPath(current);
                }
            }

            // Add the current cell to the open set (and remove from the closed set)
            openSet.remove(current);
            closedSet.add(current);

            // Check the current nodes neighbouring cells
            for(Cell neighbour : getNeighbours(current))
            {
                // The neighbour has already been processed, no need to process again
                if(closedSet.contains(neighbour))
                {
                    continue;
                }

                float tentativeGScore = current.getGCost() + euclideanDistance(current, neighbour);

                if(openSet.contains(neighbour) == false)
                {
                    openSet.add(neighbour);
                }
                else if(tentativeGScore >= neighbour.getGCost())
                {
                    continue;
                }

                // The path is the best until now so record it
                neighbour.setParent(current);
                neighbour.setGCost(tentativeGScore);
                neighbour.setFCost(neighbour.getGCost() + heuristicCostEstimate(neighbour, goalNode));
            }
        }

        return null;
    }

    // Reconstruct the path from the goal node
    private LinkedList<Cell> reconstructPath(Cell current)
    {
        // Array of cells that lead from the start to the goal node
        LinkedList<Cell> path = new LinkedList<>();

        // If the node is valid, add it and set the current node to it's parent node
        while(current != null)
        {
            path.add(current);
            current = current.getParent();
        }

        return path;
    }

    // Find all the neighbours around a specified cell, returns a list of cells
    private LinkedList<Cell> getNeighbours(Cell cell)
    {
        // List of neighbouring cells
        LinkedList<Cell> neighbours = new LinkedList<>();

        int x = cell.getPositionX(); int z = cell.getPositionZ();

        if(isValidIndex(x, z-1)) neighbours.add(cells[z-1][x]);     // Above
        if(isValidIndex(x+1, z-1)) neighbours.add(cells[z-1][x+1]); // Top right
        if(isValidIndex(x+1, z)) neighbours.add(cells[z][x+1]);     // Right
        if(isValidIndex(x+1, z+1)) neighbours.add(cells[z+1][x+1]); // Bottom Right
        if(isValidIndex(x, z+1)) neighbours.add(cells[z+1][x]);     // Bottom
        if(isValidIndex(x-1, z+1)) neighbours.add(cells[z+1][x-1]); // Bottom left
        if(isValidIndex(x-1, z)) neighbours.add(cells[z][x-1]);     // Left
        if(isValidIndex(x-1, z-1)) neighbours.add(cells[z-1][x-1]); // Top left

        return neighbours;
    }

    // Check if the co-ordinates are in the map boundaries (in array range) and the cell is not
    // a collidable type
    private boolean isValidIndex(int x, int z)
    {
        return ((x >= 0 && x < mapWidth && z >= 0 && z < mapHeight) && !cells[z][x].isCollidable());
    }

    // Heuristic cost estimate is the X distance + Y distance from one node to the goal node
    private int heuristicCostEstimate(Cell current, Cell goal)
    {
        return Math.abs(current.getPositionX() - goal.getPositionX()) +
                Math.abs(current.getPositionZ() - goal.getPositionZ());
    }

    // Euclidean distance is the exact distance from one cell to the other (pythagoras theorem)
    private float euclideanDistance(Cell current, Cell goal)
    {
        final int x = goal.getPositionX() - current.getPositionX();
        final int z = goal.getPositionZ() - current.getPositionZ();
        return (float)Math.sqrt((x * x) + (z * z));
    }

    // Find the lowest fCost cell in the open set
    private Cell lowestFCost()
    {
        int lowestIndex = -1;
        float lowestFCost = -1;
        for(int i = 0; i < openSet.size(); i++)
        {
            if(lowestIndex == -1)
            {
                lowestIndex = i;
                lowestFCost = openSet.get(i).getFCost();
            }
            else if(openSet.get(i).getFCost() < lowestFCost)
            {
                lowestIndex = i;
                lowestFCost = openSet.get(i).getFCost();
            }
        }
        return lowestIndex != -1 ? openSet.get(lowestIndex) : null;
    }
}