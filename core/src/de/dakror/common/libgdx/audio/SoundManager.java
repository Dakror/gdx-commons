package de.dakror.common.libgdx.audio;

import java.util.Iterator;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

import de.dakror.common.Callback;

/**
 * @author Maximilian Stark | Dakror
 */
public class SoundManager {
    public static class SoundEntry implements Poolable {
        Sound sound;
        float delay;
        Callback<SoundEntry> callback;
        long id;

        @Override
        public void reset() {
            sound = null;
            delay = 0;
            callback = null;
            id = 0;
        }
    }

    Array<SoundEntry> queue = new Array<SoundEntry>();

    Pool<SoundEntry> pool = new Pool<SoundEntry>(10, 100) {
        @Override
        protected SoundEntry newObject() {
            return new SoundEntry();
        }
    };

    Music music, newMusic;
    float fadeProgress = -1;
    boolean fadeOutMusic;
    boolean pauseMusic;

    boolean lastTickMusic;

    boolean playMusic;
    boolean playSound;
    
    float fadeSpeed;
    
    public SoundManager(float fadeSpeed) {
        this.fadeSpeed = fadeSpeed;
    }
    
    public void setFadeSpeed(float fadeSpeed) {
        this.fadeSpeed = fadeSpeed;
    }
    
    public void setPlayMusic(boolean playMusic) {
        this.playMusic = playMusic;
    }
    
    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
    }
    
    public void update(float delta) {
        for (Iterator<SoundEntry> iter = queue.iterator(); iter.hasNext();) {
            SoundEntry e = iter.next();
            e.delay -= delta;
            if (e.delay <= 0) {
                if (playSound) e.id = e.sound.play();
                if (e.callback != null) e.callback.call(e);
                iter.remove();
            }
        }

        if (fadeProgress > -1) {
            fadeProgress += delta * fadeSpeed;
            if (newMusic != null) {
                newMusic.setVolume(Math.min(1, fadeProgress));
                music.setVolume(1 - Math.min(1, fadeProgress));
            } else {
                if (fadeOutMusic) music.setVolume(1 - Math.min(1, fadeProgress));
                else music.setVolume(Math.min(1, fadeProgress));
            }

            if (fadeProgress >= 1) {
                fadeProgress = -1;
                if (newMusic != null) {
                    music.stop();
                    music = newMusic;
                    newMusic = null;
                }
                if (fadeOutMusic) {
                    if (pauseMusic) music.pause();
                    else {
                        music.stop();
                        music = null;
                    }
                    fadeOutMusic = false;
                    pauseMusic = false;
                }
            }
        }

        if (!fadeOutMusic && music != null && music.isPlaying() && !playMusic && lastTickMusic) {
            pauseMusic();
        }

        if (music != null && !music.isPlaying() && playMusic && !lastTickMusic) {
            resumeMusic();
        }

        lastTickMusic = playMusic;
    }

    public void stop(Sound sound) {
        sound.stop();
    }

    public void play(Sound sound) {
        play(sound, 0, null);
    }

    public void play(Sound sound, float delay, Callback<SoundEntry> callback) {
        SoundEntry e = pool.obtain();
        e.sound = sound;
        e.delay = delay;
        e.callback = callback;

        queue.add(e);
    }

    public void pauseMusic() {
        if (music == null) return;
        fadeProgress = 0;
        fadeOutMusic = true;
        pauseMusic = true;
        newMusic = null;
    }

    public void resumeMusic() {
        if (music == null) return;
        fadeOutMusic = false;
        pauseMusic = false;
        fadeProgress = -1;
        music.setVolume(1);
        if (music.isPlaying()) return;

        Music m = music;
        music = null;
        playMusic(m, true); // fade back in
    }

    public void stopMusic() {
        if (music == null) return;
        music.stop();
        fadeOutMusic = false;
        fadeProgress = -1;
        music = null;
    }

    public void playMusic(Music newMusic, boolean fade) {
        if (!playMusic) {
            if (music != null) music.stop();
            music = newMusic;

            newMusic.setLooping(true);
            newMusic.setVolume(0);
            music.play();
            music.pause();
        } else if (fade) {
            if (music != null) {
                this.newMusic = newMusic;
                newMusic.setLooping(true);
                newMusic.setVolume(0);
                newMusic.play();
                fadeOutMusic = false;
            } else {
                music = newMusic;
                music.setLooping(true);
                music.setVolume(0);
                music.play();
                fadeOutMusic = false;
            }
            if (fadeProgress == -1) fadeProgress = 0;
        } else {
            music.stop();
            music = newMusic;
            newMusic.setLooping(true);
            newMusic.play();
            fadeProgress = -1;
        }
    }
}