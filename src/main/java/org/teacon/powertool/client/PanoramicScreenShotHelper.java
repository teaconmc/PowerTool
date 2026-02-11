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
    private float rotation;
    public boolean takeScreenShot;
    private int colWidth;
    private float fovPerPixel;
    private int currentX;
    private NativeImage image;
    private State state = State.IDLE;
    private Mode mode = Mode.PRECISE;
    
    private PanoramicScreenShotHelper(){}
    
    public void writeImageSection(NativeImage image){
        var window = Minecraft.getInstance().getWindow();
//        var h = screenHeight;
        var h = window.getHeight();
        var w = (int) (h * ((float)window.getWidth()/(float)window.getHeight()));
        var centerStart = (int)(w/2f - colWidth/2f);
        for(int x = 0; x < colWidth; x++){
            for(var y = 0; y < h; y++){
                this.image.setPixelRGBA(x + this.currentX, y, image.getPixelRGBA(centerStart + x, y));
            }
        }
        this.currentX += colWidth;
        image.close();
    }
    
    public int start(CommandContext<CommandSourceStack> source){
        this.screenHeight = IntegerArgumentType.getInteger(source,"height");
        this.fov = IntegerArgumentType.getInteger(source,"fov");
        this.yaw_start = IntegerArgumentType.getInteger(source,"yaw_start");
        this.frame_delay = IntegerArgumentType.getInteger(source,"frame_delay");
        this.mode = source.getArgument("mode", Mode.class);
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
                var as = ((float)window.getWidth()/(float)window.getHeight());
                var w = (int) (h * as);
//                Minecraft.getInstance().getMainRenderTarget().resize(w,h,Minecraft.ON_OSX);
//                Minecraft.getInstance().gameRenderer.resize(w,h);
                if(INSTANCE.mode == Mode.FAST){
                    //INSTANCE.colWidth = (int) (w/Math.toDegrees(2 * Math.atan(Math.tan(Math.toRadians(INSTANCE.fov)/2f)*as)));
                    INSTANCE.colWidth = h/INSTANCE.fov;
                    INSTANCE.fovPerPixel = 1;
                    INSTANCE.image = new NativeImage(INSTANCE.colWidth*360,h,false);
                }
                else{
                    INSTANCE.colWidth = 1;
                    INSTANCE.fovPerPixel = (float)INSTANCE.fov/(float)h;
                    INSTANCE.image = new NativeImage((int)(360/INSTANCE.fovPerPixel)+1,h,false);
                }
                INSTANCE.rotation = 0;
                INSTANCE.currentX = 0;
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
                        INSTANCE.rotation += INSTANCE.mode.advanceRotation();
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
    
    public enum Mode{
        FAST{
            @Override
            public float advanceRotation() {
                return 1;
            }
        },
        PRECISE{
            @Override
            public float advanceRotation() {
                return INSTANCE.fovPerPixel;
            }
        };
        
        public float advanceRotation(){return 0;}
    }
}
