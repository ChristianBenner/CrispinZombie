package com.christianbenner.zombie.AStarTesting;

import android.content.Context;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import com.christianbenner.crispinandroid.data.Colour;
import com.christianbenner.crispinandroid.data.objects.RendererModel;
import com.christianbenner.crispinandroid.programs.PerFragMultiLightingShader;
import com.christianbenner.crispinandroid.ui.Pointer;
import com.christianbenner.crispinandroid.util.Camera;
import com.christianbenner.crispinandroid.util.Geometry;
import com.christianbenner.crispinandroid.util.Renderer;
import com.christianbenner.crispinandroid.util.Scene;
import com.christianbenner.crispinandroid.util.ShaderProgram;
import com.christianbenner.crispinandroid.util.TextureHelper;
import com.christianbenner.zombie.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import static android.opengl.GLES20.GL_ALPHA;
import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static com.christianbenner.zombie.AStarTesting.AStarDemo.CELL_TYPE.WALL;

/**
 * Created by Christian Benner on 10/06/2018.
 */

public class AStarDemo extends Scene {
    enum CELL_TYPE
    {
        GRASS,
        WALL
    }

    private final int MAP_WIDTH = 20;
    private final int MAP_HEIGHT = 20;
    private LegacyCell[][] cells;
    private RendererModel[][] models;

    private Camera camera;
    private Renderer renderer;
    private ShaderProgram shader;
    private AStarPlayer player;

    private final Geometry.Point CAMERA_START_POSITION =
            new Geometry.Point(9.0f, 25.0f, 9.0f);

    private int startX = 18;
    private int startZ = 0;
    private int targetX = 5;
    private int targetZ = 5;
    public final float TILE_SIZE = 1.0f;

    public AStarDemo(Context context)
    {
        super(context);

        camera = new Camera();
        camera.setAngles(3.141592f, -(3.141592f/2.0f));
        camera.setPosition(CAMERA_START_POSITION);

        shader = new PerFragMultiLightingShader(context);
        renderer = new Renderer(shader, camera);

        // Initiates map
        setupCells();

        // Set some cells to be walls
        cells[3][3].type = WALL;
        cells[3][4].type = WALL;
        cells[4][2].type = WALL;
        cells[4][3].type = WALL;
        cells[4][4].type = WALL;
        cells[4][5].type = WALL;
        cells[4][6].type = WALL;
        cells[4][7].type = WALL;
        cells[5][3].type = WALL;
        cells[5][4].type = WALL;
        cells[6][3].type = WALL;
        cells[7][3].type = WALL;

        cells[9][5].type = WALL;
        cells[9][6].type = WALL;
        cells[9][7].type = WALL;
        cells[9][8].type = WALL;
        cells[9][9].type = WALL;
        cells[9][10].type = WALL;
        cells[9][11].type = WALL;
        cells[9][12].type = WALL;
        cells[10][5].type = WALL;
        cells[11][5].type = WALL;
        cells[12][5].type = WALL;
        cells[13][5].type = WALL;
        cells[14][5].type = WALL;
        cells[15][5].type = WALL;
        cells[16][5].type = WALL;
        cells[17][5].type = WALL;
        cells[12][12].type = WALL;
        cells[13][12].type = WALL;
        cells[14][12].type = WALL;
        cells[15][12].type = WALL;

        // Loads the models associated with each cell in the map
        loadCellModels();

        /*int genX = generator.nextInt(MAP_WIDTH);
        int genZ = generator.nextInt(MAP_HEIGHT);
        boolean valid = false;
        while(!valid)
        {
            if(cells[genZ][genX].isCollidable() == false)
            {
                valid = true;
            }
        }

        startX = genX;
        startZ = genZ;*/

        // Create a player
        player = new AStarPlayer(TILE_SIZE, startX, startZ, renderer, context);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //  NAME:           setupCells
    //  BRIEF:          Setup map with cells
    //  DESCRIPTION:    Initializes each cell in the map and sets position
    //  PARAM[in]:      N/A
    //  RETURNS:        N/A
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void setupCells()
    {
        cells = new LegacyCell[MAP_HEIGHT][MAP_WIDTH];
        for(int z = 0; z < MAP_HEIGHT; z++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                cells[z][x] = new LegacyCell();
                cells[z][x].posx = x;
                cells[z][x].posz = z;
            }
        }
    }

