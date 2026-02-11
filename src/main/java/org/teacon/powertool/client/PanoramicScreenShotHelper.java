package org.teacon.powertool.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.teacon.powertool.client.overlay.PauseOverlay;

import java.io.IOException;

@EventBusSubscriber
public class PanoramicScreenShotHelper {
    
    public static final PanoramicScreenShotHelper INSTANCE = new PanoramicScreenShotHelper();
    
    public int fov;
    public int yaw_start;
    public int frame_delay;
    public int screenHeight;
    
    private int delay;
    private int rotation;
    public boolean takeScreenShot;
    private int colWidth;
    private NativeImage image;
    private State state = State.IDLE;
    
    private PanoramicScreenShotHelper(){}
    
    public void writeImageSection(NativeImage image){
        var window = Minecraft.getInstance().getWindow();
//        var h = screenHeight;
        var h = window.getHeight();
        var w = (int) (h * ((float)window.getWidth()/(float)window.getHeight()));
        var centerStart = w/2 - colWidth/2;
        for(int x = 0; x < colWidth; x++){
            for(var y = 0; y < h; y++){
                this.image.setPixelRGBA(x + (rotation-1)*colWidth, y, image.getPixelRGBA(centerStart + x, y));
            }
        }
    }
    
    public int start(CommandContext<CommandSourceStack> source){
        this.screenHeight = IntegerArgumentType.getInteger(source,"height");
        this.fov = IntegerArgumentType.getInteger(source,"fov");
        this.yaw_start = IntegerArgumentType.getInteger(source,"yaw_start");
        this.frame_delay = IntegerArgumentType.getInteger(source,"frame_delay");
        this.state = State.PREPARE;
        return 0;
    }
    
    @SubscribeEvent
    public static void onSetupCamera(ViewportEvent.ComputeCameraAngles event){
        switch (INSTANCE.state){
            case IDLE -> {
            }
            case PREPARE -> {
                INSTANCE.state = State.CAPTURING;
                var window = Minecraft.getInstance().getWindow();
//                var h = INSTANCE.screenHeight;
                var h = window.getHeight();
                var w = (int) (h * ((float)window.getWidth()/(float)window.getHeight()));
//                Minecraft.getInstance().getMainRenderTarget().resize(w,h,Minecraft.ON_OSX);
//                Minecraft.getInstance().gameRenderer.resize(w,h);
                INSTANCE.colWidth = w/(INSTANCE.fov*2);
                INSTANCE.image = new NativeImage(INSTANCE.colWidth*360,h,false);
                INSTANCE.rotation = 0;
                Minecraft.getInstance().setOverlay(new PauseOverlay());
            }
            case CAPTURING -> {
                if(!INSTANCE.takeScreenShot){
                    if(INSTANCE.rotation >= 360){
                        INSTANCE.state = State.FINISHING;
                        break;
                    }
                    event.setYaw((INSTANCE.rotation + INSTANCE.yaw_start) % 360);
                    event.setPitch(0);
                    if(INSTANCE.delay < INSTANCE.frame_delay){
                        INSTANCE.delay += 1;
                    }
                    else {
                        INSTANCE.delay = 0;
                        INSTANCE.rotation += 1;
                        INSTANCE.takeScreenShot = true;
                    }

                }

            }
            case FINISHING -> {
                INSTANCE.state = State.IDLE;
                var file = FMLPaths.GAMEDIR.get().resolve("screenshots").resolve("panorama" + Util.getFilenameFormattedDateTime() + ".png");
                try {
                    INSTANCE.image.writeToFile(file);
                    INSTANCE.image.close();
                    INSTANCE.image = null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Minecraft.getInstance().setOverlay(null);
//                Minecraft.getInstance().resizeDisplay();
                Minecraft.getInstance().execute(
                        () -> Minecraft.getInstance().gui.getChat().addMessage(
                                Component.literal(file.toFile().getName())
                                        .withStyle(ChatFormatting.UNDERLINE)
                                        .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.toFile().getAbsolutePath())))
                        )
                );
            }
        }
    }
    
    @SubscribeEvent
    public static void onSetupFov(ViewportEvent.ComputeFov event){
        if(INSTANCE.state == State.CAPTURING){
            event.setFOV(INSTANCE.fov);
        }
    }
    
    public enum State{
        IDLE,
        PREPARE,
        CAPTURING,
        FINISHING
    }
}
