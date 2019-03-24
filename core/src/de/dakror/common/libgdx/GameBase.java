/*******************************************************************************
 * Copyright 2017 Maximilian Stark | Dakror <mail@dakror.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package de.dakror.common.libgdx;

import static com.badlogic.gdx.graphics.GL20.*;

import java.util.Stack;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.WindowedMean;

import de.dakror.common.libgdx.ui.Scene;

/**
 * @author Maximilian Stark | Dakror
 */
public abstract class GameBase extends ApplicationAdapter implements InputProcessor, GestureListener {
    protected final Stack<Scene> sceneStack = new Stack<>();
    protected InputMultiplexer input;

    public PlatformInterface pi;

    int w, h;

    protected final WindowedMean updateTimeWindow = new WindowedMean(10);
    protected final WindowedMean frameTimeWindow = new WindowedMean(10);
    long lastUpdateTime;
    float updateTime;
    long lastFrameTime;
    float frameTime;

    protected float updateRate = 1 / 60f;

    protected GestureDetector gd;

    public GameBase(PlatformInterface pi) {
        this.pi = pi;
    }

    @Override
    public void create() {
        gd = new GestureDetector(this);
        input = new InputMultiplexer(this, gd);
        Gdx.input.setInputProcessor(input);
    }

    public Scene getScene() {
        synchronized (sceneStack) {
            if (sceneStack.isEmpty()) return null;
            Scene scene = sceneStack.peek();
            return scene;
        }
    }

    public void addScene(Scene scene) {
        synchronized (sceneStack) {
            if (scene.getInput() != null)
                input.addProcessor(0, scene.getInput());
            sceneStack.push(scene);
            scene.show();
            scene.resize(w, h);
        }
    }

    public void addSceneBelow(Scene scene) {
        synchronized (sceneStack) {
            if (scene.getInput() != null)
                input.addProcessor(1, scene.getInput());
            sceneStack.add(Math.max(0, sceneStack.size() - 1), scene);
            scene.show();
            scene.resize(w, h);
        }
    }

    public boolean dropScene(Scene scene) {
        synchronized (sceneStack) {
            scene.hide();
            boolean res = sceneStack.remove(scene);
            if (scene.getInput() != null)
                input.removeProcessor(scene.getInput());
            return res;
        }
    }

    public boolean hasScene(Scene scene) {
        synchronized (sceneStack) {
            boolean has = sceneStack.contains(scene);
            return has;
        }
    }

    public Scene dropScene() {
        synchronized (sceneStack) {
            sceneStack.peek().hide();
            input.removeProcessor(0);
            Scene scene = sceneStack.pop();
            return scene;
        }
    }

    @Override
    public void resize(int width, int height) {
        w = width;
        h = height;
        synchronized (sceneStack) {
            for (Scene scene : sceneStack)
                scene.resize(width, height);
        }
    }

    @Override
    public void pause() {
        synchronized (sceneStack) {
            for (Scene scene : sceneStack)
                scene.pause();
        }
    }

    @Override
    public void resume() {
        synchronized (sceneStack) {
            for (Scene scene : sceneStack)
                scene.resume();
        }
    }

    public void update() {
        synchronized (sceneStack) {
            try {
                for (int i = sceneStack.size() - 1; i > -1; i--)
                    sceneStack.get(i).update(updateRate);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void render() {
        synchronized (sceneStack) {
            long t = System.nanoTime();
            update();
            updateTimeWindow.addValue(System.nanoTime() - t);

            t = System.nanoTime();
            float deltaTime = Gdx.graphics.getDeltaTime();
            Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
            for (Scene scene : sceneStack)
                scene.draw(deltaTime);
            frameTimeWindow.addValue(System.nanoTime() - t);
        }
    }

    @Override
    public void dispose() {
        synchronized (sceneStack) {
            for (Scene scene : sceneStack)
                scene.dispose();
        }
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {}

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public float getUpdateTime() {
        if (System.currentTimeMillis() - lastUpdateTime > 1000) {
            updateTime = updateTimeWindow.getMean() / 1_000_000f;
            lastUpdateTime = System.currentTimeMillis();
        }
        return updateTime;
    }

    public float getFrameTime() {
        if (System.currentTimeMillis() - lastFrameTime > 1000) {
            frameTime = frameTimeWindow.getMean() / 1_000_000f;
            lastFrameTime = System.currentTimeMillis();
        }
        return frameTime;
    }
}