    private void loadCellModels()
    {
        models = new RendererModel[MAP_HEIGHT][MAP_WIDTH];

        for(int z = 0; z < MAP_HEIGHT; z++)
        {
            for(int x = 0; x < MAP_WIDTH; x++)
            {
                int modelToLoad = R.raw.tile;
                int textureToLoad = R.drawable.grass_tile;
                Geometry.Point positionOffset = new Geometry.Point(0.0f, 0.0f, 0.0f);

                switch(cells[z][x].type)
                {
                    case WALL:
                        modelToLoad = R.raw.box;
                        textureToLoad = R.drawable.box;
                        positionOffset = new Geometry.Point(0.5f, 0.5f, -0.5f);
                        break;
                }

                RendererModel model = new RendererModel(context, modelToLoad,
                        TextureHelper.loadTexture(context, textureToLoad));
                model.setPosition(new Geometry.Point((x*TILE_SIZE) + positionOffset.x, positionOffset.y,
                        (z * TILE_SIZE) + positionOffset.z));
                model.setScale(TILE_SIZE / 2.0f);

                renderer.addModel(model);
                models[z][x] = model;
            }
        }
    }

    private ArrayList<LegacyCell> closedSet = new ArrayList<>();
    private ArrayList<LegacyCell> openSet = new ArrayList<>();

    private CellBinaryHeap openSet2 = new CellBinaryHeap();

     //GOOD FOR TIMING FUNCTIONS
    long sum = 0;
    int instances = 0;

    /*
    if(instances > 20)
    {
        System.out.println("Average Time Elapsed: " + ((float)sum / instances));
    }
        else
    {
        long timeBefore = SystemClock.elapsedRealtimeNanos();
        // TIME FUNCTION GOES HERE
        long timeElapsed = SystemClock.elapsedRealtimeNanos() - timeBefore;
        sum += timeElapsed;
        instances++;
    }*/

    private LinkedList<LegacyCell> aStarVersion3(int startX, int startZ, int goalX, int goalZ)
    {
        // Clear the closed and open set for the next path calculation
        closedSet.clear();
        openSet2 = new CellBinaryHeap();

        // Define the start and end nodes
        LegacyCell startNode = cells[startZ][startX];
        LegacyCell goalNode = cells[goalZ][goalX];

        // Add the start node to the open list because it is our starting point
        openSet2.add(startNode);

        // The distance from start to start is 0
        startNode.gCost = 0;

        // The f cost is gCost + hCost but there is no gCost so fCost = heuristic in this case
        startNode.fCost = heuristicCostEstimate(startNode, goalNode);

        // Iterate through the open set (cells that need processing)
        while(openSet2.isEmpty() == false)
        {
            //Set the lowest fCost cell in the open list as the current cell
            LegacyCell current = openSet2.min();

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
            System.out.println("Removing: " + current.fCost);
            openSet2.remove(current);
            closedSet.add(current);

            // Check the current nodes neighbouring cells
            for(LegacyCell neighbour : getNeighbours(current))
            {
                // The neighbour has already been processed, no need to process again
                if(closedSet.contains(neighbour))
                {
                    continue;
                }

                //                  TRY USING HEURISTIC COST HERE
                float tentativeGScore = current.gCost + euclideanDistance2(current, neighbour);

                if(openSet2.contains(neighbour) == false)
                {
                    openSet2.add(neighbour);
                }
                else if(tentativeGScore >= neighbour.gCost)
                {
                    continue;
                }

                // The path is the best until now so record it
                neighbour.previous = current;
                neighbour.gCost = tentativeGScore;
                neighbour.fCost = neighbour.gCost + heuristicCostEstimate(neighbour, goalNode);
            }
        }

        return null;
    }

