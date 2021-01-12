package me.aulig.mlghitmarker;

import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.settings.elements.SliderElement;
import net.labymod.utils.Consumer;
import net.labymod.utils.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import java.util.List;
import java.util.UUID;

public class Main extends LabyModAddon {

    HitMarkerGuiListener guiListener;

    static final int HIT_MARKER_DEFAULT_SIZE = 25;
    static final int HIT_MARKER_DEFAULT_LENGTH_MS = 200;
    static final int HIT_MARKER_DEFAULT_VOLUME = 70;
    static final boolean HIT_MARKER_DEFAULT_RANDOMIZE = true;
    static final boolean USE_MEME_SOUND_EFFECTS_DEFAULT = true;

    // maps entity uuids to their last recorded health
    static MaxSizeHashMap<UUID, Float> lastHpMap = new MaxSizeHashMap<UUID, Float>(256);

    @Override
    public void onEnable() {

        guiListener = new HitMarkerGuiListener(this, HIT_MARKER_DEFAULT_SIZE, HIT_MARKER_DEFAULT_VOLUME, HIT_MARKER_DEFAULT_RANDOMIZE, HIT_MARKER_DEFAULT_LENGTH_MS, USE_MEME_SOUND_EFFECTS_DEFAULT);

        getApi().getEventManager().registerOnAttack(new Consumer<Entity>() {
            @Override
            public void accept(Entity entity) {

                if (entity instanceof EntityLivingBase) {

                    UUID entityUUID;

                    try {
                        entityUUID = entity.getPersistentID();
                    }
                    catch (NoSuchMethodError nsme) {

                        entityUUID = entity.getUniqueID();
                    }

                    float newHp = ((EntityLivingBase) entity).getHealth();
                    Float lastHp = lastHpMap.get(entityUUID);

                    if (lastHp == null) {

                        lastHp = -1.0f;
                    }

                    // did the entities hp change?
                    if (newHp != lastHp) {
                        // -> show hitmarker, as entity took damage from attack
                        lastHpMap.put(entityUUID, newHp);
                        boolean isKill = ((EntityLivingBase) entity).getHealth() == 0;
                        guiListener.showHitMarker(isKill);
                    }
                }
            }
        });

        getApi().registerForgeListener(guiListener);
    }

    @Override
    public void loadConfig() {

        if (getConfig().has("hitMarkerSize")) {

            guiListener.setHitMarkerSize(getConfig().get("hitMarkerSize").getAsInt());
        }

        if (getConfig().has("hitMarkerVolume")) {

            guiListener.setHitMarkerVolume(getConfig().get("hitMarkerVolume").getAsInt());
        }

        if (getConfig().has("hitMarkerLength")) {

            guiListener.setHitMarkerLength(getConfig().get("hitMarkerLength").getAsInt());
        }

        if (getConfig().has("hitMarkerRandomize")) {

            guiListener.setRandomize(getConfig().get("hitMarkerRandomize").getAsBoolean());
        }

        if (getConfig().has("memeSoundEffects")) {

            guiListener.setUseMemeSoundEffects(getConfig().get("memeSoundEffects").getAsBoolean());
        }
    }

    @Override
    protected void fillSettings(List<SettingsElement> settingsElementList) {

        SliderElement hitMarkerSizeSlider = new SliderElement("HitMarker Size", this,
                new ControlElement.IconData(Material.ARROW), "hitMarkerSize", guiListener.getHitMarkerSize());

        hitMarkerSizeSlider.setRange(1, 100);

        hitMarkerSizeSlider.addCallback(new Consumer<Integer>() {
            @Override
            public void accept(Integer newHitMarkerSize) {
                guiListener.setHitMarkerSize(newHitMarkerSize);

            }
        });
        settingsElementList.add(hitMarkerSizeSlider);


        SliderElement hitMarkerVolumeSlider = new SliderElement("Sound Effect Volume", this,
                new ControlElement.IconData(Material.NOTE_BLOCK), "hitMarkerVolume", guiListener.getHitMarkerVolume());

        hitMarkerVolumeSlider.setRange(1, 100);

        hitMarkerVolumeSlider.addCallback(new Consumer<Integer>() {
            @Override
            public void accept(Integer newHitMarkerVolume) {
                guiListener.setHitMarkerVolume(newHitMarkerVolume);
            }
        });

        settingsElementList.add(hitMarkerVolumeSlider);


        SliderElement hitMarkerLengthSlider = new SliderElement("HitMarker Display Time", this,
                new ControlElement.IconData(Material.WATCH), "hitMarkerLength", guiListener.getHitMarkerLength());

        hitMarkerLengthSlider.setRange(10, 1000);
        hitMarkerLengthSlider.setSteps(10);

        hitMarkerLengthSlider.addCallback(new Consumer<Integer>() {
            @Override
            public void accept(Integer newHitMarkerVolume) {
                guiListener.setHitMarkerLength(newHitMarkerVolume);
            }
        });

        settingsElementList.add(hitMarkerLengthSlider);


        BooleanElement randomizeToggle = new BooleanElement("Randomize HitMarker Position", this,
                new ControlElement.IconData(Material.COMMAND), "hitMarkerRandomize", guiListener.shouldRandomize());

        randomizeToggle.addCallback(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean newRandomize) {
                guiListener.setRandomize(newRandomize);
            }
        });

        settingsElementList.add(randomizeToggle);


        BooleanElement memeSoundEffectsToggle = new BooleanElement("Meme Sound Effects", this,
                new ControlElement.IconData(Material.COMMAND), "memeSoundEffects", guiListener.shouldUseMemeSoundEffects());

        memeSoundEffectsToggle.addCallback(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean newUseMemeSoundEffects) {
                guiListener.setUseMemeSoundEffects(newUseMemeSoundEffects);
            }
        });

        settingsElementList.add(memeSoundEffectsToggle);
    }
}
