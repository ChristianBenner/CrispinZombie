package com.christianbenner.zombie.AStarTesting;

import static com.christianbenner.zombie.AStarTesting.AStarDemo.CELL_TYPE.WALL;

/**
 * Created by Christian Benner on 19/06/2018.
 */

public class LegacyCell {
        public LegacyCell()
        {

        }

        public void setCosts(int hCost, int gCost)
        {
            this.hCost = hCost;
            this.gCost = gCost;
            this.fCost = (hCost + gCost);
        }

        public boolean isCollidable()
        {
                if(type == WALL)
                {
                        return true;
                }

                return false;
        }

        public int hCost = 0;
        public float gCost = 0;
        public float fCost = 0;
        public int posx;
        public int posz;
        public AStarDemo.CELL_TYPE type = AStarDemo.CELL_TYPE.GRASS;

        public LegacyCell previous = null;
}