    private LinkedList<LegacyCell> aStarVersion2(int startX, int startZ, int goalX, int goalZ)
    {
        // Clear the closed and open set for the next path calculation
        closedSet.clear();
        openSet.clear();

        // Define the start and end nodes
        LegacyCell startNode = cells[startZ][startX];
        LegacyCell goalNode = cells[goalZ][goalX];

        // Add the start node to the open list because it is our starting point
        openSet.add(startNode);

        // The distance from start to start is 0
        startNode.gCost = 0;

        // The f cost is gCost + hCost but there is no gCost so fCost = heuristic in this case
        startNode.fCost = heuristicCostEstimate(startNode, goalNode);

        // Iterate through the open set (cells that need processing)
        while(openSet.isEmpty() == false)
        {
            //Set the lowest fCost cell in the open list as the current cell
            LegacyCell current = lowestFCost();

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
            for(LegacyCell neighbour : getNeighbours(current))
            {
                // The neighbour has already been processed, no need to process again
                if(closedSet.contains(neighbour))
                {
                    continue;
                }

                //                  TRY USING HEURISTIC COST HERE
                float tentativeGScore = current.gCost + euclideanDistance2(current, neighbour);

                if(openSet.contains(neighbour) == false)
                {
                    openSet.add(neighbour);
                }
                else if(tentativeGScore >= neighbour.gCost)
                {
                    continue;
                }

                // The path is the best until now so record it
                neighbour.previous = current;
                neighbour.gCost = tentativeGScore;
                neighbour.fCost = neighbour.gCost + heuristicCostEstimate(neighbour, goalNode);
            }
        }

        return null;
    }

    private LinkedList<LegacyCell> reconstructPath(LegacyCell current)
    {
        LinkedList<LegacyCell> path = new LinkedList<>();

        while(current != null)
        {
            if(path.contains(current))
            {
                return path;
            }
            path.add(current);
            current = current.previous;
        }

        return path;
    }

    private LinkedList<LegacyCell> getNeighbours(LegacyCell cell)
    {
        LinkedList<LegacyCell> neighbours = new LinkedList<>();

        int posX = cell.posx;
        int posY = cell.posz;

        // Above
        if(isValidIndex(posX, posY-1)) neighbours.add(cells[posY-1][posX]);

        // Top right
        if(isValidIndex(posX+1, posY-1)) neighbours.add(cells[posY-1][posX+1]);

        // Right
        if(isValidIndex(posX+1, posY)) neighbours.add(cells[posY][posX+1]);

        // Bottom Right
        if(isValidIndex(posX+1, posY+1)) neighbours.add(cells[posY+1][posX+1]);

        // Bottom
        if(isValidIndex(posX, posY+1)) neighbours.add(cells[posY+1][posX]);

        // Bottom left
        if(isValidIndex(posX-1, posY+1)) neighbours.add(cells[posY+1][posX-1]);

        // Left
        if(isValidIndex(posX-1, posY)) neighbours.add(cells[posY][posX-1]);

        // Top left
        if(isValidIndex(posX-1, posY-1)) neighbours.add(cells[posY-1][posX-1]);

        return neighbours;
    }

    private boolean isValidIndex(int x, int z)
    {
        // Check if the co-ordinates are in the map boundaries (in array range)
        if(x >= 0 && x < MAP_WIDTH && z >= 0 && z < MAP_HEIGHT)
        {
            // If the cell is a collidable one (wall), don't include
            if(cells[z][x].isCollidable() == false)
            {
                return true;
            }
        }

        return false;
    }

    private int heuristicCostEstimate(LegacyCell current, LegacyCell goal)
    {
        return Math.abs(current.posx - goal.posx) + Math.abs(current.posz - goal.posz);
    }

    private int euclideanDistance(LegacyCell current, LegacyCell goal)
    {
        final int a = goal.posx - current.posx;
        final int b = goal.posz - current.posz;
        return (int)(Math.sqrt((a * a) + (b * b)) * 10.0f);
    }

