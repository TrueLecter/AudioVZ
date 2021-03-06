package truelecter.iig.screen;

import java.io.File;

import truelecter.iig.Main;
import truelecter.iig.util.ConfigHandler;
import truelecter.iig.util.Logger;
import truelecter.iig.util.Util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Loading implements Screen {

    private long toStay;
    private float stateTime;
    private Animation loaderAnimation;
    private boolean loaded;
    private SpriteBatch batch;
    private String name;
    private File f;

    private void initAnimation() {
        Texture loadingAnimationTexture = new Texture(Gdx.files.internal("loading.gif"));
        TextureRegion[][] sprites = TextureRegion.split(loadingAnimationTexture, 128, 128);
        loaderAnimation = new Animation(0.025f, sprites[0]);
    }

    public void startThread(final File f) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                if (f != null)
                    name = Util.getMP3InfoForFile(f);
                else
                    name = null;
                loaded = true;
            }
        });
        t.start();
    }

    public Loading(long toStay, File f, String name) {
        initAnimation();
        batch = new SpriteBatch();
        Logger.i("Loading screen for file '" + f.getAbsolutePath() + "' started! Label '" + name + "', waiting "
                + this.toStay);
        Logger.i("BEFORE: " + FileManager.lastFilePath);
        FileManager.lastFilePath = f.getAbsolutePath();
        Logger.i("AFTER: " + FileManager.lastFilePath);
        this.toStay = toStay;
        loaded = false;
        if (name == null) {
            startThread(f);
        } else {
            loaded = true;
        }
        this.name = name;
        this.f = f;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        toStay -= delta * 1000;
        if (!loaded || toStay > 0) {
            batch.begin();
            stateTime += delta;
            TextureRegion currentFrame = loaderAnimation.getKeyFrame(stateTime, true);
            batch.draw(currentFrame, ConfigHandler.width - currentFrame.getRegionWidth() - 25, 25);
            batch.end();
        } else {
            Logger.i("Loaded! ");
            Main.getInstance().setScreen(new AudioSpectrum(f, name));
        }
    }

    @Override
    public void resize(int width, int height) {
        ConfigHandler.width = width;
        ConfigHandler.height = height;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        System.gc();
        batch.dispose();
        System.out.println("Loading disposed!");
    }

}
