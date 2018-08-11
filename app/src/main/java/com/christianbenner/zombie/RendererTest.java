package com.christianbenner.zombie;

import android.content.Context;
import android.view.View;

import com.christianbenner.crispinandroid.programs.TextureShaderProgram;
import com.christianbenner.crispinandroid.ui.GLFont;
import com.christianbenner.crispinandroid.ui.GLText2;
import com.christianbenner.crispinandroid.ui.Pointer;
import com.christianbenner.crispinandroid.util.Scene;
import com.christianbenner.crispinandroid.util.UIRenderer;
import com.christianbenner.crispinandroid.util.UIRendererGroup;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.glClear;

public class RendererTest extends Scene {
    private GLText2 text;
    private UIRenderer renderer;
    private UIRendererGroup group;
    private TextureShaderProgram uiShader;

    public RendererTest(Context context)
    {
        super(context);
        renderer = new UIRenderer(context, R.drawable.arial_font, R.raw.arial_font_fnt);
        group = new UIRendererGroup(renderer, true);
    }

    @Override
    protected void surfaceCreated() {

    }

    @Override
    public void surfaceChanged(int width, int height) {
        renderer = new UIRenderer(context, R.drawable.arial_font, R.raw.arial_font_fnt);
        renderer.createUICanvas(width, height);
        uiShader = new TextureShaderProgram(context);
        renderer.setShader(uiShader);

        text = new GLText2(context, "Test of the new text system does it keep working though who knows testing it for the last time what is going on", 2, new GLFont(context, R.drawable.arial_font, R.raw.arial_font_fnt), 1920f, width, height, true);
      //  text.setPosition(100.0f, -20.0f);
        renderer.addUI(text);
     //   colourShaderProgram = new TextureShaderProgram(context);

    }

    @Override
    public void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//        colourShaderProgram.useProgram();

        renderer.render();

      //  text.bindData(colourShaderProgram);
      //  text.setUniforms(colourShaderProgram);
      //  text.draw();
    }

    @Override
    public void update(float deltaTime) {

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
    public void motion(View view, Pointer pointer, PointerMotionEvent pointerMotionEvent) {

    }
}