    private float euclideanDistance2(LegacyCell current, LegacyCell goal)
    {
        final int a = goal.posx - current.posx;
        final int b = goal.posz - current.posz;
        return (float)Math.sqrt((a * a) + (b * b));
    }

    private LegacyCell lowestFCost()
    {
        LegacyCell lowest = null;
        for(LegacyCell cell : openSet)
        {
          //  System.out.println("FCOST: " + cell.fCost);
            if(lowest == null || cell.fCost < lowest.fCost)
            {
                lowest = cell;
            }
        }

        return lowest;
    }

    @Override
    protected void surfaceCreated() {
        TextureHelper.updateAll(context);
    }

    @Override
    public void surfaceChanged(int width, int height) {
        camera.viewChanged(width, height);
        shader = new PerFragMultiLightingShader(context);
        renderer.setShader(shader);
        renderer.setCamera(camera);
    }

    @Override
    public void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0.4f, 0.4f, 0.4f, 1.0f);
        glEnable(GL_ALPHA);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);

        renderer.render();

        glDisable(GL_DEPTH_TEST);
    }

    private float x = 2.0f;
    private float z = 2.0f;
    private boolean done = false;
    private LinkedList<LegacyCell> path = null;
    private int pathIndex = 1;
    private Random generator = new Random();
    private long time = 0;

    @Override
    public void update(float deltaTime) {

        // Colour path green

        // Find current tile from player
        int playerCellX = player.getCellX();
        int playerCellZ = player.getCellZ();

        if(SystemClock.uptimeMillis() - time > 1000) {
            time = SystemClock.uptimeMillis();
            done = false;

            System.out.println("Generating new path.");
        }

        if(!done) {
            int genX = generator.nextInt(MAP_WIDTH);
            int genZ = generator.nextInt(MAP_HEIGHT);

            boolean valid = false;
            while (!valid) {
                if (!(genX == playerCellX && genZ == playerCellZ) &&
                        !cells[genZ][genX].isCollidable()) {
                    valid = true;
                } else {
                    genX = generator.nextInt(MAP_WIDTH);
                    genZ = generator.nextInt(MAP_HEIGHT);
                }
            }

            targetX = genX;
            targetZ = genZ;

            genX = generator.nextInt(MAP_WIDTH);
            genZ = generator.nextInt(MAP_HEIGHT);

            valid = false;
            while (!valid) {
                if (!(genX == playerCellX && genZ == playerCellZ) &&
                        !cells[genZ][genX].isCollidable()) {
                    valid = true;
                } else {
                    genX = generator.nextInt(MAP_WIDTH);
                    genZ = generator.nextInt(MAP_HEIGHT);
                }
            }

          //  startX = genX;
          //  startZ = genZ;

            // Set all green
            for (int y = 0; y < MAP_HEIGHT; y++) {
                for (int x = 0; x < MAP_WIDTH; x++) {
                    models[y][x].setColour(new Colour(1.0f, 1.0f, 1.0f));
                }
            }

            path = aStarVersion2(startX, startZ, targetX, targetZ);
            done = true;

            // Set path colour
            if(path != null)
            {
                for (int i = path.size() - 1; i != 0; i--) {
                    models[path.get(i).posz][path.get(i).posx].setColour(new Colour(1.0f, 0.3f, 0.0f));
                }
            }

            models[startX][startZ].setColour(new Colour(1.0f, 0.5f, 1.0f));
            models[targetZ][targetX].setColour(new Colour(0.5f, 0.5f, 1.0f));

            //playSound(context, R.raw.ding, 0);
        }
    }

    @Override
    protected void pause() {

    }

    @Override
    protected void resume() {

    }

    @Override
    protected void restart() {

    }

    @Override
    protected void destroy() {

    }

    @Override
    public void motionEvent(View view, MotionEvent event) {

    }

    @Override
    public void motion(View view, Pointer pointer, PointerMotionEvent pointerMotionEvent) {

    }

}
