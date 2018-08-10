package com.christianbenner.zombie.Objects;

import com.christianbenner.crispinandroid.data.Texture;
import com.christianbenner.crispinandroid.data.objects.RendererModel;

/**
 * Created by Christian Benner on 19/05/2018.
 */

public class Weapon {
    private RendererModel model;
    private Texture uiImage;
    private int maxAmmo;
    private int currentAmmo;

    // If state is drop, rotate on the floor
    private enum state {
        DROP,
        EQUIPPED
    }

}
