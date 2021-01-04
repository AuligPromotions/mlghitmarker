package me.aulig.mlghitmarker;

import io.netty.util.internal.ThreadLocalRandom;
import net.labymod.api.LabyModAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HitMarkerGuiListener {

    private final ResourceLocation markerTexture = new ResourceLocation("marker.png");

    private LabyModAddon addon;

    private int hitMarkerSize;
    // time to display marker in milliseconds
    private int hitMarkerLength;

    private long hideAt = 0;

    private float hitMarkerVolume;

    private boolean useMemeSoundEffects;

    private boolean randomize;
    private int randomOffset = 0;

    private List<Long> lastKills = new ArrayList<Long>();

    HitMarkerGuiListener(LabyModAddon addon, int hitMarkerSize, int hitMarkerVolume, boolean randomize, int hitMarkerLength, boolean useMemeSoundEffects) {

        this.addon = addon;
        this.hitMarkerSize = hitMarkerSize;
        setHitMarkerVolume(hitMarkerVolume);
        this.randomize = randomize;
        this.hitMarkerLength = hitMarkerLength;
        this.useMemeSoundEffects = useMemeSoundEffects;
    }

    public void setHitMarkerLength(int hitMarkerLength) {
        this.hitMarkerLength = hitMarkerLength;
    }

    public void setRandomize(boolean randomize) {
        this.randomize = randomize;
    }

    public void setHitMarkerVolume(int hitMarkerVolume) {
        this.hitMarkerVolume = hitMarkerVolume / 100f;
    }

    public void setHitMarkerSize(int hitMarkerSize) {
        this.hitMarkerSize = hitMarkerSize;
    }

    public void setUseMemeSoundEffects(boolean useMemeSoundEffects) {
        this.useMemeSoundEffects = useMemeSoundEffects;
    }

    public int getHitMarkerVolume() {
        return Math.round(hitMarkerVolume * 100);
    }

    public int getHitMarkerLength() {
        return hitMarkerLength;
    }

    public int getHitMarkerSize() {
        return hitMarkerSize;
    }

    public boolean shouldRandomize() {
        return randomize;
    }

    public boolean shouldUseMemeSoundEffects() {
        return useMemeSoundEffects;
    }

    void showHitMarker(final boolean isKill) {

        long currentTime = System.currentTimeMillis();

        boolean isTripleKill = false;

        if (isKill) {

            lastKills.add(currentTime);

            if (lastKills.size() > 3) {

                lastKills.remove(0);
            }

            if (lastKills.size() > 2) {

                isTripleKill = lastKills.get(2) - lastKills.get(0) < 7000;

                if (isTripleKill) {

                    lastKills.clear();
                }
            }
        }


        hideAt = currentTime + hitMarkerLength;

        if (randomize) {

            randomOffset = ThreadLocalRandom.current().nextInt(-(hitMarkerSize / 2), hitMarkerSize / 2);
        }

        try {

            final boolean finalIsTripleKill = isTripleKill;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {

                        URL resource;

                        if (useMemeSoundEffects && finalIsTripleKill) {
                            resource = getClass().getResource("/assets/minecraft/triple.wav");
                        } else if (useMemeSoundEffects && isKill && ThreadLocalRandom.current().nextInt(0, 20) == 0) {
                            resource = getClass().getResource("/assets/minecraft/cena.wav");
                        } else {
                            resource = getClass().getResource("/assets/minecraft/hit.wav");
                        }

                        AudioInputStream audioIn = AudioSystem.getAudioInputStream(resource);
                        Clip clip = AudioSystem.getClip();
                        clip.open(audioIn);
                        FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                        float range = fc.getMaximum() - fc.getMinimum();
                        float gain = (range * hitMarkerVolume) + fc.getMinimum();
                        fc.setValue(gain);
                        clip.start();
                    } catch (Exception e) {

                        System.err.println("Error while trying to play sound effect: ");
                        e.printStackTrace();
                    }
                }
            });
            t.start();

        } catch (Exception e) {

            System.err.println("Error while starting thread to play sound effect: ");
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent event) {

        if (System.currentTimeMillis() < hideAt) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(markerTexture);
            ScaledResolution scaledResolution = addon.getApi().getDrawUtils().getScaledResolution();

            double posX = scaledResolution.getScaledWidth() / 2d - (hitMarkerSize / 2d);
            double posY = scaledResolution.getScaledHeight() / 2d - (hitMarkerSize / 2d);

            if (randomize) {

                posX += randomOffset;
                posY += randomOffset;
            }

            addon.getApi().getDrawUtils().drawTexture(posX, posY, 512d / 2d, 512d / 2d, hitMarkerSize, hitMarkerSize, 1);
        }
    }
}
